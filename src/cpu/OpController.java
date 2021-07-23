package cpu;
import memory.*;

public class OpController {
	public  MMU mmu;
	public  InterruptsController ic;

	public OpController(MMU mm, InterruptsController icc) {
		mmu = mm;
		ic = icc;
	}

	public int execInstr(int opcode, RegistersController regs) {
		int lsb;
		int msb;
		int number;
		switch (opcode & 0xff) {
			case 0x0:
				return 4;
			case 0x1:
				lsb = mmu.getByte(regs.getRegval("PC")) & 0xff;
				regs.incrPC();
				msb = mmu.getByte(regs.getRegval("PC")) & 0xff;
				regs.incrPC();
				ldr16(regs, "BC", get16(lsb, msb));
				return 12;
			case 0x2:
				ldtomem(regs, "BC", regs.getRegval("A"));
				return 8;
			case 0x3:
				return inc16(regs, "BC");
			case 0x4:
				return inc(regs, "B");
			case 0x5:
				return dec(regs, "B");
			case 0x6:
				int b = mmu.getByte(regs.getRegval("PC"));
				regs.incrPC();
				return 4 + ldr8(regs, "B", b);
			case 0x7:
				RLCA(regs, "A");
				return 4;
			case 0x8:
				int ls = mmu.getByte(regs.getRegval("PC"));
				regs.incrPC();
				int m = mmu.getByte(regs.getRegval("PC"));
				regs.incrPC();
				mmu.writeByte(get16(ls,m), regs.getRegval("SP")&0xff);
				mmu.writeByte(get16(ls,m)+1, (regs.getRegval("SP")>>>8)&0xff);
			//	regs.setReg(get16(lsb,msb), "SP");
				return 20;
			case 0x9:
				addHL(regs, "BC");
				return 8;
			case 0xa:
				number = mmu.getByte(regs.getRegval("BC"));
				ldr8(regs, "A", number);
				return 8;
			case 0xb:
				dec16(regs, "BC");
				return 8;
			case 0xc:
				inc(regs, "C");
				return 4;
			case 0xd:
				dec(regs, "C");
				return 4;
			case 0xe:
				ldr8(regs, "C", mmu.getByte(regs.getRegval("PC")));
				regs.incrPC();
				return 8;
			case 0xf:
				RRCA(regs);
				return 4;
			case 0x10:
				return -1;
			case 0x11:
				lsb = mmu.getByte(regs.getRegval("PC")) & 0xff;
				regs.incrPC();
				msb = mmu.getByte(regs.getRegval("PC")) & 0xff;
				regs.incrPC();
				ldr16(regs, "DE", get16(lsb,msb));
				return 12;
			case 0x12:
				ldtomem(regs, "DE", regs.getRegval("A"));
				return 8;
			case 0x13:
				inc16(regs, "DE");
				return 8;
			case 0x14:
				inc(regs, "D");
				return 4;
			case 0x15:
				dec(regs, "D");
				return 4;
			case 0x16:
				ldr8(regs, "D", mmu.getByte(regs.getRegval("PC")));
				regs.incrPC();
				return 8;
			case 0x17:
				RLA(regs);
				return 4;
			case 0x18: {
				byte n = (byte) ((byte) mmu.getByte(regs.getRegval("PC")) & 0xff);
				regs.incrPC();
				int current_pc = regs.getRegval("PC");
				current_pc = (current_pc + n) & 0xffff;
				regs.setReg(current_pc, "PC");
				return 12;
			}
			case 0x19:
				addHL(regs, "DE");
				return 8;
			case 0x1a:
				ldr8(regs, "A", mmu.getByte(regs.getRegval("DE")));
				return 8;
			case 0x1b:
				dec16(regs, "DE");
				return 8;
			case 0x1c:
				inc(regs, "E");
				return 4;
			case 0x1d:
				dec(regs, "E");
				return 4;
			case 0x1e:
				ldr8(regs, "E", mmu.getByte(regs.getRegval("PC")));
				regs.incrPC();
				return 8;
			case 0x1f:
				RRA(regs);
				return 4;
			case 0x20:
				if (!regs.getFlagZ()) {
					byte n = (byte) ((byte) mmu.getByte(regs.getRegval("PC")) & 0xff);
					regs.incrPC();
					int current_pc = regs.getRegval("PC");
					current_pc = (current_pc + n) & 0xffff;
					regs.setReg(current_pc, "PC");
					return 12;
				} else {
					regs.incrPC();
				}
				return 8;
			case 0x21:
				lsb = mmu.getByte(regs.getRegval("PC")) & 0xff;
				regs.incrPC();
				msb = mmu.getByte(regs.getRegval("PC")) & 0xff;
				regs.incrPC();
				ldr16(regs, "HL", get16(lsb, msb));
				return 12;
			case 0x22:
				ldtomem(regs, "HL", regs.getRegval("A"));
				inc16(regs,"HL");
				return 8;
			case 0x23:
				inc16(regs, "HL");
				return 8;
			case 0x24:
				inc(regs, "H");
				return 4;
			case 0x25:
				dec(regs, "H");
				return 4;
			case 0x26:
				ldr8(regs, "H", mmu.getByte(regs.getRegval("PC")));
				regs.incrPC();
				return 8;
			case 0x27:
				DAA(regs);
				return 4;
			case 0x28:
				if (regs.getFlagZ()) {
					byte n = (byte) ((byte) mmu.getByte(regs.getRegval("PC")) & 0xff);
					regs.incrPC();
					int current_pc = regs.getRegval("PC");
					current_pc = (current_pc + n) & 0xffff;
					regs.setReg(current_pc, "PC");
					return 12;
				} else {
					regs.incrPC();
				}
				return 8;
			case 0x29:
				addHL(regs, "HL");
				return 8;
			case 0x2a:
				regs.setReg(0xff & mmu.getByte(regs.getRegval("HL")), "A");
				inc16(regs, "HL");
				return 8;
			case 0x2b:
				dec16(regs, "HL");
				return 8;
			case 0x2c:
				inc(regs, "L");
				return 4;
			case 0x2d:
				dec(regs, "L");
				return 4;
			case 0x2e:
				regs.setReg(mmu.getByte(regs.getRegval("PC")), "L");
				regs.incrPC();
				return 8;
			case 0x2f:
				cpl(regs);
				return 4;
			case 0x30:
				if (!regs.getFlagC()) {
					byte n = (byte) ((byte) mmu.getByte(regs.getRegval("PC")) & 0xff);
					regs.incrPC();
					int current_pc = regs.getRegval("PC");
					current_pc = (current_pc + n) & 0xffff;
					regs.setReg(current_pc, "PC");
					return 12;
				} else {
					regs.incrPC();
				}
				return 8;
			case 0x31:
				lsb = mmu.getByte(regs.getRegval("PC")) & 0xff;
				regs.incrPC();
				msb = mmu.getByte(regs.getRegval("PC")) & 0xff;
				regs.incrPC();
				ldr16(regs, "SP",get16(lsb,msb));
				return 12;
			case 0x32:
				ldtomem(regs, "HL", regs.getRegval("A"));
				dec16(regs ,"HL");
				return 8;
			case 0x33:
				inc16(regs, "SP");
				return 8;
			case 0x34:
				incMem(regs, regs.getRegval("HL"));
				return 12;
			case 0x35:
				decMem(regs, regs.getRegval("HL"));
				return 12;
			case 0x36:
				ldtomem(regs, "HL", mmu.getByte(regs.getRegval("PC")));
				regs.incrPC();
				return 12;
			case 0x37:
				regs.setFlagC(true);
				regs.setFlagN(0);
				regs.setFlagH(false);
				return 4;
			case 0x38:
				if (regs.getFlagC()) {
					byte n = (byte) ((byte) mmu.getByte(regs.getRegval("PC")) & 0xff);
					regs.incrPC();
					int current_pc = regs.getRegval("PC");
					current_pc = (current_pc + n) & 0xffff;
					regs.setReg(current_pc, "PC");
					return 12;
				} else {
					regs.incrPC();
				}
				return 8;
			case 0x39:
				addHL(regs, "SP");
				return 8;
			case 0x3a:
				ldr8(regs, "A", mmu.getByte(regs.getRegval("HL")));
				dec16(regs, "HL");
				return 8;
			case 0x3b:
				dec16(regs, "SP");
				return 8;
			case 0x3c:
				inc(regs, "A");
				return 4;
			case 0x3d:
				dec(regs, "A");
				return 4;
			case 0x3e:
				ldr8(regs, "A", mmu.getByte(regs.getRegval("PC")));
				regs.incrPC();
				return 8;
			case 0x3f:
				ccf(regs);
				return 4;
			case 0x40:
				ldr8(regs, "B", regs.getRegval("B"));
				return 4;
			case 0x41:
				ldr8(regs, "B", regs.getRegval("C"));
				return 4;
			case 0x42:
				ldr8(regs, "B", regs.getRegval("D"));
				return 4;
			case 0x43:
				ldr8(regs, "B", regs.getRegval("E"));
				return 4;
			case 0x44:
				ldr8(regs, "B", regs.getRegval("H"));
				return 4;
			case 0x45:
				ldr8(regs, "B", regs.getRegval("L"));
				return 4;
			case 0x46:
				ldr8(regs, "B", mmu.getByte(regs.getRegval("HL")));
				return 4;
			case 0x47:
				ldr8(regs, "B", regs.getRegval("A"));
				return 4;
			case 0x48:
				ldr8(regs, "C", regs.getRegval("B"));
				return 4;
			case 0x49:
				ldr8(regs, "C", regs.getRegval("C"));
				return 4;
			case 0x4a:
				ldr8(regs, "C", regs.getRegval("D"));
				return 4;
			case 0x4b:
				ldr8(regs, "C", regs.getRegval("E"));
				return 4;
			case 0x4c:
				ldr8(regs, "C", regs.getRegval("H"));
				return 4;
			case 0x4d:
				ldr8(regs, "C", regs.getRegval("L"));
				return 4;
			case 0x4e:
				ldr8(regs, "C", mmu.getByte(regs.getRegval("HL")));
				return 4;
			case 0x4f:
				ldr8(regs, "C", regs.getRegval("A"));
				return 4;
			case 0x50:
				ldr8(regs, "D", regs.getRegval("B"));
				return 4;
			case 0x51:
				ldr8(regs, "D", regs.getRegval("C"));
				return 4;
			case 0x52:
				ldr8(regs, "D", regs.getRegval("D"));
				return 4;
			case 0x53:
				ldr8(regs, "D", regs.getRegval("E"));
				return 4;
			case 0x54:
				ldr8(regs, "D", regs.getRegval("H"));
				return 4;
			case 0x55:
				ldr8(regs, "D", regs.getRegval("L"));
				return 4;
			case 0x56:
				ldr8(regs, "D", mmu.getByte(regs.getRegval("HL")));
				return 4;
			case 0x57:
				ldr8(regs, "D", regs.getRegval("A"));
				return 4;
			case 0x58:
				ldr8(regs, "E", regs.getRegval("B"));
				return 4;
			case 0x59:
				ldr8(regs, "E", regs.getRegval("C"));
				return 4;
			case 0x5a:
				ldr8(regs, "E", regs.getRegval("D"));
				return 4;
			case 0x5b:
				ldr8(regs, "E", regs.getRegval("E"));
				return 4;
			case 0x5c:
				ldr8(regs, "E", regs.getRegval("H"));
				return 4;
			case 0x5d:
				ldr8(regs, "E", regs.getRegval("L"));
				return 4;
			case 0x5e:
				ldr8(regs, "E", mmu.getByte(regs.getRegval("HL")));
				return 4;
			case 0x5f:
				ldr8(regs, "E", regs.getRegval("A"));
				return 4;
			case 0x60:
				ldr8(regs, "H", regs.getRegval("B"));
				return 4;
			case 0x61:
				ldr8(regs, "H", regs.getRegval("C"));
				return 4;
			case 0x62:
				ldr8(regs, "H", regs.getRegval("D"));
				return 4;
			case 0x63:
				ldr8(regs, "H", regs.getRegval("E"));
				return 4;
			case 0x64:
				ldr8(regs, "H", regs.getRegval("H"));
				return 4;
			case 0x65:
				ldr8(regs, "H", regs.getRegval("L"));
				return 4;
			case 0x66:
				ldr8(regs, "H", mmu.getByte(regs.getRegval("HL")));
				return 4;
			case 0x67:
				ldr8(regs, "H", regs.getRegval("A"));
				return 4;
			case 0x68:
				ldr8(regs, "L", regs.getRegval("B"));
				return 4;
			case 0x69:
				ldr8(regs, "L", regs.getRegval("C"));
				return 4;
			case 0x6a:
				ldr8(regs, "L", regs.getRegval("D"));
				return 4;
			case 0x6b:
				ldr8(regs, "L", regs.getRegval("E"));
				return 4;
			case 0x6c:
				ldr8(regs, "L", regs.getRegval("H"));
				return 4;
			case 0x6d:
				ldr8(regs, "L", regs.getRegval("L"));
				return 4;
			case 0x6e:
				ldr8(regs, "L", mmu.getByte(regs.getRegval("HL")));
				return 4;
			case 0x6f:
				ldr8(regs, "L", regs.getRegval("A"));
				return 4;
			case 0x70:
				ldtomem(regs, "HL", regs.getRegval("B"));
				return 8;
			case 0x71:
				ldtomem(regs, "HL", regs.getRegval("C"));
				return 8;
			case 0x72:
				ldtomem(regs, "HL", regs.getRegval("D"));
				return 8;
			case 0x73:
				ldtomem(regs, "HL", regs.getRegval("E"));
				return 8;
			case 0x74:
				ldtomem(regs, "HL", regs.getRegval("H"));
				return 8;
			case 0x75:
				ldtomem(regs, "HL", regs.getRegval("L"));
				return 8;
			case 0x76:
				return -2;
			case 0x77:
				ldtomem(regs, "HL", regs.getRegval("A"));
				return 8;
			case 0x78:
				ldr8(regs, "A", regs.getRegval("B"));
				return 4;
			case 0x79:
				ldr8(regs, "A", regs.getRegval("C"));
				return 4;
			case 0x7a:
				ldr8(regs, "A", regs.getRegval("D"));
				return 4;
			case 0x7b:
				ldr8(regs, "A", regs.getRegval("E"));
				return 4;
			case 0x7c:
				ldr8(regs, "A", regs.getRegval("H"));
				return 4;
			case 0x7d:
				ldr8(regs, "A", regs.getRegval("L"));
				return 4;
			case 0x7e:
				ldr8(regs, "A", mmu.getByte(regs.getRegval("HL")));
				return 8;
			case 0x7f:
				ldr8(regs, "A", regs.getRegval("A"));
				return 4;
			case 0x80:
				return add8(regs, regs.getRegval("B"));
			case 0x81:
				return add8(regs, regs.getRegval("C"));
			case 0x82:
				return add8(regs, regs.getRegval("D"));
			case 0x83:
				return add8(regs, regs.getRegval("E"));
			case 0x84:
				return add8(regs, regs.getRegval("H"));
			case 0x85:
				return add8(regs, regs.getRegval("L"));
			case 0x86:
				return 4 + add8(regs, mmu.getByte(regs.getRegval("HL")));
			case 0x87:
				return add8(regs, regs.getRegval("A"));
			case 0x88:
				return adc8(regs, regs.getRegval("B"));
			case 0x89:
				return adc8(regs, regs.getRegval("C"));
			case 0x8A:
				return adc8(regs, regs.getRegval("D"));
			case 0x8B:
				return adc8(regs, regs.getRegval("E"));
			case 0x8C:
				return adc8(regs, regs.getRegval("H"));
			case 0x8D:
				return adc8(regs, regs.getRegval("L"));
			case 0x8E:
				return 4 + adc8(regs, mmu.getByte(regs.getRegval("HL")));
			case 0x8f:
				return adc8(regs, regs.getRegval("A"));
			case 0x90:
				return sub8(regs, regs.getRegval("B"));
			case 0x91:
				return sub8(regs, regs.getRegval("C"));
			case 0x92:
				return sub8(regs, regs.getRegval("D"));
			case 0x93:
				return sub8(regs, regs.getRegval("E"));
			case 0x94:
				return sub8(regs, regs.getRegval("H"));
			case 0x95:
				return sub8(regs, regs.getRegval("L"));
			case 0x96:
				return 4 + sub8(regs, mmu.getByte(regs.getRegval("HL")));
			case 0x97:
				return sub8(regs, regs.getRegval("A"));
			case 0x98:
				return sbc8(regs, regs.getRegval("B"));
			case 0x99:
				return sbc8(regs, regs.getRegval("C"));
			case 0x9a:
				return sbc8(regs, regs.getRegval("D"));
			case 0x9b:
				return sbc8(regs, regs.getRegval("E"));
			case 0x9c:
				return sbc8(regs, regs.getRegval("H"));
			case 0x9d:
				return sbc8(regs, regs.getRegval("L"));
			case 0x9e:
				return 4 + sbc8(regs, mmu.getByte(regs.getRegval("HL")));
			case 0x9f:
				return sbc8(regs, regs.getRegval("A"));
			case 0xa0:
				return and(regs, regs.getRegval("B"));
			case 0xa1:
				return and(regs, regs.getRegval("C"));
			case 0xa2:
				return and(regs, regs.getRegval("D"));
			case 0xa3:
				return and(regs, regs.getRegval("E"));
			case 0xa4:
				return and(regs, regs.getRegval("H"));
			case 0xa5:
				return and(regs, regs.getRegval("L"));
			case 0xa6:
				return 4 + and(regs, mmu.getByte(regs.getRegval("HL")));
			case 0xa7:
				return and(regs, regs.getRegval("A"));
			case 0xa8:
				return xor(regs, regs.getRegval("B"));
			case 0xa9:
				return xor(regs, regs.getRegval("C"));
			case 0xaa:
				return xor(regs, regs.getRegval("D"));
			case 0xab:
				return xor(regs, regs.getRegval("E"));
			case 0xac:
				return xor(regs, regs.getRegval("H"));
			case 0xad:
				return xor(regs, regs.getRegval("L"));
			case 0xae:
				return 4 + xor(regs, mmu.getByte(regs.getRegval("HL")));
			case 0xaf:
				return xor(regs, regs.getRegval("A"));
			case 0xb0:
				return or(regs, regs.getRegval("B"));
			case 0xb1:
				return or(regs, regs.getRegval("C"));
			case 0xb2:
				return or(regs, regs.getRegval("D"));
			case 0xb3:
				return or(regs, regs.getRegval("E"));
			case 0xb4:
				return or(regs, regs.getRegval("H"));
			case 0xb5:
				return or(regs, regs.getRegval("L"));
			case 0xb6:
				return 4 + or(regs, mmu.getByte(regs.getRegval("HL")));
			case 0xb7:
				return or(regs, regs.getRegval("A"));
			case 0xb8:
				return cp(regs, regs.getRegval("B"));
			case 0xb9:
				return cp(regs, regs.getRegval("C"));
			case 0xba:
				return cp(regs, regs.getRegval("D"));
			case 0xbb:
				return cp(regs, regs.getRegval("E"));
			case 0xbc:
				return cp(regs, regs.getRegval("H"));
			case 0xbd:
				return cp(regs, regs.getRegval("L"));
			case 0xbe:
				return cp(regs, mmu.getByte(regs.getRegval("HL")));
			case 0xbf:
				return cp(regs, regs.getRegval("A"));
			case 0xc0:
				if (!regs.getFlagZ()) {
					ret(regs);
					return 20;
				} else {
					return 8;
				}
			case 0xc1:
				regs.setReg(regs.popSP(), "BC");
				return 12;
			case 0xc2:
				if (!regs.getFlagZ()) {
					lsb = mmu.getByte(regs.getRegval("PC"));
					regs.incrPC();
					msb = mmu.getByte(regs.getRegval("PC"));
					regs.incrPC();
					jp(regs, get16(lsb, msb));
					return 16;
				} else {
					regs.incrPC();
					regs.incrPC();
					return 12;
				}
			case 0xc3:
				lsb = mmu.getByte(regs.getRegval("PC"));
				regs.incrPC();
				msb = mmu.getByte(regs.getRegval("PC"));
				regs.incrPC();
				jp(regs, get16(lsb, msb));
				return 16;
			case 0xc4:
				if (!regs.getFlagZ()) {
					lsb = mmu.getByte(regs.getRegval("PC"));
					regs.incrPC();
					msb = mmu.getByte(regs.getRegval("PC"));
					regs.incrPC();
					call(regs, get16(lsb, msb));
					return 24;
				} else {
					regs.incrPC();
					regs.incrPC();
					return 12;
				}
			case 0xc5:
				regs.pushSP(regs.getRegval("BC"));
				return 16;
			case 0xc6:
				add8(regs, mmu.getByte(regs.getRegval("PC")));
				regs.incrPC();
				return 8;
			case 0xc7:
				regs.pushSP(regs.getRegval("PC"));
				jp(regs, 0x0);
				return 16;
			case 0xc8:
				if (regs.getFlagZ()) {
					ret(regs);
					return 20;
				} else {
					return 8;
				}
			case 0xc9:
				ret(regs);
				return 16;
			case 0xca:
				if (regs.getFlagZ()) {
					lsb = mmu.getByte(regs.getRegval("PC"));
					regs.incrPC();
					msb = mmu.getByte(regs.getRegval("PC"));
					regs.incrPC();
					jp(regs, get16(lsb, msb));
					return 16;
				} else {
					regs.incrPC();
					regs.incrPC();
					return 12;
				}
			case 0xcb:
				return execCB(regs);
			case 0xcc:
				if (regs.getFlagZ()) {
					lsb = mmu.getByte(regs.getRegval("PC"));
					regs.incrPC();
					msb = mmu.getByte(regs.getRegval("PC"));
					regs.incrPC();
					call(regs, get16(lsb, msb));
					return 24;
				} else {
					regs.incrPC();
					regs.incrPC();
					return 12;
				}
			case 0xcd:
				lsb = mmu.getByte(regs.getRegval("PC"));
				regs.incrPC();
				msb = mmu.getByte(regs.getRegval("PC"));
				regs.incrPC();
				call(regs, get16(lsb, msb));
				return 24;
			case 0xce:
				adc8(regs, mmu.getByte(regs.getRegval("PC")));
				regs.incrPC();
				return 8;
			case 0xcf:
				regs.pushSP(regs.getRegval("PC"));
				jp(regs, 0x8);
				return 16;
			case 0xd0:
				if (!regs.getFlagC()) {
					ret(regs);
					return 20;
				} else {
					return 8;
				}
			case 0xd1:
				regs.setReg(regs.popSP(), "DE");
				return 12;
			case 0xd2:
				if (!regs.getFlagC()) {
					lsb = mmu.getByte(regs.getRegval("PC"));
					regs.incrPC();
					msb = mmu.getByte(regs.getRegval("PC"));
					regs.incrPC();
					jp(regs, get16(lsb, msb));
					return 16;
				} else {
					regs.incrPC();
					regs.incrPC();
					return 12;
				}
			case 0xd4:
				if (!regs.getFlagC()) {
					lsb = mmu.getByte(regs.getRegval("PC"));
					regs.incrPC();
					msb = mmu.getByte(regs.getRegval("PC"));
					regs.incrPC();
					call(regs, get16(lsb, msb));
					return 24;
				} else {
					regs.incrPC();
					regs.incrPC();
					return 12;
				}
			case 0xd5:
				regs.pushSP(regs.getRegval("DE"));
				return 16;
			case 0xd6:
				sub8(regs, mmu.getByte(regs.getRegval("PC")));
				regs.incrPC();
				return 8;
			case 0xd7:
				regs.pushSP(regs.getRegval("PC"));
				jp(regs, 0x10);
				return 16;
			case 0xd8:
				if (regs.getFlagC()) {
					ret(regs);
					return 20;
				} else {
					return 8;
				}
			case 0xd9:
				ic.enableIME();
				ret(regs);
				return 16;
			case 0xda:
				if (regs.getFlagC()) {
					lsb = mmu.getByte(regs.getRegval("PC"));
					regs.incrPC();
					msb = mmu.getByte(regs.getRegval("PC"));
					regs.incrPC();
					jp(regs, get16(lsb, msb));
					return 16;
				} else {
					regs.incrPC();
					regs.incrPC();
					return 12;
				}
			case 0xdc:
				if (regs.getFlagC()) {
					lsb = mmu.getByte(regs.getRegval("PC"));
					regs.incrPC();
					msb = mmu.getByte(regs.getRegval("PC"));
					regs.incrPC();
					call(regs, get16(lsb, msb));
					return 24;
				} else {
					regs.incrPC();
					regs.incrPC();
					return 12;
				}
			case 0xde:
				sbc8(regs, mmu.getByte(regs.getRegval("PC")));
				regs.incrPC();
				return 8;
			case 0xdf:
				regs.pushSP(regs.getRegval("PC"));
				jp(regs, 0x18);
				return 16;
			case 0xe0: {
				int address = 0xff00 + mmu.getByte(regs.getRegval("PC"));
				regs.incrPC();
				mmu.writeByte(address, regs.getRegval("A"));
				return 12;
			}
			case 0xe1:
				regs.setReg(regs.popSP(), "HL");
				return 12;
			case 0xe2: {
				int address = 0xff00 + regs.getRegval("C");
				mmu.writeByte(address, regs.getRegval("A"));
				return 8;
			}
			case 0xe5:
				regs.pushSP(regs.getRegval("HL"));
				return 16;
			case 0xe6:
				and(regs, mmu.getByte(regs.getRegval("PC")));
				regs.incrPC();
				return 8;
			case 0xe7:
				regs.pushSP(regs.getRegval("PC"));
				jp(regs, 0x20);
				return 16;
			case 0xe8:
				addSP(regs, (byte)mmu.getByte(regs.getRegval("PC")));
			/*	int x = (byte)mmu.getByte(regs.getRegval("PC"));
				int n = 0xffff&regs.getRegval("SP");
				int result = (n+x)&0xffff;
				regs.setFlagN(0);
				regs.setFlagZ(1);
				regs.setFlagH((((n&0xf)+(x&0xf))&0x10) == 0x10);
				regs.setFlagC((((n&0xff)+(x&0xff))&0x0100)== 0x0100);
				regs.setReg(result, "SP"); */
				regs.incrPC();
				return 16;
			case 0xe9:
				jp(regs, regs.getRegval("HL"));
				return 4;
			case 0xea:
				lsb = mmu.getByte(regs.getRegval("PC"));
				regs.incrPC();
				msb = mmu.getByte(regs.getRegval("PC"));
				regs.incrPC();
				ldtomemAdr(regs, get16(lsb, msb), "A");
				return 16;
			case 0xee:
				xor(regs, mmu.getByte(regs.getRegval("PC")));
				regs.incrPC();
				return 8;
			case 0xef:
				regs.pushSP(regs.getRegval("PC"));
				jp(regs, 0x28);
				return 16;
			case 0xf0: {
				int address = 0xff00 + mmu.getByte(regs.getRegval("PC"));
				regs.incrPC();
				regs.setReg(mmu.getByte(address), "A");
				return 12;
			}
			case 0xf1:
				regs.setReg(regs.popSP(), "AF");
				return 12;
			case 0xf2:
				ldr8(regs, "A", mmu.getByte(0xff00 + regs.getRegval("C")));
				return 8;
			case 0xf3:
				ic.disableIME();
				return 4;
			case 0xf5:
				regs.pushSP(regs.getRegval("AF"));
				return 16;
			case 0xf6:
				or(regs, mmu.getByte(regs.getRegval("PC")));
				regs.incrPC();
				return 8;
			case 0xf7:
				regs.pushSP(regs.getRegval("PC"));
				jp(regs, 0x30);
				return 16;
			case 0xf8:
				int n = regs.getRegval("SP");
				int x = (byte)mmu.getByte(regs.getRegval("PC"));
				int result = (n+x) & 0xffff;
				regs.setFlagN(0);
				regs.setFlagZ(1);
				regs.setFlagH((((n & 0xf) + (x & 0xf)) & 0x10) == 0x10);
				regs.setFlagC(((n & 0xff) + (x & 0xff)) > 0xff);
				ldr16(regs, "HL", result);
				regs.incrPC();
				return 12;
			case 0xf9:
				ldr16(regs, "SP", regs.getRegval("HL"));
				return 8;
			case 0xfa:
				lsb = 0xff&mmu.getByte(regs.getRegval("PC"));
				regs.incrPC();
				msb = 0xff&mmu.getByte(regs.getRegval("PC"));
				regs.incrPC();
				ldr8(regs, "A", mmu.getByte(get16(lsb, msb)));
				return 16;
			case 0xfb:
				ic.enableIME();
				return 4;
			case 0xfe:
				cp(regs, mmu.getByte(regs.getRegval("PC")));
				regs.incrPC();
				return 8;
			case 0xff:
				regs.pushSP(regs.getRegval("PC"));
				jp(regs, 0x38);
				return 16;
		}
		return 0;
	}

