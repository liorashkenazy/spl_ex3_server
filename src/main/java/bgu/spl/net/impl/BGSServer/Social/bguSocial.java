package bgu.spl.net.impl.BGSServer.Social;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class bguSocial {

    private ConcurrentHashMap<String, User> username_to_user;
    private String filtered_words[] = {"fuck", "sex", "asshole"};

    public bguSocial() {
        username_to_user = new ConcurrentHashMap<>();
    }

    public boolean isUserNameExist(String user_name) {
        return username_to_user.containsKey(user_name);
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
        String filtered_message = message;
        for (String to_filter : filtered_words) {
            filtered_message = filtered_message.replaceAll(to_filter, "<filtered>");
        }
        return filtered_message;
    }
}
