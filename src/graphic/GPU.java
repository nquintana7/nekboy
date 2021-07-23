

package graphic;
import javax.swing.JFrame;
import java.awt.Color;
import memory.*;
import cpu.*;

public class GPU {
    public static final int TILE_SIZE = 16;
    /* Every two bits in the palette data byte represent a colour.
	Bits 7-6 maps to colour id 11, bits 5-4 map to colour id 10,
	bits 3-2 map to colour id 01 and bits 1-0 map to colour id 00.*/
	/* 0  White
        1  Light gray
        2  Dark gray
        3  Black*/

    public MMU mmu;
    public int LCDC = 0xff40;
    /* Bit 7 - LCD Display Enable (0=Off, 1=On)
	Bit 6 - Window Tile Map Display Select (0=9800-9BFF, 1=9C00-9FFF)
	Bit 5 - Window Display Enable (0=Off, 1=On)
	Bit 4 - BG & Window Tile Data Select (0=8800-97FF, 1=8000-8FFF)
	Bit 3 - BG Tile Map Display Select (0=9800-9BFF, 1=9C00-9FFF)
	Bit 2 - OBJ (Sprite) Size (0=8x8, 1=8x16)
	Bit 1 - OBJ (Sprite) Display Enable (0=Off, 1=On)
	Bit 0 - BG Display (for CGB see below) (0=Off, 1=On) */
    public int LCD_STAT = 0xff41;
    /*
	Bits 0&1: 	00: H-Blank
				01: V-Blank
				10: Searching Sprites Atts
				11: Transfering Data to LCD Driver
	Bit 2: Coincidence Interupt Request
	Bit 3: Mode 0 Interupt Enabled
	Bit 4: Mode 1 Interupt Enabled
	Bit 5: Mode 2 Interupt Enabled
	Bit 6: Coincidence Interupt Enabled */
    private int SCY = 0xff42;
    private int SCX = 0xff43;
    private int LY = 0xff44;
    private int LYC = 0xff45;
    public static final int WY = 0xFF4A;
    public static final int WX = 0xFF4B; // true value is this - 7
    public static final int BGP = 0xFF47;
    public static final int OBP0 = 0xFF48;
    public static final int OBP1 = 0xFF49;
    private int DMA_REQUEST = 0xFF46;

    public int mode = 2;
    private int modeClock;
    public int tileSet[][] = new int[160][144];
    public InterruptsController ic;
    private Screen screen;

