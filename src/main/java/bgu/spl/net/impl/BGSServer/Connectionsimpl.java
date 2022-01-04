package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.impl.BGSServer.Messages.bgsMessage;
import bgu.spl.net.srv.ConnectionHandler;

import java.util.concurrent.ConcurrentHashMap;

public class Connectionsimpl implements Connections<bgsMessage> {
    ConcurrentHashMap<Integer, ConnectionHandler<bgsMessage>> id_to_ch;

    public Connectionsimpl(){
        id_to_ch = new ConcurrentHashMap<>();
    }

    @Override
    public boolean send(int connectionId, bgsMessage msg) {
        ConnectionHandler<bgsMessage> ch = id_to_ch.get(connectionId);
        if (ch != null) {
            ch.send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void broadcast(bgsMessage msg) {
        for (ConnectionHandler<bgsMessage> ch : id_to_ch.values()) {
            ch.send(msg);
        }
    }

    @Override
    public void disconnect(int connectionId) {
        id_to_ch.remove(connectionId);
    }
}
