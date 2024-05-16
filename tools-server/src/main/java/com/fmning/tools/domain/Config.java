package com.fmning.tools.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name="configurations")
@DynamicUpdate
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Config {

    @Id
    @Column(name="key")
    String key;

    @Column(name="value")
    String value;

    public Set<String> getValueSet() {
        List<String> list = new ArrayList<>(Arrays.asList(value.split(",")));
        Set<String> res = new HashSet<>();
        list.forEach(s -> res.add(s.trim()));
        return res;
    }
}
