package bgu.spl.net.impl.bgs;

import bgu.spl.net.api.MessagingProtocol;

public class bgsProtocol implements MessagingProtocol<bgsMessage> {

    private boolean shouldTerminate;

    @Override
    public bgsMessage process(bgsMessage msg) {
        return null;
    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }
}
