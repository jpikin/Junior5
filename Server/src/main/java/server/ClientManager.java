package server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientManager implements Runnable {
    private final Socket socket;
    private String name;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    public static ArrayList<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket) {
        this.socket = socket;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            name = bufferedReader.readLine();
            clients.add(this);
            System.out.println(name + " подключился к чату.");
            broadcastMessage("Server: " + name + " подключился к чату.");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private void removeClient() {
        clients.remove(this);
        System.out.println(name + " покинул чат.");
        try {
            broadcastMessage("Server: " + name + " покинул чат.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClient();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void broadcastMessage(String message) throws IOException {
        String[] msgSplit = message.split(" ");
        String tryName = msgSplit[1];
        String isServer = msgSplit[0];

        if (isServer.equals("Server:")) {
            for (ClientManager client : clients) {
                if (!client.name.equals(name) && message != null) {
                    client.bufferedWriter.write(message);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                }
            }
        } else {
            if (!isPrivateMessage(tryName)) {
                for (ClientManager client : clients) {
                    if (!client.name.equals(name) && message != null) {
                        client.bufferedWriter.write(message);
                        client.bufferedWriter.newLine();
                        client.bufferedWriter.flush();
                    }
                }
            } else {
                for (ClientManager client : clients) {
                    if (client.name.equals(tryName) && message != null) {
                        client.bufferedWriter.write(message);
                        client.bufferedWriter.newLine();
                        client.bufferedWriter.flush();
                    }
                }
            }
        }
    }


    /*
     * Выясняем личное ли сообщение отправлено.
     */
    private Boolean isPrivateMessage(String msg) throws IOException {
        Boolean flag = false;
        for (ClientManager client : clients) {
            if (client.name.equals(msg)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                // Чтение данных
                messageFromClient = bufferedReader.readLine();

                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);

                break;
            }
        }
    }
}
