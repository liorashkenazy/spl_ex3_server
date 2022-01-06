package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.BGSServer.Messages.*;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class bgsEncoderDecoder implements MessageEncoderDecoder<bgsMessage> {

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private byte[] opcode = new byte[2];
    private int opcode_len = 0;
    private int len = 0;

    private void pushBytes(byte nextByte) {
        if (opcode_len != opcode.length) {
            opcode[opcode_len++] = nextByte;
        }
        else {
            if (len == bytes.length) {
                bytes = Arrays.copyOf(bytes, len * 2);
            }
            bytes[len++] = nextByte;
        }
    }

    @Override
    public bgsMessage decodeNextByte(byte nextByte) {
        if (nextByte != ';') {
            pushBytes(nextByte);
        }
        else {
            return popMessage();
        }
        return null;
    }

    @Override
    public byte[] encode(bgsMessage message) {
        return message.toBytes();
    }

    private bgsMessage popMessage() {
        short op = ByteBuffer.wrap(opcode).getShort();
        bgsMessage msg = null;
        switch (op) {
            case 1:
                msg = new RegisterMessage();
                break;
            case 2:
                msg = new LoginMessage();
                break;
            case 3:
                msg = new LogoutMessage();
                break;
            case 4:
                msg = new FollowMessage();
                break;
            case 5:
                msg = new PostMessage();
                break;
            case 6:
                msg = new PMMessage();
                break;
            case 7:
                msg = new LogStatMessage();
                break;
            case 8:
                msg = new StatMessage();
                break;
            case 12:
                msg = new BlockMessage();
                break;
        }
        msg.fromBytes(bytes, len);
        len = 0;
        opcode_len = 0;
        return msg;
    }
}
