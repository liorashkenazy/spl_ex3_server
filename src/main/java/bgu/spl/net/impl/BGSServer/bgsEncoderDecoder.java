package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.BGSServer.Messages.*;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class bgsEncoderDecoder implements MessageEncoderDecoder<bgsMessage> {

    private static final Class<? extends bgsMessage> types_arr[] = new Class[] {RegisterMessage.class,
                                                                                LoginMessage.class,
                                                                                LogoutMessage.class,
                                                                                FollowMessage.class,
                                                                                PostMessage.class,
                                                                                PMMessage.class,
                                                                                LogStatMessage.class,
                                                                                StatMessage.class,
                                                                                BlockMessage.class};

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
        try {
            msg = types_arr[0].getConstructor().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        msg.fromBytes(bytes, len);
        len = 0;
        opcode_len = 0;
        return msg;
    }
}
