import java.util.concurrent.BlockingQueue;

/**
 * Created by nano on 15.10.15.
 */
public class Worker extends Thread {

    private boolean isStopped = false;
    private BlockingQueue<Runnable> blockQueue;
    private static int nextId = 0;
    private int id;

    public Worker(BlockingQueue blockQueue) {
        this.blockQueue = blockQueue;
        id = nextId++;
    }


    @Override
    public void run() {
        while (!isStopped) {

            Runnable task;

            synchronized (blockQueue) {
                while (blockQueue.isEmpty()) {
                    try {
                        blockQueue.wait();
                    } catch (InterruptedException e ) {
                        System.err.println(e);
                    }
                }
                task = blockQueue.poll();
            }
            try {
                task.run();
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    public int getIdThread() {
        return id;
    }
}
