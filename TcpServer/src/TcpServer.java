import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TcpServer {
    private static final int PORT = 12345;
    private static Set<String> users = new HashSet<>();
    private static List<String> history = new ArrayList<>();
    private static Set<PrintWriter> clients = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Servidor TCP iniciado...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String nickname;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                // Establecer el timeout para este socket (60 segundos)
                this.socket.setSoTimeout(60000);  // 60 segundos
            } catch (SocketException e) {
                System.err.println("Error al establecer el timeout: " + e.getMessage());
            }
        }

        public void run() {
            try {
                // Establecer la entrada y salida del cliente
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                System.out.println("Esperando que el cliente ingrese su nickname...");
                synchronized (users) {
                    out.println("Ingrese su nickname:");
                    nickname = in.readLine();

                    // Verificar que el nickname no sea nulo o vacío
                    while (nickname == null || nickname.trim().isEmpty() || users.contains(nickname)) {
                        if (nickname == null || nickname.trim().isEmpty()) {
                            out.println("El nickname no puede estar vacío. Intenta nuevamente.");
                        } else {
                            out.println("El nickname ya está en uso, elija otro:");
                        }
                        nickname = in.readLine();  // Leer el nickname de nuevo
                    }

                    users.add(nickname);
                    clients.add(out);
                    System.out.println("Cliente conectado con el nickname: " + nickname);
                }

                out.println("Bienvenido al chat, " + nickname);

                // Enviar el historial de mensajes al nuevo cliente
                synchronized (history) {
                    for (String msg : history) {
                        out.println(msg);
                    }
                }

                // Leer y transmitir los mensajes del cliente
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.trim().isEmpty()) {
                        break;
                    }

                    String formattedMessage = nickname + ": " + message;

                    synchronized (history) {
                        history.add(formattedMessage);
                    }

                    synchronized (clients) {
                        for (PrintWriter client : clients) {
                            client.println(formattedMessage);
                        }
                    }
                }

            } catch (IOException e) {
                System.err.println("Error de comunicación con el cliente " + nickname);
                e.printStackTrace();
            } finally {
                // Limpieza de los recursos y cierre del socket
                synchronized (users) {
                    if (nickname != null) {
                        users.remove(nickname);
                    }
                }
                synchronized (clients) {
                    clients.remove(out);
                }

                try {
                    if (socket != null && !socket.isClosed()) {
                        System.out.println("Cerrando el socket para el cliente: " + nickname);
                        socket.close();
                    }
                } catch (IOException e) {
                    System.err.println("Error al cerrar el socket para el cliente: " + nickname);
                    e.printStackTrace();
                }
            }
        }
    }
}