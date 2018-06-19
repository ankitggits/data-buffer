package no.sample.solution2;

import rx.functions.Action1;

import java.util.List;

public class Consumer implements Action1<List<String>> {
    @Override
    public void call(List<String> randomStrings) {
        randomStrings.forEach((k) -> System.out.println(String.format("Consumed item %s with  thread name %s", k, Thread.currentThread().getName())));
    }
}
