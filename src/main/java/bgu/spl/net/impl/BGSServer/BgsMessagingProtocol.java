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
    public void start(int connectionId, Connections<bgsMessage> connections) {
        this.id = connectionId;
        this.connections = connections;
        curr_user = null;
    }

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
            return;
        }
        curr_user = user;
        connections.send(id, new AckMessage(msg.getOp(), null));
        ConcurrentLinkedQueue<bgsMessage> unreceived_msg =  curr_user.getUnreceivedMsg();
        for (bgsMessage message : unreceived_msg) {
            connections.send(id, message);
        }
        curr_user.emptyUnreceivedMsgQueue();
    }

    private void handleLogout(LogoutMessage msg) {
        if (curr_user == null || !curr_user.logout()) {
            connections.send(id, new ErrorMessage(msg.getOp()));
            return;
        }
        curr_user = null;
        connections.send(id, new AckMessage(msg.getOp(), null));
        connections.disconnect(id);
    }

    private void handleFollow(FollowMessage msg) {
        if (curr_user == null || !curr_user.isLoggedIn()) {
            connections.send(id, new ErrorMessage(msg.getOp()));
            return;
        }
        User other_user = social.getUserByName(msg.getUsername());
        if (other_user == null) {
            connections.send(id, new ErrorMessage(msg.getOp()));
            return;
        }
        // Follow case
        if (msg.getFollowAction() == 0) {
            // if current user already following the other user or if other user blocked him
            // than Error message will be sent back
            if (!curr_user.follow(other_user)) {
                connections.send(id, new ErrorMessage(msg.getOp()));
                return;
            }
        }
        // Unfollow case
        else {
            // if current user is not following the other user
            if (!curr_user.unfollow(other_user)) {
                connections.send(id, new ErrorMessage(msg.getOp()));
                return;
            }
        }
        // The follow command succeeded
        connections.send(id, new AckMessage(msg.getOp(), msg.getUsername().getBytes(StandardCharsets.UTF_8)));
    }

    private void handlePost(PostMessage msg) {
        if (curr_user == null) {
            connections.send(id, new ErrorMessage(msg.getOp()));
            return;
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
        social.addPostPM(msg);
    }

    private void handlePM(PMMessage msg) {
        User user = social.getUserByName(msg.getUsername());
        if (curr_user == null || user == null || user.isBlocking(curr_user) || curr_user.isBlocking(user)) {
            connections.send(id, new ErrorMessage(msg.getOp()));
            return;
        }
        String filtered_content = social.filterMessage(msg.getContent());
        filtered_content += msg.getDateAndTime();
        NotificationMessage notification = new NotificationMessage((byte)0, curr_user.getUsername(), filtered_content);
        sendMessageToUser(user, notification);
        connections.send(id, new AckMessage(msg.getOp(), null));
        social.addPostPM(msg);
    }

    private void handleLogStat(LogStatMessage msg) {
        if (curr_user == null) {
            connections.send(id, new ErrorMessage(msg.getOp()));
            return;
        }
        else {
            int count = 0;
            byte[] info = new byte[social.getRegisteredUsers().size()*8];
            ByteBuffer info_buf = ByteBuffer.wrap(info);
            for (User user : social.getRegisteredUsers()) {
                if (user.isLoggedIn() && !user.isBlocking(curr_user) && !curr_user.isBlocking(user)) {
                    appendUserStat(info_buf, user);
                    count++;
                }
            }
            byte[] ack_optional = new byte[count*8];
            copyByteArray(info, ack_optional);
            connections.send(id, new AckMessage(msg.getOp(), ack_optional));
        }
    }

    private void handleStat(StatMessage msg) {
        if (curr_user == null) {
            connections.send(id, new ErrorMessage(msg.getOp()));
            return;
        }
        else {
            int count = 0;
            String[] usernames_list = msg.getListOfUsernames().split("|");
            byte[] info = new byte[usernames_list.length*8];
            ByteBuffer info_buf = ByteBuffer.wrap(info);
            for (String username : usernames_list) {
                User user = social.getUserByName(username);
                if (user == null || user.isBlocking(curr_user) && curr_user.isBlocking(user)) {
                    connections.send(id, new ErrorMessage(msg.getOp()));
                    return;
                }
                else {
                    appendUserStat(info_buf, user);
                    count++;
                }
            }
            byte[] ack_optional = new byte[count*8];
            copyByteArray(info, ack_optional);
            connections.send(id, new AckMessage(msg.getOp(), ack_optional));
        }
    }

    private void handleBlock(BlockMessage msg) {
        if (curr_user == null) {
            connections.send(id, new ErrorMessage(msg.getOp()));
            return;
        }
        User user_to_block = social.getUserByName(msg.getUsername());
        // if user to block doesn't exist an Error message will be sent back
        if (user_to_block == null) {
            connections.send(id, new ErrorMessage(msg.getOp()));
            return;
        }
        curr_user.block(user_to_block);
        connections.send(id, new AckMessage(msg.getOp(), null));
    }

    private void appendUserStat(ByteBuffer info_buf, User user) {
        info_buf.putShort(user.getAge());
        info_buf.putShort(user.getNumPosts());
        info_buf.putShort(user.getNumFollowers());
    }

    private void copyByteArray(byte[] old_arr, byte[] new_arr) {
        for (int i=0; i<new_arr.length; i++) {
            new_arr[i] = old_arr[i];
        }
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
            if (user != null && !user.isBlocking(curr_user) && !curr_user.isBlocking(user)) {
                tagged_users.add(user);
            }
        }
        return tagged_users;
    }
}
