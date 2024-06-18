import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final int PORT = 5000;
    private static ConcurrentHashMap<String, HashSet<PrintWriter>> chatRooms = new ConcurrentHashMap<>();
    private static HashMap<String, PrintWriter> clients = new HashMap<>();

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
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String currentRoom;

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
                for (PrintWriter writer : clients.values()) {
                    writer.println("MESSAGE " + name + " has joined");
                }


                while (true) {
                    String input = in.readLine();
                    if (input.startsWith("/create ")) {
                        createRoom(input.substring(8));
                    } else if (input.startsWith("/join ")) {
                        joinRoom(input.substring(6));
                    } else if (input.equals("/list")) {
                        listRooms();
                    } else if (input.startsWith("/msg ")) {
                        if (currentRoom != null) {
                            sendMessageToRoom(currentRoom, name + ": " + input.substring(5));
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
                        leaveRoom(currentRoom);
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
            chatRooms.putIfAbsent(roomName, new HashSet<>());
            out.println("Room " + roomName + " created.");
        }

        private void joinRoom(String roomName) {
            if (!chatRooms.containsKey(roomName)) {
                out.println("Room " + roomName + " does not exist.");
                return;
            }
            if (currentRoom != null) {
                leaveRoom(currentRoom);
            }
            currentRoom = roomName;
            chatRooms.get(roomName).add(out);
            sendMessageToRoom(roomName, name + " has joined " + roomName);
        }

        private void leaveRoom(String roomName) {
            if (currentRoom != null) {
                chatRooms.get(roomName).remove(out);
                sendMessageToRoom(roomName, name + " has left " + roomName);
                currentRoom = null;
            }
        }

        private void listRooms() {
            StringBuilder roomList = new StringBuilder("Available rooms:\n");
            for (String room : chatRooms.keySet()) {
                roomList.append(room).append("\n");
            }
            out.println(roomList.toString());
        }

        private void sendMessageToRoom(String roomName, String message) {
            for (PrintWriter writer : chatRooms.get(roomName)) {
                writer.println("MESSAGE " + message);
            }
        }
    }
}