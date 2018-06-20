package no.sample.solution2;

import reactor.core.publisher.WorkQueueProcessor;

import java.util.UUID;

public class Producer implements Runnable {
    private final WorkQueueProcessor<String> unicastProcessor;

    public Producer(WorkQueueProcessor<String> unicastProcessor) {
        this.unicastProcessor = unicastProcessor;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            String randomString = UUID.randomUUID().toString();
            try {
                unicastProcessor.onNext(randomString);
                System.out.println(String.format("Produced : %s on thread %s", randomString, Thread.currentThread().getName()));
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
