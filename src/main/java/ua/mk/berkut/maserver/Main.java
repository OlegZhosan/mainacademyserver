package ua.mk.berkut.maserver;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class Main {
    Connection connection;

    public static void main(String[] args)throws Exception {
        new Main().run();
    }

    private void run() throws Exception {
        Properties properties = new Properties();
        properties.load(Files.newBufferedReader(Paths.get("chat.cfg")));
        connection = DriverManager.getConnection(
                properties.getProperty("url"),
                properties);
    }
}
