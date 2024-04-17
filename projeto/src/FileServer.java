import java.io.*;
import java.net.*;

public class FileServer {
    private static final String UPLOAD_DIR = "server_files/";

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(9999);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 9999.");
            System.exit(1);
        }

        Socket clientSocket = null;
        System.out.println("Server is running. Waiting for connections...");

        while (true) {
            try {
                clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostName());

                // Handling client request in a separate thread
                Thread clientHandler = new ClientHandler(clientSocket);
                clientHandler.start();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                // Input and output streams
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Read client request
                String request = in.readLine();
                String[] parts = request.split(" ");
                String command = parts[0];
                String fileName = parts[1];

                // Handle client request
                switch (command) {
                    case "UPLOAD":
                        uploadFile(fileName, in);
                        out.println("File uploaded successfully.");
                        break;
                    case "DOWNLOAD":
                        downloadFile(fileName, out);
                        break;
                    case "DELETE":
                        deleteFile(fileName, out);
                        break;
                    default:
                        out.println("Invalid command.");
                }

                // Close streams and socket
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void uploadFile(String fileName, BufferedReader in) throws IOException {
            String filePath = UPLOAD_DIR + fileName;
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                String line;
                while ((line = in.readLine()) != null && !line.equals("END")) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }

        private void downloadFile(String fileName, PrintWriter out) throws IOException {
            String filePath = UPLOAD_DIR + fileName;
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    out.println(line);
                }
            }
            out.println("END"); // Signal end of file
        }

        private void deleteFile(String fileName, PrintWriter out) {
            String filePath = UPLOAD_DIR + fileName;
            File file = new File(filePath);
            if (file.exists() && file.delete()) {
                out.println("File deleted successfully.");
            } else {
                out.println("Failed to delete file.");
            }
        }
    }
}
