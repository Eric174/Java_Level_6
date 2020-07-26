package NIO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

public class NIOServer implements Runnable {
    private ServerSocketChannel server;
    private Selector selector;
    private Path path = Paths.get("./common/src/main/resources/serverFiles");

    public NIOServer() throws IOException {
        server = ServerSocketChannel.open();
        server.socket().bind(new InetSocketAddress(8189));
        server.configureBlocking(false);
        selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {
        try {
            System.out.println("server started");
            while (server.isOpen()) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        System.out.println("client accepted");
                        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ);
                        //channel.write(ByteBuffer.wrap("Hello!".getBytes()));
                    }
                    if (key.isReadable()) {
                        // TODO: 7/23/2020 fileStorage handle
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        System.out.println("read key");
                        SocketChannel channel = (SocketChannel) key.channel();
                        try {
                            int count = channel.read(buffer);
                        } catch (IOException e) {
                            break;
                        }
                        buffer.flip();
                        StringBuilder command = new StringBuilder();
                        while (buffer.hasRemaining()) {
                            command.append((char)buffer.get());
                        }
                        System.out.println(command);
                        String [] op = command.toString().split(" ");
                        if (op[0].equals("./download")) {
                            System.out.println("download");
                            Path fileName = Paths.get(path + "/" + op[1]);
                            System.out.println("find file with name: " + fileName);
                            if (Files.exists(fileName)) {
                                RandomAccessFile fileR = new RandomAccessFile(fileName.toString(), "rw");
                                //os.writeUTF("OK");
                                channel.write(ByteBuffer.wrap("OK".getBytes()));
                                //os.writeLong(len);
                                System.out.println("send length");
                                long len = fileR.length();
                                String lenStr = Long.toString(len);
                                //ByteBuffer longBuffer = ByteBuffer.allocate(Long.BYTES);
                                //longBuffer.putLong(len);
                                //longBuffer.flip();
                                System.out.println(len);
                                //channel.write(longBuffer);
                                channel.write(ByteBuffer.wrap(lenStr.getBytes()));
                                System.out.println("create filechannel");
                                FileChannel fileChannel = fileR.getChannel();
                                int i = 0;
                                StringBuilder answer = new StringBuilder();
                                //ожидаем ответа клиента получения и обработки длины файла, иначе будет ошибка на клиенте
                                while(!answer.toString().equals("GETFILE")) {
                                    answer.setLength(0);
                                    buffer.clear();
                                    channel.read(buffer);
                                    buffer.flip();
                                    while (buffer.hasRemaining()) {
                                        answer.append((char) buffer.get());
                                    }
                                    if (answer.length() > 0) {
                                        System.out.println(answer);
                                    }
                                }
                                if (answer.toString().equals("GETFILE")) {
                                    System.out.println("Sending file");
                                    buffer.clear();
                                    while (fileChannel.read(buffer) > 0) {
                                        System.out.println("send part " + ++i);
                                        buffer.flip();
                                        channel.write(buffer);
                                        buffer.clear();
                                    }
                                    fileChannel.close();
                                }
                                /*
                                FileInputStream fis = new FileInputStream(file);
                                while (fis.available() > 0) {
                                    count = fis.read(buffer.toString().getBytes());
                                    os.write(buffer, 0, count);
                                }
                                */
                            } else {
                                channel.write(ByteBuffer.wrap("File not exists".getBytes()));
                                //os.writeUTF("File not exists");
                            }
                        } else {
                            // TODO: 7/23/2020 upload
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new Thread(new NIOServer()).start();
    }
}
