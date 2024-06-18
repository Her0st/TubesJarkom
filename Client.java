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
        userArea.setText("You are not in a room");
        userPanel.add(new JScrollPane(userArea), BorderLayout.CENTER);
        userPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(textColor), "Users in Room", 0, 0, font, textColor));
        userPanel.setBackground(backgroundColor);

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3, 10, 10));
        buttonPanel.setBackground(backgroundColor);
        JButton createRoomButton = new JButton("Create Room");
        JButton joinRoomButton = new JButton("Join Room");
        JButton leaveRoomButton = new JButton("Leave Room");
        buttonPanel.add(createRoomButton);
        buttonPanel.add(joinRoomButton);
        buttonPanel.add(leaveRoomButton);
        inputPanel.add(buttonPanel, BorderLayout.NORTH);

        JPanel roomButtonPanel = new JPanel();
        roomButtonPanel.setLayout(new GridLayout(3, 1, 10, 10));
        roomButtonPanel.setBackground(backgroundColor);
        JButton refreshRoomButton = new JButton("Refresh");
        JButton closeRoomButton = new JButton("Close Room");
        JButton kickUserButton = new JButton("Kick User");
        roomButtonPanel.add(refreshRoomButton);
        roomButtonPanel.add(closeRoomButton);
        roomButtonPanel.add(kickUserButton);
        roomPanel.add(roomButtonPanel, BorderLayout.SOUTH);

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

        // Action listeners for buttons
        createRoomButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showCreateRoomDialog();
                out.println("/refresh");
            }
        });

        joinRoomButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showJoinRoomDialog();
                out.println("/refresh");
            }
        });

        leaveRoomButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println("/leave");
                messageArea.setText("");
                userArea.setText("You are not in a room");
                out.println("/refresh");
            }
        });

        refreshRoomButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                roomArea.setText("");
                userArea.setText("");
                out.println("/refresh");
            }
        });

        closeRoomButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showCloseRoomDialog();
                out.println("/refresh");
            }
        });

        kickUserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showKickUserDialog();
                out.println("/refresh");
            }
        });
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
        String serverAddress = InetAddress.getLocalHost().getHostAddress();
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
                updateRoomList(line.substring(9));
            } else {
                JOptionPane.showMessageDialog(frame, line, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateRoomList(String rooms) {
        roomArea.setText("");
        String[] roomArray = rooms.split(",");
        for (String room : roomArray) {
            roomArea.append(room + "\n");
        }
    }


    private void showCreateRoomDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        JTextField roomNameField = new JTextField(10);
        JTextField limitField = new JTextField(10);
        panel.add(new JLabel("Room Name (No Space):"));
        panel.add(roomNameField);
        panel.add(new JLabel("User Limit:"));
        panel.add(limitField);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Create Room", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String roomName = roomNameField.getText().trim();
            String limitText = limitField.getText().trim();
            if (!roomName.isEmpty() && !limitText.isEmpty()) {
                try {
                    int limit = Integer.parseInt(limitText);
                    out.println("/create " + roomName + " " + limit);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "User limit must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void showJoinRoomDialog() {
        String roomName = JOptionPane.showInputDialog(frame, "Enter room name:", "Join Room", JOptionPane.PLAIN_MESSAGE);
        if (roomName != null && !roomName.trim().isEmpty()) {
            out.println("/join " + roomName);
        }
    }

    private void showCloseRoomDialog() {
        String roomName = JOptionPane.showInputDialog(frame, "Enter room name to close:", "Close Room", JOptionPane.PLAIN_MESSAGE);
        if (roomName != null && !roomName.trim().isEmpty()) {
            out.println("/close " + roomName);
        }
    }

    private void showKickUserDialog() {
        String userName = JOptionPane.showInputDialog(frame, "Enter user name to kick:", "Kick User", JOptionPane.PLAIN_MESSAGE);
        if (userName != null && !userName.trim().isEmpty()) {
            out.println("/kick " + userName);
        }
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.run();
    }
}
