import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public ListView<String> lv;
    public TextField txt;
    public Button send;
    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    private final String clientFilesPath = "./common/src/main/resources/clientFiles";
    private final int bufferSize = 1024;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        File dir = new File(clientFilesPath);
        for (String file : dir.list()) {
            lv.getItems().add(file);
        }
    }

    // ./download fileName
    // ./upload fileName
    public void sendCommand(ActionEvent actionEvent) {
        String command = txt.getText();
        String [] op = command.split(" ");
        if (op[0].equals("./download")) {
            try {
                os.write(command.getBytes());
                //os.writeUTF(op[0]);
                //os.writeUTF(op[1]);
                //String response = is.readUTF();
                byte[] buffer = new byte[bufferSize];
                is.read(buffer);
                String response = new String(buffer).trim();
                System.out.println("response: " + response);
                if (response.equals("OK")) {
                    File file = new File(clientFilesPath + "/" + op[1]);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    System.out.println("file create");
                    buffer = new byte[bufferSize];
                    is.read(buffer);
                    /*
                    String bufferStr = Arrays.toString(buffer);
                    String[] strArray = bufferStr.substring(1, bufferStr.length() - 1).split(",");
                    int endBuffer = strArray.length;
                    for (int i = 0; i < strArray.length; i++) {
                        if (strArray[i].trim().equals("0")) {
                            System.out.println("i = " + i);
                            endBuffer = i;
                            break;
                        }
                    }
                    */
                    //ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
                    //byteBuffer = ByteBuffer.wrap(buffer);
                    //byteBuffer.flip();
                    //byteBuffer.put(buffer);
                    //long len = byteBuffer.getLong();
                    String lenStr = new String(buffer).trim();
                    //String lenStr = new String(buffer, 0, endBuffer);

                    System.out.println(lenStr);
                    long len = Long.parseLong(lenStr);
                    System.out.println("File size is: " + len);
                    buffer = new byte[bufferSize];
                    os.write("GETFILE".getBytes());
                    try(FileOutputStream fos = new FileOutputStream(file)) {
                        if (len < bufferSize) {
                            int count = is.read(buffer);
                            fos.write(buffer, 0, count);
                        } else {
                            for (long i = 0; i < len / bufferSize; i++) {
                                int count = is.read(buffer);
                                fos.write(buffer, 0, count);
                            }
                        }
                    }
                    lv.getItems().add(op[1]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
           //upload
        }
    }
}
