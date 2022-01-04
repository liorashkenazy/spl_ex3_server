package bgu.spl.net.impl.BGSServer.Messages;

public abstract class bgsMessage {

    public abstract short getOp();

    public abstract void fromBytes(byte[] msg, int length);

    public abstract byte[] toBytes();

    protected int getNextNullTerminator(byte[] msg, int last_index) {
        for (int i = last_index; i < msg.length; i++) {
            if (msg[i] == 0) {
                return i;
            }
        }
        return -1;
    }
}
