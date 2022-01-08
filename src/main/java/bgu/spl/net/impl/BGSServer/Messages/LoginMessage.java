package bgu.spl.net.impl.BGSServer.Messages;

import java.nio.charset.StandardCharsets;

public class LoginMessage extends bgsMessage {

    private final short op = 2;
    private String username;
    private String password;
    private byte captcha;

    @Override
    public short getOp() {
        return op;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public byte getCaptcha() { return captcha; }

    @Override
    public void fromBytes(byte[] msg, int length) {
        int index = getNextNullTerminator(msg,0);
        username = new String(msg, 0, index, StandardCharsets.UTF_8);
        password = new String(msg, index + 1, getNextNullTerminator(msg,index + 1) - index, StandardCharsets.UTF_8);
        captcha = msg[getNextNullTerminator(msg,index + 1) + 1];
    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }
}