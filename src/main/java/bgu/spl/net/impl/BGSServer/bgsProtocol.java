package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.BGSServer.Messages.bgsMessage;

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
