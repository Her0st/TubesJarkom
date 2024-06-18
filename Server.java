import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 5000;
    private static HashMap<String, PrintWriter> clients = new HashMap<>();
    private static HashMap<String, HashSet<String>> chatRooms = new HashMap<>();

    public static void main(String[] args) throws Exception {
        System.out.println("Server started...");
        System.out.println("IP Address to Connect : " + InetAddress.getLocalHost());
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class Handler extends Thread {
        private String name;
        private String currentRoom;
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

                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (clients) {
                        if (!clients.containsKey(name)) {
                            clients.put(name, out);
                            break;
                        }
                    }
                }

                out.println("NAMEACCEPTED");
                listRooms();

                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    if (input.startsWith("/create ")) {
                        createRoom(input.substring(8));
                    } else if (input.startsWith("/join ")) {
                        joinRoom(input.substring(6));
                    } else if (input.startsWith("/refresh")) {
                        listRooms();
                        sendUserList();
                    } else if (input.startsWith("/msg ")) {
                        sendMessageToRoom(input.substring(5));
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                if (name != null) {
                    clients.remove(name);
                    if (currentRoom != null) {
                        chatRooms.get(currentRoom).remove(name);
                        if (chatRooms.get(currentRoom).isEmpty()) {
                            chatRooms.remove(currentRoom);
                        }
                    }
                }
                if (out != null) {
                    clients.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }

        private void createRoom(String roomName) {
            synchronized (chatRooms) {
                if (!chatRooms.containsKey(roomName)) {
                    chatRooms.put(roomName, new HashSet<>());
                    joinRoom(roomName);
                } else {
                    out.println("MESSAGE Room already exists");
                }
            }
        }

        private void joinRoom(String roomName) {
            synchronized (chatRooms) {
                if (chatRooms.containsKey(roomName)) {
                    if (currentRoom != null) {
                        chatRooms.get(currentRoom).remove(name);
                    }
                    chatRooms.get(roomName).add(name);
                    currentRoom = roomName;
                    out.println("MESSAGE Joined room " + roomName);
                    sendUserList();
                } else {
                    out.println("MESSAGE Room does not exist");
                }
            }
        }

        private void sendMessageToRoom(String message) {
            synchronized (chatRooms) {
                if (currentRoom != null) {
                    for (String user : chatRooms.get(currentRoom)) {
                        PrintWriter writer = clients.get(user);
                        writer.println("MESSAGE " + name + ": " + message);
                    }
                } else {
                    out.println("MESSAGE You are not in any room");
                }
            }
        }

        private void listRooms() {
            StringBuilder rooms = new StringBuilder();
            synchronized (chatRooms) {
                for (String room : chatRooms.keySet()) {
                    rooms.append(room).append(",");
                }
            }
            out.println("ROOMLIST " + rooms.toString());
        }

        private void sendUserList() {
            StringBuilder users = new StringBuilder();
            synchronized (chatRooms) {
                if (currentRoom != null) {
                    for (String user : chatRooms.get(currentRoom)) {
                        users.append(user).append(",");
                    }
                }
            }
            out.println("USERLIST " + users.toString());
        }
    }
}