	public int execCB(RegistersController regs) {
		int opcode = mmu.getByte(regs.getRegval("PC"));
		regs.incrPC();
		switch (opcode & 0xff) {
			case 0x0:
				RLC(regs, "B");
				return 8;
			case 0x1:
				RLC(regs, "C");
				return 8;
			case 0x2:
				RLC(regs, "D");
				return 8;
			case 0x3:
				RLC(regs, "E");
				return 8;
			case 0x4:
				RLC(regs, "H");
				return 8;
			case 0x5:
				RLC(regs, "L");
				return 8;
			case 0x6:
				RLCmem(regs, "HL");
				return 16;
			case 0x7:
				RLC(regs, "A");
				return 8;
			case 0x8:
				RRC(regs, "B");
				return 8;
			case 0x9:
				RRC(regs, "C");
				return 8;
			case 0xa:
				RRC(regs, "D");
				return 8;
			case 0xb:
				RRC(regs, "E");
				return 8;
			case 0xc:
				RRC(regs, "H");
				return 8;
			case 0xd:
				RRC(regs, "L");
				return 8;
			case 0xe:
				RRCmem(regs, "HL");
				return 8;
			case 0xf:
				RRC(regs, "A");
				return 8;
			case 0x10:
				RL(regs, "B");
				return 8;
			case 0x11:
				RL(regs, "C");
				return 8;
			case 0x12:
				RL(regs, "D");
				return 8;
			case 0x13:
				RL(regs, "E");
				return 8;
			case 0x14:
				RL(regs, "H");
				return 8;
			case 0x15:
				RL(regs, "L");
				return 8;
			case 0x16:
				RLmem(regs, "HL");
				return 16;
			case 0x17:
				RL(regs, "A");
				return 8;
			case 0x18:
				RR(regs, "B");
				return 8;
			case 0x19:
				RR(regs, "C");
				return 8;
			case 0x1a:
				RR(regs, "D");
				return 8;
			case 0x1b:
				RR(regs, "E");
				return 8;
			case 0x1c:
				RR(regs, "H");
				return 8;
			case 0x1d:
				RR(regs, "L");
				return 8;
			case 0x1e:
				RRmem(regs, "HL");
				return 16;
			case 0x1f:
				RR(regs, "A");
				return 8;
			case 0x20:
				SLA(regs, "B");
				return 8;
			case 0x21:
				SLA(regs, "C");
				return 8;
			case 0x22:
				SLA(regs, "D");
				return 8;
			case 0x23:
				SLA(regs, "E");
				return 8;
			case 0x24:
				SLA(regs, "H");
				return 8;
			case 0x25:
				SLA(regs, "L");
				return 8;
			case 0x26:
				SLAmem(regs, "HL");
				return 16;
			case 0x27:
				SLA(regs, "A");
				return 8;
			case 0x28:
				SRA(regs, "B");
				return 8;
			case 0x29:
				SRA(regs, "C");
				return 8;
			case 0x2a:
				SRA(regs, "D");
				return 8;
			case 0x2b:
				SRA(regs, "E");
				return 8;
			case 0x2c:
				SRA(regs, "H");
				return 8;
			case 0x2d:
				SRA(regs, "L");
				return 8;
			case 0x2e:
				SRAmem(regs, "HL");
				return 16;
			case 0x2f:
				SRA(regs, "A");
				return 8;
			case 0x30:
				swapR(regs, "B");
				return 8;
			case 0x31:
				swapR(regs, "C");
				return 8;
			case 0x32:
				swapR(regs, "D");
				return 8;
			case 0x33:
				swapR(regs, "E");
				return 8;
			case 0x34:
				swapR(regs, "H");
				return 8;
			case 0x35:
				swapR(regs, "L");
				return 8;
			case 0x36:
				swapmem(regs, "HL");
				return 16;
			case 0x37:
				swapR(regs, "A");
				return 8;
			case 0x38:
				SRL(regs, "B");
				return 8;
			case 0x39:
				SRL(regs, "C");
				return 8;
			case 0x3a:
				SRL(regs, "D");
				return 8;
			case 0x3b:
				SRL(regs, "E");
				return 8;
			case 0x3c:
				SRL(regs, "H");
				return 8;
			case 0x3d:
				SRL(regs, "L");
				return 8;
			case 0x3e:
				SRLmem(regs, "HL");
				return 16;
			case 0x3f:
				SRL(regs, "A");
				return 8;
			case 0x40:
				bit(regs, 0, regs.getRegval("B"));
				return 8;
			case 0x41:
				bit(regs, 0, regs.getRegval("C"));
				return 8;
			case 0x42:
				bit(regs, 0, regs.getRegval("D"));
				return 8;
			case 0x43:
				bit(regs, 0, regs.getRegval("E"));
				return 8;
			case 0x44:
				bit(regs, 0, regs.getRegval("H"));
				return 8;
			case 0x45:
				bit(regs, 0, regs.getRegval("L"));
				return 8;
			case 0x46:
				bit(regs, 0, mmu.getByte(regs.getRegval("HL")));
				return 12;
			case 0x47:
				bit(regs, 0, regs.getRegval("A"));
				return 8;
			case 0x48:
				bit(regs, 1, regs.getRegval("B"));
				return 8;
			case 0x49:
				bit(regs, 1, regs.getRegval("C"));
				return 8;
			case 0x4a:
				bit(regs, 1, regs.getRegval("D"));
				return 8;
			case 0x4b:
				bit(regs, 1, regs.getRegval("E"));
				return 8;
			case 0x4c:
				bit(regs, 1, regs.getRegval("H"));
				return 8;
			case 0x4d:
				bit(regs, 1, regs.getRegval("L"));
				return 8;
			case 0x4e:
				bit(regs, 1, mmu.getByte(regs.getRegval("HL")));
				return 12;
			case 0x4f:
				bit(regs, 1, regs.getRegval("A"));
				return 8;
			case 0x50:
				bit(regs, 2, regs.getRegval("B"));
				return 8;
			case 0x51:
				bit(regs, 2, regs.getRegval("C"));
				return 8;
			case 0x52:
				bit(regs, 2, regs.getRegval("D"));
				return 8;
			case 0x53:
				bit(regs, 2, regs.getRegval("E"));
				return 8;
			case 0x54:
				bit(regs, 2, regs.getRegval("H"));
				return 8;
			case 0x55:
				bit(regs, 2, regs.getRegval("L"));
				return 8;
			case 0x56:
				bit(regs, 2, mmu.getByte(regs.getRegval("HL")));
				return 12;
			case 0x57:
				bit(regs, 2, regs.getRegval("A"));
				return 8;
			case 0x58:
				bit(regs, 3, regs.getRegval("B"));
				return 8;
			case 0x59:
				bit(regs, 3, regs.getRegval("C"));
				return 8;
			case 0x5a:
				bit(regs, 3, regs.getRegval("D"));
				return 8;
			case 0x5b:
				bit(regs, 3, regs.getRegval("E"));
				return 8;
			case 0x5c:
				bit(regs, 3, regs.getRegval("H"));
				return 8;
			case 0x5d:
				bit(regs, 3, regs.getRegval("L"));
				return 8;
			case 0x5e:
				bit(regs, 3, mmu.getByte(regs.getRegval("HL")));
				return 12;
			case 0x5f:
				bit(regs, 3, regs.getRegval("A"));
				return 8;
			case 0x60:
				bit(regs, 4, regs.getRegval("B"));
				return 8;
			case 0x61:
				bit(regs, 4, regs.getRegval("C"));
				return 8;
			case 0x62:
				bit(regs, 4, regs.getRegval("D"));
				return 8;
			case 0x63:
				bit(regs, 4, regs.getRegval("E"));
				return 8;
			case 0x64:
				bit(regs, 4, regs.getRegval("H"));
				return 8;
			case 0x65:
				bit(regs, 4, regs.getRegval("L"));
				return 8;
			case 0x66:
				bit(regs, 4, mmu.getByte(regs.getRegval("HL")));
				return 12;
			case 0x67:
				bit(regs, 4, regs.getRegval("A"));
				return 8;
			case 0x68:
				bit(regs, 5, regs.getRegval("B"));
				return 8;
			case 0x69:
				bit(regs, 5, regs.getRegval("C"));
				return 8;
			case 0x6a:
				bit(regs, 5, regs.getRegval("D"));
				return 8;
			case 0x6b:
				bit(regs, 5, regs.getRegval("E"));
				return 8;
			case 0x6c:
				bit(regs, 5, regs.getRegval("H"));
				return 8;
			case 0x6d:
				bit(regs, 5, regs.getRegval("L"));
				return 8;
			case 0x6e:
				bit(regs, 5, mmu.getByte(regs.getRegval("HL")));
				return 12;
			case 0x6f:
				bit(regs, 5, regs.getRegval("A"));
				return 8;
			case 0x70:
				bit(regs, 6, regs.getRegval("B"));
				return 8;
			case 0x71:
				bit(regs, 6, regs.getRegval("C"));
				return 8;
			case 0x72:
				bit(regs, 6, regs.getRegval("D"));
				return 8;
			case 0x73:
				bit(regs, 6, regs.getRegval("E"));
				return 8;
			case 0x74:
				bit(regs, 6, regs.getRegval("H"));
				return 8;
			case 0x75:
				bit(regs, 6, regs.getRegval("L"));
				return 8;
			case 0x76:
				bit(regs, 6, mmu.getByte(regs.getRegval("HL")));
				return 12;
			case 0x77:
				bit(regs, 6, regs.getRegval("A"));
				return 8;
			case 0x78:
				bit(regs, 7, regs.getRegval("B"));
				return 8;
			case 0x79:
				bit(regs, 7, regs.getRegval("C"));
				return 8;
			case 0x7a:
				bit(regs, 7, regs.getRegval("D"));
				return 8;
			case 0x7b:
				bit(regs, 7, regs.getRegval("E"));
				return 8;
			case 0x7c:
				bit(regs, 7, regs.getRegval("H"));
				return 8;
			case 0x7d:
				bit(regs, 7, regs.getRegval("L"));
				return 8;
			case 0x7e:
				bit(regs, 7, mmu.getByte(regs.getRegval("HL")));
				return 12;
			case 0x7f:
				bit(regs, 7, regs.getRegval("A"));
				return 8;
			case 0x80:
				resetR(regs, 0, "B");
				return 8;
			case 0x81:
				resetR(regs, 0, "C");
				return 8;
			case 0x82:
				resetR(regs, 0, "D");
				return 8;
			case 0x83:
				resetR(regs, 0, "E");
				return 8;
			case 0x84:
				resetR(regs, 0, "H");
				return 8;
			case 0x85:
				resetR(regs, 0, "L");
				return 8;
			case 0x86:
				resetemem(regs, 0, "HL");
				return 16;
			case 0x87:
				resetR(regs, 0, "A");
				return 8;
			case 0x88:
				resetR(regs, 1, "B");
				return 8;
			case 0x89:
				resetR(regs, 1, "C");
				return 8;
			case 0x8a:
				resetR(regs, 1, "D");
				return 8;
			case 0x8b:
				resetR(regs, 1, "E");
				return 8;
			case 0x8c:
				resetR(regs, 1, "H");
				return 8;
			case 0x8d:
				resetR(regs, 1, "L");
				return 8;
			case 0x8e:
				resetemem(regs, 1, "HL");
				return 16;
			case 0x8f:
				resetR(regs, 1, "A");
				return 8;
			case 0x90:
				resetR(regs, 2, "B");
				return 8;
			case 0x91:
				resetR(regs, 2, "C");
				return 8;
			case 0x92:
				resetR(regs, 2, "D");
				return 8;
			case 0x093:
				resetR(regs, 2, "E");
				return 8;
			case 0x94:
				resetR(regs, 2, "H");
				return 8;
			case 0x95:
				resetR(regs, 2, "L");
				return 8;
			case 0x96:
				resetemem(regs, 2, "HL");
				return 16;
			case 0x97:
				resetR(regs, 2, "A");
				return 8;
			case 0x98:
				resetR(regs, 3, "B");
				return 8;
			case 0x99:
				resetR(regs, 3, "C");
				return 8;
			case 0x9a:
				resetR(regs, 3, "D");
				return 8;
			case 0x9b:
				resetR(regs, 3, "E");
				return 8;
			case 0x9c:
				resetR(regs, 3, "H");
				return 8;
			case 0x9d:
				resetR(regs, 3, "L");
				return 8;
			case 0x9e:
				resetemem(regs, 3, "HL");
				return 16;
			case 0x9f:
				resetR(regs, 3, "A");
				return 8;
			case 0xa0:
				resetR(regs, 4, "B");
				return 8;
			case 0xa1:
				resetR(regs, 4, "C");
				return 8;
			case 0xa2:
				resetR(regs, 4, "D");
				return 8;
			case 0xa3:
				resetR(regs, 4, "E");
				return 8;
			case 0xa4:
				resetR(regs, 4, "H");
				return 8;
			case 0xa5:
				resetR(regs, 4, "L");
				return 8;
			case 0xa6:
				resetemem(regs, 4, "HL");
				return 16;
			case 0xa7:
				resetR(regs, 4, "A");
				return 8;
			case 0xa8:
				resetR(regs, 5, "B");
				return 8;
			case 0xa9:
				resetR(regs, 5, "C");
				return 8;
			case 0xaa:
				resetR(regs, 5, "D");
				return 8;
			case 0xab:
				resetR(regs, 5, "E");
				return 8;
			case 0xac:
				resetR(regs, 5, "H");
				return 8;
			case 0xad:
				resetR(regs, 5, "L");
				return 8;
			case 0xae:
				resetemem(regs, 5, "HL");
				return 16;
			case 0xaf:
				resetR(regs, 5, "A");
				return 8;
			case 0xb0:
				resetR(regs, 6, "B");
				return 8;
			case 0xb1:
				resetR(regs, 6, "C");
				return 8;
			case 0xb2:
				resetR(regs, 6, "D");
				return 8;
			case 0xb3:
				resetR(regs, 6, "E");
				return 8;
			case 0xb4:
				resetR(regs, 6, "H");
				return 8;
			case 0xb5:
				resetR(regs, 6, "L");
				return 8;
			case 0xb6:
				resetemem(regs, 6, "HL");
				return 16;
			case 0xb7:
				resetR(regs, 6, "A");
				return 8;
			case 0xb8:
				resetR(regs, 7, "B");
				return 8;
			case 0xb9:
				resetR(regs, 7, "C");
				return 8;
			case 0xba:
				resetR(regs, 7, "D");
				return 8;
			case 0xbb:
				resetR(regs, 7, "E");
				return 8;
			case 0xbc:
				resetR(regs, 7, "H");
				return 8;
			case 0xbd:
				resetR(regs, 7, "L");
				return 8;
			case 0xbe:
				resetemem(regs, 7, "HL");
				return 16;
			case 0xbf:
				resetR(regs, 7, "A");
				return 8;
			case 0xc0:
				setR(regs, 0, "B");
				return 8;
			case 0xc1:
				setR(regs, 0, "C");
				return 8;
			case 0xc2:
				setR(regs, 0, "D");
				return 8;
			case 0xc3:
				setR(regs, 0, "E");
				return 8;
			case 0xc4:
				setR(regs, 0, "H");
				return 8;
			case 0xc5:
				setR(regs, 0, "L");
				return 8;
			case 0xc6:
				setmem(regs, 0, "HL");
				return 16;
			case 0xc7:
				setR(regs, 0, "A");
				return 8;
			case 0xc8:
				setR(regs, 1, "B");
				return 8;
			case 0xc9:
				setR(regs, 1, "C");
				return 8;
			case 0xca:
				setR(regs, 1, "D");
				return 8;
			case 0xcb:
				setR(regs, 1, "E");
				return 8;
			case 0xcc:
				setR(regs, 1, "H");
				return 8;
			case 0xcd:
				setR(regs, 1, "L");
				return 8;
			case 0xce:
				setmem(regs, 1, "HL");
				return 16;
			case 0xcf:
				setR(regs, 1, "A");
				return 8;
			case 0xd0:
				setR(regs, 2, "B");
				return 8;
			case 0xd1:
				setR(regs, 2, "C");
				return 8;
			case 0xd2:
				setR(regs, 2, "D");
				return 8;
			case 0xd3:
				setR(regs, 2, "E");
				return 8;
			case 0xd4:
				setR(regs, 2, "H");
				return 8;
			case 0xd5:
				setR(regs, 2, "L");
				return 8;
			case 0xd6:
				setmem(regs, 2, "HL");
				return 16;
			case 0xd7:
				setR(regs, 2, "A");
				return 8;
			case 0xd8:
				setR(regs, 3, "B");
				return 8;
			case 0xd9:
				setR(regs, 3, "C");
				return 8;
			case 0xda:
				setR(regs, 3, "D");
				return 8;
			case 0xdb:
				setR(regs, 3, "E");
				return 8;
			case 0xdc:
				setR(regs, 3, "H");
				return 8;
			case 0xdd:
				setR(regs, 3, "L");
				return 8;
			case 0xde:
				setmem(regs, 3, "HL");
				return 16;
			case 0xdf:
				setR(regs, 3, "A");
				return 8;
			case 0xe0:
				setR(regs, 4, "B");
				return 8;
			case 0xe1:
				setR(regs, 4, "C");
				return 8;
			case 0xe2:
				setR(regs, 4, "D");
				return 8;
			case 0xe3:
				setR(regs, 4, "E");
				return 8;
			case 0xe4:
				setR(regs, 4, "H");
				return 8;
			case 0xe5:
				setR(regs, 4, "L");
				return 8;
			case 0xe6:
				setmem(regs, 4, "HL");
				return 8;
			case 0xe7:
				setR(regs, 4, "A");
				return 8;
			case 0xe8:
				setR(regs, 5, "B");
				return 8;
			case 0xe9:
				setR(regs, 5, "C");
				return 8;
			case 0xea:
				setR(regs, 5, "D");
				return 8;
			case 0xeb:
				setR(regs, 5, "E");
				return 8;
			case 0xec:
				setR(regs, 5, "H");
				return 8;
			case 0xed:
				setR(regs, 5, "L");
				return 8;
			case 0xee:
				setmem(regs, 5, "HL");
				return 16;
			case 0xef:
				setR(regs, 5, "A");
				return 8;
			case 0xf0:
				setR(regs, 6, "B");
				return 8;
			case 0xf1:
				setR(regs, 6, "C");
				return 8;
			case 0xf2:
				setR(regs, 6, "D");
				return 8;
			case 0xf3:
				setR(regs, 6, "E");
				return 8;
			case 0xf4:
				setR(regs, 6, "H");
				return 8;
			case 0xf5:
				setR(regs, 6, "L");
				return 8;
			case 0xf6:
				setmem(regs, 6, "HL");
				return 16;
			case 0xf7:
				setR(regs, 6, "A");
				return 8;
			case 0xf8:
				setR(regs, 7, "B");
				return 8;
			case 0xf9:
				setR(regs, 7, "C");
				return 8;
			case 0xfa:
				setR(regs, 7, "D");
				return 8;
			case 0xfb:
				setR(regs, 7, "E");
				return 8;
			case 0xfc:
				setR(regs, 7, "H");
				return 8;
			case 0xfd:
				setR(regs, 7, "L");
				return 8;
			case 0xfe:
				setmem(regs, 7, "HL");
				return 16;
			case 0xff:
				setR(regs, 7, "A");
				return 8;
		}
		return 0;
	}