    public GPU(MMU mm, InterruptsController icc, Joypad j) {
        mmu = mm;
        ic = icc;
        int width = 160;
        int height = 144;
        JFrame frame = new JFrame("NekBoy");
        screen = new Screen(width, height, this);
        frame.add(screen);
        frame.pack();
        frame.addKeyListener(new JoypadListener(j));
        frame.setVisible(true);
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void update(int cycles) {
        boolean reqInt = false;

        int actual_LCD_STAT = mmu.getByte(LCD_STAT);

        if (isLCDenabled()) {
            modeClock+=cycles;
        } else {
            actual_LCD_STAT = Bits.setBit(actual_LCD_STAT, false, 1);
            actual_LCD_STAT = Bits.setBit(actual_LCD_STAT, false, 1);
            mode = 1;
            modeClock= 0;
            mmu.io[LY-0xff00]= 0;
            mmu.io[LCD_STAT-0xff00]= actual_LCD_STAT;
            return;
        }

        switch (mode) {

            case 0: //HBLANK
                if(modeClock >= 204) {
                    renderFrame();
                    if(mmu.getByte(LY) == 143) {
                        mode = 1;
                        ic.requestInterrupt(0);
                        actual_LCD_STAT = Bits.setBit(actual_LCD_STAT, true, 0);
                        actual_LCD_STAT = Bits.setBit(actual_LCD_STAT, false, 1);
                    } else {
                        mode = 2;
                        actual_LCD_STAT = Bits.setBit(actual_LCD_STAT, true, 1);
                        actual_LCD_STAT = Bits.setBit(actual_LCD_STAT, false, 0);
                    }
                    modeClock=0;
                    mmu.io[LY-0xff00] = mmu.io[LY-0xff00]+1;
                    if(mmu.getByte(LY) == mmu.getByte(LYC)) {
                        actual_LCD_STAT = Bits.setBit(actual_LCD_STAT, true, 2);
                        if(Bits.isBit(actual_LCD_STAT, 6)) {
                            ic.requestInterrupt(1);
                        }
                    } else {
                        actual_LCD_STAT = Bits.setBit(actual_LCD_STAT, false, 2);
                    }
                }
               break;
            case 1:	//Vertical blank mode
                //CPU can access both the display RAM (8000h-9FFFh) and OAM (FE00h-FE9Fh)
                if (modeClock >= 456) {					//required clock cycles for full H-blank cycle (used for invisible scanlines)
                    modeClock = 0;							//reset the clock
                    // increment line number
                    mmu.io[LY-0xff00] = mmu.io[LY-0xff00]+1;
                    if (mmu.getByte(LY) > 153) { 			//reached full frame (including invisible lines)
                        mmu.io[LY-0xff00] = 0;		//reset scan line number
                        mode = 2;						//set mode as access OAM mode
                        actual_LCD_STAT = Bits.setBit(actual_LCD_STAT, true, 1);
                        actual_LCD_STAT = Bits.setBit(actual_LCD_STAT, false, 0);
                    }
                }
                break;

            case 2: //Scanline (accessing OAM)
                //CPU <cannot> access OAM memory (FE00h-FE9Fh) during this period
                if (modeClock >= 80) {					//required clock cycles for accessing the OAM
                    mode = 3;						//set mode as access VRAM mode
                    actual_LCD_STAT = Bits.setBit(actual_LCD_STAT, true, 1);
                    actual_LCD_STAT = Bits.setBit(actual_LCD_STAT, true, 0);
                    modeClock =0;			//reset the clock
                }
                break;

            case 3: //Scanline (accessing VRAM & OAM)
                //CPU <cannot> access OAM and VRAM during this period. CGB Mode: Cannot access Palette Data (FF69,FF6B) either.

                if (modeClock >= 172) {				//required clock cycles for accessing VRAM & OAM
                    mode = 0;						//set mode as H-blank
                    actual_LCD_STAT = Bits.setBit(actual_LCD_STAT, false, 1);
                    actual_LCD_STAT = Bits.setBit(actual_LCD_STAT, false, 0);
                    if(Bits.isBit(actual_LCD_STAT, 3)) ic.requestInterrupt(1);
                    modeClock = 0;
                    renderScan();
                }
                break;

        }
            mmu.io[LCD_STAT-0xff00]= actual_LCD_STAT;
    }


    private void renderFrame() {
        screen.fillCanvas();
    }

    public void renderScan(){
    int control = mmu.getByte(LCDC);
        if (Bits.isBit(control, 0))
            renderTiles();
        if (Bits.isBit(control, 1))
            renderSprites();
    }

    private void renderSprites(){
        int scanline = mmu.getByte(LY);

        for(int i = 0xFE00; i< 0xFE9F; i+=4) {   // Iterates through each sprite on memory
            int ypos = mmu.getByte(i) - 16;
            int xpos = mmu.getByte(i+1)-8;

            if(xpos>= 160) continue;
            if(ypos <= scanline && (ypos + 8) > scanline) {
                int tileIndex = mmu.getByte(i+2);
                int tileAddress = (0x8000+(tileIndex*0x10))&0xffff;  // Tile Addr is 0x8000 + the index * 16

                int flags = mmu.getByte(i+3)&0xff;

                boolean longObjectMode = Bits.isBit(mmu.getByte(LCDC), 2);

                boolean bg_prio = Bits.isBit(flags, 7);  // not yet implemented

                int palette = Bits.isBit(flags, 4) ? 1 : 0;
                boolean yflip = Bits.isBit(flags, 6);
                boolean xflip = Bits.isBit(flags, 5);

                int ysize = 8;
                if(longObjectMode) ysize = 16;

                int line = scanline - ypos;

                if(yflip) {
                    line -= ysize;
                    line *= -1;
                }

                line *= 2;

                tileAddress += line;
                tileAddress &= 0xffff;  // double checks inside range

                int byte1 = mmu.getByte(tileAddress);
                int byte2 = mmu.getByte(tileAddress+1);

                for (int pixel = 7; pixel >=0; pixel--) {  //
                    int colourBit = pixel;

                    if(xflip) {
                       colourBit -= 7;
                        colourBit *= -1;
                    }

                    int colourNum = Bits.isBit(byte2, colourBit) ? 1 : 0;
                    colourNum <<= 1;
                    colourNum |= (Bits.isBit(byte1, colourBit) ? 1 : 0);

                    if(colourNum == 0) continue;

                    Color col = getSpriteColour(colourNum, palette);

                    tileSet[xpos+(7-pixel)][scanline] = col.getRGB();
                }
            }
        }
    }

    private void renderTiles(){
        int scrollY = mmu.getByte(SCY);
        int scrollX = mmu.getByte(SCX);
        int windowX = mmu.getByte(WX) - 7; //shifted 7 pixels
        int windowY = mmu.getByte(WY);
        int scanLine = mmu.getByte(LY);
        boolean signed = false;
        boolean inWindow = false;
        int tileData;
        int tileMemAddr;
        int tileAddr;
        int tileDataAddr;
        int yPos = 0;
        int xPos;
        int tileCol;
        int tileRow;
        int tileLine;
        int colourBit;
        int data1;
        int data2;
		/* Bit 7 - LCD Display Enable (0=Off, 1=On)
		Bit 6 - Window Tile Map Display Select (0=9800-9BFF, 1=9C00-9FFF)
		Bit 5 - Window Display Enable (0=Off, 1=On)
		Bit 4 - BG & Window Tile Data Select (0=8800-97FF, 1=8000-8FFF)  8800 IS SIGNED
		Bit 3 - BG Tile Map Display Select (0=9800-9BFF, 1=9C00-9FFF)
		Bit 2 - OBJ (Sprite) Size (0=8x8, 1=8x16)
		Bit 1 - OBJ (Sprite) Display Enable (0=Off, 1=On)
		Bit 0 - BG Display (for CGB see below) (0=Off, 1=On) */

        if (Bits.isBit(mmu.getByte(LCDC), 5) && (scanLine >= windowY))
            inWindow = true;

        if (Bits.isBit(mmu.getByte(0xff40), 4)) {
            tileData = 0x8000;
        } else {
            signed = true;
            tileData = 0x8800;
        }

        if (inWindow)
            tileMemAddr = (Bits.isBit(mmu.getByte(LCDC), 6)) ? 0x9C00 : 0x9800;
        else
            tileMemAddr = (Bits.isBit(mmu.getByte(LCDC), 3)) ? 0x9C00 : 0x9800;



        if (inWindow)
            yPos = scanLine - windowY;
        else
            yPos = (scanLine + scrollY&0xff)&0xff;


        tileRow = (yPos / 8)*32;

        for(int pixel = 0; pixel < 160; pixel++){

            xPos = scrollX + pixel;

            if (inWindow && (pixel >= windowX))
                xPos = pixel - windowX;

            tileCol = xPos / 8;

            tileAddr = tileMemAddr + tileCol + tileRow;


            if (signed)
                tileDataAddr = ((byte) mmu.getByte(tileAddr) )+ 128;
            else
                tileDataAddr = mmu.getByte(tileAddr);

            tileDataAddr &= 0xFF;

           tileLine = yPos % 8;

            data1 = mmu.getByte(tileData + (tileDataAddr*TILE_SIZE) + (tileLine*2));
            data2 = mmu.getByte(tileData + (tileDataAddr*TILE_SIZE) + (tileLine*2) + 1);

            colourBit = xPos % 8;

            colourBit -= 7;
            colourBit *= -1;

            int colourNum = 0;
            if(Bits.isBit(data2, colourBit)) colourNum= 1;
            colourNum <<= 1;
            int a = 0;
            if(Bits.isBit(data1, colourBit)) a = 1;


            colourNum |= a;

            Color col = getColour(colourNum);

            tileSet[pixel][scanLine] = col.getRGB();

			/* Every two bits in the palette data byte represent a colour.
			Bits 7-6 maps to colour id 11, bits 5-4 map to colour id 10,
			bits 3-2 map to colour id 01 and bits 1-0 map to colour id 00. */
        }
    }

    private void updateBg () {

    }

    public int getScanLine(){
        return mmu.getByte(LY);
    }

    public void incScanLine(){
        mmu.writeByte(LY, mmu.getByte(LY)+1);
    }

    public void resetScanLine(){
        mmu.writeByte(LY, 0);
    }

    private Color getColour(int colourID){
        int colNum = 0;
        Color returnCol = Color.BLACK;
        int palette = mmu.getByte(0xff47);

        switch(colourID){
            case 0: colNum = palette & 0x3;				break;
            case 1: colNum = (palette & 0xC) >>> 2;		break;
            case 2: colNum = (palette & 0x30) >>> 4;	break;
            case 3: colNum = (palette & 0xC0) >>> 6;	break;
        }

        switch(colNum){
            case 0: returnCol = Color.WHITE; 		break;
            case 1: returnCol = Color.LIGHT_GRAY;  	break;
            case 2: returnCol = Color.DARK_GRAY;  	break;
            case 3: returnCol = Color.BLACK; 		break;
        }
        return returnCol;
    }

    private Color getSpriteColour(int colourID, int which) {
        int colNum = 0;
        Color returnCol = Color.BLACK;
        int add = 0xff48;
        if(which == 1) add = 0xff49;
        int palette = mmu.getByte(add);

        switch(colourID){
            case 1: colNum = (palette & 0xC) >>> 2;		break;
            case 2: colNum = (palette & 0x30) >>> 4;	break;
            case 3: colNum = (palette & 0xC0) >>> 6;	break;
        }

        switch(colNum){
            case 0: returnCol = null; 		break;
            case 1: returnCol = Color.LIGHT_GRAY;  	break;
            case 2: returnCol = Color.DARK_GRAY;  	break;
            case 3: returnCol = Color.BLACK; 		break;
        }
        return returnCol;
    }




    public boolean isLCDenabled() {
        return Bits.isBit(mmu.getByte(LCDC), 7);
    }
    public boolean isVramAccessible() {
        if(mode != 3) return true;
        return false;
    }
    public boolean isOamAccessible() {
        if(mode == 0 || mode == 1) {
            return true;
        }
        return false;
    }
}
