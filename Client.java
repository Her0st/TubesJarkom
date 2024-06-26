import javax.swing.*;
import javax.swing.text.*;
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
    JTextPane messagePane = new JTextPane();
    JTextArea roomArea = new JTextArea(8, 20);  
    JTextArea userArea = new JTextArea(8, 20);
    StyledDocument doc;

    public Client() {
        Color backgroundColor = new Color(45, 45, 45);
        Color textColor = new Color(230, 230, 230);
        Color textFieldColor = new Color(60, 63, 65);
        Font font = new Font("SansSerif", Font.PLAIN, 14);

        DefaultCaret caret = (DefaultCaret)messagePane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        textField.setEditable(false);
        textField.setBackground(textFieldColor);
        textField.setForeground(textColor);
        textField.setFont(font);
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

        
        JPanel inputPanel = new RoundedPanel(15, backgroundColor);
        inputPanel.setLayout(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        
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

        
        frame.getContentPane().add(inputPanel, BorderLayout.SOUTH);
        frame.getContentPane().add(chatPanel, BorderLayout.CENTER);
        frame.getContentPane().add(roomPanel, BorderLayout.EAST);
        frame.getContentPane().add(userPanel, BorderLayout.WEST);
        frame.getContentPane().setBackground(backgroundColor);
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        clearChatButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                messagePane.setText("");
            };
        });
        
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = textField.getText();
                out.println("/msg " + message);
                appendMessage("You: " + message, StyleConstants.ALIGN_RIGHT);
                textField.setText("");
            }
        });

        
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
                messagePane.setText("");
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

        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println("/logout");
            }
        });
    }

    private JButton createCustomButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(70, 73, 75));
        button.setForeground(new Color(230, 230, 230));
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45)));
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); 
        button.setFocusPainted(false); 
        return button;
    }


    private String getName() {
        return JOptionPane.showInputDialog(
                frame,
                "Choose a screen name:",
                "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);
    }

    private String getIP() {
        return JOptionPane.showInputDialog(
                frame,
                "Insert server IP:",
                "Server IP Input",
                JOptionPane.PLAIN_MESSAGE);
    }

    String userName;

    private void run() throws IOException {
        
        String serverAddress = getIP();
        Socket socket = new Socket(serverAddress, 5000);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        
        while (true) {
            String line = in.readLine();
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
                }else if(line.startsWith("LEAVE")){
                    out.println("/leave");
                    messagePane.setText("");
                } 
                else if (line.startsWith("LOGOUT")) {
                    resetClient();
                    break;
                } else {
                    JOptionPane.showMessageDialog(frame, line, "!!!", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                frame.dispose();
                break;
            }
        }
    }

    private void resetClient() {
        textField.setEditable(false);
        messagePane.setText("");
        roomArea.setText("");
        userArea.setText("You are not in a room");
        runClient();
    }

    private void runClient() {
        try {
            run();
        } catch (IOException e) {
            e.printStackTrace();
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
                    messagePane.setText("");
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "User limit must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void showJoinRoomDialog() {
        String roomName = JOptionPane.showInputDialog(frame, "Enter room name:", "Join Room", JOptionPane.PLAIN_MESSAGE);
        if (roomName != null && !roomName.trim().isEmpty()) {
            messagePane.setText("");
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

    private void appendMessage(String message, int alignment) {
        SimpleAttributeSet set = new SimpleAttributeSet();
        StyleConstants.setAlignment(set, alignment);
        StyleConstants.setForeground(set, Color.WHITE);
        StyleConstants.setFontFamily(set, "SansSerif");
        StyleConstants.setFontSize(set, 14);
        try {
            doc.setParagraphAttributes(doc.getLength(), 1, set, false);
            doc.insertString(doc.getLength(), message + "\n", set);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.runClient();
    }
}

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