	// ---- ALU-OPERATIONS -----
	public  int add8(RegistersController regs, int x) {
		int a = regs.getRegval("A") & 0xff;
		int result = (a + x) & 0xff;
		regs.setFlagZ(result);
		regs.setFlagN(0);
		boolean b = false;
		if ((((a & 0xf) + (x & 0xf)) & 0x10) == 0x10) b = true;
		regs.setFlagH(b);
		regs.setFlagC((a + x) > 0xff);
		regs.setReg(result, "A");
		return 4;
	}

	public  int adc8(RegistersController regs, int x) {
		int a = regs.getRegval("A")&0xff;
		int carry = 0;
		if(regs.getFlagC()) carry = 1;
		int result = (a+x+carry)&0xff;
		regs.setFlagZ(result);
		regs.setFlagN(0);
		boolean b = false;
		if(((((a&0xf)+(x&0xf))+carry)&0x10) == 0x10) b = true;
		regs.setFlagH(b);
		regs.setFlagC((a+x+carry)>0xff);
		regs.setReg(result, "A");
		return 4;
	}

	public  int sub8(RegistersController regs, int x) {
		int a = regs.getRegval("A") & 0xff;
		int result = (a - x) & 0xff;
		regs.setFlagZ(result);
		regs.setFlagN(1);
		regs.setFlagH((x & 0xf) > (a & 0xf));
		regs.setFlagC((x & 0xff) > (a & 0xff));
		regs.setReg(result, "A");
		return 4;
	}

