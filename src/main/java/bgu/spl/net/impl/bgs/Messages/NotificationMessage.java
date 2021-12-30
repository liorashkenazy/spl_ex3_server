package bgu.spl.net.impl.bgs.Messages;

import bgu.spl.net.impl.bgs.Messages.bgsMessage;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class NotificationMessage extends bgsMessage {

    private final short opcode = 9;
    private byte type;
    private String postingUser;
    private String content;

    public NotificationMessage(byte type, String user, String content) {
        this.type = type;
        this.postingUser = user;
        this.content = content;
    }

    @Override
    public short getOp() {
        return opcode;
    }

    @Override
    public void fromBytes(byte[] msg, int length) {
    }

    @Override
    public byte[] toBytes() {
        byte[] ret = new byte[postingUser.length() + content.length() + 5];
        ByteBuffer ret_buf = ByteBuffer.wrap(ret);
        ret_buf.putShort(opcode);
        ret_buf.put(type);
        ret_buf.put(postingUser.getBytes(StandardCharsets.UTF_8));
        ret_buf.put((byte) 0);
        ret_buf.put(content.getBytes(StandardCharsets.UTF_8));
        ret_buf.put((byte) 0);
        return ret;
    }
}
