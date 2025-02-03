import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;

public class UdpCliente {
    private static final int SERVER_PORT = 12346;
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private JFrame frame;
    private JTextArea messageArea;
    private JTextField inputField;
    private JTextField nameField;
    private String nickname;
    private boolean nameSet = false;

    public UdpCliente() {
        try {
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName("localhost");
            initializeGUI();
            new Thread(this::receiveMessages).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeGUI() {
        frame = new JFrame("Chat UDP");
        messageArea = new JTextArea(20, 50);
        messageArea.setEditable(false);
        inputField = new JTextField(50);
        nameField = new JTextField("Ingrese su nickname aquí", 20);
        frame.setLayout(new BorderLayout());
        frame.add(nameField, BorderLayout.NORTH);
        frame.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.add(inputField, BorderLayout.SOUTH);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        nameField.addActionListener(this::setNickname);
        inputField.addActionListener(this::sendMessage);
    }

    private void setNickname(ActionEvent e) {
        if (!nameSet) {
            nickname = nameField.getText().trim();
            if (!nickname.isEmpty()) {
                nameSet = true;
                nameField.setEditable(false);
            }
        }
    }

    private void sendMessage(ActionEvent e) {
        if (!nameSet) {
            return;
        }
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            try {
                String fullMessage = nickname + ": " + message;
                byte[] buffer = fullMessage.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, SERVER_PORT);
                socket.send(packet);
                inputField.setText("");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void receiveMessages() {
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                SwingUtilities.invokeLater(() -> messageArea.append(message + "\n"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new UdpCliente();
    }
}