package bgu.spl.net.impl.bgs.Messages;

import bgu.spl.net.impl.bgs.Messages.bgsMessage;

import java.nio.charset.StandardCharsets;

public class PostMessage extends bgsMessage {

    private final int opcode = 5;
    private String content;

    @Override
    public short getOp() {
        return 0;
    }

    @Override
    public void fromBytes(byte[] msg, int length) {
        content = new String(msg, 0, getNextNullTerminator(msg, 0), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }

    public String getContent() {
        return content;
    }
}
