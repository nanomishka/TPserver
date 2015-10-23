import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by nano on 15.10.15.
 */
public class ThreadPool {

    private BlockingQueue<Runnable> blockQueue;
    public List<Worker> workers = new ArrayList<Worker>();

    public ThreadPool(int numberOfThreads, int maxSizeOfBlockQueue) {

        blockQueue = new LinkedBlockingQueue<Runnable>(maxSizeOfBlockQueue);
        for (int i = 0; i < numberOfThreads; ++i) {
            Worker worker = new Worker(blockQueue);
            worker.start();
            workers.add(worker);
            System.out.println("Запущен worker: " + i);
        }
    }

    public synchronized void execute (Runnable task) throws Exception {
        synchronized (blockQueue) {
            blockQueue.add(task);
            blockQueue.notify();
        }
    }
}
