package com.fmning.tools.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.sql.Date;

@Entity
@Table(name="tl_spending_transactions")
@DynamicUpdate
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpendingTransaction {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;

    @Column(name="account_id")
    private int accountId;

    @Column(name="identifier")
    private String identifier;

    @Column(name="name")
    private String name;

    @Column(name="original_name")
    private String originalName;

    @Column(name="amount")
    private String amount;

    @Column(name="category")
    private String category;

    @Column(name="location")
    private String location;

    @Column(name="date")
    private Date date;

}
