package bgu.spl.net.impl.bgs;

import java.nio.charset.StandardCharsets;

public class StatMessage extends bgsMessage {

    private final int opcode = 8;
    private String listOfUsernames;

    @Override
    public short getOp() {
        return opcode;
    }

    public String getListOfUsernames() { return listOfUsernames; };

    @Override
    public void fromBytes(byte[] msg, int length) {
        listOfUsernames = new String(msg, 0, getNextNullTerminator(msg, 0), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }
}
