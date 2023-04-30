package ir.farbod.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaBookLibraryEventConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaBookLibraryEventConsumerApplication.class, args);
    }

}
