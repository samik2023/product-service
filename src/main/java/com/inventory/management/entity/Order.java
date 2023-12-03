package com.inventory.management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class Order{

    private Long orderId;
    private Long userId;
    private Long productId;
    private String userName;

    private OrderStatus status;

}

