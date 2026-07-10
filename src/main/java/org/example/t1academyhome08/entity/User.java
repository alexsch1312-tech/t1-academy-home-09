package org.example.t1academyhome08.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tbl_limit_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private Long id;

    @Column(name = "actual_limit", nullable = false)
    private BigDecimal actualLimit;

    @Column(name = "reserved_limit", nullable = false)
    private BigDecimal reservedLimit;
}
