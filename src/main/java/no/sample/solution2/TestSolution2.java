package no.sample.solution2;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestSolution2 {
    public static void main(String[] args) {
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(100);
        startTask(new Producer(blockingQueue), 3);
        startTask(new Consumer(blockingQueue), 1);
    }

    private static void startTask(Runnable task, int size) {
        ExecutorService executor = Executors.newFixedThreadPool(size);
        for (int i = 0; i < size; i++) {
            executor.submit(task);
        }
    }
}
