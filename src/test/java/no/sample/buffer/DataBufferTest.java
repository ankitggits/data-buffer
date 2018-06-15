package no.sample.buffer;

import com.googlecode.junittoolbox.MultithreadingTester;
import com.googlecode.junittoolbox.PollingWait;
import com.microsoft.azure.eventhubs.EventData;
import org.json.JSONObject;
import org.junit.*;
import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class DataBufferTest {

    private static List<List<String>> result = new ArrayList<>();

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

    private static final Action1<List<EventData>> TEST_ACTION = events -> {
        Assert.assertTrue(events.size() == 5);
    };

    @Before
    public void clear(){
        result.clear();
    }

    @Ignore
    @Test
    public void test()  {

        EventDataBufferOperator<EventData> bufferUntilOperator = new EventDataBufferOperator<>(new AzureEventHubBufferStats(5, 600));

        List<EventData> events = getEventData("Sample", 1, 50);

        Observable<List<EventData>> lift = Observable
                .from(events)
                .lift(bufferUntilOperator);
        lift.subscribe(ACTION, Throwable::printStackTrace);
        lift.subscribe(TEST_ACTION);

        Assert.assertEquals(10, result.size());
    }

    @Ignore
    @Test
    public void publishFromAnotherSourceTest() {

        EventDataBufferOperator<EventData> bufferUntilOperator = new EventDataBufferOperator<>(new AzureEventHubBufferStats(5, 600));
        PublishSubject<EventData> subject = PublishSubject.create();
        Observable<List<EventData>> lift = subject.lift(bufferUntilOperator);
        lift.subscribe(ACTION, Throwable::printStackTrace);

        int start = 1;
        int number = 10;

        getEventData("1", start, number).forEach(next->{
            printMatrix(start, number, next);
            subject.onNext(next);
        });
        getEventData("2", start, number).forEach(next->{
            printMatrix(start, number, next);
            subject.onNext(next);
        });
        getEventData("3", start, number).forEach(next->{
            printMatrix(start, number, next);
            subject.onNext(next);
        });

        subject.onCompleted();
        System.out.println(result);
        Assert.assertTrue(validate(6, 5));
    }

    private boolean validate(int resultSize, int eachSize){
        return result.size()==resultSize && result.stream().noneMatch(each-> each.size()!=eachSize);
    }

    private PollingWait wait = new PollingWait().timeoutAfter(10, SECONDS)
            .pollEvery(100, MILLISECONDS);


    @Ignore
    @Test
    public void asyncPublishFromAnotherSourceTest() {
        EventDataBufferOperator<EventData> bufferUntilOperator = new EventDataBufferOperator<>(new AzureEventHubBufferStats(5, 600));
        PublishSubject<EventData> subject = PublishSubject.create();
        Observable<List<EventData>> lift = subject.lift(bufferUntilOperator);
        lift.observeOn(Schedulers.computation()).subscribe(ACTION, Throwable::printStackTrace);

        int start = 1;
        int number = 10;

        new MultithreadingTester()
                                .numThreads(2)
                                .numRoundsPerThread(1)
                                .add(() -> getEventData(String.valueOf(Thread.currentThread().getId()), start, number).forEach(next->{
                                    printMatrix(start, number, next);
                                    subject.onNext(next);
                                }))
                                .run();

        subject.onCompleted();
        wait.until(() -> validate(12, 5));
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

}
