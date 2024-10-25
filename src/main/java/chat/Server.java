/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.chat;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author Karol
 */
public class Server {
    
    private static Vector<ClientThread> clientThreads = new Vector<>();
    private static final int PORT = 2011;

    public static void main(String[] args) {
        System.out.println("Server running...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket incoming = serverSocket.accept();
                ClientThread thread = new ClientThread(incoming);
                clientThreads.add(thread);
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ClientThread extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                username = in.readLine();

                sendMessage("Serwer: " + username + " dołączył do chatu.");
                updateClientList();

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("@")) {
                        String[] parts = message.split(" ", 2);
                        if (parts.length == 2) {
                            String recipientName = parts[0].substring(1);
                            String privateMessage = parts[1];
                            sendPrivateMessage(recipientName, privateMessage);
                        }
                    } else {
                        sendMessage(username + ": " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }

        private void sendPrivateMessage(String recipientName, String message) {
            for (ClientThread client : clientThreads) {
                if (client.username.equals(recipientName)) {
                    client.out.println("(Prywatna " + this.username + ") " + username + ": " + message);
                } else if (client.equals(this)) {
                    client.out.println("(Prywatna " + recipientName + ") " + username + ": " + message);
                }
            }
        }

        private void sendMessage(String message) {
            for (ClientThread client : clientThreads) {
                client.out.println(message);
            }
        }

        private void updateClientList() {
            StringBuilder clientList = new StringBuilder("Serwer: Obecnie na chacie: ");
            for (int i = 0; i < clientThreads.size(); i++) {
                clientList.append(clientThreads.get(i).username);
                if (i < clientThreads.size() - 1) {
                    clientList.append(", ");
                }
            }
            for (ClientThread client : clientThreads) {
                client.out.println(clientList.toString());
            }
        }

        private void disconnect() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            clientThreads.remove(this);
            sendMessage("Server: " + username + " opuścił chat.");
            updateClientList();
        }
    }
}
