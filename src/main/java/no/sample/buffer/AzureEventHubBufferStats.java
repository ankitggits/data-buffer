package no.sample.buffer;

import com.microsoft.azure.eventhubs.EventData;

class AzureEventHubBufferStats implements BufferStats<EventData> {

    private final long maxSize;
    private final long maxPayloadLength;

    private volatile long size;
    private volatile long payloadLength;

    AzureEventHubBufferStats(long maxSize, long maxPayloadLength) {
        this.maxSize = maxSize;
        this.maxPayloadLength = maxPayloadLength;
    }

    @Override
    public boolean isFull() {
        if(this.payloadLength >= maxPayloadLength) {
            System.out.println("Buffer payload limit reached, emitting data");
            return true;
        } else if(this.size >= maxSize){
            System.out.println("Buffer size limit reached, emitting data");
            return true;
        } else{
            System.out.println("Buffer under limits, continue...");
            return false;
        }
    }

    @Override
    public void append(EventData eventData) {
        this.size++;
        this.payloadLength +=  eventData.getBodyLength();
    }

    @Override
    public void reset() {
        this.size = 0;
        this.payloadLength = 0;
    }
}
