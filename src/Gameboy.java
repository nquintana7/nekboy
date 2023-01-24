import cpu.*;
import java.io.IOException;

public class Gameboy {
    public static void main(String[] args) throws IOException, InterruptedException {
        CPU cpu = new CPU(args[0]);
		cpu.reset();
		cpu.run();
	}
}
