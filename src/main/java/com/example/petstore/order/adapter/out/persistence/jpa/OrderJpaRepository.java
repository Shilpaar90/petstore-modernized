package com.example.petstore.order.adapter.out.persistence.jpa;

import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface OrderJpaRepository extends Repository<OrderEntity, String> {

    OrderEntity save(OrderEntity order);

    Optional<OrderEntity> findById(String orderId);

    List<OrderEntity> findByUsernameOrderByPlacedAtDesc(String username);
}
