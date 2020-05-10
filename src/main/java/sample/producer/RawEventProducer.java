package sample.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

@SpringBootApplication
public class RawEventProducer {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    public static final ExecutorService exec = Executors.newSingleThreadExecutor();
    BlockingQueue<RawEvent> unbounded = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        SpringApplication.run(RawEventProducer.class, args);
    }

    @Bean
    public Supplier<RawEvent> supplier() {
        return () -> unbounded.poll();
    }


    @Bean
    public ApplicationRunner runner() {
        return args -> {
            System.out.println("Send some raw events to raw-event topic");
            exec.execute(() -> {
                while (true) {
                    unbounded.offer(getRandomRawEvent());
                    try {
                        Thread.sleep(1_000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        };
    }


    static class RawEvent {

        String id;
        String type;

        public RawEvent(String type) {
            this.id = UUID.randomUUID().toString();
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getId() {
            return id;
        }
    }

    public RawEvent getRandomRawEvent() {
        List<String> givenList = Arrays.asList("received", "scheme_response", "sent", "initiated", "finalized");
        Random rand = new Random();
        String type = givenList.get(rand.nextInt(givenList.size()));
        return new RawEvent(type);
    }
}

