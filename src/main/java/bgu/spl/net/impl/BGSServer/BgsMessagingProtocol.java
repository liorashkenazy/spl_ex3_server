package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.impl.BGSServer.Messages.*;
import bgu.spl.net.impl.BGSServer.Social.User;
import bgu.spl.net.impl.BGSServer.Social.bguSocial;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BgsMessagingProtocol implements BidiMessagingProtocol<bgsMessage> {

    private boolean shouldTerminate = false;
    private Connections<bgsMessage> connections;
    private int id;
    private User curr_user;
    private final bguSocial social;

    public BgsMessagingProtocol(bguSocial social) {
        this.social = social;
    }

    @Override
    //TODO VERY VERY IMPORTANT, check if we can change the signature
    public void start(int connectionId, Connections<bgsMessage> connections) {
        this.id = connectionId;
        this.connections = connections;
        curr_user = null;
    }

    // TODO: change return value in NBCH class (line 50)
    @Override
    public void process(bgsMessage message) {
        switch (message.getOp()) {
            case 1:
                handleRegister((RegisterMessage)message);
                break;
            case 2:
                handleLogin((LoginMessage)message);
                break;
            case 3:
                handleLogout((LogoutMessage)message);
                break;
            case 4:
                handleFollow((FollowMessage)message);
                break;
            case 5:
                handlePost((PostMessage)message);
                break;
            case 6:
                handlePM((PMMessage)message);
                break;
            case 7:
                handleLogStat((LogStatMessage)message);
                break;
            case 8:
                handleStat((StatMessage)message);
                break;
            case 12:
                handleBlock((BlockMessage)message);
                break;
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    private void handleRegister(RegisterMessage msg) {
        if (social.registerUser(msg.getUsername(), msg.getPassword(), msg.getBirthday())) {
            connections.send(id, new AckMessage(msg.getOp(), null));
        }
        else {
            connections.send(id, new ErrorMessage(msg.getOp()));
        }
    }

    private void handleLogin(LoginMessage msg) {
        User user = social.getUserByName(msg.getUsername());
        if (curr_user != null || user == null || !user.logIn(msg.getPassword(), id)) {
            connections.send(id, new ErrorMessage(msg.getOp()));
        }
        curr_user = user;
        ConcurrentLinkedQueue<bgsMessage> unreceived_msg =  curr_user.getUnreceivedMsg();
        for (bgsMessage message : unreceived_msg) {
            connections.send(id, message);
        }
        curr_user.emptyUnreceivedMsgQueue();
        connections.send(id, new AckMessage(msg.getOp(), null));
    }

    private void handleLogout(LogoutMessage msg) {
        if (curr_user == null || !curr_user.logout()) {
            connections.send(id, new ErrorMessage(msg.getOp()));
        }
        curr_user = null;
        connections.send(id, new AckMessage(msg.getOp(), null));
    }

    private void handleFollow(FollowMessage msg) {
        if (curr_user == null || !curr_user.isLoggedIn()) {
            connections.send(id, new ErrorMessage(msg.getOp()));
        }
        User other_user = social.getUserByName(msg.getUsername());
        if (other_user == null) {
            connections.send(id, new ErrorMessage(msg.getOp()));
        }
        // Follow case
        if (msg.getFollow() == 0) {
            // if current user already following the other user or if other user blocked him
            // than Error message will be sent back
            if (!curr_user.follow(other_user)) {
                connections.send(id, new ErrorMessage(msg.getOp()));
            }
        }
        // Unfollow case
        else {
            // if current user is not following the other user
            if (!curr_user.unfollow(other_user)) {
                connections.send(id, new ErrorMessage(msg.getOp()));
            }
        }
        // The follow command succeeded
        connections.send(id, new AckMessage(msg.getOp(), msg.getUsername().getBytes(StandardCharsets.UTF_8)));
    }

    private void handlePost(PostMessage msg) {
        if (curr_user == null) {
            connections.send(id, new ErrorMessage(msg.getOp()));
        }
        String content = msg.getContent();
        NotificationMessage notification = new NotificationMessage((byte)1, curr_user.getUsername(), content);
        HashSet<User> users = new HashSet<>();
        users.addAll(curr_user.getFollowers());
        users.addAll(getTaggedUsers(content));
        for (User user : users) {
            sendMessageToUser(user, notification);
        }
        curr_user.increaseNumPosts();
        connections.send(id,new AckMessage(msg.getOp(),null));
    }

    private void handlePM(PMMessage msg) {
        User user = social.getUserByName(msg.getUsername());
        if (curr_user == null || user == null || user.isBlocking(curr_user)) {
            connections.send(id, new ErrorMessage(msg.getOp()));
        }
        String filtered_content = social.filterMessage(msg.getContent());
        NotificationMessage notification = new NotificationMessage((byte)0, curr_user.getUsername(), filtered_content);
        sendMessageToUser(user, notification);
        connections.send(id, new AckMessage(msg.getOp(), null));
    }

    //TODO: Change the decoder for LogStat msg in client
    private void handleLogStat(LogStatMessage msg) {
        if (curr_user == null) {
            connections.send(id, new ErrorMessage(msg.getOp()));
        }
        // TODO: change the byte array size
        else {
            byte[] info = new byte[1024];
            ByteBuffer info_buf = ByteBuffer.wrap(info);
            for (User user : social.getRegisteredUsers()) {
                if (user.isLoggedIn()) {
                    if(!getUserStat(info_buf, user)) {
                        connections.send(id, new ErrorMessage(msg.getOp()));
                        return;
                    }
                }
            }
           connections.send(id, new AckMessage(msg.getOp(), info));
        }
    }

    //TODO: Change the decoder for LogStat msg in client
    private void handleStat(StatMessage msg) {
        if (curr_user == null) {
            connections.send(id, new ErrorMessage(msg.getOp()));
        }
        else {
            // TODO: change the byte array size
            byte[] info = new byte[1024];
            ByteBuffer info_buf = ByteBuffer.wrap(info);
            for (String username : msg.getListOfUsernames().split("|")) {
                User user = social.getUserByName(username);
                if (user == null || !getUserStat(info_buf, user)) {
                    connections.send(id, new ErrorMessage(msg.getOp()));
                    return;
                }
            }
            connections.send(id, new AckMessage(msg.getOp(), info));
        }
    }

    private void handleBlock(BlockMessage msg) {
        if (curr_user == null) {
            connections.send(id, new ErrorMessage(msg.getOp()));
        }
        User user_to_block = social.getUserByName(msg.getUsername());
        // if user to block doesn't exist an Error message will be sent back
        if (user_to_block == null) {
            connections.send(id, new ErrorMessage(msg.getOp()));
        }
        curr_user.block(user_to_block);
        connections.send(id, new AckMessage(msg.getOp(), null));
    }

    private boolean getUserStat(ByteBuffer info_buf, User user) {
        if (!user.isBlocking(curr_user)) {
            info_buf.putShort(user.getAge());
            info_buf.putShort(user.getNumPosts());
            info_buf.putShort(user.getNumFollowers());
            //TODO: remove the last /0 from bytes array
            info_buf.put((byte) 0);
            return true;
        }
        return false;
    }

    private void sendMessageToUser(User user, NotificationMessage notification) {
        if (user.isLoggedIn()) {
            connections.send(user.getCurrentConnectionId(), notification);
        }
        else {
            user.addUnreceivedMsg(notification);
        }
    }

    private LinkedList<User> getTaggedUsers(String content) {
        LinkedList<User> tagged_users = new LinkedList<>();
        Pattern pattern = Pattern.compile("(@[^|; ]+)", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String username = matcher.group();
            User user = social.getUserByName(username);
            if (user != null) {
                tagged_users.add(user);
            }
        }
        return tagged_users;
    }
}
