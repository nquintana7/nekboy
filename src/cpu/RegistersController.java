package cpu;

import memory.*;

public class RegistersController {
	private int z_position = 7;
	private int n_position = 6;
	private int h_position = 5;
	private int c_position = 4;
	private MMU mmu;

	private int a,b,c,d,e,f,h,l,sp,pc;

	private enum Register{
		A,
		B,
		C,
		D,
		E,
		F,
		H,
		L,
		AF,
		BC,
		DE,
		HL,
		SP,
		PC
	};

	public RegistersController (MMU mmu) {
		this.mmu = mmu;
		this.a = 0x0;
		this.b = 0x0;
		this.c = 0x0;
		this.d = 0x0;
		this.e = 0x0;
		this.f = 0x0;
		this.h = 0x0;
		this.l = 0x0;
		this.sp = 0x0;
		this.pc = 0x0;
	}

	public int getRegval(String ch) {
		Register r = Register.valueOf(ch);
		switch (r) {
			case A:
				return a&0xff;
			case B:
				return b&0xff;
			case C:
				return c&0xff;
			case D:
				return d&0xff;
			case E:
				return e&0xff;
			case F:
				return f&0xff;
			case H:
				return h&0xff;
			case L:
				return l&0xff;
			case SP:
				return sp&0xffff;
			case PC:
				return pc&0xffff;
			case AF:
				return 0xffff&(a<<8)|f;
			case BC:
				return 0xffff&((b)<<8)|c;
			case DE:
				return 0xffff&(d<<8)|e;
			case HL:
				return 0xffff&(h<<8)|l;
		}
		return 0;
	}

	public void setReg(int set, String ch) {
		Register r = Register.valueOf(ch);
		switch (r) {
		case A:
			a = set&0xff;
			break;
		case B:
			b= set&0xff;
			break;
		case C:
			c= set&0xff;
			break;
		case D:
			d = set&0xff;
			break;
		case E:
			e = set&0xff;
			break;
		case F:
			f= set&0xf0;
			break;
		case H:
			h = set&0xff;
			break;
		case L:
			l = set&0xff;
			break;
		case AF: {
			set = set & 0xffff;
			int msb = (set >>> 8) & 0xff;
			int lsb = set & 0xf0;
			a=msb&0xff;
			f=lsb&0xf0;
		}
			break;
		case BC: {
			set = set&0xffff;
			int msb = (set>>>8)&0xff;
			int lsb = set&0xff;
			b=msb&0xff;
			c=lsb&0xff;
		}
			break;
			case DE: {
			set = set&0xffff;
			int msb = (set>>>8)&0xff;
			int lsb = set&0xff;
			d = msb&0xff;
			e = lsb&0xff;
		}
			break;
		case HL: {
			set = set&0xffff;
			int msb = (set>>>8)&0xff;
			int lsb = set&0xff;
			h=msb&0xff;
			l=lsb&0xff;
		}
			break;
		case SP:
			sp = set&0xffff;
			break;
		case PC:
			pc = set&0xffff;
			break;
	}
		
	}

	public void incrSP () {
		if(sp < 0xffff) {
			sp= 0xffff&(sp+1);
		}
	}

	public void decrSP () {
		sp = (sp-1) &0xffff;
	}

	public void incrPC() {
		pc = (pc+1) &0xffff;
	}

	public void decrPC() {
		pc = (pc-1)&0xffff;
	}

	public int popSP() {
		int lsb = mmu.getByte(sp)&0xff;
		incrSP();
		int msb = (mmu.getByte(sp)&0xff)<<8;
		incrSP();
		return (msb|lsb)&0xffff;
	}

	public void pushSP(int value) {
		value = value&0xffff;
		decrSP();
		mmu.writeByte(sp, (value>>8)&0xff);
		decrSP();
		mmu.writeByte(sp, value&0xff);
	}

	public void enableIME() {

	}

	public boolean getFlagZ() {   //true if 1, false if 0
		return Bits.isBit(f&0xff, z_position);
	}
	
	public boolean getFlagN() {   //true if 1, false if 0
		return Bits.isBit(f&0xff, n_position);
	}
	
	public boolean getFlagH() {   //true if 1, false if 0
		return Bits.isBit(f&0xff, h_position);
	}
	
	public boolean getFlagC() {   //true if 1, false if 0
		return Bits.isBit(f&0xff, c_position);
	}

	public void setFlagZ(int result) { //sets to 1 if result==0, resets otherwise
		f= Bits.setBit(f&0xff, result==0, z_position);
	}
	
	public void setFlagN(int sub) { //sets to 1 if result==0, resets otherwise
		f= Bits.setBit(f&0xff, sub==1, n_position);
	}

	public void setFlagH(boolean b) { //sets to 1 if result==0, resets otherwise
		f= Bits.setBit(f&0xff, b, h_position);
	}
	
	public void setFlagC(boolean c) { //sets to 1 if result==0, resets otherwise
		f= Bits.setBit(f&0xff, c, c_position);
	}
}
