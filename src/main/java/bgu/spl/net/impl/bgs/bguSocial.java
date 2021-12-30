package bgu.spl.net.impl.bgs;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class bguSocial {

    private ConcurrentLinkedQueue<User> registered_users;
    // Contains only logged-in users
    private ConcurrentHashMap<Integer, User> id_to_user;
    private ConcurrentHashMap<String, User> username_to_user;

    public bguSocial(){
        registered_users = new ConcurrentLinkedQueue<User>();
        id_to_user = new ConcurrentHashMap<Integer, User>();
        username_to_user = new ConcurrentHashMap<String, User>();
    }
    public boolean isUserNameExist(String user_name) {
        for (User user : registered_users) {
            if (user.getUserName().equals(user_name)) {
                return true;
            }
        }
        return false;
    }

    public void addRegisteredUser(User user) {
        registered_users.add(user);
        username_to_user.put(user.getUserName(), user);
    }

    public User getUserById(int id) {
        return id_to_user.get(id);
    }

    public User getUserByName(String username) {
        return username_to_user.get(username);
    }

    public LinkedList<User> getLoggedInUsers() {
        // TODO: is this casting legal?
        return (LinkedList<User>) id_to_user.values();
    }
}
