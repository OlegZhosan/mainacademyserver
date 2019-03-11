package ua.mk.berkut.maserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args)throws Exception {
        new Main().run();
    }

    private void run() throws SQLException {
        Connection connection = DriverManager.getConnection(
                "jdbc:mariadb://localhost:3306/mainacademy2",
                "eugeny",
                "123");
    }
}
