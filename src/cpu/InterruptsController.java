package cpu;

public class InterruptsController {
    public boolean IME;
    public int IF;
    public int IE;
    public int bit_vblank = 0;
    public int bit_lcd = 1<<1;
    public int bit_timer = 1<<2;
    public int bit_serial = 1<<3;
    public int bit_joypad = 1<<4;


    public void enableIME() { IME = true; }
    public void disableIME() { IME = false; }
    public boolean getIME() {return IME;}

    public void resetIF(int bit) {IF= Bits.setBit(IF, false, bit);}
    public void resetIE(int bit) {IE= Bits.setBit(IE, false, bit);}
    public int getIE(){
        return IE;
    }
    public int getIF() {
        return IF;
    }

    public void writeIE(int num) {IE = num; }
    public void writeIF(int num) {IF = num;}

    public void setIE(int bit){
        IE = Bits.setBit(IE, true, bit);
    }
    public void setIF(int bit) {
        IF = Bits.setBit(IF, true, bit);
    }
    public void requestInterrupt(int which) {
        IF = Bits.setBit(IF, true, which);
    }
    public boolean isRequested(int which) { return Bits.isBit(IF, which);}
    public boolean isEnabled(int which ) { return Bits.isBit(IE, which);}
    public void enableInterrupt(int which) {
        IE = Bits.setBit(IE, true, which);
    }
}
