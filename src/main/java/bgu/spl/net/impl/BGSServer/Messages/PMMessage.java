package bgu.spl.net.impl.BGSServer.Messages;

import java.nio.charset.StandardCharsets;

public class PMMessage extends bgsMessage {

    private final short op = 6;
    private String username;
    private String content;
    private String dateAndTime;

    @Override
    public short getOp() {
        return op;
    }

    public String getUsername() { return username; }
    public String getContent() { return content; }
    public String getDateAndTime() { return dateAndTime; }

    @Override
    public void fromBytes(byte[] msg, int length) {
        int index = getNextNullTerminator(msg,0);
        username = new String(msg, 0, index, StandardCharsets.UTF_8);
        content = new String(msg, index + 1, getNextNullTerminator(msg,index + 1) - (index + 1), StandardCharsets.UTF_8);
        index = getNextNullTerminator(msg,index + 1) + 1;
        dateAndTime = new String(msg, index, getNextNullTerminator(msg, index) - index, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }
}