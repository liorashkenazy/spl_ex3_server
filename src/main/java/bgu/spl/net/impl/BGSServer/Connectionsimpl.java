package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.impl.BGSServer.Messages.bgsMessage;
import bgu.spl.net.srv.ConnectionHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Connectionsimpl implements Connections<bgsMessage> {
    private ConcurrentHashMap<Integer, ConnectionHandler<bgsMessage>> id_to_ch;
    private AtomicInteger curr_id = new AtomicInteger(0);

    private Connectionsimpl(){
        id_to_ch = new ConcurrentHashMap<>();
    }

    public int connect(ConnectionHandler<bgsMessage> ch) {
        id_to_ch.put(curr_id.get(), ch);
        return curr_id.getAndIncrement();
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

    private static class SingletonHolder {
        private static Connectionsimpl instance = new Connectionsimpl();
    }

    public static Connectionsimpl getInstance() {
        return SingletonHolder.instance;
    }
}
