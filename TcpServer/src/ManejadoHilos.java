import java.io.*;
import java.net.*;

public class ManejadoHilos extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname;

    public ManejadoHilos(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            // Establecer la entrada y salida del cliente
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Esperando que el cliente ingrese su nickname...");
            synchronized (TcpServer.users) {
                out.println("Ingrese su nickname:");
                nickname = in.readLine();

                // Verificar que el nickname no sea nulo o vacío
                while (nickname == null || nickname.trim().isEmpty() || TcpServer.users.contains(nickname)) {
                    if (nickname == null || nickname.trim().isEmpty()) {
                        out.println("El nickname no puede estar vacío. Intenta nuevamente.");
                    } else {
                        out.println("El nickname ya está en uso, elija otro:");
                    }
                    nickname = in.readLine();  // Leer el nickname de nuevo
                }

                TcpServer.users.add(nickname);
                TcpServer.clients.add(out);
                System.out.println("Cliente conectado con el nickname: " + nickname);
            }

            out.println("Bienvenido al chat, " + nickname);

            // Enviar el historial de mensajes al nuevo cliente
            synchronized (TcpServer.history) {
                for (String msg : TcpServer.history) {
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

                synchronized (TcpServer.history) {
                    TcpServer.history.add(formattedMessage);
                }

                synchronized (TcpServer.clients) {
                    for (PrintWriter client : TcpServer.clients) {
                        client.println(formattedMessage);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Error de comunicación con el cliente " + nickname);
            e.printStackTrace();
        } finally {
            // Limpieza de los recursos y cierre del socket
            synchronized (TcpServer.users) {
                if (nickname != null) {
                    TcpServer.users.remove(nickname);
                }
            }
            synchronized (TcpServer.clients) {
                TcpServer.clients.remove(out);
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