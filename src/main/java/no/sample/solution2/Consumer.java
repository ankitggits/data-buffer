package no.sample.solution2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Consumer implements Runnable {
    private final BlockingQueue<String> blockingQueue;

    public Consumer(BlockingQueue<String> blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            List<String> randomStrings = new ArrayList<>();
            blockingQueue.drainTo(randomStrings, 3);
            randomStrings.forEach((k) -> System.out.println(String.format("Consumed item %s with  thread name %s", k, Thread.currentThread().getName())));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
