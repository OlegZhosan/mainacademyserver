package ua.mk.berkut.maserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class FriendDAO {
    private Connection connection;

    public FriendDAO(Connection connection) {
        this.connection = connection;
    }


    public Set<Integer> getFriedsFor(int id) {
        Set<Integer> result = new HashSet<>();
        try (PreparedStatement ps = connection.prepareStatement("select id2 from friend where id1 = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int val = rs.getInt(1);
                result.add(val);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
