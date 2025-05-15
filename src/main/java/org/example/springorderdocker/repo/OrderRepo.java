package org.example.springorderdocker.repo;

import org.example.springorderdocker.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepo extends JpaRepository<Order, Integer> {
}
