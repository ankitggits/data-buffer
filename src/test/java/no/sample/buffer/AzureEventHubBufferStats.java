package no.sample.buffer;

import com.microsoft.azure.eventhubs.EventData;
import no.sample.buffer.BufferStats;

import java.util.concurrent.atomic.AtomicLong;

public class AzureEventHubBufferStats implements BufferStats<EventData> {

    private final long maxSize;
    private final long maxPayloadLength;
    private volatile Meta meta;

    AzureEventHubBufferStats(long maxSize, long maxPayloadLength) {
        this.maxSize = maxSize;
        this.maxPayloadLength = maxPayloadLength;
        meta = new Meta();
        reset();
    }

    @Override
    public boolean isFull() {
        if(this.meta.payloadLength.longValue() >= maxPayloadLength) {
            System.out.println("Buffer payload limit reached, emitting data");
            return true;
        } else if(this.meta.size.longValue() >= maxSize){
            System.out.println("Buffer size limit reached, emitting data");
            return true;
        } else{
            System.out.println("Buffer under limits, continue...");
            return false;
        }
    }

    @Override
    public void append(EventData eventData) {
        this.meta.size.incrementAndGet();
        this.meta.payloadLength.accumulateAndGet(eventData.getBodyLength(), (left, right) -> left+right);
    }

    @Override
    public void reset() {
        this.meta.size = new AtomicLong();
        this.meta.payloadLength = new AtomicLong();
    }

    private static class Meta{
        private volatile AtomicLong size;
        private volatile AtomicLong payloadLength;
    }
}
