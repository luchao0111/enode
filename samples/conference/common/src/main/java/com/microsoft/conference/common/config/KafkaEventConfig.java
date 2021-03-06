package com.microsoft.conference.common.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.enodeframework.kafka.KafkaApplicationMessageListener;
import org.enodeframework.kafka.KafkaDomainEventListener;
import org.enodeframework.kafka.KafkaPublishableExceptionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.adapter.RetryingMessageListenerAdapter;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.conference.common.QueueProperties.DEFAULT_CONSUMER_GROUP0;
import static com.microsoft.conference.common.QueueProperties.KAFKA_SERVER;

public class KafkaEventConfig {

    @Value("${spring.enode.mq.topic.event}")
    private String eventTopic;

    @Value("${spring.enode.mq.topic.command}")
    private String commandTopic;

    @Value("${spring.enode.mq.topic.application}")
    private String applicationTopic;

    @Value("${spring.enode.mq.topic.exception}")
    private String exceptionTopic;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, DEFAULT_CONSUMER_GROUP0);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        props.put(ProducerConfig.RETRIES_CONFIG, 1);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 1024000);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public RetryTemplate retryTemplate() {
        return new RetryTemplate();
    }

    @Bean(name = "enodeKafkaTemplate")
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public KafkaMessageListenerContainer<String, String> domainEventListenerContainer(KafkaDomainEventListener domainEventListener, RetryTemplate retryTemplate) {
        ContainerProperties properties = new ContainerProperties(eventTopic);
        properties.setGroupId(DEFAULT_CONSUMER_GROUP0);
        RetryingMessageListenerAdapter<String, String> listenerAdapter = new RetryingMessageListenerAdapter<>(domainEventListener, retryTemplate);
        properties.setMessageListener(listenerAdapter);
        properties.setMissingTopicsFatal(false);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL);
        return new KafkaMessageListenerContainer<>(consumerFactory(), properties);
    }

    @Bean
    public KafkaMessageListenerContainer<String, String> commandListenerContainer(KafkaDomainEventListener domainEventListener, RetryTemplate retryTemplate) {
        ContainerProperties properties = new ContainerProperties(commandTopic);
        properties.setGroupId(DEFAULT_CONSUMER_GROUP0);
        RetryingMessageListenerAdapter<String, String> listenerAdapter = new RetryingMessageListenerAdapter<>(domainEventListener, retryTemplate);
        properties.setMessageListener(listenerAdapter);
        properties.setMissingTopicsFatal(false);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL);
        return new KafkaMessageListenerContainer<>(consumerFactory(), properties);
    }

    @Bean
    public KafkaMessageListenerContainer<String, String> applicationMessageListenerContainer(KafkaApplicationMessageListener applicationMessageListener, RetryTemplate retryTemplate) {
        ContainerProperties properties = new ContainerProperties(applicationTopic);
        properties.setGroupId(DEFAULT_CONSUMER_GROUP0);
        RetryingMessageListenerAdapter<String, String> listenerAdapter = new RetryingMessageListenerAdapter<>(applicationMessageListener, retryTemplate);
        properties.setMessageListener(listenerAdapter);
        properties.setMissingTopicsFatal(false);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL);
        return new KafkaMessageListenerContainer<>(consumerFactory(), properties);
    }

    @Bean
    public KafkaMessageListenerContainer<String, String> publishableExceptionListenerContainer(KafkaPublishableExceptionListener publishableExceptionListener, RetryTemplate retryTemplate) {
        ContainerProperties properties = new ContainerProperties(exceptionTopic);
        properties.setGroupId(DEFAULT_CONSUMER_GROUP0);
        RetryingMessageListenerAdapter<String, String> listenerAdapter = new RetryingMessageListenerAdapter<>(publishableExceptionListener, retryTemplate);
        properties.setMessageListener(listenerAdapter);
        properties.setMissingTopicsFatal(false);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL);
        return new KafkaMessageListenerContainer<>(consumerFactory(), properties);
    }
}
