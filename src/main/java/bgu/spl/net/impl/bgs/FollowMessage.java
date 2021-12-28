package bgu.spl.net.impl.bgs;

import java.nio.charset.StandardCharsets;

public class FollowMessage extends bgsMessage {

    private final int op = 4;
    private byte follow;
    private String username;

    @Override
    public short getOp() {
        return 0;
    }

    @Override
    public void fromBytes(byte[] msg, int length) {
        follow = msg[0];
        username = new String(msg, 1, length - 1, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }
}
