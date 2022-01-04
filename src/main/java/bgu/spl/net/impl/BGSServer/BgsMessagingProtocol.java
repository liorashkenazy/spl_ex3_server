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
                connections.send(id, handleRegister((RegisterMessage)message));
            case 2:
                connections.send(id, handleLogin((LoginMessage)message));
            case 3:
                connections.send(id, handleLogout((LogoutMessage)message));
            case 4:
                connections.send(id, handleFollow((FollowMessage)message));
            case 5:
                connections.send(id, handlePost((PostMessage)message));
            case 6:
                connections.send(id, handlePM((PMMessage)message));
            case 7:
                LinkedList<bgsMessage> returned_log_stat_messages = handleLogStat((LogStatMessage)message);
                for (bgsMessage msg : returned_log_stat_messages) {
                    connections.send(id, msg);
                }
            case 8:
                LinkedList<bgsMessage> returned_stat_messages = handleStat((StatMessage)message);
                for (bgsMessage msg : returned_stat_messages) {
                    connections.send(id, msg);
                }
            case 12:
                connections.send(id, handleBlock((BlockMessage)message));
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    private bgsMessage handleRegister(RegisterMessage msg) {
        if (social.registerUser(msg.getUsername(), msg.getPassword(), msg.getBirthday())) {
            return new AckMessage(msg.getOp(), null);
        }
        else {
            return new ErrorMessage(msg.getOp());
        }
    }

    private bgsMessage handleLogin(LoginMessage msg) {
        User user = social.getUserByName(msg.getUsername());
        if (user == null || !user.logIn(msg.getPassword(), id)) {
            return new ErrorMessage(msg.getOp());
        }
        curr_user = user;
        return new AckMessage(msg.getOp(), null);
    }

    private bgsMessage handleLogout(LogoutMessage msg) {
        if (curr_user == null || !curr_user.logout()) {
            return new ErrorMessage(msg.getOp());
        }
        curr_user = null;
        return new AckMessage(msg.getOp(), null);
    }

    private bgsMessage handleFollow(FollowMessage msg) {
        if (!curr_user.isLoggedIn()) {
            return new ErrorMessage(msg.getOp());
        }
        User other_user = social.getUserByName(msg.getUsername());
        // Follow case
        if (msg.getFollow() == 0) {
            // if current user already following the other user or if other user blocked him
            // than Error message will be sent back
            if (!curr_user.follow(other_user)) {
                return new ErrorMessage(msg.getOp());
            }
        }
        // Unfollow case
        else {
            // if current user is not following the other user
            if (!curr_user.unfollow(other_user)) {
                return new ErrorMessage(msg.getOp());
            }
        }
        // The follow command succeeded
        return new AckMessage(msg.getOp(), msg.getUsername().getBytes(StandardCharsets.UTF_8));
    }

    private bgsMessage handlePost(PostMessage msg) {
        if (curr_user == null) {
            return new ErrorMessage(msg.getOp());
        }
        String content = msg.getContent();
        NotificationMessage notification = new NotificationMessage((byte)1, curr_user.getUsername(), content);
        HashSet<User> users = new HashSet<>();
        users.addAll(curr_user.getFollowers());
        users.addAll(getTaggedUsers(content));
        for (User user : users) {
            if (user.isLoggedIn()) {
                connections.send(user.getCurrentConnectionId(), notification);
            }
            else {
                user.addUnreceivedMsg(notification);
            }
        }
        return new AckMessage(msg.getOp(),null);
    }

    private bgsMessage handlePM(PMMessage msg) {
        User user = social.getUserByName(msg.getUsername());
        if (curr_user == null || user == null || user.isBlocking(curr_user)) {
            return new ErrorMessage(msg.getOp());
        }
        String filtered_content = social.filterMessage(msg.getContent());
        NotificationMessage notification = new NotificationMessage((byte)0, curr_user.getUsername(), filtered_content);
        if (user.isLoggedIn()) {
            connections.send(user.getCurrentConnectionId(), notification);
        }
        else {
            user.addUnreceivedMsg(notification);
        }
        return new AckMessage(msg.getOp(), null);
    }

    private LinkedList<bgsMessage> handleLogStat(LogStatMessage msg) {
        LinkedList<bgsMessage> return_messages = new LinkedList<>();
        if (curr_user == null) {
            return_messages.add(new ErrorMessage(msg.getOp()));
        }
        else {
            for (User user : social.getRegisteredUsers()) {
                if (user.isLoggedIn()) {
                    return_messages.add(getUserStat(msg.getOp(), user));
                }
            }
        }
        return return_messages;
    }

    private LinkedList<bgsMessage> handleStat(StatMessage msg) {
        LinkedList<bgsMessage> return_messages = new LinkedList<>();
        if (curr_user == null) {
            return_messages.add(new ErrorMessage(msg.getOp()));
        }
        else {
            for (String username : msg.getListOfUsernames().split("|")) {
                if (!social.isUserNameExist(username)) {
                    return_messages.add(new ErrorMessage(msg.getOp()));
                    return return_messages;
                }
                return_messages.add(getUserStat(msg.getOp(),social.getUserByName(username)));
            }
        }
        return return_messages;
    }

    private bgsMessage handleBlock(BlockMessage msg) {
        User user_to_block = social.getUserByName(msg.getUsername());
        // if user to block doesn't exist an Error message will be sent back
        if (user_to_block == null) {
            return new ErrorMessage(msg.getOp());
        }
        curr_user.block(user_to_block);
        return new AckMessage(msg.getOp(), null);
    }

    private bgsMessage getUserStat(short opcode, User user) {
        if (!user.isBlocking(curr_user)) {
            byte[] info = new byte[8];
            ByteBuffer info_buf = ByteBuffer.wrap(info);
            info_buf.putShort(user.getAge());
            info_buf.putShort(user.getNumPosts());
            info_buf.putShort(user.getNumPosts());
            info_buf.putShort(user.getNumFollowers());
            info_buf.putShort(user.getNumFollowing());
            return new AckMessage(opcode, info);
        }
        return new ErrorMessage(opcode);
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
