import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class TcpCliente {
    private JFrame frame;
    private JTextArea messageArea;
    private JTextField inputField;
    private PrintWriter out;

    public TcpCliente() {
        frame = new JFrame("Chat TCP");
        messageArea = new JTextArea(20, 50);
        messageArea.setEditable(false);
        inputField = new JTextField(50);

        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.add(inputField, BorderLayout.SOUTH);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);



        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Enviar un nickname
            inputField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    out.println(inputField.getText());
                    inputField.setText("");
                }
            });
            //out.println("cliente1");

            // Recibir mensajes del servidor
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                System.out.println(serverMessage);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

//        try (Socket socket = new Socket("localhost", 12345);
//            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
//            out = new PrintWriter(socket.getOutputStream(), true);
//            new Thread(() -> {
//                String serverMsg;
//                try {
//                    while ((serverMsg = in.readLine()) != null) {
//                        messageArea.append(serverMsg + "\n");
//                    }
//                } catch (IOException ex) {
//                    System.out.println("Conexión cerrada por el servidor.");
//                }
//            }).start();
//        } catch (IOException e) {
//            System.out.println("adios");
//            e.printStackTrace();
//        }
    }

    public static void main(String[] args) {
        new TcpCliente();
    }
}
