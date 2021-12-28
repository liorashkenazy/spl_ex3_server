package bgu.spl.net.impl.bgs;

import java.nio.charset.StandardCharsets;

public class RegisterMessage extends bgsMessage {

    private final short op = 1;
    private String username;
    private String password;
    private String birthday;

    @Override
    public short getOp() {
        return op;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getBirthday() { return birthday; }

    @Override
    public void fromBytes(byte[] msg, int length) {
        int index = getNextNullTerminator(msg,0);
        username = new String(msg, 0, index, StandardCharsets.UTF_8);
        password = new String(msg, index + 1, getNextNullTerminator(msg,index + 1) - index, StandardCharsets.UTF_8);
        index = getNextNullTerminator(msg,index + 1) + 1;
        birthday = new String(msg, index, getNextNullTerminator(msg, index) - index, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }
}
