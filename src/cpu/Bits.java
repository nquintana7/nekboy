package cpu;

public abstract class Bits {
	public static boolean isBit(int numb, int shift) { //returns 1 if true, 0 if false.
		return ((numb>>shift)&1)==1;
	}

	public static int setBit(int numb, boolean toset, int shift) { //setsBit if toset=true, clears bit otherwise
		if(toset) {
			numb |= (1<<shift);
		} else {
			numb &= ~(1<<shift);
		}
		return numb&0xff;
	}
}
