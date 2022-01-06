package bgu.spl.net.impl.BGSServer.Social;

import bgu.spl.net.impl.BGSServer.Messages.NotificationMessage;
import bgu.spl.net.impl.BGSServer.Messages.bgsMessage;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class User {

    private final String username;
    private final String password;
    private final String birthday;
    private int current_connection_id;
    private AtomicBoolean logged_in;
    private ConcurrentLinkedQueue<User> followers;
    private AtomicInteger following_count;
    private ConcurrentLinkedQueue<bgsMessage> unreceived_msg;
    private ConcurrentLinkedQueue<User> block_list;
    private int num_posts;

    public User(String username, String password, String birthday) {
        this.username = username;
        this.password = password;
        this.birthday = birthday;
        current_connection_id = -1;
        logged_in = new AtomicBoolean(false);
        followers = new ConcurrentLinkedQueue<>();
        following_count = new AtomicInteger(0);
        unreceived_msg = new ConcurrentLinkedQueue<>();
        block_list = new ConcurrentLinkedQueue<>();
        num_posts = 0;
    }

    public int getCurrentConnectionId() {
        return current_connection_id;
    }

    public boolean logIn(String password, int connection_id) {
         if (!this.password.equals(password) || !(logged_in.compareAndSet(false, true))) {
             return false;
         }
         current_connection_id = connection_id;
         return true;
    }

    public boolean logout() {
        return logged_in.compareAndSet(true, false);
    }

    public boolean isLoggedIn() {
        return logged_in.get();
    }

    private void addFollower(User user) {
        followers.add(user);
    }

    public boolean follow(User user) {
        if (user.followers.contains(this) || user.isBlocking(this) || isBlocking(user)) {
            return false;
        }
        user.addFollower(this);
        following_count.incrementAndGet();
        return true;
    }

    public boolean unfollow(User user) {
        if (user.followers.contains(this)) {
            user.removeFollower(this);
            following_count.decrementAndGet();
            return true;
        }
        return false;
    }

    public boolean removeFollower(User user) {
        return followers.remove(user);
    }

    public ConcurrentLinkedQueue<User> getFollowers() {
        return followers;
    }

    public void addUnreceivedMsg(NotificationMessage msg) {
        unreceived_msg.add(msg);
    }

    public ConcurrentLinkedQueue<bgsMessage> getUnreceivedMsg() { return unreceived_msg; }

    public void emptyUnreceivedMsgQueue() {
        unreceived_msg.clear();
    }

    public void block(User to_block) {
        block_list.add(to_block);
        if (followers.contains(to_block)) {
            followers.remove(to_block);
            to_block.following_count.decrementAndGet();
        }
        to_block.removeFollower(to_block);
    }

    public boolean isBlocking(User other) {
        return block_list.contains(other);
    }

    public void increaseNumPosts(){
        num_posts++;
    }

    public String getUsername() {
        return username;
    }

    public short getAge() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        short age = (short)Period.between(LocalDate.parse(birthday, formatter), LocalDate.now()).getYears();
        return age;
    }

    public short getNumPosts() {
        return (short) num_posts;
    }

    public short getNumFollowers() {
        return (short) followers.size();
    }

}
