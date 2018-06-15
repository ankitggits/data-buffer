package no.sample.buffer;

import lombok.Synchronized;
import rx.Observable;
import rx.Subscriber;

import java.util.ArrayList;
import java.util.List;

class EventDataBufferOperator<T> implements Observable.Operator<List<T>, T> {

    private final BufferStats<T> bufferStats;

    EventDataBufferOperator(BufferStats<T> bufferStats) {
        this.bufferStats = bufferStats;
    }

    @Override
    public Subscriber<? super T> call(Subscriber<? super List<T>> delegate) {
        BufferWhileSubscriber parent = new BufferWhileSubscriber(delegate, bufferStats);
        delegate.add(parent);
        return parent;
    }

    private final class BufferWhileSubscriber extends Subscriber<T> {

        private final Subscriber<? super List<T>> actual;
        private final BufferStats<T> bufferStats;
        private List<T> buffer = new ArrayList<>();

        private BufferWhileSubscriber(Subscriber<? super List<T>> actual, BufferStats<T> bufferStats) {
            this.actual = actual;
            this.bufferStats = bufferStats;
        }

        @Synchronized
        @Override
        public void onNext(T t) {
            buffer.add(t);
            bufferStats.append(t);
            if (bufferStats.isFull()) {
                actual.onNext(buffer);
                buffer = new ArrayList<>();
                bufferStats.reset();
            }
        }

        @Override
        public void onError(Throwable e) {
            this.buffer = null;
            actual.onError(e);
        }

        @Override
        public void onCompleted() {
            List<T> b = buffer;
            this.buffer = null;
            if (!b.isEmpty()) {
                actual.onNext(b);
            }
            actual.onCompleted();
        }
    }
}