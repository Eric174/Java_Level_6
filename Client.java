import java.io.*;
import java.net.Socket;

public class Client {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1", 8189);
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        DataInputStream is = new DataInputStream(socket.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int idClient = Integer.parseInt(is.readUTF());
        String path = "./" + idClient;
        File clientDir = new File(path);
        if (!clientDir.exists()) {
            clientDir.mkdir();
        }
        String fileName;

        while (true) {
            System.out.println("Command list: \"get\" or \"load\"_\"file name\", \"info\", \"quit\"");
            String query = reader.readLine();
            String command = query.split(" ")[0];

            if (command.equalsIgnoreCase("get")) {

            } else if (command.equalsIgnoreCase("load")) {
                fileName = query.split(" ")[1];
                FileInputStream fileIs = new FileInputStream(new File(path + "/" + fileName));
                os.writeUTF(command + " " + fileName);
                byte[] buffer = new byte[8192];
                int part;
                while ((part = fileIs.read(buffer)) != -1) {
                    os.write(buffer, 0, part);
                }
            } else if (command.equalsIgnoreCase("info")) {

            } else if (command.equalsIgnoreCase("quit")) {
                break;
            } else {
                System.out.println("Command: " + command + " unknown");
            }
        }
        is.close();
        os.close();
    }
}
