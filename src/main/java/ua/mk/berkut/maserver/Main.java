package ua.mk.berkut.maserver;

import ua.mk.berkut.maserver.clients.ClientThread;
import ua.mk.berkut.maserver.dao.UserDAO;
import ua.mk.berkut.maserver.db.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private Properties properties = new Properties();

    UserDAO userDAO;

    public Set<ClientThread> onlineUsersTreads;

    public static void main(String[] args) throws Exception { new Main().run(); }

    public void run() throws Exception {
        try (Connection ignore = startServer()) {
            //почему соединение называется ignore. почему не подходит другое название?
            onlineUsersTreads = new HashSet<>();
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(properties.getProperty("port", "1234")));
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientThread(socket, this).start();
            }
        }
    }

    /**
     * Запуск сервера.
     * Поддтягивает свойства для соединения из файла chat.cfg
     * @return подключение к БД
     * @throws IOException если сетевое подключение невозможно
     * @throws SQLException если произошла ошибка с БД
     */
    private Connection startServer() throws IOException, SQLException {
        properties.load(Files.newBufferedReader(Paths.get("chat.cfg")));
        Connection connection = DriverManager.getConnection(properties.getProperty("url"), properties);
        userDAO = new UserDAO(connection);
        return connection;
    }


    public User register(String message) {
        try {
            String[] s = message.split(":");
            String login = s[1];
            String password = s[2];
            String username = s[3];
            String dateStr = s[4];
            String[] split = dateStr.split("\\D");
            LocalDate birthday = LocalDate.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
            String city = s[5];
            String description = s[6];
            User user = new User(login, password, username, birthday, city, description);
            user = userDAO.addUser(user);
            return user;
        }catch (Exception e){
            return null;
        }
    }

    public User searchUser(String login, String password) {
        User user = userDAO.searchUser(login, password);
        return user;
    }

    public List<User> getOnlineUsers() {
        return onlineUsersTreads.stream().map(ClientThread::getUser).collect(Collectors.toList());
    }

    public synchronized void addToOnlineUsers(ClientThread clientThread){
        onlineUsersTreads.add(clientThread);
    }

    public void removeToOnlineUsers(ClientThread clientThread) {
        onlineUsersTreads.remove(clientThread);
    }
}
