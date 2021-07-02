package cpu;

import graphic.*;
import memory.*;

import java.io.FileNotFoundException;
import java.io.IOException;

public class CPU {

	enum State{
		HALT,
		STOP,
		RUN
	}

	private InterruptsController inc;
	private RegistersController regc;
	private MMU mmu;
	private OpController insc;
	private int cycles;
	private Timer timer;
	private State state;
	private GPU gpu;

	public CPU() throws IOException {
		inc = new InterruptsController();
		timer = new Timer(inc);
		mmu = new MMU(inc, timer);
	//	gpu = new GPU(mmu, inc);
		regc = new RegistersController(mmu);
		insc = new OpController(mmu, inc);
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
		mmu.writeByte(0, 0x80);// LCD STAT;
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
		mmu.writeByte(0xFF40, 0x91); // LCDC
		mmu.writeByte(0xFF42, 0x00); // SCY
		mmu.writeByte(0xFF43, 0x00); // SCX
		mmu.writeByte(0xFF45, 0x00); // LYC
		mmu.writeByte(0xFF47, 0xFC); // BGP
		mmu.writeByte(0xFF48, 0xFF); // OBP0
		mmu.writeByte(0xFF49, 0xFF); // OBP1
		mmu.writeByte(0xFF4A, 0x00); // WY
		mmu.writeByte(0xFF4B, 0x00); // WX/home/dk/Documents/Gameboy/7.txt
		mmu.writeByte(0xFFFF, 0x00); // I
	}

	public void run() throws FileNotFoundException {
	/*	PrintStream out = new PrintStream(new FileOutputStream("7.txt"));
		System.setOut(out);*/
		while(true) {
			int current_op = mmu.getByte(regc.getRegval("PC"));
		//	System.out.println("A: " + String.format("%x",regc.getRegval("A")) + ", BC: " + String.format("%x",regc.getRegval("BC")) + ", DE:" + String.format("%x", regc.getRegval("DE")) + ", HL:" + String.format("%x", regc.getRegval("HL")));
		//	System.out.println("//PC: " + String.format("%x",regc.getRegval("PC")) +  "    OPCODE: " + String.format("%x", current_op) + " STACKADD: "+ String.format("%x", regc.getRegval("SP"))+ "STACK:  "+ String.format("%x", mmu.getByte(regc.getRegval("PC")))+ ", "+ String.format("%x", mmu.getByte(regc.getRegval("SP")+1)) +" NEXT TWO BYTES: " + String.format("%x", mmu.getByte(regc.getRegval("PC")+1)) + " , " + String.format("%x", mmu.getByte(regc.getRegval("PC")+2))+ " ///// "+ Bits.isBit(regc.getRegval("F"), 7));
			regc.incrPC();
			int cycl=insc.execInstr(current_op, regc);
	//		if(cycl == -1 || cycl == -2) break;
			cycles+= cycl;
			timer.tick();
			if (mmu.getByte(0xff02) == 0x81) {
				int c = mmu.getByte(0xff01);
				System.out.println((char)c);
				mmu.writeByte(0xff02, 0x0);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		CPU cpu = new CPU();
		cpu.reset();
		cpu.run();
	}

}
