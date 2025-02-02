import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class UdpCliente {
    private static final int SERVER_PORT = 12346;
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private JFrame frame;
    private JTextArea messageArea;
    private JTextField inputField;

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
        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.add(inputField, BorderLayout.SOUTH);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        inputField.addActionListener(this::sendMessage);
    }

    private void sendMessage(ActionEvent e) {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            try {
                byte[] buffer = message.getBytes();
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
