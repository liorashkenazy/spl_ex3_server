package bgu.spl.net.impl.BGSServer.Social;

import bgu.spl.net.impl.BGSServer.Messages.bgsMessage;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class bguSocial {

    private ConcurrentHashMap<String, User> username_to_user;
    private String filtered_words[] = {"fuck", "sex", "asshole"};
    private ConcurrentLinkedQueue<bgsMessage> post_pm_list;


    public bguSocial() {
        username_to_user = new ConcurrentHashMap<>();
        post_pm_list = new ConcurrentLinkedQueue<>();
    }

    public User getUserByName(String username) {
        return username_to_user.get(username);
    }

    public boolean registerUser(String username, String password, String birthday) {
        User user = new User(username, password, birthday);
        return username_to_user.putIfAbsent(username, user) == null;
    }

    public Collection<User> getRegisteredUsers() {
        return username_to_user.values();
    }

    public String filterMessage(String message) {
        String filtered_message = message + " ";
        for (String to_filter : filtered_words) {
            filtered_message = filtered_message.replaceAll(String.format("(%s)([. ,?!:]+)", to_filter), "<filtered>$2");
        }
        return filtered_message;
    }

    public void addPostPM(bgsMessage msg) {
        post_pm_list.add(msg);
    }
}
