package no.sample.buffer;

import rx.functions.Action1;
import rx.subjects.PublishSubject;

import java.util.List;

public class BufferBuilder {

    public static <T> BufferPublisher<T> build(Action1<List<T>> action, BufferCondition<T> bufferCondition){
        PublishSubject<T> subject = PublishSubject.create();
        subject.lift(new BufferOperator<>(bufferCondition)).subscribe(action, Throwable::printStackTrace);
        return new BufferPublisher<T>(subject);
    }
}
