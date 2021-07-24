package memory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ROMController {
    private int[] boot_rom;
    private int[] rom_0;
    private int[][] rom_banks;
    private boolean ex_ram = false;
    private boolean enabled_ramtimer = false;
    private boolean enabled_eram = false;
    private boolean enabled_timer = false;
    private int[][] eram;
    private boolean battery;
    private int actual_bank;
    private int actual_ram;
    private boolean left_boot;
    private MBC_status mbc;
    private int RTC;

    private enum MBC_status {
        NO_BANK,
        MBC1,
        MBC3
    };

    public int getByte(int addr) {
        if (addr <= 0x3fff) {
            if(!left_boot && addr < 256) {
                return boot_rom[addr];
            } else {
                return rom_banks[0][addr];
            }
        } else if (addr <=0x7fff){
            return rom_banks[actual_bank][addr-0x4000];
        } else if (addr >= 0xa000 && addr <= 0xbfff) {
          if(enabled_eram)  {
              return eram[actual_ram][addr-0xa000];
          } else if(enabled_timer) {
              return RTC;
          }
        }
        return 0;
    }

    public void disable_bootrom () { left_boot = true; }

    public void writeByte(int addr, int value) {
        addr = addr&0xffff;
        value = value&0xff;
        if(mbc != MBC_status.NO_BANK) {
            if(addr <= 0x1fff) {
                if(value == 0) {
                    enabled_eram = false;
                    enabled_timer = false;
                    enabled_ramtimer = false;
                } else if(value == 0xa) {
                    enabled_eram = true;
                    enabled_ramtimer = true;
                }
            } else if(addr <= 0x3fff) {
                actual_bank = value;
                if(value == 0) actual_bank = 1;
            } else if(addr <= 0x5fff) {
                if(enabled_ramtimer) {
                    if(value <= 0x3) {
                        enabled_eram = true;
                        enabled_timer = false;
                        actual_ram = value;
                    } else if(value >= 0x8 && value <= 0xc) {
                        enabled_eram = false;
                        enabled_timer = true;
                    }
                }
            } else if(addr >= 0xa000 & addr <= 0xbfff) {
              if(enabled_eram)  eram[actual_ram][addr-0xa000] = value;
            }
        }
    }

    public void loadROM() throws IOException{
        File file = new File("pokemonred.gb");
        byte[] bytes = new byte[(int) file.length()];
        rom_0 = new int[16384];
        FileInputStream fis = new FileInputStream(file);
        fis.read(bytes);
        fis.close();
        for(int i = 0; i<rom_0.length;i++) {
            rom_0[i] = ((int)bytes[i])&0xff;
        }
        left_boot = false;
        decideMBC(bytes);
        decideRAM(bytes);
        loadBootROM();
    }

    private void decideMBC(byte[] bytes) {
        switch(rom_0[0x147])  {
            case(0): {
                rom_banks = new int[2][16384];
                rom_banks[0] = rom_0;
                rom_banks[1] = new int[16384];
                for(int i = 16384; i<bytes.length;i++) {
                    rom_banks[1][i-16384] = ((int)bytes[i])&0xff;
                }
                mbc = MBC_status.NO_BANK;
                actual_bank = 1;
                break;
            }
            case(0x1) : {
                int rom_size = rom_0[0x148];
                switch(rom_size) {
                    case(0x1): {
                        rom_banks = new int[4][16384];
                        rom_banks[0] = rom_0;
                        for(int i = 1; i<4; i++) {
                            for(int j = 0; j<16384;j++) {
                                rom_banks[i][j] = ((int)bytes[(i*16384)+j])&0xff;
                            }
                        }
                        break;
                }
                }
                mbc = MBC_status.MBC1;
                actual_bank = 1;
                break;
            }

            case(0x13): {
                int rom_size = rom_0[0x148];
                switch(rom_size) {
                    case(0x5): {
                        rom_banks = new int[64][16384];
                        rom_banks[0] = rom_0;
                        for(int i = 1; i<64; i++) {
                            for(int j = 0; j<16384;j++) {
                                rom_banks[i][j] = ((int)bytes[(i*16384)+j])&0xff;
                            }
                        }
                        mbc = MBC_status.MBC3;
                        break;
                    }
                }
                actual_bank = 1;
                break;
            }
        }
    }

    private void decideRAM(byte[] bytes) {
        switch(rom_0[0x149]) {
            case(0x0): {
                break;
            }
            case(0x2): {
                ex_ram = true;
                eram = new int[1][8192];
                actual_ram = 0;
                break;
            }
            case(0x3): {
                ex_ram = true;
                eram = new int[4][8192];
                actual_ram = 0;
                break;
            }
            case(0x4): {
                ex_ram = true;
                eram = new int[16][8192];
                actual_ram = 0;
            }
        }
    }

    private void loadBootROM() {
        left_boot = false;
        boot_rom = new int[]{
                0x31, 0xFE, 0xFF, 0xAF, 0x21, 0xFF, 0x9F, 0x32, 0xCB, 0x7C, 0x20, 0xFB, 0x21, 0x26, 0xFF, 0x0E,
                0x11, 0x3E, 0x80, 0x32, 0xE2, 0x0C, 0x3E, 0xF3, 0xE2, 0x32, 0x3E, 0x77, 0x77, 0x3E, 0xFC, 0xE0,
                0x47, 0x11, 0x04, 0x01, 0x21, 0x10, 0x80, 0x1A, 0xCD, 0x95, 0x00, 0xCD, 0x96, 0x00, 0x13, 0x7B,
                0xFE, 0x34, 0x20, 0xF3, 0x11, 0xD8, 0x00, 0x06, 0x08, 0x1A, 0x13, 0x22, 0x23, 0x05, 0x20, 0xF9,
                0x3E, 0x19, 0xEA, 0x10, 0x99, 0x21, 0x2F, 0x99, 0x0E, 0x0C, 0x3D, 0x28, 0x08, 0x32, 0x0D, 0x20,
                0xF9, 0x2E, 0x0F, 0x18, 0xF3, 0x67, 0x3E, 0x64, 0x57, 0xE0, 0x42, 0x3E, 0x91, 0xE0, 0x40, 0x04,
                0x1E, 0x02, 0x0E, 0x0C, 0xF0, 0x44, 0xFE, 0x90, 0x20, 0xFA, 0x0D, 0x20, 0xF7, 0x1D, 0x20, 0xF2,
                0x0E, 0x13, 0x24, 0x7C, 0x1E, 0x83, 0xFE, 0x62, 0x28, 0x06, 0x1E, 0xC1, 0xFE, 0x64, 0x20, 0x06,
                0x7B, 0xE2, 0x0C, 0x3E, 0x87, 0xE2, 0xF0, 0x42, 0x90, 0xE0, 0x42, 0x15, 0x20, 0xD2, 0x05, 0x20,
                0x4F, 0x16, 0x20, 0x18, 0xCB, 0x4F, 0x06, 0x04, 0xC5, 0xCB, 0x11, 0x17, 0xC1, 0xCB, 0x11, 0x17,
                0x05, 0x20, 0xF5, 0x22, 0x23, 0x22, 0x23, 0xC9, 0xCE, 0xED, 0x66, 0x66, 0xCC, 0x0D, 0x00, 0x0B,
                0x03, 0x73, 0x00, 0x83, 0x00, 0x0C, 0x00, 0x0D, 0x00, 0x08, 0x11, 0x1F, 0x88, 0x89, 0x00, 0x0E,
                0xDC, 0xCC, 0x6E, 0xE6, 0xDD, 0xDD, 0xD9, 0x99, 0xBB, 0xBB, 0x67, 0x63, 0x6E, 0x0E, 0xEC, 0xCC,
                0xDD, 0xDC, 0x99, 0x9F, 0xBB, 0xB9, 0x33, 0x3E, 0x3C, 0x42, 0xB9, 0xA5, 0xB9, 0xA5, 0x42, 0x3C,
                0x21, 0x04, 0x01, 0x11, 0xA8, 0x00, 0x1A, 0x13, 0xBE, 0x00, 0x00, 0x23, 0x7D, 0xFE, 0x34, 0x20,
                0xF5, 0x06, 0x19, 0x78, 0x86, 0x23, 0x05, 0x20, 0xFB, 0x86, 0x00, 0x00, 0x3E, 0x01, 0xE0, 0x50
        };
    }
}
