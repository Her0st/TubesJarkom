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
    JTextArea userArea = new JTextArea(8, 20);

    public Client() {
        // Setup GUI with dark theme
        Color backgroundColor = new Color(45, 45, 45);
        Color textColor = new Color(230, 230, 230);
        Color textFieldColor = new Color(60, 63, 65);
        Font font = new Font("SansSerif", Font.PLAIN, 14);

        textField.setEditable(false);
        textField.setBackground(textFieldColor);
        textField.setForeground(textColor);
        textField.setFont(font);
        
        messageArea.setEditable(false);
        messageArea.setBackground(backgroundColor);
        messageArea.setForeground(textColor);
        messageArea.setFont(font);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        roomArea.setEditable(false);
        roomArea.setBackground(backgroundColor);
        roomArea.setForeground(textColor);
        roomArea.setFont(font);
        roomArea.setLineWrap(true);
        roomArea.setWrapStyleWord(true);

        userArea.setEditable(false);
        userArea.setBackground(backgroundColor);
        userArea.setForeground(textColor);
        userArea.setFont(font);
        userArea.setLineWrap(true);
        userArea.setWrapStyleWord(true);

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

        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BorderLayout());
        userPanel.add(new JScrollPane(userArea), BorderLayout.CENTER);
        userPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(textColor), "Users in Room", 0, 0, font, textColor));
        userPanel.setBackground(backgroundColor);

        // Add panels to frame
        frame.getContentPane().add(inputPanel, BorderLayout.SOUTH);
        frame.getContentPane().add(chatPanel, BorderLayout.CENTER);
        frame.getContentPane().add(roomPanel, BorderLayout.EAST);
        frame.getContentPane().add(userPanel, BorderLayout.WEST);
        frame.getContentPane().setBackground(backgroundColor);
        frame.setSize(800, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Action listener for text field
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println("/msg " + textField.getText());
                textField.setText("");
            }
        });

        // Buttons for creating and joining rooms
        JButton createRoomButton = new JButton("Create Room");
        createRoomButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showCreateRoomDialog();
            }
        });

        JButton joinRoomButton = new JButton("Join Room");
        joinRoomButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showJoinRoomDialog();
            }
        });

        JButton refreshRoomButton = new JButton("Refresh Room");
        refreshRoomButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                out.println("/list");
            };
        });

        inputPanel.add(createRoomButton, BorderLayout.WEST);
        inputPanel.add(joinRoomButton, BorderLayout.EAST);
        roomPanel.add(refreshRoomButton, BorderLayout.EAST);
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
        // Connect to the server
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 5000);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Process all messages from the server
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

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.run();
    }
}