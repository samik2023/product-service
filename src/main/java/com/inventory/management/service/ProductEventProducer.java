package com.inventory.management.service;

import com.inventory.management.entity.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProductEventProducer {
        @Autowired
        private KafkaTemplate<String, Event> kafkaTemplate;

        public void publishEvent(Event txn,String topicName) {

            kafkaTemplate.send(topicName, txn);
        }
}
