import java.io.*;
import java.net.*;

public class FileClient {
    public static void main(String[] args) throws IOException {
        String serverAddress = "127.0.0.1";
        int serverPort = 9999;

        try (
                Socket socket = new Socket(serverAddress, serverPort);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))
        ) {
        
            System.out.println("Escolha a opção:");
            System.out.println("1. Upload");
            System.out.println("2. Download");
            System.out.println("3. Delete");
            System.out.println("4. Sair");
            System.out.print("Opção: ");
            String option = userInput.readLine();

            switch (option) {
                case "1":
                    uploadFile(userInput, out);
                    break;
                case "2":
                    downloadFile(userInput, out, in);
                    break;
                case "3":
                    deleteFile(userInput, out, in);
                    break;
                case "4":
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        } catch (UnknownHostException e) {
            System.err.println("Host não encontrado" + serverAddress);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("conexão falhou " + serverAddress);
            System.exit(1);
        }
    }

    private static void uploadFile(BufferedReader userInput, PrintWriter out) throws IOException {
        System.out.print("Digite o caminho do arquivo a ser enviado: ");
        String filePath = userInput.readLine();
        File fileToUpload = new File(filePath);
        if (!fileToUpload.exists()) {
            System.out.println("Arquivo não encontrado.");
            return;
        }
        out.println("UPLOAD " + fileToUpload.getName());
        try (BufferedReader reader = new BufferedReader(new FileReader(fileToUpload))) {
            String line;
            while ((line = reader.readLine()) != null) {
                out.println(line);
            }
        }
        out.println("END");
        System.out.println("Arquivo enviado com sucesso.");
    }

    private static void downloadFile(BufferedReader userInput, PrintWriter out, BufferedReader in) throws IOException {
        System.out.print("Digite o nome do arquivo a ser baixado: ");
        String fileName = userInput.readLine();
        out.println("DOWNLOAD " + fileName);
        String response = in.readLine();
        if (response.equals("FILE_NOT_FOUND")) {
            System.out.println("Arquivo não encontrado no servidor.");
            return;
        }
        File file = new File("client_files/" + fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            String line;
            while ((line = in.readLine()) != null && !line.equals("END")) {
                writer.write(line);
                writer.newLine();
            }
        }
        System.out.println("Arquivo baixado com sucesso.");
    }

    private static void deleteFile(BufferedReader userInput, PrintWriter out, BufferedReader in) throws IOException {
        System.out.print("Digite o nome do arquivo a ser excluído: ");
        String fileName = userInput.readLine();
        out.println("DELETE " + fileName);
        String response = in.readLine();
        System.out.println("Resposta do servidor: " + response);
    }
}
