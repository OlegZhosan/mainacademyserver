package ua.mk.berkut.maserver.dao;

import ua.mk.berkut.maserver.db.User;

import java.sql.*;
import java.time.LocalDate;

public class UserDAO {

    private Connection connection;
    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Добавляет нового пользователя в таблицу users БД
     * @param user объект, содержащий информацию о добавляемом пользователе
     * @return добавленного пользователя, если успешно или null, в противном случае
     */
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
            return user;
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Поиск пользователя в бд по введенным логину и паролю
     * @param login логин пользователя
     * @param password введенный пароль
     * @return пользователя, если логин и пароль корректны и соответствуют друг другу
     */
    public User searchUser(String login, String password) {
        try (PreparedStatement ps = connection.prepareStatement("select id, login, `password` from chatuser where login=?")) {
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                if (rs.getString("password").equals(password)) {
                    return searchWidthId(rs.getInt("id"));
                }else return null;
            }else return null;
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Находит пользователя по id
     * @param id идентификатор пользователя
     * @return найденного пользователя или null, если пользователь с таким id не существует
     */
    private User searchWidthId(int id) {
        try (PreparedStatement ps = connection.prepareStatement("select * from chatuser where id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()) return null;
            String login = rs.getString("login");
            return getUser(rs, id, login);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Вспомогательный метод для извлечения пользователей из ResultSet (код общий для нескольких методов, потому и вытесен в отдельный метод)
     * @param rs ResultSet - результат запроса, из которого надо получить список пользователей
     * @param id ид пользователя
     * @param login логин пользователя
     * @return пользователя по ид или логину
     * @throws SQLException если случилась проблема с БД
     */
    private User getUser(ResultSet rs, int id, String login) throws SQLException {
        String password = rs.getString("password");
        String username = rs.getString("username");
        Date date = rs.getDate("birthday");
        LocalDate birthday;
        if (date != null) {
            birthday = date.toLocalDate();
        } else birthday = LocalDate.of(0, 0, 0);
        String city = rs.getString("city");
        String description = rs.getString("description");
        return new User(id, login, password, username, birthday, city, description);
    }
}
