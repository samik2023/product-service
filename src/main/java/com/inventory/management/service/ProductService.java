package com.inventory.management.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.management.entity.Event;
import com.inventory.management.entity.Order;
import com.inventory.management.entity.Product;
import com.inventory.management.repository.ProductRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class ProductService {

    private static ObjectMapper mapper = new ObjectMapper();
    @Autowired
    ProductRepository repository;

    @Autowired
    ProductEventProducer eventProducer;

    public ResponseEntity<Product> addProduct(Product product){
            repository.save(product);
        Event event =new Event();
        try {
            event.setEventType("PRODUCT_CREATED");
            event.setCommandObjStr(mapper.writeValueAsString(product));
            event.setTimeStamp(LocalDateTime.now());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        //eventProducer.publishEvent(event,"productUpdateTopic");
        return ResponseEntity.ok(product);
    }

    public ResponseEntity<String> deleteProduct(Long pid){
        repository.deleteById(pid);
        log.info("Product deleted  successfully !");
        return ResponseEntity.ok("Product deleted");
    }

   // @CircuitBreaker(name="productCircuitBreaker", fallbackMethod = "handleProductUpdateFailure")
  //  @Retry(name = "productCircuitBreaker" ,fallbackMethod = "retryfallback")
    public void updateProductInventory(Event event) {

            if ("PRODUCT_UPDATE".equalsIgnoreCase(event.getEventType())) {
                decreaseProductQuantity(event);

            }else if (event.getEventType().equalsIgnoreCase("PRODUCT_RESTOCK")) {
                restockProduct(event);
            }
    }

    public void orderUpdate(String eventType,String topic,Order order)throws JsonProcessingException {
        Event event = new Event();
        event.setEventType(eventType);
        event.setOrderId(order.getOrderId());
        event.setCommandObjStr(mapper.writeValueAsString(order));
        eventProducer.publishEvent(event, topic);
    }

    public void broadcastProductUpdate(String eventType,String topic,Product product)throws JsonProcessingException {
        Event event = new Event();
        event.setEventType(eventType);
        event.setCommandObjStr(mapper.writeValueAsString(product));
        eventProducer.publishEvent(event, topic);
    }

    public void restockProduct(Event event){

            Optional<Product> productOpt;
            Product product = null;
            try {
                product = mapper.readValue(event.getCommandObjStr(), Product.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            productOpt = repository.findById(product.getProductId());
            if(productOpt.isPresent()){
                productOpt.get().setQuantity(productOpt.get().getQuantity() + 1);
                repository.save(productOpt.get());
                try {
                    broadcastProductUpdate("PRD_RESTOCK_SUCCESS","productBroadcastTopic",productOpt.get());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }else{
                Order order = new Order();
                order.setOrderId(event.getOrderId());
                try {
                    orderUpdate("RESTOCK_FAILURE","orderUpdateTopic",order);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }


    public void decreaseProductQuantity(Event event) {

        Optional<Product> productOpt;
        Product product = null;
        try {
            product = mapper.readValue(event.getCommandObjStr(), Product.class);

            productOpt = repository.findById(product.getProductId());
            if (productOpt.isPresent()) {
                long newQuantity = productOpt.get().getQuantity() - 1;
                if (newQuantity < 0) {
                    //send out of stock event
                    event.setEventType("OUT_OF_STOCK");
                    eventProducer.publishEvent(event,"orderUpdateTopic");
                    return;
                }
                productOpt.get().setQuantity(productOpt.get().getQuantity() - 1);
                repository.save(productOpt.get());

                broadcastProductUpdate("PRD_UPD_SUCCESS", "productBroadcastTopic", productOpt.get());
                event.setEventType("PRD_UPD_SUCCESS");
                eventProducer.publishEvent(event, "orderUpdateTopic");

            }
        } catch (Exception e) {
            e.getMessage();
        }

           /*Order order = new Order();
           order.setOrderId(event.getOrderId());
            try {
                orderUpdate("PRD_UPD_FAILURE","orderUpdateTopic",order);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }*/

    }
}