	public  int sbc8(RegistersController regs, int x) {
		x = x&0xff;
		int a = regs.getRegval("A")&0xff;
		int carry = 0;
		if(regs.getFlagC()) carry = 1;
		int result = (a-(x+carry))&0xff;
		regs.setFlagZ(result);
		regs.setFlagN(1);
		regs.setFlagH(((x&0xf)+carry)>(a&0xf));
		regs.setFlagC((x+carry)>(a));
		regs.setReg(result, "A");
		return 4;
	}


	public  int and(RegistersController regs, int x) {
		int a = regs.getRegval("A");
		int result = a & x & 0xff;
		regs.setFlagZ(result);
		regs.setFlagN(0);
		regs.setFlagH(true);
		regs.setFlagC(false);
		regs.setReg(result, "A");
		return 4;
	}

	public  int or(RegistersController regs, int x) {
		int a = regs.getRegval("A");
		int result = (a | (x & 0xff)) & 0xff;
		regs.setFlagZ(result);
		regs.setFlagN(0);
		regs.setFlagH(false);
		regs.setFlagC(false);
		regs.setReg(result, "A");
		return 4;
	}

	public  int xor(RegistersController regs, int x) {
		int a = regs.getRegval("A");
		int result = a ^ (x & 0xff);
		regs.setFlagZ(result);
		regs.setFlagN(0);
		regs.setFlagH(false);
		regs.setFlagC(false);
		regs.setReg(result, "A");
		return 4;
	}

