package com.isep.dataengineservice.Config;

import com.isep.dataengineservice.Models.GeoPosition;
import com.isep.dataengineservice.Models.Place;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

     @Bean
    public ProducerFactory<String, GeoPosition> producerFactory(){
        Map<String, Object> producerParams = new HashMap<>();
        producerParams.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.104.249:9092");
        producerParams.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerParams.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(producerParams);
    }

    @Bean
    public ConsumerFactory<String, GeoPosition> consumerFactory() {
        Map<String, Object> consumerParams = new HashMap<>();
        consumerParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.104.249:9092");
        consumerParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        JsonDeserializer<GeoPosition> deserializer = new JsonDeserializer<>(GeoPosition.class);
        deserializer.addTrustedPackages("*");
        consumerParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);

        return new DefaultKafkaConsumerFactory<>(consumerParams, new StringDeserializer(), deserializer);
    }

    @Bean
    public KafkaTemplate<String, GeoPosition> kafkaTemplate(){
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory kafkaListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory concurrentKafkaListenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        concurrentKafkaListenerContainerFactory.setConsumerFactory(consumerFactory());
        return concurrentKafkaListenerContainerFactory;
    }
}
