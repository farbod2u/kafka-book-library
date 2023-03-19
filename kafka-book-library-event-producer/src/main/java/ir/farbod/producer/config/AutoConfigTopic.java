package ir.farbod.producer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class AutoConfigTopic {

    @Bean
    public NewTopic getTopic()
    {
        return TopicBuilder.name("book-lib-event")
                .partitions(3)
                .replicas(3)
                .build();
    }
}
