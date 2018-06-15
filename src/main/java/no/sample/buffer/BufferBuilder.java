package no.sample.buffer;

import rx.functions.Action1;
import rx.subjects.PublishSubject;

import java.util.List;

public class BufferBuilder {

    public static <T> BufferPublisher<T> build(Action1<List<T>> action, BufferStats<T> bufferStats){
        PublishSubject<T> subject = PublishSubject.create();
        subject.lift(new BufferOperator<>(bufferStats)).subscribe(action, Throwable::printStackTrace);
        return new BufferPublisher<T>(subject);
    }
}
