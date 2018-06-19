package no.sample.solution2;

import reactor.core.publisher.UnicastProcessor;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestSolution2 {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(100);
        UnicastProcessor<String> unicastProcessor = UnicastProcessor.create(blockingQueue);
        startTask(new Producer(unicastProcessor));

        unicastProcessor
                .buffer(3)
                .subscribe((s) -> {
                    System.out.println(String.format("%s on thread %s", s, Thread.currentThread().getName()));
                });

    }

    private static void startTask(Runnable task) {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 3; i++) {
            executor.submit(task);
        }
    }
}
