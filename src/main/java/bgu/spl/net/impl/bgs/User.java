package bgu.spl.net.impl.bgs;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;

public class User {

    String username;
    String password;
    String birthday;
    int age;
    int id;
    boolean log_in;
    ConcurrentLinkedQueue<User> followers;
    ConcurrentLinkedQueue<User> following;
    ConcurrentLinkedQueue<bgsMessage> unreceived_msg;
    ConcurrentLinkedQueue<User> blocked_by;
    int num_posts;


    public User(String username, String password, String birthday) {
        this.username = username;
        this.password = password;
        this.birthday = birthday;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        age = Period.between(LocalDate.parse(birthday, formatter), LocalDate.now()).getYears();
        id = -1;
        log_in = false;
        followers = new ConcurrentLinkedQueue<>();
        following = new ConcurrentLinkedQueue<>();
        unreceived_msg = new ConcurrentLinkedQueue<>();
        blocked_by = new ConcurrentLinkedQueue<>();
        num_posts = 0;
    }

    public int getId() {
        return id;
    }

    public void setLogIn(boolean log_stat) {
        log_in = log_stat;
    }

    public boolean isLoggedIn() {
        return log_in;
    }

    public boolean addFollower(User user){
        if(isBlockedByUser(user)) {
            return false;
        }
        followers.add(user);
        return true;
    }

    public boolean removeFollower(User user){
        return followers.remove(user);
    }

    public ConcurrentLinkedQueue<User> getFollowers() {
        return followers;
    }

    public boolean addFollowing(User user){
        if(isBlockedByUser(user)) {
            return false;
        }
        following.add(user);
        return true;
    }

    public boolean removeFollowing(User user){
        return following.remove(user);
    }

    public ConcurrentLinkedQueue<User> getFollowing() {
        return following;
    }

    public ConcurrentLinkedQueue<bgsMessage> getUnreceivedMsg() {
        return unreceived_msg;
    }

    public boolean isBlockedByUser(User user) {
        return blocked_by.contains(user);
    }

    public void increaseNumPosts(){
        num_posts++;
    }

    // Use for STAT msg
    public String toString() {
        return "user age: " + age + "number of posts: " + num_posts + "number of followers: " + followers.size()
                + "number of following: " + following.size();
    }
}
