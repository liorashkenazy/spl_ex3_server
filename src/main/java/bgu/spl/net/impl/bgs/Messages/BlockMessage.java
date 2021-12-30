package bgu.spl.net.impl.bgs.Messages;

import bgu.spl.net.impl.bgs.Messages.bgsMessage;

import java.nio.charset.StandardCharsets;

public class BlockMessage extends bgsMessage {

    private final int opcode = 12;
    private String username;

    @Override
    public short getOp() {
        return 0;
    }

    public String getUsername() { return username; }

    @Override
    public void fromBytes(byte[] msg, int length) {
        username = new String(msg, 0, getNextNullTerminator(msg, 0), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }
}
