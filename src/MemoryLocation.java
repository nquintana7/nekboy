package src;

public interface MemoryLocation {
    public int getByte();
    public void writeByte(int addr, int value);
}
