package ir.farbod.producer.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("dev")
@Slf4j
public class AutoConfigTopic {

    @Value("${spring.kafka.template.default-topic}")
    private String TOPIC;

    @Bean
    public NewTopic getTopic()
    {
        log.info("TTTTTOPIC ==== " + TOPIC);

        return TopicBuilder.name(TOPIC)
                .partitions(3)
                .replicas(3)
                .build();
    }
}
