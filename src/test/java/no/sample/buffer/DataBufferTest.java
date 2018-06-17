package no.sample.buffer;

import com.googlecode.junittoolbox.MultithreadingTester;
import com.googlecode.junittoolbox.PollingWait;
import com.microsoft.azure.eventhubs.EventData;
import lombok.Getter;
import org.json.JSONObject;
import org.junit.*;
import rx.functions.Action1;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class DataBufferTest {

    private static List<List<String>> result = new ArrayList<>();

    private PollingWait wait = new PollingWait().timeoutAfter(10, SECONDS)
            .pollEvery(100, MILLISECONDS);


    private static final Action1<List<EventData>> ACTION = events -> {
        if(events.isEmpty()){
           return;
        }
        try {
            StringBuilder builder = new StringBuilder(String.valueOf(events.size()) + "  -  ");
            List<String> strings = new ArrayList<>();
            events.stream().map(event -> new String(event.getBody(), StandardCharsets.UTF_8)).map(JSONObject::new).forEachOrdered(pushMessage -> {
                strings.add( String.valueOf(pushMessage.get("topic")) +"-"+ String.valueOf(pushMessage.get("content")));
            });
            builder.append(strings);
            // Simulating rest call to azure
            Thread.sleep(100);
            System.out.println(builder.toString());
            result.add(strings);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };

    @Before
    public void clear(){
        result.clear();
    }

    @Test
    public void publishFromAnotherSourceTest() {

        BufferPublisher<EventData> publisher = BufferBuilder.build(ACTION, new TestBufferCondition(5, 600));

        int start = 1;
        int number = 10;

        for(int i = 1; i <= 3; i++){
            getEventData(String.valueOf(i), start, number).forEach(next->{
                printMatrix(start, number, next);
                publisher.publish(next);
            });
        }

        publisher.finish();
        Assert.assertTrue(validate(6, 5));
    }

    @Test
    public void asyncPublishFromAnotherSourceTest() {

        BufferPublisher<EventData> publisher = BufferBuilder.build(ACTION, new TestBufferCondition(5, 600));

        int start = 1;
        int number = 10;

        new MultithreadingTester()
                                .numThreads(3)
                                .numRoundsPerThread(1)
                                .add(() -> getEventData(String.valueOf(Thread.currentThread().getId()), start, number).forEach(next->{
                                    printMatrix(start, number, next);
                                    publisher.publish(next);
                                }))
                                .run();

        publisher.finish();
        wait.until(() -> validate(6, 5));
    }

    private boolean validate(int resultSize, int eachSize){
        return result.size()==resultSize && result.stream().noneMatch(each-> each.size()!=eachSize);
    }

    private void printMatrix(int start, int number, EventData next) {
        JSONObject jsonObject = new JSONObject(new String(next.getBody(), StandardCharsets.UTF_8));
        Integer i = (Integer) jsonObject.get("content");
        String id = (String) jsonObject.get("topic");
        if(i.equals(start)){
            System.out.println("pushing first element of "+ id + " : "+ i);
        }else if(i.equals(start+number-1)){
            System.out.println("pushing last element of "+ id + " : "+ i);
        }
    }

    private List<EventData> getEventData(String id, int start, int number) {
        List<EventData> events = new ArrayList<>();
        for (int i = start; i < start + number; i++){
            try {
                PushMessage message = new PushMessage(id, String.valueOf(i));
                EventData eventData = new EventData(message.json().getBytes("UTF-8"));
                events.add(eventData);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return events;
    }

    @Getter
    private static class PushMessage {
        private final String topic;
        private final String content;

        private PushMessage(String topic, String content) {
            this.topic = topic;
            this.content = content;
        }

        private String json() {
            return "{\"topic\": \"" + topic + "\", \"content\": " + content + "}";
        }
    }

    private static class TestBufferCondition implements BufferCondition<EventData> {

        private final long maxSize;
        private final long maxPayloadLength;
        private volatile Meta meta;

        TestBufferCondition(long maxSize, long maxPayloadLength) {
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

}

