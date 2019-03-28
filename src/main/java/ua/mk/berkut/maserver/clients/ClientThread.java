//реализация работы сервера с клиентом
//поток, взаимодействующий с клиентом. для каждого клиента создает отдельный поток

package ua.mk.berkut.maserver.clients;

import ua.mk.berkut.maserver.Main;
import ua.mk.berkut.maserver.db.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;

public class ClientThread extends Thread{
    //extends Thread - важный момент. обратить на это внимание!!!

    private PrintWriter out;//обьект передачи текста клиенту
    private BufferedReader in;//чтение полученого от клиента
    private Main main;//соединение с классом Main
    private Socket socket;//соединение для клиента
    private User user;//user для одного потока

    /**
     * Конструктор потока
     * @param socket сокет для подключения
     * @param main ссылка на объект главного класса сервера
     */
    public ClientThread(Socket socket, Main main) {
        this.socket = socket;//соединение для клиента
        this.main = main;//соединение с классом Main
    }

    /**
     * Главный метод потока, в нем происходит "общение" клиента с сервером
     */
    //ждет информации от клиента
    @Override
    public void run(){
        try(BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            //присвоение значения этих переменный (описанных в try) общим переменным класса
            this.in = in;
            this.out = out;

            if(login()){//если сработал метод login
                main.addToOnline(this);//добавление пользователя в список онлайн
                List<User> onlineUsers;//список пользователей онлайн
                String message;//строка собщения
                while ((message = in.readLine())!= null){//пока соединение не закрыто???

                    //обновление списка друзей
                    if ("<<<".equals(message)){// Show online friends
                        onlineUsers = main.getOnlineUsers();

                        //фильтрация из всех пользоваетлей оннлайн только друзей
                        sendList(onlineUsers.stream().filter(u->user.getFriendsIds().contains(u.getId())).collect(Collectors.toList()));

                    }else if (message.startsWith("+++")){ main.processAddFriend(message.substring(3));//добавление в друзья
                    }else if (">>>exit<<<".equals(message)) break; //выход
                    else  main.processMessage(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                socket.close();//закрывает соединение, если не получилось залогинится???
            } catch (IOException e) {
                e.printStackTrace(); }
            main.remove(this);//закрывает соединение, если не получилось залогинится???
        }
    }

    /**
     * Отправка списка пользователей подключенному клиенту
     * @param list список, содержащий информацию о пользователях в формате: id;username;login
     */
    private void sendList(List<User> list) {
        out.println("<<<");
        for (User u : list) out.println(u.getId() + ";" + u.getUsername() + ";" + u.getLogin());
        out.println("<<<");
    }

    /**
     * Обработка логина и пароля вновь подключившегося пользователя.
     * Отправляет на регистрацию, если нужно
     * Сначала отправляется признак подключения: {@code Server Ok}
     * Затем, проверяются login-password и если подключение удалось, отправлем признак подключения: {@code Login Ok}
     * @return true, если подключение успешно, и false - в противном случае
     * @throws IOException если произошла ошибка при подключении
     */
    private boolean login() throws IOException {
        System.out.println("Server Ok");
        out.println("Server Ok");//выводить клиенту при правильном подключении (признак подключения)
        String line = in.readLine();//считать строку
        if(line.startsWith("register"))return register(line);
        String[] s = line.split(";");//обьявление массива строк, со словами, разделенными пробелом
        //s[0] - "login"
        //s[1] - login
        //s[2] - password
        if(!"login".equals(s[0])){//если первое слово в строке не "login"
            System.out.println("Login failed 1");
            out.println("Login failed 1");
            return false;
        }
        if(s.length != 3){//если массив не равен трем
            System.out.println("Login failed 2");
            out.println("Login failed 2");
            return false;
        }
        String login = s[1];//второе слово воспринимать как значение параметра login
        String password = s[2];//второе слово воспринимать как значение параметра password
        User user = findUser(login, password);//поиск пользователя в бд по введенному логину
        if(user == null){//если обьект user равен нулю
            System.out.println("Login failed 3");
            out.println("Login failed 3");
            return false;
        }
        this.user = user;//присвоение значения этой переменной общей переменной класса
        System.out.println("Login OK");
        out.println("Login OK");
        return true;
    }

    /**
     * Метод, делегирующий в Main поиск пользователя по логину и паролю
     * @param login логин пользователя
     * @param password пароль пользователя
     * @return найденного пользователя
     */
    private User findUser(String login, String password) {
        return main.findUser(login, password);
    }

    /**
     * Метод, делегирующий в Main регистрацию пользователя
     * @param line строка регистрации
     * @return true - если пользователь зарегистрировался успешно, false - в противном случае
     */
    private boolean register(String line){
        User user = main.register(line);
        if (user==null)return false;
        System.out.println("Register OK");
        out.println("Register OK");
        return true;
    }

    //геттер пользователя онлайн
    //геттер залогиненого пользователя
    //получение пользователя, ассоциированного с потоком
    //нужен для отправки соощения???
    /**
     * Получение пользователя, ассоциированного с потоком
     * @return пользователя, ассоциированного с потоком
     */
    public User getUser() {
        return user;
    }

    //отправить клиенту сообщение
    public void  send(String sender, String text){ out.println(">>>" + sender + ">>>" + text); }
}
