//старт сервера и распределние действий классов

package ua.mk.berkut.maserver;

import ua.mk.berkut.maserver.clients.ClientThread;
import ua.mk.berkut.maserver.dao.FriendDAO;
import ua.mk.berkut.maserver.dao.UserDAO;
import ua.mk.berkut.maserver.db.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public class Main {
    private Properties properties = new Properties();//подключение к стандартному классу Properties (свойства) для вытягивания свойств из файла chat.cfg и соединения с putty
    //List<User> users;//список всех пользователей

    /**
     * Список потоков, обслуживающих пользователей
     */
    private List<ClientThread> onlineUsersTreads;

    private UserDAO userDAO;//подключение к классу запросов к бд для вызова нужных методов
    private FriendDAO friendDAO;//соединение с классом FriendDAO (друзья клиента)

    /**
     * Разделитель слов в строках
     * исспользуется только в этом классе
     */
    private final  static  String SEPARATOR = ";";

    //запуск
    public static void main(String[] args)throws Exception {new Main().run();}

    //последовательность выполнения методов
    public void run() throws Exception {
        try (Connection ignored = startServer()){//запуск сервера (подключение к бд)
            //users = userDAO.getAllUsers();//вытягивание из бд списока друзей онлайн???
            onlineUsersTreads = new ArrayList<>();//список пользователей онлайн
            //printUsers(users);//вывод на конслоль всех клиентов

            //подключение для клиента
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(properties.getProperty("port", "1234")));

            //безконечное предоставление подключения
            //noinspection InfiniteLoopStatement
            for (;;) {//бесконечный цыкл
                Socket socket = serverSocket.accept();//accept - стандартный метод, ожидающий подключения
                new ClientThread(socket, this).start();//передаем socket в ClientThread и запускаем его
                //метод start() для ClientThread возможен только, если в ClientThread есть "public static void main(String[] args) {new Main().run();}"
            }
        }
//        connection.close();//закрыть соединение, если реализован выход
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

        //подключения
        userDAO = new UserDAO(connection);//к классу обработки запросов пользователя
        friendDAO = new FriendDAO(connection);//к классу обработки запросов касательно бд
        return connection;
    }

    /**
     * Отключение клиента, завершившего работу
     * @param clientThread ссылка на поток клиента, завершившего работу
     */
    public void remove(ClientThread clientThread) {
        onlineUsersTreads.remove(clientThread);
    }

    /**
     * Поиск пользователя по логину и паролю. Для проверки, был ли ранее зарегистрирован пользователь с такими данными
     * @param login введенный логин пользователя
     * @param password введенный пароль пользователя
     * @return объект пользователя, включая список ID друзей или null если пользователь с такими login-password не зарегистрирован
     */
    public User findUser(String login, String password) {
        User user = userDAO.findUser(login, password);
        if (user!=null) {//если такой пользователь найден
            user.setFriendsIds(friendDAO.getFriendsFor(user.getId()));//достань пользователя и список его друзей по Id
        }
        return user;
    }

    /**
     * Список пользователей online
     * @return список всех пользователей, которіе сейчас online
     */
    public List<User> getOnlineUsers() {
        //выдай пользователей онлайн из потоков
        return onlineUsersTreads.stream().map(ClientThread::getUser).collect(Collectors.toList());
    }

    /**
     * Пересылка сообщения от одного клиента другому
     * @param message сообщение, передаваемое от одного клиента другому
     */
    public synchronized void processMessage(String message) {
        String[] s = message.split(SEPARATOR);//получение сообщения
        if(s.length != 3) return;
        String receirver = s[0];//получатель
        String sender = s[1];//отправитель
        String text = s[2];//текст сообщения

        //лямпды отправки сообщений
        Optional<ClientThread> clientThread = onlineUsersTreads.stream().filter(t -> t.getUser().getLogin().equals(receirver)).findAny();
        //onlineUsersTreads загоняем в поток, фильтруем пользователя по тому, если логин совпадает с логином получателяб найди первое совпадение
        clientThread.ifPresent(clientThread1 -> clientThread1.send(sender, text));
        //если он найден (ifPresent), примени к нему метод send и передачей полей (sender, text)
    }

    //метод добавления пользователя в онлайн, когда он вошел в сеть
    //synchronized - синхронизация!!!
    public synchronized void addToOnline(ClientThread clientThread){
        onlineUsersTreads.add(clientThread);
    }

    //регистрация
    public User register(String line) {
        try {
            String[] s = line.split(SEPARATOR);
            if(s.length!=6)return null;
            String login = s[1];
            String password = s[2];
            String username = s[3];
            String dateStr = s[4];
            String[] split = dateStr.split("\\D");// "\\D" - любой нецифровой символ
            LocalDate birthday = LocalDate.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
            String city = s[5];
            User user = new User(login, password, username, birthday, city, "");
            user = userDAO.addUser(user);
            return user;
        }catch (Exception e){
            return null;
        }
    }

    //добавление в друзья
    public void processAddFriend(String line) {
        String[] s = line.split(SEPARATOR);
        if(s.length!=2)return;
        String login1 = s[0];//добавляющий
        String login2 = s[1];//добавляемый
        User u1 = userDAO.findByLogin(login1);
        User u2 = userDAO.findByLogin(login2);
        friendDAO.addFriendFor(u1.getId(), u2.getId());
    }

    //геттер списка всех пользователей
    //public List<User> getUsers() {return users;}
}
