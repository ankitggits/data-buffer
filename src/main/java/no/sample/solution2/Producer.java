package no.sample.solution2;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class Producer implements Runnable {
    private final BlockingQueue<String> blockingQueue;

    public Producer(BlockingQueue<String> blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            String randomString = UUID.randomUUID().toString();
            try {
                blockingQueue.put(randomString);
                System.out.println(String.format("Produced : %s on thread %s", randomString, Thread.currentThread().getName()));
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