	public  int cp(RegistersController regs, int x) {
		int a = regs.getRegval("A");
		int result = (a & 0xff) - (x & 0xff);
		regs.setFlagZ(result);
		regs.setFlagN(1);
		regs.setFlagH((x & 0xf) > (a & 0xf));
		regs.setFlagC((x & 0xff) > (a & 0xff));
		return 4;
	}

	public  int inc(RegistersController regs, String x) {
		int n = regs.getRegval(x);
		int result = ((n & 0xff) + 1) & 0xff;
		regs.setFlagZ(result);
		regs.setFlagN(0);
		regs.setFlagH((((n & 0xf) + 1) & 0x10) == 0x10);
		regs.setReg(result, x);
		return 4;
	}

	public  int dec(RegistersController regs, String x) {
		int n = regs.getRegval(x);
		int result = ((n & 0xff) - 1) & 0xff;
		regs.setFlagZ(result);
		regs.setFlagN(1);
		regs.setFlagH(1 > (n & 0xf));
		regs.setReg(result, x);
		return 4;
	}

	public  int addHL(RegistersController regs, String x) {
		int n = regs.getRegval(x)&0xffff;
		int hl = regs.getRegval("HL")&0xffff;
		int result = (hl+n)&0xffff;
		regs.setFlagN(0);
		regs.setFlagC(hl>(0xffff-n));
		regs.setFlagH((((hl&0xfff)+(n&0xfff))&0x1000)==0x1000);
		regs.setReg(result, "HL");
		return 8;
	}

