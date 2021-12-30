package bgu.spl.net.impl.bgs;

import bgu.spl.net.srv.BidiMessagingProtocol;
import sun.awt.image.ImageWatched;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.LinkedList;

public class BgsMessagingProtocol implements BidiMessagingProtocol<bgsMessage> {

    private boolean shouldTerminate = false;
    private final Connections connections;
    private int id;
    private User curr_user;
    private final bguSocial social;

    public BgsMessagingProtocol(bguSocial social) {
        this.social = social;
    }

    @Override
    public void start(int connectionId, Connections connections) {
        this.id = connectionId;
        this.connections = connections;
        curr_user = social.getUserById(id);
    }

    // TODO: change return value in NBCH class (line 50)
    @Override
    public void process(bgsMessage message) {
        short opCode = message.getOp();
        switch (opCode) {
            case 1:
                connections.send(id, handleRegister((RegisterMessage) message));
            case 2:
                connections.send(id, handleLogin((LoginMessage) message));
            case 3:
                connections.send(id, handleLogout((LogoutMessage) message));
            case 4:
                connections.send(id, handleFollow((FollowMessage) message));
            case 5:
                connections.send(id, handlePost((PostMessage) message));
            case 6:
                connections.send(id, handlePM((PMMessage) message));
            case 7:
                LinkedList<bgsMessage> returned_log_stat_messages = handleLogStat((LogStatMessage) message);
                for (bgsMessage msg : returned_log_stat_messages) {
                    connections.send(id, msg);
                }
            case 8:
                LinkedList<bgsMessage> returned_stat_messages = handleStat((StatMessage) message);
                for (bgsMessage msg : returned_stat_messages) {
                    connections.send(id, msg);
                }
            case 12:
                connections.send(id, handleBlock((BlockMessage) message));
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    private bgsMessage handleRegister(RegisterMessage msg) {
        if(social.isUserNameExist(msg.getUsername())) {
            return new ErrorMessage((short)1);
        }
        social.addUser();
        return new AckMessage((short)1,null);
    }

    private bgsMessage handleLogin(LoginMessage msg) {
        return null;
    }

    private bgsMessage handleLogout(LogoutMessage msg) {
        return null;
    }

    private bgsMessage handleFollow(FollowMessage msg) {
        if (!curr_user.isLoggedIn()) {
            return new ErrorMessage((short)4);
        }
        User other_user = social.getUserByName(msg.getUsername());
        // Follow case
        if (msg.getFollow() == 0) {
            // if current user already following the other user or if other user blocked him
            // than Error message will be sent back
            if(!other_user.addFollower(curr_user)) {
                return new ErrorMessage((short)4);
            }
            curr_user.addFollowing(other_user);
        }
        // Unfollow case
        else {
            // if current user is not following the other user
            if(!other_user.removeFollower(curr_user)) {
                return new ErrorMessage((short)4);
            }
            curr_user.removeFollowing(other_user);
        }
        // The follow command succeeded
        return new AckMessage((short)4,msg.getUsername().getBytes(StandardCharsets.UTF_8));
    }

    private bgsMessage handlePost(PostMessage msg) {
        return null;
    }

    private bgsMessage handlePM(PMMessage msg) {
        return null;
    }

    private LinkedList<bgsMessage> handleLogStat(LogStatMessage msg) {
        return createMsgForStat(msg.getOp(), social.getLoggedInUsers());
    }

    private LinkedList<bgsMessage> handleStat(StatMessage msg) {
        LinkedList<User> users_list = new LinkedList<User>();
        LinkedList<bgsMessage> return_messages = new LinkedList<>();
        // If there is username that doesn't exist, Error message will be sent back
        if (!convertUsernameToUser(msg.getListOfUsernames() ,users_list)) {
            return_messages.add(new ErrorMessage(msg.getOp()));
        }
        return createMsgForStat(msg.getOp(),users_list);
    }

    private bgsMessage handleBlock(BlockMessage msg) {
        User user_to_block = social.getUserByName(msg.getUsername());
        // if user to block doesn't exist an Error message will be sent back
        if (user_to_block == null) {
            return new ErrorMessage((short)12);
        }
        // TODO: add method "addBlockedBy" in User class (it will remove curr_user from relevant lists)
        user_to_block.addBlockedBy(curr_user);
        // TODO: consider change it to handle by (Un)Follow massage/ addBlockedby
        curr_user.removeFollowing(user_to_block);
        curr_user.removeFollower(user_to_block);
        return new AckMessage((short)12, null);
    }

    // Converts list of usernames (String) to list of Users objects
    // returns false if there is a username that doesn't exist
    private boolean convertUsernameToUser (String usernames_list, LinkedList<User> users_list) {
        int start_index = 0;
        int end_index = usernames_list.indexOf("|");
        User user;
        while (start_index < usernames_list.length()) {
            // Save the user by username
            if (end_index != -1) {
                user = social.getUserByName(usernames_list.substring(start_index, end_index));
                start_index = end_index + 1;
                end_index = usernames_list.indexOf("|", start_index + 1);
            } else {
                user = social.getUserByName(usernames_list.substring(start_index));
                start_index = usernames_list.length();
            }
            // If username doesn't exist - return false
            if (user == null) {
                return false;
            }
            else {
                users_list.add(user);
            }
        }
        return true;
    }

    // Creates list of bgs message to be returned from Stat commands
    private LinkedList<bgsMessage> createMsgForStat (short opcode, LinkedList<User> users) {
        // TODO: if user is not register, return error (covered by isLoggedin?)
        LinkedList<bgsMessage> return_messages = new LinkedList<>();
        if (!curr_user.isLoggedIn()) {
            return_messages.add(new ErrorMessage(opcode));
            return return_messages;
        }
        // TODO: add method in User class that returns all info in byte array
        for (User user : users) {
            // If current user is blocked by the other user - he can't get his info
            if (!isBlockedBy(user)) {
                return_messages.add(new AckMessage(opcode, user.infoInBytes()));
            }
        }
        return return_messages;
    }
}
