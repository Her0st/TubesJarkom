import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class Client {
    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Chat Room");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);
    JTextArea roomArea = new JTextArea(8, 20);
    JTextArea userArea = new JTextArea(8,20);

    public Client() {
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.NORTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();

<<<<<<< Updated upstream
=======
        roomArea.setEditable(false);
        roomArea.setBackground(backgroundColor);
        roomArea.setForeground(textColor);
        roomArea.setFont(font);
        roomArea.setLineWrap(true);
        roomArea.setWrapStyleWord(true);

        // Create panels
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.add(textField, BorderLayout.CENTER);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.setBackground(backgroundColor);

        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        chatPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(textColor), "Chat Area", 0, 0, font, textColor));
        chatPanel.setBackground(backgroundColor);

        JPanel roomPanel = new JPanel();
        roomPanel.setLayout(new BorderLayout());
        roomPanel.add(new JScrollPane(roomArea), BorderLayout.CENTER);
        roomPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(textColor), "Available Rooms", 0, 0, font, textColor));
        roomPanel.setBackground(backgroundColor);

        // Add panels to frame
        frame.getContentPane().add(inputPanel, BorderLayout.SOUTH);
        frame.getContentPane().add(chatPanel, BorderLayout.CENTER);
        frame.getContentPane().add(roomPanel, BorderLayout.EAST);
        frame.getContentPane().setBackground(backgroundColor);
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Action listener for text field
>>>>>>> Stashed changes
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println("/msg " + textField.getText());
                textField.setText("");
            }
        });
    }

    private String getServerAddress() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Welcome to the Chat Room",
            JOptionPane.QUESTION_MESSAGE);
    }

    private String getName() {
        return JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE);
    }

    private void run() throws IOException {
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 5000);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        while (true) {
            String line = in.readLine();
            if (line.startsWith("SUBMITNAME")) {
                out.println(getName());
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
            } else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");
            } else if (line.startsWith("USERLIST")) {
                userArea.setText(line.substring(9).replace(",", "\n"));
            } else if (line.startsWith("ROOMLIST")) {
                roomArea.setText(line.substring(9).replace(",", "\n"));
            }
        }
    }

    private void showCreateRoomDialog() {
        String roomName = JOptionPane.showInputDialog(frame, "Enter room name:", "Create Room", JOptionPane.PLAIN_MESSAGE);
        if (roomName != null && !roomName.trim().isEmpty()) {
            out.println("/create " + roomName);
        }
    }

    private void showJoinRoomDialog() {
        String roomName = JOptionPane.showInputDialog(frame, "Enter room name:", "Join Room", JOptionPane.PLAIN_MESSAGE);
        if (roomName != null && !roomName.trim().isEmpty()) {
            out.println("/join " + roomName);
        }
    }

    private void requestRoomList() {
        out.println("/list");
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}