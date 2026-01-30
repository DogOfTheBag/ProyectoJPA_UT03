package alber.repository;

import jakarta.persistence.EntityManager;

public class Repository {

    private EntityManager em;
    public Repository(EntityManager em) {
        this.em = em;
    }




}
