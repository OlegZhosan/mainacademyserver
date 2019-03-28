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

    //конструктор потока
    public ClientThread(Socket socket, Main main) {
        this.socket = socket;//соединение для клиента
        this.main = main;//соединение с классом Main
    }

    //запуск
    public static void main(String[] args) throws Exception { new Main().run(); }

    //главный метод потока
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
                    if ("<<<".equals(message)){
                        onlineUsers = main.getOnlineUsers();

                        //фильтрация из всех пользоваетлей оннлайн только друзей
                        sendList(onlineUsers.stream().filter(u->user.getFriendsIds().contains(u.getId())).collect(Collectors.toList()));

                        //добавление в друзья
                    }else if (message.startsWith("+++")){
                        main.processAddFriend(message.substring(3));

                    }else if (">>>exit<<<".equals(message)){ break; }//выход
                    else { main.processMessage(message); }
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

    //выдача списка пользователей онлайн
    private void sendList(List<User> list) {
        out.println("<<<");
        for (User u : list) { out.println(u.getId() + ";" + u.getUsername() + ";" + u.getLogin()); }
        out.println("<<<");
    }

    //метод обработки логина и пароля, введенного клиентом
    //выводит стартовое сообщение
    //отправляет на регистрацию, если нужно
    private boolean login() throws IOException {
        out.println("Server Ok");//выводить клиенту при правильном подключении (признак подключения)
        String line = in.readLine();//считать строку
        if(line.startsWith("register")){return register(line);}
        String[] s = line.split(";");//обьявление массива строк, со словами, разделенными пробелом
        //s[0] - "login"
        //s[1] - login
        //s[2] - password
        if(s.length != 3){//если массив не равен трем
            out.println("Login failed 2");
            return false;
        }
        if(!"login".equals(s[0])){//если первое слово в строке не "login"
            out.println("Login failed 1");
            return false;
        }
        String login = s[1];//второе слово воспринимать как значение параметра login
        String password = s[2];//второе слово воспринимать как значение параметра password
        User user = findUser(login, password);//поиск пользователя в бд по введенному логину
        if(user == null){//если обьект user равен нулю
            out.println("Login failed 3");
            return false;
        }
        this.user = user;//присвоение значения этой переменной общей переменной класса
        out.println("Login OK");
        return true;
    }

    //поиск пользователя в бд по введенному логину
    private User findUser(String login, String password) {
        return main.findUser(login, password);
    }

    //регистрация
    private boolean register(String line){
        User user = main.register(line);
        if (user==null)return false;
        out.println("Register OK");
        return true;
    }

    //геттер пользователя онлайн
    //геттер залогиненого пользователя
    //получение пользователя, ассоциированного с потоком
    //нужен для отправки соощения???
    public User getUser() { return user; }

    //сеттер пользователя
    public void setUser(User user) { this.user = user; }

    //отправить клиенту сообщение
    public void  send(String sender, String text){ out.println(">>>" + sender + ">>>" + text); }
}
