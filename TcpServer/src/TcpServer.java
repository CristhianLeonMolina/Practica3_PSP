import java.io.*;
import java.net.*;
import java.util.*;

public class TcpServer {
    private static final int PORT = 12345;
    public static Set<String> users = new HashSet<>();
    public static List<String> history = new ArrayList<>();
    public static Set<PrintWriter> clients = new HashSet<>();

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
}