package org.example.t1academyhome08.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tbl_limit_operation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LimitOperation {
    @Id
    @Column(name = "operation_id")
    private String operationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationStatus status;
}

