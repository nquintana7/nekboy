package cpu;

import controller.Joypad;
import graphic.*;
import memory.*;

import java.io.IOException;

public class CPU {

	private InterruptsManager ic;
	private RegistersManager regc;
	private MMU mmu;
	private OpManager insc;
	private int cycles;
	private Timer timer;
	private State state;
	private GPU gpu;
	private long start;
	private int counter;

	public static int a = 0;

	public CPU(String filename) throws IOException {
		ic = new InterruptsManager();
		timer = new Timer(ic);
		Joypad j = new Joypad(ic);
		mmu = new MMU(ic, timer, j, new ROMController(filename));
	//	gpu = new GPU(mmu, inc);
		regc = new RegistersManager(mmu);
		insc = new OpManager(mmu, ic);
		gpu = new GPU(mmu, ic, j);
	}

	public enum State{
		HALT,
		STOP,
		RUN
	}

	public void reset() {
		regc.setReg(0x01, "A");
		regc.setReg(0x00, "B");
		regc.setReg(0x13, "C");
		regc.setReg(0x00, "D");
		regc.setReg(0xD8, "E");
		regc.setReg(0xB0, "F");
		regc.setReg(0x01, "H");
		regc.setReg(0x4D, "L");
		regc.setReg(0xFFFE, "SP");
		regc.setReg(0x100, "PC");
		mmu.io[0xff41-0xff00] = 0x80;
		mmu.io[0xff40-0xff00] = 0x91;
		mmu.writeByte(0xFF05, 0x00); // TIMA
		mmu.writeByte(0xFF06, 0x00); // TMA
		mmu.writeByte(0xFF07, 0x00); // TAC
		mmu.writeByte(0xFF10, 0x80); // NR10
		mmu.writeByte(0xFF11, 0xBF); // NR11
		mmu.writeByte(0xFF12, 0xF3); // NR12
		mmu.writeByte(0xFF14, 0xBF); // NR14
		mmu.writeByte(0xFF16, 0x3F); // NR21
		mmu.writeByte(0xFF17, 0x00); // NR22
		mmu.writeByte(0xFF19, 0xBF); // NR24
		mmu.writeByte(0xFF1A, 0x7F); // NR30
		mmu.writeByte(0xFF1B, 0xFF); // NR31
		mmu.writeByte(0xFF1C, 0x9F); // NR32
		mmu.writeByte(0xFF1E, 0xBF); // NR33
		mmu.writeByte(0xFF20, 0xFF); // NR41
		mmu.writeByte(0xFF21, 0x00); // NR42
		mmu.writeByte(0xFF22, 0x00); // NR43
		mmu.writeByte(0xFF23, 0xBF); // NR30
		mmu.writeByte(0xFF24, 0x77); // NR50
		mmu.writeByte(0xFF25, 0xF3); // NR51
		mmu.writeByte(0xFF26, 0xF1); // NR52
		mmu.writeByte(0xFF42, 0x00); // SCY
		mmu.writeByte(0xFF43, 0x00); // SCX
		mmu.writeByte(0xFF45, 0x00); // LYC
		mmu.writeByte(0xFF47, 0xFC); // BGP
		mmu.writeByte(0xFF48, 0xFF); // OBP0
		mmu.writeByte(0xFF49, 0xFF); // OBP1
		mmu.writeByte(0xFF4A, 0x00); // WY
		mmu.writeByte(0xFF4B, 0x00); // WX
		mmu.writeByte(0xFFFF, 0x00); // I
	}

	public void run() throws InterruptedException {
		while(true) {
			if(ic.getIME() & ic.delayCheck()) {
				cycles +=interruptsHandler();
			}
			int current_op = mmu.getByte(regc.getRegval("PC"));
			regc.incrPC();
			int cycles_passed = insc.execInstr(current_op, regc);
			if(cycles_passed<0) cycles_passed=0;
			cycles+=cycles_passed;
			timer.tick(cycles_passed);
		//	mmu.DMA_count(cycles_passed);
			gpu.update(cycles_passed);
		}
	}

	private int interruptsHandler () {
		int ief = ic.getIF();
		int ie = ic.getIE();
		if(Bits.isBit(ief, 0) & Bits.isBit(ie, 0)) { //v-blank interrupt
			ic.resetIF(0);
			ic.disableIME();
			insc.call(regc, 0x40);
			return 12;
		} else if(Bits.isBit(ief,1) & Bits.isBit(ie, 1)) { //lcd stat
			ic.resetIF(1);
			ic.disableIME();
			insc.call(regc, 0x48);
			return 12;
		} else if(Bits.isBit(ief, 2) & Bits.isBit(ie, 2))  { //Timer
			ic.resetIF(2);
			ic.disableIME();
			insc.call(regc, 0x50);
		} else if(Bits.isBit(ief, 3) & Bits.isBit(ie, 3)) { // Serial
			ic.resetIF(3);
			ic.disableIME();
			insc.call(regc, 0x58);
			return 12;
		} else if(Bits.isBit(ief, 4) & Bits.isBit(ie, 4)) { // Joypad
			ic.resetIF(4);
			ic.disableIME();
			insc.call(regc, 0x60);
			return 12;
		}
		return 0;
	}

	

}
