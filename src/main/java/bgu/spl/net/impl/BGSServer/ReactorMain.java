package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.impl.BGSServer.Messages.bgsMessage;
import bgu.spl.net.impl.BGSServer.Social.bguSocial;
import bgu.spl.net.srv.Reactor;

public class ReactorMain {
    public static void main(String[] args) {
        bguSocial social = new bguSocial();
        Reactor<bgsMessage> reactor = new Reactor<>(Integer.decode(args[1]).intValue(),
                Integer.decode(args[0]).intValue(),
                () -> new BgsMessagingProtocol(social),
                () -> new bgsEncoderDecoder());
        reactor.serve();
    }
}
