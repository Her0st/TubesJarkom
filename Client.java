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
<<<<<<< Updated upstream
        
        messageArea.setEditable(false);
        messageArea.setBackground(backgroundColor);
        messageArea.setForeground(textColor);
        messageArea.setFont(font);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
=======
        textField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        messagePane.setEditable(false);
        messagePane.setBackground(backgroundColor);
        messagePane.setForeground(textColor);
        messagePane.setFont(font);
        messagePane.setMargin(new Insets(10, 10, 10, 10));
        doc = messagePane.getStyledDocument();

        roomArea.setEditable(false);
        roomArea.setBackground(backgroundColor);
        roomArea.setForeground(textColor);
        roomArea.setFont(font);
        roomArea.setLineWrap(true);
        roomArea.setWrapStyleWord(true);
        roomArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        userArea.setEditable(false);
        userArea.setBackground(backgroundColor);
        userArea.setForeground(textColor);
        userArea.setFont(font);
        userArea.setLineWrap(true);
        userArea.setWrapStyleWord(true);
        userArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
>>>>>>> Stashed changes

        // Create panels
        JPanel inputPanel = new RoundedPanel(15, backgroundColor);
        inputPanel.setLayout(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
<<<<<<< Updated upstream
        inputPanel.setBackground(backgroundColor);

        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        chatPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(textColor), "Chat Area", 0, 0, font, textColor));
        chatPanel.setBackground(backgroundColor);
=======

        // Panel for buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        buttonPanel.setBackground(backgroundColor);
        JButton createRoomButton = createCustomButton("Create Room");
        JButton joinRoomButton = createCustomButton("Join Room");
        JButton leaveRoomButton = createCustomButton("Leave Room");
        JButton logoutButton = createCustomButton("Logout");
        buttonPanel.add(createRoomButton);
        buttonPanel.add(joinRoomButton);
        buttonPanel.add(leaveRoomButton);
        buttonPanel.add(logoutButton);
        inputPanel.add(buttonPanel, BorderLayout.NORTH);

        // Text field panel with gap
        JPanel textFieldPanel = new JPanel(new BorderLayout());
        textFieldPanel.setBackground(backgroundColor);
        textFieldPanel.add(textField, BorderLayout.CENTER);
        textFieldPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.add(textFieldPanel, BorderLayout.SOUTH);

        JPanel chatPanel = new RoundedPanel(15, backgroundColor);
        chatPanel.setLayout(new BorderLayout());
        chatPanel.add(new JScrollPane(messagePane), BorderLayout.CENTER);
        chatPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(textColor), "Chat Area", 0, 0, font, textColor));

        JPanel roomPanel = new RoundedPanel(15, backgroundColor);
        roomPanel.setLayout(new BorderLayout());
        roomPanel.add(new JScrollPane(roomArea), BorderLayout.CENTER);
        roomPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(textColor), "Available Rooms", 0, 0, font, textColor));

        JPanel userPanel = new RoundedPanel(15, backgroundColor);
        userPanel.setLayout(new BorderLayout());
        userArea.setText("You are not in a room");
        userPanel.add(new JScrollPane(userArea), BorderLayout.CENTER);
        userPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(textColor), "Users in Room", 0, 0, font, textColor));

        JPanel roomButtonPanel = new JPanel(new GridLayout(4, 1, 10, 5));
        roomButtonPanel.setBackground(backgroundColor);
        JButton refreshRoomButton = createCustomButton("Refresh");
        JButton closeRoomButton = createCustomButton("Close Room");
        JButton kickUserButton = createCustomButton("Kick User");
        JButton clearChatButton = createCustomButton("Clear Chat");
        roomButtonPanel.add(clearChatButton);
        roomButtonPanel.add(refreshRoomButton);
        roomButtonPanel.add(closeRoomButton);
        roomButtonPanel.add(kickUserButton);
        roomPanel.add(roomButtonPanel, BorderLayout.SOUTH);
>>>>>>> Stashed changes

        // Add panels to frame
        frame.getContentPane().add(inputPanel, BorderLayout.SOUTH);
        frame.getContentPane().add(chatPanel, BorderLayout.CENTER);
        frame.getContentPane().setBackground(backgroundColor);
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Action listener for text field
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
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

    private JButton createCustomButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(70, 73, 75));
        button.setForeground(new Color(230, 230, 230));
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45)));
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Button padding
        button.setFocusPainted(false); // Remove focus border
        return button;
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
<<<<<<< Updated upstream
            if (line.startsWith("SUBMITNAME")) {
                out.println(getName());
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
            } else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");
=======
            if (line != null) {
                if (line.startsWith("SUBMITNAME")) {
                    String msgSubmitName = getName();
                    out.println(msgSubmitName);
                    userName=msgSubmitName;
                } else if (line.startsWith("NAMEACCEPTED")) {
                    textField.setEditable(true);
                } else if (line.startsWith("MESSAGE")) {
                    if(!line.substring(8).startsWith(userName)){
                        appendMessage(line.substring(8), StyleConstants.ALIGN_LEFT);
                    }
                } else if (line.startsWith("USERLIST")) {
                    userArea.setText(line.substring(9).replace(",", "\n"));
                } else if (line.startsWith("ROOMLIST")) {
                    updateRoomList(line.substring(9));
                } else if (line.startsWith("LOGOUT")) {
                    resetClient();
                    break;
                } else {
                    JOptionPane.showMessageDialog(frame, line, "!!!", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                frame.dispose();
                break;
>>>>>>> Stashed changes
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.run();
    }
}

// Custom JPanel class to create rounded corners and gradient background
class RoundedPanel extends JPanel {
    private int cornerRadius;
    private Color backgroundColor;

    public RoundedPanel(int cornerRadius, Color backgroundColor) {
        this.cornerRadius = cornerRadius;
        this.backgroundColor = backgroundColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
    }
}
