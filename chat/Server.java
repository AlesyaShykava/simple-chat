package main.java.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    private static class Handler extends Thread {
        private Socket socket;

        public Handler (Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("Cоединение с удаленным адресом установлено" + socket.getRemoteSocketAddress());
            String userName = null;
            try (Connection connection = new Connection(socket)) {
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);
            } catch (IOException | ClassNotFoundException ex) {
                ConsoleHelper.writeMessage("Ошибка при обмене данными с удаленным адресом");
            } finally {
                if (userName != null) {
                    connectionMap.remove(userName);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                }
                ConsoleHelper.writeMessage("Соединение с удаленным сервером закрыто");
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message messageReceived = connection.receive();
                if (messageReceived.getType() != MessageType.TEXT) {
                    ConsoleHelper.writeMessage("Сообщение не является текстом");
                } else if (messageReceived.getType() == MessageType.TEXT) {
                    Message messageNew = new Message(MessageType.TEXT, String.format("%s: %s", userName, messageReceived.getData()));
                    Server.sendBroadcastMessage(messageNew);
                }
            }
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> pair : connectionMap.entrySet()) {
                if (!pair.getKey().equals(userName)) {
                    Message message = new Message(MessageType.USER_ADDED, pair.getKey());
                    connection.send(message);
                }
            }
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            Message nameRequest = new Message(MessageType.NAME_REQUEST);
            Message messageRecevied = null;
            while(true) {
                connection.send(nameRequest);
                messageRecevied = connection.receive();
                if (messageRecevied.getType() != MessageType.USER_NAME || messageRecevied.getData().isEmpty() || connectionMap.containsKey(messageRecevied.getData())) {
                        continue;
                }
                else {
                    break;
                }
            }
            connectionMap.put(messageRecevied.getData(), connection);
            connection.send(new Message(MessageType.NAME_ACCEPTED));
            return messageRecevied.getData();
        }
    }

    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> pair: connectionMap.entrySet()) {
            try {
                pair.getValue().send(message);
            } catch (IOException e) {
                System.out.println("Сообщение не отправлено");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket socket = null;
        try {
            int number = ConsoleHelper.readInt();
            socket = new ServerSocket(number);
            System.out.println("Сервер запущен");
            while (true) {
                Socket client = socket.accept();
                Handler handler = new Handler(client);
                handler.start();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            socket.close();
        }

    }
}
