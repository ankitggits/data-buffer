package no.sample.buffer;

import lombok.Synchronized;
import rx.subjects.PublishSubject;

public class BufferPublisher<T> {

    private final PublishSubject<T> subject;

    BufferPublisher(PublishSubject<T> subject) {
        this.subject = subject;
    }

    //TODO: remove sync
    @Synchronized
    public void publish(T t){
        subject.onNext(t);
    }

    public void finish(){
        subject.onCompleted();
    }
}
