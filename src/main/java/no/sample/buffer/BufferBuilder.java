package no.sample.buffer;

import rx.Observable;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

import java.util.List;

public class BufferBuilder {

    public static <T> BufferPublisher<T> build(Action1<List<T>> action, BufferStats<T> bufferStats){
        BufferOperator<T> bufferUntilOperator = new BufferOperator<>(bufferStats);
        PublishSubject<T> subject = PublishSubject.create();
        Observable<List<T>> lift = subject.lift(bufferUntilOperator);
        lift.subscribe(action, Throwable::printStackTrace);
        return new BufferPublisher<T>(subject);
    }
}
