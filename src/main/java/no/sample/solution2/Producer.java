package no.sample.solution2;

import reactor.core.publisher.UnicastProcessor;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class Producer implements Runnable {
    private final UnicastProcessor<String> unicastProcessor;

    public Producer(UnicastProcessor<String> unicastProcessor) {
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
