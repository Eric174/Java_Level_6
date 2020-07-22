import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
    private Socket socket;
    private int idClient;

    public Server(Socket socket, int id) {
        this.socket = socket;
        this.idClient = id;
    }

    public void run() {
        try {
            File clientDir = new File("./server/Client"+idClient);
            clientDir.mkdir();
            DataInputStream is = new DataInputStream(socket.getInputStream());
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            os.writeUTF(String.valueOf(idClient));
            while (true) {
                String[] query = is.readUTF().split(" ");
                String command = query[0];

                if (command.equalsIgnoreCase("get")) { //выгрузить файл

                } else if (command.equalsIgnoreCase("load")) { //загрузить файл
                    String fileName = query[1];
                    File file = new File(clientDir.getPath() + "/" + fileName);
                    file.createNewFile();
                    FileOutputStream fileOs = new FileOutputStream(file);
                    byte[] buffer = new byte[8192];
                    int part;
                    int i = 0;
                    while((part = is.read(buffer)) != -1) {
                        fileOs.write(buffer, 0, part);
                        System.out.println(i++);
                    }
                    fileOs.close();
                } else if (command.equalsIgnoreCase("info")) { //список файлов на сервере

                } else if (command.equalsIgnoreCase("quit")) {
                    socket.close();
                    System.out.println("Client with id: " + idClient + " exit");
                    break;
                } else {
                    System.out.println("Command: " + command + " unknown");
                }
            }
            is.close();
            os.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(8189);
        int con_count = 0; // счетчик поключений
        File serverDir = new File("./server");
        if (!serverDir.exists()) {
            serverDir.mkdir();
        }
        while (true) {
            Socket socket = server.accept();
            new Server(socket, con_count++).start();
        }
    }
}