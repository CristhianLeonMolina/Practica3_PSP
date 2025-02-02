import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.ActionEvent;

class UdpServer {
    private static final int PORT = 12346;
    private static List<String> history = new ArrayList<>();
    private static Set<InetSocketAddress> clients = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Servidor UDP iniciado...");
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                InetSocketAddress clientAddress = new InetSocketAddress(packet.getAddress(), packet.getPort());
                synchronized (clients) {
                    clients.add(clientAddress);
                }
                synchronized (history) {
                    history.add(message);
                }
                byte[] sendData = message.getBytes();
                synchronized (clients) {
                    for (InetSocketAddress client : clients) {
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, client);
                        socket.send(sendPacket);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error en el servidor UDP: " + e.getMessage());
            e.printStackTrace();
        }
    }
}