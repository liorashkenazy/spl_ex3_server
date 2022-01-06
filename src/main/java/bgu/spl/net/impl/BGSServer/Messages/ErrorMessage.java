package bgu.spl.net.impl.BGSServer.Messages;

import java.nio.ByteBuffer;

public class ErrorMessage extends bgsMessage {

    private final short opcode = 11;
    private short messageOpcode;

    public ErrorMessage(short message_op) {
        this.messageOpcode = message_op;
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
        byte[] ret = new byte[4];
        ByteBuffer ret_buf = ByteBuffer.wrap(ret);
        ret_buf.putShort(opcode);
        ret_buf.putShort(messageOpcode);
        ret_buf.put((byte)';');
        return ret;
    }
}
