package com.tools.dao;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StaleObjectStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class CommonDao<T> {

    private final Class<T> clazz;

    protected CommonDao(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Autowired
    public EntityManagerFactory entityManagerFactory;

    /**
     * Create a new database session
     * @return the database session
     */
    public Session getSession() {
        return entityManagerFactory.unwrap(SessionFactory.class).openSession();
    }

    /**
     * Save the domain object to the database
     * @param t the domain object
     */
    public void save(T t) {
        Session session = getSession();
        session.beginTransaction();
        session.save(t);
        session.getTransaction().commit();
        session.close();
    }

    /**
     * Save a list of domain objects to the database, using bulk insert with 20 objects per batch
     * @param tList the domain object list
     */
    public void saveAll(List<T> tList) {
        Session session = getSession();
        session.beginTransaction();

        AtomicInteger counter = new AtomicInteger();
        tList.forEach(t -> {
            session.save(t);

            if (counter.incrementAndGet() % 20 == 0) {
                session.flush();
                session.clear();
            }
        });

        session.getTransaction().commit();
        session.close();
    }

    /**
     * Update the domain object base on ID
     * @param t the domain object to be updated
     * @throws StaleObjectStateException if the id does not exist
     */
    public void update(T t) throws StaleObjectStateException {
        Session session = getSession();
        session.beginTransaction();
        session.update(t);
        session.getTransaction().commit();
        session.close();
    }

    /**
     * Save or update the domain object.
     * If the domain object is transient (by identifier), save the object. Otherwise update base on identifier (ID)
     * @param t the domain object to be saved or updated
     */
    public void saveOrUpdate(T t) {
        Session session = getSession();
        session.beginTransaction();
        session.saveOrUpdate(t);
        session.getTransaction().commit();
        session.close();
    }

    /**
     * Find all domain objects for the domain class
     * @return a list of all the domain objects
     */
    public List<T> findAll() {
        Session session = getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteria = builder.createQuery(clazz);
        criteria.from(clazz);
        List<T> resultList = session.createQuery(criteria).getResultList();
        session.close();

        return resultList;
    }

    /**
     * Find the domain object base on ID
     * @param id the id, primary key
     * @return the object. Null if no object is found
     */
    public T findById(int id) {
        Session session = getSession();
        T result = session.get(clazz, id);
        session.close();

        return result;
    }

    /**
     * Delete the domain object, base on ID
     * @param t the domain object to be deleted
     */
    public void delete(T t) {
        Session session = getSession();
        session.beginTransaction();
        session.delete(t);
        session.getTransaction().commit();
        session.close();
    }

    /**
     * Delete the domain object, base on ID
     * @param id the ID of the domain object
     */
    public void deleteById(int id) {
        Session session = getSession();
        T t = session.get(clazz, id);

        if (t != null) {
            session.beginTransaction();
            session.delete(t);
            session.getTransaction().commit();
        }

        session.close();
    }

}