	public  int addSP(RegistersController regs, int x) {
		int n = regs.getRegval("SP");
		int result = (n + x) & 0xffff;
		regs.setFlagN(0);
		regs.setFlagZ(1);
		regs.setFlagH((((n & 0xf) + (x & 0xf)) & 0x10) == 0x10);
		regs.setFlagC(((n & 0xff) + (x & 0xff)) > 0xff);
		regs.setReg(result, "SP");
		return 16;
	}

	public  int inc16(RegistersController regs, String x) {
			int n = regs.getRegval(x);
				n = (n + 1) & 0xffff;
				regs.setReg(n, x);
		return 8;
	}

	public  int dec16(RegistersController regs, String x) {
		int n = regs.getRegval(x);
		n = (n - 1) & 0xffff;
		regs.setReg(n, x);
		return 8;
	}

	// ------ LOADS ------
	public int ldr8(RegistersController regs, String destr8, int valuer8) {
		regs.setReg(valuer8, destr8);
		return 4;
	}

	public  int ldr16(RegistersController regs, String destination, int source) {
		regs.setReg(source, destination);
		return 4;
	}

	public  int ldtomem(RegistersController regs, String des, int value) {
		mmu.writeByte(regs.getRegval(des), value);
		return 8;
	}

	public  int ldtomemAdr(RegistersController regs, int des, String value) {
		mmu.writeByte(des, regs.getRegval(value));
		return 8;
	}

