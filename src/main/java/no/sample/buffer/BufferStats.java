package no.sample.buffer;

public interface BufferStats<T> {
    boolean isFull();
    void append(T t);
    void reset();
}