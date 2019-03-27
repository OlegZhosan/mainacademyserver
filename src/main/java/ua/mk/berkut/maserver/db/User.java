//"чертеж" клиента

package ua.mk.berkut.maserver.db;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class User {
    private int id;
    private String login;
    private String password;
    private String username;
    private LocalDate birthday;
    private String city;
    private String description;
    private Set<Integer> friendsIds;

    public User(String login, String password, String username, LocalDate birthday, String city, String description) {
        this(0, login, password, username, birthday, city, description);
    }

    public User(int id, String login, String password, String username, LocalDate birthday, String city, String description) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.username = username;
        this.birthday = birthday;
        this.city = city;
        this.description = description;
        friendsIds = new HashSet<>();
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public String getCity() {
        return city;
    }

    public String getDescription() {
        return description;
    }

    public Set<Integer> getFriendsIds() { return friendsIds; }

    public void setFriendsIds(Set<Integer> friendsIds) {
        this.friendsIds = friendsIds;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                ", birthday=" + birthday +
                ", city='" + city + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
