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
import java.util.List;
import java.util.Properties;

public class Main {
    Properties properties = new Properties();
    Connection connection;
    List<User> users;
    UserDAO userDAO;

    public static void main(String[] args)throws Exception {
        new Main().run();
    }

    private void run() throws Exception {
        startServer();
        users = userDAO.getAllUsers();
        printUsers(users);
        ServerSocket serverSocket = new ServerSocket(
                Integer.parseInt(properties.getProperty("port", "1234"))
        );
        for( ; ; ) {
            Socket socket = serverSocket.accept();
            new ClientThread(socket, this).start();
        }
    }

    private void printUsers(List<User> users) {
        users.forEach(System.out::println);
    }

    private void startServer() throws IOException, SQLException {

        properties.load(Files.newBufferedReader(Paths.get("chat.cfg")));
        connection = DriverManager.getConnection(
                properties.getProperty("url"),
                properties);
        userDAO = new UserDAO(connection);
    }
}
