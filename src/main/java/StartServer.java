import java.io.IOException;

/**
 * Created by nano on 15.10.15.
 */
public class StartServer {

    private static final int SIZE_BLOCKING_QUEUE = 100;
    private static final int THREAD_POOL_SIZE = 4;
    private static final int PORT = 8080;

    public static void main(String args[]) throws IOException {
        int port = PORT;

        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        System.out.println("WebServer started on " + port + " port");

        ThreadPool threadPool =
                new ThreadPool(THREAD_POOL_SIZE, SIZE_BLOCKING_QUEUE);

        WebQueue server = new WebQueue(port);
        server.start(threadPool);
    }
}
