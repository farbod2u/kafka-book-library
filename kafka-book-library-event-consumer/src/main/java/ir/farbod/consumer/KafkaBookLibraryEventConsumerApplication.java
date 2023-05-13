package ir.farbod.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KafkaBookLibraryEventConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaBookLibraryEventConsumerApplication.class, args);
    }

}
