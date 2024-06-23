import java.awt.BorderLayout;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

public class Server {
    private static final int PORT = 5000;
    private static Map<String, PrintWriter> clients = new HashMap<>();
    private static Map<String, Room> chatRooms = new HashMap<>();

    
    public static void main(String[] args) throws Exception {
        System.out.println("Server started...");
        // System.out.println("IP Address to Connect: " + InetAddress.getLocalHost().getHostAddress());
        ServerSocket listener = new ServerSocket(PORT);

        JFrame frame = new JFrame("Server IP Address");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 100);
        JLabel ipLabel = new JLabel("Server IP Address: ", SwingConstants.CENTER);
        frame.getContentPane().add(ipLabel, BorderLayout.CENTER);

        // Get the local IP address
        String ipAddress = InetAddress.getLocalHost().getHostAddress();
        ipLabel.setText("Server IP Address: " + ipAddress);

        // Display the window.
        frame.setVisible(true);


        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class Room {
        String name;
        String owner;
        int limit;
        Set<PrintWriter> clients = new HashSet<>();

        Room(String name, String owner, int limit) {
            this.name = name;
            this.owner = owner;
            this.limit = limit;
        }

        boolean isFull() {
            return clients.size() >= limit;
        }

        void addClient(PrintWriter out) {
            clients.add(out);
        }

        void removeClient(PrintWriter out) {
            clients.remove(out);
        }

        void broadcast(String message) {
            for (PrintWriter client : clients) {
                client.println(message);
            }
        }

        void listUsers(PrintWriter out) {
            StringBuilder users = new StringBuilder("USERLIST ");
            synchronized (clients) {
                for (PrintWriter client : clients) {
                    users.append(
                            Server.clients.entrySet().stream()
                                    .filter(entry -> entry.getValue().equals(client))
                                    .map(Map.Entry::getKey)
                                    .findFirst()
                                    .orElse("Unknown")).append(",");
                }
            }
            out.println(users.toString());
        }

        void broadcastUserList() {
            StringBuilder users = new StringBuilder("USERLIST ");
            synchronized (clients) {
                for (PrintWriter client : clients) {
                    users.append(
                            Server.clients.entrySet().stream()
                                    .filter(entry -> entry.getValue().equals(client))
                                    .map(Map.Entry::getKey)
                                    .findFirst()
                                    .orElse("Unknown")).append(",");
                }
            }
            String userListMessage = users.toString();
            for (PrintWriter client : clients) {
                client.println(userListMessage);
            }
        }

        void sendRoomInfo(PrintWriter out) {
            // out.println("ROOMINFO " + name + " " + owner + " " + clients.size() + "/" + limit);
        }
    }

