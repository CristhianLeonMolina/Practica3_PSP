import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;

class TcpCliente {
    private JFrame frame;
    private JTextArea messageArea;
    private JTextField inputField;
    private PrintWriter out;
    private BufferedReader in;

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

        inputField.addActionListener(e -> {
            String message = inputField.getText().trim();
            if (!message.isEmpty()) {
                out.println(message);
                inputField.setText("");
            }
        });

        try {
            Socket socket = new Socket("localhost", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            new Thread(() -> {
                try {
                    String serverMsg;
                    while ((serverMsg = in.readLine()) != null) {
                        String finalMsg = serverMsg; // Necesario para usar en la función lambda
                        SwingUtilities.invokeLater(() -> messageArea.append(finalMsg + "\n"));
                    }
                } catch (IOException ex) {
                    System.out.println("Conexión cerrada por el servidor.");
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        new TcpCliente();
    }
}
