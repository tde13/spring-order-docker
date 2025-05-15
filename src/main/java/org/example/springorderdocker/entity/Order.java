package org.example.springorderdocker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name ="orders")
@Entity
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    private Integer productId;
    private Integer quantity;
    private Timestamp orderDate;
    private BigDecimal totalAmount;
    private String status;
    private Timestamp createdAt;

    @PrePersist
    public void prePersist() {
        Timestamp now = Timestamp.from(Instant.now());
        if (this.orderDate == null) this.orderDate = now;
        if (this.createdAt == null) this.createdAt = now;
    }
}
