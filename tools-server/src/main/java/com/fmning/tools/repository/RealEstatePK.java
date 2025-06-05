package com.fmning.tools.repository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Objects;

import java.sql.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RealEstatePK {

    private String zid;
    private Date date;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RealEstatePK realEstatePK)) return false;
        return zid.equals(realEstatePK.zid) && date.equals(realEstatePK.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zid, date);
    }
}
