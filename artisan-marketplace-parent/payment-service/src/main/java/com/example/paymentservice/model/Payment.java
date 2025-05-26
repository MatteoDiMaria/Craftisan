package com.example.paymentservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private String paymentMethod; // e.g., "MOCK_CREDIT_CARD"

    @Column(nullable = false)
    private String status; // e.g., "PENDING", "SUCCESSFUL", "FAILED"

    @PrePersist
    protected void onCreate() {
        paymentDate = LocalDateTime.now();
    }
}
