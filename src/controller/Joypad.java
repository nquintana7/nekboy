package controller;

import cpu.*;

public class Joypad {
    private int reg;
    private int[] pad_state = {0xf, 0xf}; // [0] directional buttons - [1] action buttons

    public InterruptsManager ic;

    public Joypad (InterruptsManager icc) {
        this.ic = icc;
        reg = 0xff;
    }

    public void writePad_State(int index, boolean set, int shift) {
        pad_state[index] = 0xf&Bits.setBit(pad_state[index], set, shift);
    }

    public void writeByte(int value) {
        reg = value&0x30;
    }

    public int getByte() {
        switch(reg) {
            case 0x10: {
                return (reg)|(pad_state[1]&0xf);
            }
            case 0x20: {
                return (reg)|(pad_state[0]&0xf);
            }
            default: return reg|0xf;
        }
    }
}
