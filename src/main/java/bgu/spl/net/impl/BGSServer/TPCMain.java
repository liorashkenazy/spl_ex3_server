package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.impl.BGSServer.Messages.bgsMessage;
import bgu.spl.net.impl.BGSServer.Social.bguSocial;
import bgu.spl.net.srv.BaseServer;
import bgu.spl.net.srv.Reactor;
import bgu.spl.net.srv.TPCBaseServer;

public class TPCMain {
    public static void main(String[] args) {
        bguSocial social = new bguSocial();
        Connectionsimpl connectionsimpl = Connectionsimpl.getInstance();
        TPCBaseServer<bgsMessage> baseServer = new TPCBaseServer<>(Integer.decode(args[0]).intValue(),
                () -> new BgsMessagingProtocol(social),
                () -> new bgsEncoderDecoder());
        baseServer.serve();
    }
}
