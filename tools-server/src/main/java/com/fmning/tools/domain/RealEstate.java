package com.fmning.tools.domain;

import com.fmning.tools.repository.RealEstatePK;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.sql.Date;

@Entity
@IdClass(RealEstatePK.class)
@Table(name="tl_real_estates")
@DynamicUpdate
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealEstate {

    @Id
    @Column(name="zid")
    private String zid;

    @Id
    @Column(name="date")
    private Date date;

    @Column(name="value")
    private int value;

    @Column(name="balance")
    private int balance;

}
