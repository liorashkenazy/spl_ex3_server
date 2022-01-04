package bgu.spl.net.impl.BGSServer.Messages;

public class LogStatMessage extends bgsMessage {

    @Override
    public short getOp() {
        return 7;
    }

    @Override
    public void fromBytes(byte[] msg, int length) { }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }
}
