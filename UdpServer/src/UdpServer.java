import java.io.*;
import java.net.*;
import java.util.*;

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
                    byte[] mensajes = message.getBytes();
                    DatagramPacket historyPacket = new DatagramPacket(mensajes, mensajes.length, clientAddress);
                    socket.send(historyPacket);
                }

                // Si el cliente solicita el historial
                if (message.startsWith("NEW_CLIENT")) {
                    // Enviar historial solo si el cliente lo solicita
                    synchronized (history) {
                        for (String oldMessage : history) {
                            byte[] historyData = oldMessage.getBytes();
                            DatagramPacket historyPacket = new DatagramPacket(historyData, historyData.length, clientAddress);
                            socket.send(historyPacket);
                        }
                    }
                }

                synchronized (history) {
                    history.add(message);
                }

                byte[] sendData = message.getBytes();
                if (!message.equals("NEW_CLIENT")) { // No reenviar el mensaje NEW_CLIENT a otros clientes
                    synchronized (clients) {
                        for (InetSocketAddress client : clients) {
                            if (!client.equals(clientAddress)) { // Evita reenviar al emisor
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, client);
                                socket.send(sendPacket);
                            }
                        }
                    }
                }

            }
        } catch (IOException e) {
            System.out.println("Error en el servidor UDP: " + e.getMessage());
            e.printStackTrace();
        }
    }
}