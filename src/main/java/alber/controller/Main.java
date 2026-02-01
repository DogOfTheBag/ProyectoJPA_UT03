package alber.controller;

import alber.repository.Repository;
import alber.view.VentanaPrincipal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class Main {
    public static void main(String[] args) {

        /*Por temas de orden creo que lo mejor es hacer el entity manager directamente aqui, crear el repo y pasarle el entity manager
        * y luego crear la ventana y pasarle el repo*/
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ProyectoJPA_UT03");
        EntityManager em = emf.createEntityManager();

        Repository repo = new Repository(em);

        System.out.println(repo.cargarDatosIniciales());
        new VentanaPrincipal(repo).setVisible(true);
    }
}