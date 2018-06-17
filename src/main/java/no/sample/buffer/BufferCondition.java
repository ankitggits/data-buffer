package no.sample.buffer;

public interface BufferCondition<T> {
    boolean isFull();
    void append(T t);
    void reset();
}