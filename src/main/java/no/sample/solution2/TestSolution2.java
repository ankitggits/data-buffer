package no.sample.solution2;

import reactor.core.publisher.WorkQueueProcessor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestSolution2 {
    public static void main(String[] args) throws InterruptedException {
        WorkQueueProcessor<String> workQueueProcessor = WorkQueueProcessor
                .<String>builder()
                .requestTaskExecutor(Executors.newFixedThreadPool(3))
                .bufferSize(64)
                .build();

        workQueueProcessor
                .buffer(3)
                .subscribe(TestSolution2::loggingSubsciber);

        workQueueProcessor
                .buffer(5)
                .subscribe(TestSolution2::loggingSubsciber);

        workQueueProcessor
                .buffer(8)
                .subscribe(TestSolution2::loggingSubsciber);

        startTask(new Producer(workQueueProcessor));
    }

    private static void loggingSubsciber(List<String> s) {
        String name = Thread.currentThread().getName();
        Pattern pattern = Pattern.compile("(?:.*)?-(\\d+)$");
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            Thread.currentThread().setName(String.format("Client-pool-%s", matcher.group(1)));
        }
        System.out.println(String.format("%s on thread %s", s, Thread.currentThread().getName()));
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void startTask(Runnable task) {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 3; i++) {
            executor.submit(task);
        }
    }
}
