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
        Connectionsimpl connectionsimpl = new Connectionsimpl();
        Reactor<bgsMessage> reactor = new Reactor<>(5,
                10000,
                new Supplier<BidiMessagingProtocol<bgsMessage>>() {
                    private AtomicInteger counter = new AtomicInteger(0);
                    @Override
                    public BidiMessagingProtocol<bgsMessage> get() {
                        //TODO Add the new object to the Connections
                        BidiMessagingProtocol<bgsMessage> ret = new BgsMessagingProtocol(social);
                        ret.start(counter.getAndIncrement(), connectionsimpl);
                        return ret;
                    }
                },
                () -> new bgsEncoderDecoder());
        reactor.serve();
    }
}
