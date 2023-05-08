package ir.farbod.consumer.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import java.util.List;

@Configuration
@Profile("dev")
@EnableKafka
@Slf4j
public class BookLibraryConsumerConfig {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Value("book-lib-event.RETRY")
    private String retryTopic;

    @Value("book-lib-event.DLT")
    private String deadLetterTopic;

    @Bean
    public NewTopic getRetryTopic()
    {
        log.info("Create retryTopic ==> " + retryTopic);

        return TopicBuilder.name(retryTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }


    public DeadLetterPublishingRecoverer publishingRecoverer() {
        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (consumerRecord, e) -> {
                    if (e.getCause() instanceof RecoverableDataAccessException) {
                        log.info("********* retryTopic, partition ==> {}", consumerRecord.partition());
                        return new TopicPartition(retryTopic, consumerRecord.partition());
                    }
                    else {
                        log.info("********* deadLetterTopic, partition ==> {}", consumerRecord.partition());
                        return new TopicPartition(deadLetterTopic, consumerRecord.partition());
                    }
                });

        return recoverer;
    }

    public DefaultErrorHandler defaultErrorHandler() {

        //var backOff = new FixedBackOff(1000L, 2);
        var exponentialBackOff = new ExponentialBackOffWithMaxRetries(2);
        exponentialBackOff.setInitialInterval(1_000L);
        exponentialBackOff.setMultiplier(2.0);
        exponentialBackOff.setMaxInterval(2000L);
        var defaultErrorHandler = new DefaultErrorHandler(
                publishingRecoverer(),
                //backOff
                exponentialBackOff
        );

        var notRetryableExceptionList = List.of(
                IllegalArgumentException.class
        );
        notRetryableExceptionList.forEach(defaultErrorHandler::addNotRetryableExceptions);
        // or add defaultErrorHandler.addRetryableExceptions();

        defaultErrorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.error("Retry Listener Exception : {} , deliveryAttempt : {}", ex.getCause(), deliveryAttempt);
        });

        return defaultErrorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ObjectProvider<ConsumerFactory<Object, Object>> kafkaConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();

        configurer.configure(factory, kafkaConsumerFactory.getIfAvailable());

        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(defaultErrorHandler());

        return factory;
    }

}
