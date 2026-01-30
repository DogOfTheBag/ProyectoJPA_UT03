package alber.controller;

import alber.repository.Repository;
import alber.view.VentanaPrincipal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class Main {
    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ProyectoJPA_UT03");
        EntityManager em = emf.createEntityManager();

        Repository repo = new Repository(em);

        new VentanaPrincipal(repo).setVisible(true);
    }
}