package memory;

import controller.Joypad;
import cpu.*;
import graphic.GPU;

import java.io.IOException;


public class MMU {
	private boolean left_boot;
	private final int vram_offset = 0x8000;
	private final int oam_offset = 0xFE00;
	private final int wram_offet = 0xC000;
	private final int hram_offet = 0xFF80;
	private final int[] wram, hram, vram, oam, io;
	private Timer tm;
	private InterruptsManager ic;
	private boolean DMA_intransfer;
	private int DMA_counter;
	public int pad_state;
	public Joypad joy;
	private ROMController romcon;

	public MMU(InterruptsManager ic, Timer tmeru, Joypad j, ROMController romm) {
		left_boot = true;
		oam = new int[160];
		vram = new int[8192];
		hram = new int[127];
		wram = new int[8192];
		io = new int[128];
		this.ic = ic;
		tm = tmeru;
		//DMA_intransfer = false;
		joy = j;
		this.romcon = romm;
	}

	public MMU(GPU gpu, InterruptsManager ic, Timer timer, ROMController romm) {
	}

	public int getByte (int addr) {
		addr = addr&0xffff;
		if(addr<=0x7fff) {
			return romcon.getByte(addr);
		} else if(addr > 0x7fff && addr <= 0x9fff) {
				return vram[addr-vram_offset]&0xff;
		} else if (addr <= 0xbfff) {
				return romcon.getByte(addr);
		} else if (addr >= 0xc000 && addr <=0xdfff) {
				return wram[addr-wram_offet]&0xff;
		} else if(addr >= 0xfe00 && addr <= 0xfe9f) {
			return oam[addr - oam_offset] & 0xff;
		} else if(addr == 0xff00) {
			return joy.getByte();
		} else if(addr>0xff00 && addr <=0xff7f) {
			if(addr == 0xFF04) {
				return tm.DIV&0xff;
			} else if(addr == 0xff0f) {
				return ic.getIF();
			} else if(addr == 0xFF05) {
				return tm.TIMA&0xff;
			} else if(addr == 0xFF06) {
				return tm.TMA&0xff;
			} else if(addr == 0xFF07) {
				return tm.TAC&0xff;
			} else if(addr == 0xFF40) {
				return io[addr-0xFF00]&0xff;
			} else {
				return io[addr-0xFF00]&0xff;
			}
		} else if(addr>=0xff80 && addr <= 0xfffe) {
			 return hram[addr-hram_offet]&0xff;
		} else if(addr==0xffff) {
			return ic.getIE();
		}
		return 0;
	}

	public void writeByte (int addr, int value) {
		addr = addr&0xffff;
		value = value&0xff;
		if(DMA_intransfer) {
			if(addr>=0xff80 && addr <= 0xfffe) {
				hram[addr - hram_offet] = value;
			}
			return;
		}
		if(addr == 0xff00) {
			joy.writeByte(value&0xff);
			return;
		}
		if ((addr == 0xFF50) && (value != 0)) { //unmap bios
			romcon.disable_bootrom();
			return;
		}
		if(addr<=0x7fff) {
			romcon.writeByte(addr, value);
		} else if(addr <= 0x9fff) {
			vram[addr - vram_offset] = value & 0xff;
		} else if(addr <= 0xbfff) {
			romcon.writeByte(addr, value);
		} else if (addr <=0xdfff) {
			wram[addr-wram_offet] = value&0xff;
		} else if(addr >= 0xfe00 && addr <= 0xfe9f) {
				oam[addr-oam_offset]=value&0xff;
		} else if(addr>0xff00 && addr <=0xff7f) {
			if(addr == 0xFF04) {
				tm.writeToDiv();
			} else if (addr == 0xff0f) {
				ic.writeIF(value);
			} else if(addr == 0xFF05) {
				tm.writeToTIMA(value&0xff);
			} else if(addr == 0xFF06) {
				tm.writeToTMA(value&0xff);
			} else if(addr == 0xFF07) {
				tm.writeToTac(value&0xff);
			} else if(addr == 0xFF40) {
				io[addr-0xFF00] = (value&0xff)|1<<7;
			} else if(addr == 0xff46) {
				int address = (value<<8)&0xffff; // source address is data * 100
				for (int i = 0 ; i < 0xA0; i++)
				{
					writeByte(0xFE00+i,0xff&getByte(address+i)); ;
				}
			} else if(addr == 0xff41) {
				io[addr-0xff00] = (value&0b1111000)&(0xff&io[addr-0xff00]);
			} else if(addr==0xff44) {
				return;
			}else{
			io[addr - 0xFF00] = value & 0xff;
				}
		} else if(addr>=0xff80 && addr <= 0xfffe) {
			hram[addr-hram_offet] = value;
		} else if(addr==0xffff) {
			ic.writeIE(value);
		}
	}
	public void DMA_count (int cycles) {
		if(DMA_intransfer)  {
			DMA_counter+=cycles;
			if(DMA_counter >=(160)) {
				DMA_intransfer = false;
				DMA_counter = 0;
			}
		}
	}
}
