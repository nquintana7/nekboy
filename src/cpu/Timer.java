package cpu;

public class Timer {
    public int DIV = 0;
    public int TIMA = 0;
    public int TMA = 0;
    public int TAC = 0;
    public InterruptsController ic;
    public int divcounter;
    public int timacounter;


    public Timer(InterruptsController c) {
        this.ic = c;
    }

    public void tick(int cycles) {
        divcounter = divcounter+cycles;
        if(divcounter >= 256) {
            DIV++;
            divcounter = 0;
        }
        if(Bits.isBit(TAC,2)) {
            int clockmode = TAC&0b11;
            timacounter+=cycles;
            if(clockmode == 0) {
                if(timacounter >= 1024) {
                    TIMA++;
                    timacounter = 0;
                }
            } else if(clockmode == 0b01) {
                if(timacounter >= 16) {
                    TIMA++;
                    timacounter = 0;
                }
            } else if(clockmode ==0b10) {
                if(timacounter >= 64) {
                    TIMA++;
                    timacounter = 0;
                }
            } else if(clockmode ==0b11) {
                if(timacounter >= 256) {
                    TIMA++;
                    timacounter = 0;
                }
            }
            if(TIMA > 0xff) {
                TIMA = TMA&0xff;
                this.ic.requestInterrupt(2);
            }
        }
    }

    public void writeToTac(int value) {
        TAC = value;
    }

    public void writeToTMA(int value) {
        TMA = value;
    }

    public void writeToDiv() {
        DIV = 0;
    }

    public void  writeToTIMA(int value) {
        TIMA = value;
    }
}
