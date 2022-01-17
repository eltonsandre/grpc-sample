package com.github.eltonsandre.sample.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveKafkaProducer {

    private final ReactiveKafkaProducerTemplate<String, Object> kafkaProducer;

    public Mono<SenderResult<String>> send(final String topicName, final String key, final Object payload) {
        return this.send(new ProducerRecord<>(topicName, null, Instant.now().toEpochMilli(), key, payload));
    }

    public Mono<SenderResult<String>> send(final ProducerRecord<String, Object> producerRecord) {
        return this.send(SenderRecord.create(producerRecord, UUID.randomUUID().toString()));
    }

    public Mono<SenderResult<String>> send(final SenderRecord<String, Object, String> senderRecord) {
        return this.kafkaProducer.send(senderRecord)
                .doOnSuccess(senderResult -> {
                    final var metadata = senderResult.recordMetadata();
                    log.info("kafkaProducer, topic: {}, status: {}, correlationId: {}, partition: {}, offset:{} timestamp: {}",
                            metadata.topic(), "SUCCESS", senderResult.correlationMetadata(),
                            metadata.partition(), metadata.offset(), metadata.timestamp());
                })
                .doOnError(e -> log.error("kafkaProducer, topic: {}, status: {}, correlationId: {},",
                        senderRecord.topic(), "ERROR", senderRecord.correlationMetadata(), e));
    }

}
