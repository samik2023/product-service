package com.inventory.management.service;

import com.inventory.management.entity.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductEventConsumer {

    @Autowired
    private ProductService service;

    private static final String TOPIC_UPDATE_PRODUCT = "productUpdateTopic";

    @KafkaListener(topics = TOPIC_UPDATE_PRODUCT,groupId = "product-group", containerFactory = "tranRecordListener")
    private void listenProductUpdateTopic(Event event) throws Exception{
        log.info("Received message :" + event +  " in " + TOPIC_UPDATE_PRODUCT );
        service.updateProductInventory(event);
    }

}
