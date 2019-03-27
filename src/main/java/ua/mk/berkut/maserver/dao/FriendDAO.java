//взаимодействие с таблицей friend

package ua.mk.berkut.maserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class FriendDAO {

    //соединение с бд
    private Connection connection;
    public FriendDAO(Connection connection) {
        this.connection = connection;
    }

    //метод выдачи клиенту всех его друзей по его id
    public Set<Integer> getFriendsFor(int id) {//метод-множество значений id
        Set<Integer> result = new HashSet<>();//множество
        try (PreparedStatement ps1 = connection.prepareStatement("select id2 from friend where id1 = ?");
             PreparedStatement ps2 = connection.prepareStatement("select id1 from friend where id2 = ?")
             //достань строку колонки id2 (кто дружит) из таблицы friend для заданного значания id1(его друзья)???
        ) {

            //подписка
            //вытягивание дружбы в одну сторону
            ps1.setInt(1, id);//получение значения для знака вопроса
            ResultSet rs = ps1.executeQuery();//присвоение вытянутому полученного
            while (rs.next()) {//пока в присвоенном чтото есть
                int val = rs.getInt(1);//доставай то, что с columnIndex 1
                result.add(val);//результат добавляй во множество result
            }

            //подписка
            //вытягивание дружбы в другую сторону
            ps2.setInt(1, id);
            Set<Integer> result2 = new HashSet<>();
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next()) {
                Integer val = rs2.getInt(1);
                result2.add(val);
            }

            result.retainAll(result2);//оставление только обоюдной дружбы
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    //добавление в друзья
    //from - кто
    //to - с кем
    public void addFriendFor(int from, int to){
        try (PreparedStatement preparedStatement = connection.prepareStatement("insert into friend (id2, id1) values (?, ?)")) {
            preparedStatement.setInt(1, from);
            preparedStatement.setInt(2, to);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}