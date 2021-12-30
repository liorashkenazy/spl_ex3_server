package bgu.spl.net.impl.bgs.Messages;

import bgu.spl.net.impl.bgs.Messages.bgsMessage;

import java.nio.ByteBuffer;

public class AckMessage extends bgsMessage {

    private final short opcode = 10;
    private short messageOpcode;
    private byte[] optional_msg;

    public AckMessage(short message_op, byte[] msg) {
        this.messageOpcode = message_op;
        this.optional_msg = msg;
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
        int size = 4 + (optional_msg == null ? 0 : optional_msg.length);
        byte[] ret = new byte[size];
        ByteBuffer ret_buf = ByteBuffer.wrap(ret);
        ret_buf.putShort(opcode);
        ret_buf.putShort(messageOpcode);
        if (optional_msg != null) {
            ret_buf.put(optional_msg);
        }
        return ret;
    }
}
