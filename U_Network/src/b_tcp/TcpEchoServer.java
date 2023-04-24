package b_tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * 两个socket，一个专门负责建立连接，一个负责数据通信
 */
public class TcpEchoServer {
    private ServerSocket serverSocket = null;

    public TcpEchoServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void start() throws IOException {
        System.out.println("服务器启动！");
        while (true) {
            // 需要先建立连接（如果没有客户端来建立连接，当前的 accept() 就会阻塞）
            // accept 返回了一个 socket 对象，称为 clientSocket，后续和客户端之间的的沟通，都是通过 clientSocket 来完成的
            Socket clientSocket = serverSocket.accept();
            processConnection(clientSocket);
        }
    }

    private void processConnection(Socket clientSocket) {
        System.out.printf("[%s:%d] 客户端建立连接！", clientSocket.getInetAddress(), clientSocket.getPort());
        // 接下来处理请求和响应
        // 针对 TCP socket 进行读写
        try (InputStream inputStream = clientSocket.getInputStream()) {
            try (OutputStream outputStream = clientSocket.getOutputStream()) {
                // 循环地处理每个请求，分别返回响应
                Scanner scanner = new Scanner(inputStream);
                while (true) {
                    // 1.读取请求
                    if (!scanner.hasNextLine()) {
                        System.out.printf("[%s:%d] 客户端断开连接！", clientSocket.getInetAddress(), clientSocket.getPort());
                        break;
                    }
                    // 此处用 Scanner 更方便，如果不用 Scanner 就用原生的 InputStream 的 read 也是可以的
                    String request = scanner.next();
                    // 2.根据请求，计算响应
                    String response = process(request);

                    // 3. 把这个响应返回给客户端
                    // 为了方便起见，可以使用 PrintWriter 把 OutputStream 包裹一下
                    PrintWriter printWriter = new PrintWriter(outputStream);
                    printWriter.println(response);
                    // 刷新缓冲区，如果没有这个刷新，可能客户端就不能第一时间看到响应结果
                    printWriter.flush();
                    System.out.printf("[%s:%d] req: %s, resp: %s\n", clientSocket.getInetAddress().toString(),
                            clientSocket.getPort(), request, response);
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 此处要记得关闭连接
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String process(String request) {
        return request;
    }


    public static void main(String[] args) throws IOException {
        TcpEchoServer server = new TcpEchoServer(9090);
        server.start();
    }
}
