package com.fmning.tools.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import java.math.BigInteger;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class QueryService {

    private final EntityManager entityManager;

    public int getNextQuerySequence() {
        BigInteger next = (BigInteger)entityManager
                .createNativeQuery("select nextval('posts_id_seq')")
                .getSingleResult();
        return next.intValue();
    }

}
