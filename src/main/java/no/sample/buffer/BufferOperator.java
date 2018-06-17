package no.sample.buffer;

import rx.Observable;
import rx.Subscriber;

import java.util.ArrayList;
import java.util.List;

class BufferOperator<T> implements Observable.Operator<List<T>, T> {

    private final BufferCondition<T> bufferCondition;

    BufferOperator(BufferCondition<T> bufferCondition) {
        this.bufferCondition = bufferCondition;
    }

    @Override
    public Subscriber<? super T> call(Subscriber<? super List<T>> delegate) {
        BufferWhileSubscriber parent = new BufferWhileSubscriber(delegate, bufferCondition);
        delegate.add(parent);
        return parent;
    }

    private final class BufferWhileSubscriber extends Subscriber<T> {

        private final Subscriber<? super List<T>> actual;
        private final BufferCondition<T> bufferCondition;
        private List<T> buffer = new ArrayList<>();

        private BufferWhileSubscriber(Subscriber<? super List<T>> actual, BufferCondition<T> bufferCondition) {
            this.actual = actual;
            this.bufferCondition = bufferCondition;
        }

        @Override
        public void onNext(T t) {
            buffer.add(t);
            bufferCondition.append(t);
            if (bufferCondition.isFull()) {
                actual.onNext(buffer);
                buffer = new ArrayList<>();
                bufferCondition.reset();
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