    private static class Handler extends Thread {
        private String name;
        private Room currentRoom;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Request name from client
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null || name.isEmpty() || name.equals("null")) {
                        out.println("Name must not be empty, Restart the program");
                        return;
                    }
                    synchronized (clients) {
                        if (!clients.containsKey(name)) {
                            clients.put(name, out);
                            break;
                        }
                    }
                }

                out.println("NAMEACCEPTED " + name);
                listRooms();

                // Command handling loop
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    if (input.startsWith("/create ")) {
                        String[] tokens = input.split(" ");
                        if (tokens.length == 3) {
                            try {
                                int limit = Integer.parseInt(tokens[2]);
                                createRoom(tokens[1], limit);
                            } catch (NumberFormatException e) {
                                out.println("Invalid limit.");
                            }
                        } else {
                            out.println("Usage: /create <roomName> <limit>");
                        }
                    } else if (input.startsWith("/join ")) {
                        joinRoom(input.substring(6));
                    } else if (input.startsWith("/leave")) {
                        leaveRoom();
                    } else if (input.startsWith("/close ")) {
                        closeRoom(input.substring(7));
                    } else if (input.startsWith("/kick ")) {
                        kickUser(input.substring(6));
                    } else if (input.startsWith("/refresh")) {
                        listRooms();
                        if (currentRoom != null) {
                            currentRoom.listUsers(out);
                            sendRoomInfo();
                        }
                    } else if (input.startsWith("/roominfo")) {
                        sendRoomInfo();
                    } else if (input.startsWith("/msg ")) {
                        sendMessage(input.substring(5));
                    } else if (input.startsWith("/logout")) {
                        logout();
                    } else {
                        out.println("Unknown command");
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                if (name != null) {
                    clients.remove(name);
                    if (currentRoom != null) {
                        leaveRoom();
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }

        private void sendRoomInfo() {
            if (currentRoom != null) {
                currentRoom.sendRoomInfo(out);
            } else {
                // out.println("ROOMINFO None None 0/0");
            }
        }

        private void createRoom(String roomName, int limit) {
            synchronized (chatRooms) {
                if (!chatRooms.containsKey(roomName)) {
                    if(limit > 0){
                        chatRooms.put(roomName, new Room(roomName, name, limit));
                        // out.println("Room " + roomName + " created with limit " + limit + ".");
                        joinRoom(roomName);
                    }
                    else{
                        out.println("Room limit must be positive integer");
                    }
                } else {
                    out.println("Room " + roomName + " already exists.");
                }
            }
        }

        private void joinRoom(String roomName) {
            synchronized (chatRooms) {
                if (chatRooms.containsKey(roomName)) {
                    Room room = chatRooms.get(roomName);
                    if (!room.isFull()) {
                        if (currentRoom != null) {
                            leaveRoom();
                        }
                        currentRoom = room;
                        currentRoom.addClient(out);
                        currentRoom.broadcast("MESSAGE " + name + " has joined " + roomName);
                        currentRoom.broadcastUserList();
                        currentRoom.listUsers(out);
                        sendRoomInfo();
                    } else {
                        out.println("Room " + roomName + " is full.");
                    }
                } else {
                    out.println("Room " + roomName + " does not exist.");
                }
            }
        }

        private void leaveRoom() {
            if (currentRoom != null) {
                currentRoom.removeClient(out);
                currentRoom.broadcast("MESSAGE " + name + " has left " + currentRoom.name);
                currentRoom.broadcastUserList();
                currentRoom = null;
                listRooms();
            }
        }

        private void listRooms() {
            StringBuilder roomList = new StringBuilder("ROOMLIST ");
            synchronized (chatRooms) {
                for (Room room : chatRooms.values()) {
                    roomList.append(room.name).append(" (Owner: ").append(room.owner).append(", Limit: ").append(room.clients.size()).append("/").append(room.limit).append("),");
                }
            }
            for (PrintWriter client : clients.values()) {
                client.println(roomList.toString());
            }
        }

        private void closeRoom(String roomName) {
            synchronized (chatRooms) {
                Room room = chatRooms.get(roomName);
                if (room != null && room.owner.equals(name)) {
                    room.broadcast("MESSAGE The room " + roomName + " is closed by the owner.");
                    for (PrintWriter writer : room.clients) {
                        writer.println("MESSAGE The room " + roomName + " is closed. You are disconnected.");
                        writer.println("LEAVE");
                    }
                    chatRooms.remove(roomName);
                    listRooms();
                } else {
                    out.println("You are not the owner of the room.");
                }
            }
        }

        private void kickUser(String userName) {
            synchronized (chatRooms) {
                if (currentRoom != null && currentRoom.owner.equals(name)) {
                    PrintWriter kickedUserOut = clients.get(userName);
                    if (kickedUserOut != null) {
                        kickedUserOut.println("MESSAGE You are kicked out from the room " + currentRoom.name);
                        kickedUserOut.println("LEAVE");
                        currentRoom.removeClient(kickedUserOut);
                        currentRoom.broadcast("MESSAGE " + userName + " is kicked out from the room.");
                        currentRoom.broadcastUserList();
                        currentRoom.listUsers(out);
                    } else {
                        out.println("User " + userName + " not found in the room.");
                    }
                } else {
                    out.println("You are not the owner of the room.");
                }
            }
        }

        private void sendMessage(String message) {
            if (currentRoom != null) {
                currentRoom.broadcast("MESSAGE " + name + ": " + message);
            } else {
                out.println("You are not in a room.");
            }
        }

        private void logout() {
            if (currentRoom != null) {
                leaveRoom();
            }
            clients.remove(name);
            out.println("LOGOUT");
            name = null;
            run();
        }
    }
}
