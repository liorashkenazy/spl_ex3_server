package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.impl.BGSServer.Messages.bgsMessage;
import bgu.spl.net.impl.BGSServer.Social.bguSocial;
import bgu.spl.net.srv.Reactor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ReactorMain {
    public static void main(String[] args) {
        bguSocial social = new bguSocial();
        Connectionsimpl connectionsimpl = Connectionsimpl.getInstance();
        Reactor<bgsMessage> reactor = new Reactor<>(Integer.decode(args[1]).intValue(),
                Integer.decode(args[0]).intValue(),
                () -> new BgsMessagingProtocol(social),
                () -> new bgsEncoderDecoder());
        reactor.serve();
    }
}
