package ua.mk.berkut.maserver.clients;

import ua.mk.berkut.maserver.Main;
import ua.mk.berkut.maserver.db.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Optional;

public class ClientThread extends Thread {

    private Socket socket;
    private Main main;
    private PrintWriter out;
    private BufferedReader in;
    private User user;

    /**
     * Конструктор потока
     * @param socket сокет для подключения
     * @param main   ссылка на объект главного класса сервера
     */
    public ClientThread(Socket socket, Main main) {
        this.socket = socket;
        this.main = main;
    }

    /**
     * Главный метод потока, в нем происходит "общение" клиента с сервером
     */
    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            //за счет socket передает и получает сообщения от клиента
            this.in = in;
            this.out = out;
            out.println("Server Ok");
            while (!socket.isClosed()) {
                String message = in.readLine();
                System.out.println(message);
                if (message.startsWith("register")){
                    register(message);
                }else if (message.startsWith("login")){
                    login(message);
                }else if (message.startsWith("online")){
                    List<User> onlineUsers = main.getOnlineUsers();
                    out.println(onlineUsers);
                    System.out.println(main.onlineUsersTreads);
                }else if (message.startsWith("message")){
                    correspondence(message);
                }else if (message.startsWith("output")){
                    output();
                }
                //todo реализовать проверки
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                socket.close();
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            main.removeToOnlineUsers(this);
            //todo реализовать корректное закнытие соединения (сделать, что бы сервер не падал в exception, когда останавливается работа клиентской программы)
        }
    }

    private boolean correspondence(String message) {
        processMessage(message);
        return true;
    }

    private boolean register(String message){
        User user = main.register(message);
        if (user==null){
            out.println("Register Error");
            System.out.println("Register Error");
            return false;
        }
        out.println("Register OK");
        System.out.println("Register OK");
        return true;
    }

    private boolean login(String message){
        String[] s = message.split(":");
        String login = s[1];
        String password = s[2];
        User user = main.searchUser(login, password);
        if(user == null){
            out.println("Login Error");
            System.out.println("Login Error");
            return false;
        }
        this.user = user;
        main.addToOnlineUsers(this);
        out.println(user);
        System.out.println("Login OK");
        return true;
    }

    public User getUser() {
        return user;
    }

    public synchronized void processMessage(String message) {
        String[] s = message.split(":");
        String recipient = s[1];//получатель
        String sender = s[2];//отправитель
        String text = s[3];
        Optional<ClientThread> clientThread = main.onlineUsersTreads.stream().filter(t -> t.getUser().getLogin().equals(recipient)).findAny();
        //onlineUsersTreads загоняем в поток, фильтруем пользователя по тому, если логин совпадает с логином получателя и находим первое совпадение
        clientThread.ifPresent(clientThread1 -> clientThread1.out.println("new message from '" + sender + "': '" + text + "'"));
        //если он найден (ifPresent), примени к нему метод send с передачей полей (sender, text)
        //todo почему получатель получает сообщение от отправителя только полсе нажатия на enter в консоли получателя?
    }

    private boolean output() {
        main.removeToOnlineUsers(this);
        out.println("Output OK");
        return true;
    }
}
