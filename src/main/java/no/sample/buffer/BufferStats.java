package no.sample.buffer;

interface BufferStats<T> {
    boolean isFull();
    void append(T t);
    void reset();
}