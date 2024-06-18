import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 5000;
    private static Map<String, PrintWriter> clients = new HashMap<>();
    private static Map<String, Room> chatRooms = new HashMap<>();

    public static void main(String[] args) throws Exception {
        System.out.println("Server started...");
        System.out.println("IP Address to Connect: " + InetAddress.getLocalHost().getHostAddress());
        ServerSocket listener = new ServerSocket(PORT);
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
        Set<PrintWriter> clients = new HashSet<>();

        Room(String name, String owner) {
            this.name = name;
            this.owner = owner;
        }

        void addClient(PrintWriter out) {
            clients.add(out);
        }

        void removeClient(PrintWriter out) {
            clients.remove(out);
        }

        void broadcast(String message) {
            for (PrintWriter client : clients) {
                client.println("MESSAGE " + message);
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
                    if (name == null || name.isEmpty()) {
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
                        createRoom(input.substring(8));
                        listRooms();
                        if (currentRoom != null) {
                            currentRoom.listUsers(out);
                        }
                    } else if (input.startsWith("/join ")) {
                        joinRoom(input.substring(6));
                        listRooms();
                        if (currentRoom != null) {
                            currentRoom.listUsers(out);
                        }
                    } else if (input.startsWith("/leave")) {
                        leaveRoom();
                        listRooms();
                        if (currentRoom != null) {
                            currentRoom.listUsers(out);
                        }
                    } else if (input.startsWith("/close ")) {
                        closeRoom(input.substring(7));
                        listRooms();
                        if (currentRoom != null) {
                            currentRoom.listUsers(out);
                        }
                    } else if (input.startsWith("/kick ")) {
                        kickUser(input.substring(6));
                        listRooms();
                        if (currentRoom != null) {
                            currentRoom.listUsers(out);
                        }
                    } else if (input.startsWith("/refresh")) {
                        listRooms();
                        if (currentRoom != null) {
                            currentRoom.listUsers(out);
                        }
                    } else if (input.startsWith("/msg ")) {
                        sendMessage(input.substring(5));
                        listRooms();
                        if (currentRoom != null) {
                            currentRoom.listUsers(out);
                        }
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

        private void createRoom(String roomName) {
            synchronized (chatRooms) {
                if (!chatRooms.containsKey(roomName)) {
                    chatRooms.put(roomName, new Room(roomName, name));
                    out.println("Room " + roomName + " created.");
                    joinRoom(roomName);
                } else {
                    out.println("Room " + roomName + " already exists.");
                }
            }
        }

        private void joinRoom(String roomName) {
            synchronized (chatRooms) {
                if (chatRooms.containsKey(roomName)) {
                    if (currentRoom != null) {
                        leaveRoom();
                    }
                    currentRoom = chatRooms.get(roomName);
                    currentRoom.addClient(out);
                    currentRoom.broadcast(name + " has joined " + roomName);
                    currentRoom.listUsers(out);
                } else {
                    out.println("Room " + roomName + " does not exist.");
                }
            }
        }

        private void leaveRoom() {
            if (currentRoom != null) {
                currentRoom.removeClient(out);
                currentRoom.broadcast(name + " has left " + currentRoom.name);
                currentRoom = null;
            }
        }

        private void listRooms() {
            StringBuilder roomList = new StringBuilder("ROOMLIST ");
            synchronized (chatRooms) {
                for (String room : chatRooms.keySet()) {
                    roomList.append(room).append(",");
                }
            }
            out.println(roomList.toString());
        }

        private void closeRoom(String roomName) {
            synchronized (chatRooms) {
                Room room = chatRooms.get(roomName);
                if (room != null && room.owner.equals(name)) {
                    room.broadcast("The room " + roomName + " is closed by the owner.");
                    for (PrintWriter writer : room.clients) {
                        writer.println("MESSAGE The room " + roomName + " is closed. You are disconnected.");
                    }
                    chatRooms.remove(roomName);
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
                        currentRoom.removeClient(kickedUserOut);
                        currentRoom.broadcast(userName + " is kicked out from the room.");
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
                currentRoom.broadcast(name + ": " + message);
            } else {
                out.println("You are not in a room.");
            }
        }
    }
}
