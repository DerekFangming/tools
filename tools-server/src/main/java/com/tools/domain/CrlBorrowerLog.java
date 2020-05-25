package com.tools.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name="crl_borrower_logs")
@DynamicUpdate
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrlBorrowerLog {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    @Column(name="equipment_id")
    private int equipmentId;

    @Column(name="name")
    private String name;

    @Column(name="ut_eid")
    private String utEid;

    @Column(name="borrow_date")
    private Instant borrowDate;

    @Column(name="return_date")
    private Instant returnDate;
}
