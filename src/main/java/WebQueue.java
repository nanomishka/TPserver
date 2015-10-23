import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by nano on 15.10.15.
 */
public class WebQueue {

    private ServerSocket serverSocket;
    private Socket socket;

    public WebQueue(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void start(ThreadPool poolThread) {
        try {
            while(true) {
                socket = serverSocket.accept(); 
//                poolThread.execute(new HTTPConnector(socket));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }





    }
}
