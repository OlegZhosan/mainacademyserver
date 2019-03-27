//взаимодействие с таблицей chatuser
//обьект доступа к таблице chatuser

package ua.mk.berkut.maserver.dao;

import ua.mk.berkut.maserver.db.User;

import java.sql.*;
import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;

public class UserDAO {

    //соединение с бд
    private Connection connection;//обьявление соединения
    public UserDAO(Connection connection) {
        this.connection = connection;
    }//конструктор соединения

    //регистрация
    //добавление нового пользователя в бд
    public User addUser(User user) {
        try (PreparedStatement ps = connection.prepareStatement(
                "insert into chatuser(login, password, username, birthday, city, description)values (?,?,?,?,?,?)")) {
            ps.setString(1, user.getLogin());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getUsername());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            ps.setString(5, user.getCity());
            ps.setString(6, user.getDescription());
            ps.executeUpdate();
        } catch (SQLException e) {
            return null;
        }
        return user;
    }

    //вывод списка всех пользователей на консоль
//    public List<User> getAllUsers(){
//        List<User> result = new ArrayList<>();
//        try (Statement statement = connection.createStatement()) {
//            ResultSet rs = statement.executeQuery("select * from chatuser");
//            while (rs.next()){
//                int id = rs.getInt("id");
//                String login = rs.getString("login");
//                String password = rs.getString("password");
//                String username = rs.getString("username");
//                String city = rs.getString("city");
//                String description = rs.getString("description");
//                Date date = rs.getDate("birthday");
//                LocalDate birthday;
//                if (date != null) {
//                    birthday = date.toLocalDate();
//                }else {
//                    birthday = LocalDate.of(0000,0,0);
//                }
//                result.add(new User(id, login, password, username, birthday, city, description));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return result;
//    }

    //поиск пользователя в базе данных по введенному логину и паролю
    public User findUser(String login, String password) {
        try (PreparedStatement ps = connection.prepareStatement("select id, login, `password` from chatuser where login=?")) {
            //вытягиваем id, login и password и подставляем значение логина
            ps.setString(1, login);//подставляем значение логина на место знака вопроса
            ResultSet rs = ps.executeQuery();//полученному подставленное???
            if (rs.next()){//если совпало с логином в бд
                if (rs.getString("password").equals(password)) {//проверка пароля
                    return find(rs.getInt("id"));//передает id???
                }else return null;
            }else return null;
        } catch (SQLException e) {
            return null;
        }
    }

    //вытягивание пользователя по id
    @SuppressWarnings("WeakerAccess")//подавление варнинга
    public User find(int id) {
        try (PreparedStatement ps = connection.prepareStatement("select * from chatuser where  id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()) return null;
            String login = rs.getString("login");
            return getUser(rs, id, login);
        } catch (SQLException e) {
            return null;
        }
    }

    //вытягивание пользователя по логину
    public User findByLogin(String login) {
        try (PreparedStatement ps = connection.prepareStatement("select * from chatuser where  login=?")) {
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                int id = rs.getInt("id");
                return getUser(rs, id, login);
            }else{ return null;}
        } catch (SQLException e) {
            return null;
        }
    }

    //метод вытягивания пользователя из ResultSet (результат запроса)
    //общий для нескольких методов, по тому и выненесен отдельно
    //вспомогательный метод
    private User getUser(ResultSet rs, int id, String login) throws SQLException {
        String password = rs.getString("password");
        String username = rs.getString("username");
        Date date = rs.getDate("birthday");
        LocalDate birthday;
        if (date != null) {
            birthday = date.toLocalDate();
        } else {
            birthday = LocalDate.of(0, 0, 0);
        }
        String city = rs.getString("city");
        String description = rs.getString("description");
        return new User(id, login, password, username, birthday, city, description);
    }
}
