package bgu.spl.net.impl.BGSServer.Messages;

import java.nio.charset.StandardCharsets;

public class PostMessage extends bgsMessage {

    private final int opcode = 5;
    private String content;

    @Override
    public short getOp() {
        return opcode;
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
