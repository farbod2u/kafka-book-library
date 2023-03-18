package ir.farbod.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaBookLibraryEventProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaBookLibraryEventProducerApplication.class, args);
    }

}