	public  int cpl(RegistersController regs) {
		regs.setReg((~regs.getRegval("A")) & 0xff, "A");
		regs.setFlagN(1);
		regs.setFlagH(true);
		return 4;
	}

	public  int ccf(RegistersController regs) {
		if (regs.getFlagC()) {
			regs.setFlagC(false);
		} else {
			regs.setFlagC(true);
		}
		regs.setFlagN(0);
		regs.setFlagH(false);
		return 4;
	}

	public  int ret(RegistersController regs) {
		regs.setReg(regs.popSP(), "PC");
		return 8;
	}

	public  int get16(int lsb, int msb) {
		return (lsb | (msb << 8)) & 0xffff;
	}

	public  int jp(RegistersController regs, int to) {
		regs.setReg(to, "PC");
		return 0;
	}

	public  int call(RegistersController regs, int to) {
		regs.pushSP(regs.getRegval("PC"));
		jp(regs, to);
		return 12;
	}

	public  int RLA(RegistersController regs) {
		int c = 0;
		if (regs.getFlagC()) c = 1;
		regs.setFlagC(Bits.isBit(regs.getRegval("A"), 7));
		regs.setFlagN(0);
		regs.setFlagZ(1);
		regs.setFlagH(false);
		regs.setReg((regs.getRegval("A") << 1) | c, "A");
		return 4;
	}

	public  int RL(RegistersController regs, String torotate) {
		int value = 0xff & regs.getRegval(torotate);
		int c = value & 0b10000000;
		value = 0xff & (value << 1);
		if (regs.getFlagC()) value = value | 0x1;
		regs.setFlagC(c == 0b10000000);
		regs.setFlagN(0);
		regs.setFlagZ(value);
		regs.setFlagH(false);
		regs.setReg(value, torotate);
		return 4;
	}

	public  int RLCA(RegistersController regs, String torotate) {
		int b = 0;
		if (Bits.isBit(regs.getRegval(torotate), 7)) b = 1;
		regs.setFlagC(Bits.isBit(regs.getRegval(torotate), 7));
		regs.setFlagN(0);
		regs.setFlagZ(1);
		regs.setFlagH(false);
		regs.setReg(regs.getRegval(torotate) << 1 | b, torotate);
		return 4;
	}

	public  int RLC(RegistersController regs, String torotate) {
		int b = 0;
		int value = regs.getRegval(torotate)&0xff;
		if (Bits.isBit(value, 7)) b = 1;
		regs.setFlagC(Bits.isBit(value, 7));
		regs.setFlagN(0);
		regs.setFlagZ(value << 1);
		regs.setFlagH(false);
		regs.setReg((value << 1) | b, torotate);
		return 4;
	}

	public  int RLCmem(RegistersController regs, String torotate) {
		int b = 0;
		int value = mmu.getByte(regs.getRegval(torotate));
		if (Bits.isBit(value, 7)) b = 1;
		regs.setFlagC(Bits.isBit(value, 7));
		regs.setFlagN(0);
		regs.setFlagZ(value << 1);
		regs.setFlagH(false);
		mmu.writeByte(regs.getRegval(torotate), (value << 1) | b);
		return 4;
	}

	public  int RLmem(RegistersController regs, String torotate) {
		int value = 0xff & mmu.getByte(0xffff & regs.getRegval(torotate));
		int c = value & 0b10000000;
		value = 0xff & (value << 1);
		if (regs.getFlagC()) value = value | 0x1;
		regs.setFlagC(c == 0b10000000);
		regs.setFlagN(0);
		regs.setFlagZ(value);
		regs.setFlagH(false);
		mmu.writeByte(0xffff & regs.getRegval(torotate), value);
		return 16;
	}

	public  int RRCmem(RegistersController regs, String torotate) {
		int value = 0xff & mmu.getByte(regs.getRegval(torotate));
		int c = value & 0x1;
		int v = 0xff & Bits.setBit(value >> 1, c == 1, 7);
		regs.setFlagN(0);
		regs.setFlagZ(v);
		regs.setFlagC(c == 1);
		regs.setFlagH(false);
		mmu.writeByte(regs.getRegval(torotate), v);
		return 4;
	}

