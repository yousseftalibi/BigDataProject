package com.isep.dataengineservice.Config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.isep.dataengineservice.Models.GeoPosition;
import com.isep.dataengineservice.Models.Place;
import com.isep.dataengineservice.Services.GeoNodeService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Configuration
@EnableKafka
public class KafkaConfig {

    static class PlaceDeserializer extends JsonDeserializer<List<Place>> {
        public List<Place> deserialize(String topic, Headers headers, byte[] data) {
            return deserialize(topic, data);
        }
        @Override
        public List<Place> deserialize(String topic, byte[] data) {
            if (data == null) {
                return null;
            }
            try {
                return objectMapper.readValue(data, new TypeReference<List<Place>>() {
                });
            } catch (IOException e) {
                throw new SerializationException("Can't deserialize data [" + Arrays.toString(data) +
                        "] from topic [" + topic + "]", e);
            }
        }
    }

    private Map<String, Object> producerParams() {
        Map<String, Object> params = new HashMap<>();
        params.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.104.249:9092");
        params.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        params.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return params;
    }

    private Map<String, Object> consumerParams() {
        Map<String, Object> params = new HashMap<>();
        params.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.104.249:9092");
        params.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        params.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        params.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        return params;
    }

    @Bean
    public ProducerFactory<String, GeoPosition> geoPositionProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerParams());
    }

    @Bean
    public ConsumerFactory<String, GeoPosition> geoPositionConsumerFactory() {
        JsonDeserializer<GeoPosition> deserializer = new JsonDeserializer<>(GeoPosition.class);
        deserializer.addTrustedPackages("*");
        return new DefaultKafkaConsumerFactory<>(consumerParams(), new StringDeserializer(), deserializer);
    }

    @Bean
    public KafkaTemplate<String, GeoPosition> geoPositionKafkaTemplate() {
        return new KafkaTemplate<>(geoPositionProducerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, GeoPosition> geoPositionListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, GeoPosition> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(geoPositionConsumerFactory());
        return factory;
    }

    @Bean
    public ProducerFactory<String, List<Place>> placeListProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerParams());
    }

    @Bean
    public ConsumerFactory<String, List<Place>> placeListConsumerFactory() {
        JsonDeserializer<List<Place>> deserializer = new PlaceDeserializer();
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(consumerParams(), new StringDeserializer(), deserializer);
    }

    @Bean
    public KafkaTemplate<String, List<Place>> placeListKafkaTemplate() {
        return new KafkaTemplate<>(placeListProducerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, List<Place>> placeListListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, List<Place>> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(placeListConsumerFactory());
        return factory;
    }
}