	public  int RRmem(RegistersController regs, String torotate) {
		int value = 0xff & mmu.getByte(0xffff & regs.getRegval(torotate));
		int b0 = value & 0x1;
		value = 0xff & (value >>> 1);
		if (regs.getFlagC()) value = value | 0b10000000;
		regs.setFlagN(0);
		regs.setFlagZ(value);
		regs.setFlagC(b0 == 0x1);
		regs.setFlagH(false);
		mmu.writeByte(regs.getRegval(torotate), value);
		return 16;
	}

	public  int SLAmem(RegistersController regs, String torotate) {
		int value = 0xff & mmu.getByte(regs.getRegval(torotate));
		regs.setFlagC(Bits.isBit((value), 7));
		value = 0xff & (value << 1);
		regs.setFlagN(0);
		regs.setFlagZ(value);
		regs.setFlagH(false);
		mmu.writeByte(regs.getRegval(torotate), value);
		return 4;
	}

	public  int SRAmem(RegistersController regs, String torotate) {
		int value = 0xff & mmu.getByte(0xffff & regs.getRegval(torotate));
		regs.setFlagC(Bits.isBit(value, 0));
		value = 0xff & (value >>> 1);
		value |= (value & 0b01000000) << 1;
		regs.setFlagN(0);
		regs.setFlagZ(value);
		regs.setFlagH(false);
		mmu.writeByte(0xffff & regs.getRegval(torotate), value);
		return 4;
	}

	public  int SRLmem(RegistersController regs, String torotate) {
		int value = mmu.getByte(regs.getRegval(torotate));
		regs.setFlagC(Bits.isBit(value, 0));
		regs.setFlagN(0);
		regs.setFlagZ(value >>> 1);
		regs.setFlagH(false);
		mmu.writeByte(regs.getRegval(torotate), value >>> 1);
		return 4;
	}

	public  int swapmem(RegistersController regs, String r) {
		int value = mmu.getByte(regs.getRegval(r));
		int msb = (value >>> 4) & 0xf;
		int lsb = value & 0xf;
		regs.setFlagZ(value);
		regs.setFlagC(false);
		regs.setFlagN(0);
		regs.setFlagH(false);
		mmu.writeByte(regs.getRegval(r), (lsb << 4) | msb);
		return 4;
	}


	public  int RRCA(RegistersController regs) {
		int c = 0;
		if (Bits.isBit(regs.getRegval("A"), 0)) c = 1;
		int v = Bits.setBit(regs.getRegval("A") >>> 1, c == 1, 7);
		regs.setFlagN(0);
		regs.setFlagZ(1);
		regs.setFlagC(c == 1);
		regs.setFlagH(false);
		regs.setReg(v, "A");
		return 4;
	}

	public  int RRC(RegistersController regs, String to) {
		int value = 0xff & regs.getRegval(to);
		int c = value & 0x1;
		int v = 0xff & Bits.setBit(value >>> 1, c == 1, 7);
		regs.setFlagN(0);
		regs.setFlagZ(v);
		regs.setFlagC(c == 1);
		regs.setFlagH(false);
		regs.setReg(v, to);
		return 4;
	}

	public  int RRA(RegistersController regs) {
		int b0 = 0, c = 0;
		if (Bits.isBit(regs.getRegval("A"), 0)) b0 = 1;
		if (regs.getFlagC()) c = 1;
		int v = Bits.setBit(regs.getRegval("A") >>> 1, c == 1, 7);
		regs.setFlagN(0);
		regs.setFlagZ(1);
		regs.setFlagC(b0 == 1);
		regs.setFlagH(false);
		regs.setReg(v, "A");
		return 4;
	}

	public  int RR(RegistersController regs, String to) {
		int value = 0xff & regs.getRegval(to);
		int b0 = value & 0x1;
		value = 0xff & (value >>> 1);
		if (regs.getFlagC()) value = value | 0b10000000;
		regs.setFlagN(0);
		regs.setFlagZ(value);
		regs.setFlagC(b0 == 0x1);
		regs.setFlagH(false);
		regs.setReg(value, to);
		return 4;
	}

	public  int SLA(RegistersController regs, String torotate) {
		int value = 0xff & regs.getRegval(torotate);
		regs.setFlagC(Bits.isBit((value), 7));
		value = 0xff & (value << 1);
		regs.setFlagN(0);
		regs.setFlagZ(value);
		regs.setFlagH(false);
		regs.setReg(value, torotate);
		return 4;
	}

	public  int SRA(RegistersController regs, String torotate) {
		int value = 0xff & regs.getRegval(torotate);
		regs.setFlagC(Bits.isBit(value, 0));
		value = 0xff & (value >>> 1);
		value |= (value & 0b01000000) << 1;
		regs.setFlagN(0);
		regs.setFlagZ(value);
		regs.setFlagH(false);
		regs.setReg(value, torotate);
		return 4;
	}

	public  int SRL(RegistersController regs, String torotate) {
		regs.setFlagC(Bits.isBit(regs.getRegval(torotate), 0));
		regs.setFlagN(0);
		regs.setFlagZ(regs.getRegval(torotate) >>> 1);
		regs.setFlagH(false);
		regs.setReg(regs.getRegval(torotate) >>> 1, torotate);
		return 4;
	}

	public  int swapR(RegistersController regs, String r) {
		int msb = (regs.getRegval(r) >>> 4) & 0xf;
		int lsb = regs.getRegval(r) & 0xf;
		regs.setFlagZ(regs.getRegval(r));
		regs.setFlagC(false);
		regs.setFlagN(0);
		regs.setFlagH(false);
		regs.setReg((lsb << 4) | msb, r);
		return 4;
	}

	public  int bit(RegistersController regs, int bit, int value) {
		int b = 0;
		if (Bits.isBit(value, bit)) b = 1;
		regs.setFlagZ(b);
		regs.setFlagN(0);
		regs.setFlagH(true);
		return 8;
	}


	public  int setR(RegistersController regs, int bit, String r) {
		regs.setReg(Bits.setBit(regs.getRegval(r), true, bit), r);
		return 8;
	}

	public  int setmem(RegistersController regs, int bit, String r) {
		int value = mmu.getByte(regs.getRegval(r));
		mmu.writeByte(regs.getRegval(r), Bits.setBit(value, true, bit));
		return 8;
	}

	public  int resetemem(RegistersController regs, int bit, String r) {
		int value = mmu.getByte(regs.getRegval(r));
		mmu.writeByte(regs.getRegval(r), Bits.setBit(value, false, bit));
		return 8;
	}

	public  int resetR(RegistersController regs, int bit, String r) {
		regs.setReg(Bits.setBit(regs.getRegval(r), false, bit), r);
		return 8;
	}

	public  int incMem(RegistersController regs, int addr) {
		int value = 0xff & mmu.getByte(addr & 0xffff);
		regs.setFlagN(0);
		regs.setFlagH((value & 0xf) == 0xf);
		mmu.writeByte(addr & 0xffff, value + 1);
		regs.setFlagZ(0xff & (value + 1));
		return 12;
	}

	public  int decMem(RegistersController regs, int addr) {
		int value = 0xff & mmu.getByte(addr & 0xffff);
		regs.setFlagN(1);
		regs.setFlagH((value & 0xf) == 0x0);
		mmu.writeByte(addr & 0xffff, (value - 1)&0xff);
		regs.setFlagZ(value - 1);
		return 12;
	}

	private  void DAA(RegistersController regs) {
		int a = regs.getRegval("A");
		if (regs.getFlagN()) {
			if (regs.getFlagC()) {
				regs.setReg(0xff & (a - 0x60), "A");
				a = regs.getRegval("A");
			}
			if (regs.getFlagH()) {
				regs.setReg(0xff & (a - 0x6), "A");
				a = regs.getRegval("A");
			}
		} else {
			if (regs.getFlagC() || a > 0x9f) {
				regs.setReg((a + 0x60) & 0xff, "A");
				regs.setFlagC(true);
				a = regs.getRegval("A");
			}
			if (regs.getFlagH() || (a & 0xf) > 0x9) {
				regs.setReg((a + 0x6) & 0xff, "A");
				a = regs.getRegval("A");
			}
		}
		regs.setFlagZ(a);
		regs.setFlagH(false);
	}
}