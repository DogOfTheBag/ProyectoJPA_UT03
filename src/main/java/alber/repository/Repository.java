package alber.repository;

import alber.model.Asignatura;
import alber.model.Centro;
import alber.model.Especialidad;
import alber.model.Profesor;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Repository {

    private EntityManager em;
    public Repository(EntityManager em) {
        this.em = em;
    }

    public String listarProf_Asig() {
        StringBuilder sb = new StringBuilder();

        List<Asignatura> asignaturas = findAsignaturas();

        if(asignaturas.isEmpty()){
            return ("No hay asignaturas");
        }

        Asignatura asigMax = null;
        int maxProfes = -1;

        for (Asignatura asignatura : asignaturas) {
            List<Profesor> profesores = em.createQuery("SELECT DISTINCT p " +
                "FROM Profesor p " +
                "JOIN p.asignaturas a " +
                "LEFT JOIN FETCH p.especialidad " +
                "LEFT JOIN FETCH p.centro " +
                "LEFT JOIN FETCH p.jefeDepartamento " +
                "WHERE a.id = :idAsig " +
                "ORDER BY p.nombre", Profesor.class).setParameter("idAsig", asignatura.getId()).getResultList();

            int numProfes = profesores.size();

            sb.append(asignatura.getNombre()).append(", ").append("ID: " + asignatura.getId()).append(", ").append("Número de profes: " + numProfes).append("\n");
            if(numProfes == 0){
                sb.append("Nadie da la asignatura\n");
            }else{
                sb.append("PROFESORES QUE DAN LA ASIGNATURA:\n");
                for (Profesor p : profesores) {
                    String especialidad = (p.getEspecialidad() != null ? p.getEspecialidad().getNombre() : "Sin especialidad");
                    String Centro = (p.getCentro() != null ? p.getCentro().getNombre() : "Sin Centro");
                    String Jefe = (p.getJefeDepartamento() != null ? p.getJefeDepartamento().getNombre() :  "Sin Jefe");
                    sb.append(p.getNombre()).append(", ").append(p.getId()).append(", ").append(especialidad)
                            .append(", ").append(Centro).append(", ").append("JEFE: " + Jefe).append("\n");
                }
            }

            if(numProfes > maxProfes){
                maxProfes = numProfes;
                asigMax = asignatura;
            }
            sb.append("\n\n");
        }

        sb.append("\n --------------------------------\n");
        sb.append("ASIGNATURA CON MÁS PROFES: " + asigMax.getNombre()).append(", ").append(maxProfes + " profesores.").append("\n");

        return sb.toString();
    }

    public String listarCentros() {
        StringBuilder sb = new StringBuilder();
        List<Centro> centros = findCentros();

        if(centros.isEmpty()){
            return ("No hay centros");
        }

        for (Centro c : centros) {
            //buscamos el numero de profes por centro filtrando por el centro
            Long numProfes = em.createQuery("SELECT COUNT(p.id) FROM Profesor p WHERE p.centro.id = :idCentro", Long.class).setParameter("idCentro", c.getId()).getSingleResult();

            //contamos las asignaturas por id distinto desde la tabla de profesores, teniendo en cuenta cada centro
            Long numAsignaturas = em.createQuery("SELECT COUNT (DISTINCT a.id) FROM Profesor p JOIN p.asignaturas a WHERE p.centro.id = : idCentro",
                    Long.class).setParameter("idCentro", c.getId()).getSingleResult();

            String director = (c.getDirector() != null ? c.getDirector().getNombre() : "Sin Director");

            sb.append("Centro: " + c.getNombre())
                    .append(", ").append("ID: " + c.getId())
                    .append(", ").append("Localidad: " + c.getLocalidad())
                    .append(", ").append("Director: " + director)
                    .append(", ").append("Numero de asignaturas: " + numAsignaturas)
                    .append(", ").append("Numero de profesores: " + numProfes)
                    .append("\n\n");
        }
        return sb.toString();
    }

    public String insertarAsignaturaEnProfesor(Long idAsig, Long idProf) {
        if(idAsig == null || idProf == null){
            return"IDs inválidos (null).";
        }

        Asignatura a = em.find(Asignatura.class, idAsig);
        if(a == null){
            return "No existe la asignatura con id: " + idAsig;
        }

        Profesor p = em.find(Profesor.class, idProf);
        if(p == null){
            return "No existe el profesor con id: " + idProf;
        }

        boolean yaLaImparte = em.createQuery("SELECT COUNT(a.id)" +
                "FROM Profesor p JOIN p.asignaturas a WHERE p.id = :idProf AND a.id = : idAsig", Long.class).setParameter("idProf", idProf)
                .setParameter("idAsig", idAsig).getSingleResult() > 0;

        if(yaLaImparte){
            return "El profesor "+ idProf + " ya imparte la asignatura: " + idAsig;
        }

        try{
            em.getTransaction().begin();

            p.añadirAsignatura(a);

            em.merge(p);

            em.getTransaction().commit();
            return "Operación realizada correctamente: Profesor " + idProf + " imparte la asignatura " + idAsig;
        } catch (Exception e) {
            if(em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            return "Error al hacer la operacion: " + e.getMessage();
        }

    }

    public List<Asignatura> findAsignaturas() {
        return em.createNamedQuery("Asignatura.findAll").getResultList();
    }

    public List<Centro> findCentros() {
        return em.createNamedQuery("Centro.findAll").getResultList();
    }


    public String cargarDatosInicialesCompletos() {
        try {
            em.getTransaction().begin();

            Long hayCentros = em.createQuery("SELECT COUNT(c.id) FROM Centro c", Long.class).getSingleResult();
            Long hayAsignaturas = em.createQuery("SELECT COUNT(a.id) FROM Asignatura a", Long.class).getSingleResult();
            Long hayEspecialidades = em.createQuery("SELECT COUNT(e.id) FROM Especialidad e", Long.class).getSingleResult();
            Long hayProfes = em.createQuery("SELECT COUNT(p.id) FROM Profesor p", Long.class).getSingleResult();

            if (hayCentros > 0 || hayAsignaturas > 0 || hayEspecialidades > 0 || hayProfes > 0) {
                em.getTransaction().rollback();
                return "Ya hay datos en la BD. No se insertó nada.";
            }

            // ============ 1) ESPECIALIDADES (8) ============
            String[] espNombres = {
                    "Matemáticas", "Lengua", "Inglés", "Biología",
                    "Geografía e Historia", "Física y Química", "Tecnología", "Educación Física"
            };

            Map<String, Especialidad> mapaEsp = new HashMap<>();
            for (String n : espNombres) {
                Especialidad e = new Especialidad();
                e.setNombre(n);
                // e.setProfesores(new ArrayList<>()); // opcional
                em.persist(e);
                mapaEsp.put(n, e);
            }

            // ============ 2) ASIGNATURAS (15) ============
            String[] asigNombres = {
                    "Matemáticas I", "Matemáticas II", "Lengua Castellana", "Inglés",
                    "Biología", "Geología", "Física", "Química", "Tecnología",
                    "Historia", "Geografía", "Economía", "Filosofía", "Educación Física", "Dibujo Técnico"
            };

            Map<String, Asignatura> mapaAsig = new HashMap<>();
            for (String n : asigNombres) {
                Asignatura a = new Asignatura();
                a.setNombre(n);
                a.setProfesores(new ArrayList<>()); // IMPORTANTE para tus métodos auxiliares
                em.persist(a);
                mapaAsig.put(n, a);
            }

            // ============ 3) CENTROS (5) ============
            Object[][] centrosData = {
                    {"IES Sierra Norte", "Madrid"},
                    {"IES Valle Verde", "Alcalá de Henares"},
                    {"IES Mar Azul", "Getafe"},
                    {"IES Camino Real", "Móstoles"},
                    {"IES Puerta del Sol", "Leganés"}
            };

            List<Centro> centros = new ArrayList<>();
            for (Object[] cdata : centrosData) {
                Centro c = new Centro();
                c.setNombre((String) cdata[0]);
                c.setLocalidad((String) cdata[1]);
                c.setProfesores(new ArrayList<>()); // para que no sea null si lo usas
                em.persist(c);
                centros.add(c);
            }

            // ============ 4) PROFESORES (ejemplo: 3 por centro = 15) ============
            // Creamos profesores con especialidad + centro, y ponemos jefes en cada centro
            // Nota: jefes no obligatorios; aquí pongo 1 jefe por centro (primer profe), y el resto lo tiene.
            List<Profesor> todos = new ArrayList<>();

            // Helper rápido para crear profesor
            java.util.function.BiFunction<String, Especialidad, Profesor> nuevoProfesorBase = (nombre, especialidad) -> {
                Profesor p = new Profesor();
                p.setNombre(nombre);
                p.setEspecialidad(especialidad);
                p.setAsignaturas(new ArrayList<>());
                return p;
            };

            // Centro 1
            Centro c1 = centros.get(0);
            Profesor p1 = nuevoProfesorBase.apply("Ana Martín", mapaEsp.get("Matemáticas"));
            p1.setCentro(c1);
            em.persist(p1);

            Profesor p2 = nuevoProfesorBase.apply("Luis García", mapaEsp.get("Lengua"));
            p2.setCentro(c1);
            p2.setJefeDepartamento(p1);
            em.persist(p2);

            Profesor p3 = nuevoProfesorBase.apply("Marta López", mapaEsp.get("Inglés"));
            p3.setCentro(c1);
            p3.setJefeDepartamento(p1);
            em.persist(p3);

            // Centro 2
            Centro c2 = centros.get(1);
            Profesor p4 = nuevoProfesorBase.apply("Carlos Pérez", mapaEsp.get("Física y Química"));
            p4.setCentro(c2);
            em.persist(p4);

            Profesor p5 = nuevoProfesorBase.apply("Sara Díaz", mapaEsp.get("Biología"));
            p5.setCentro(c2);
            p5.setJefeDepartamento(p4);
            em.persist(p5);

            Profesor p6 = nuevoProfesorBase.apply("Irene Ruiz", mapaEsp.get("Geografía e Historia"));
            p6.setCentro(c2);
            p6.setJefeDepartamento(p4);
            em.persist(p6);

            // Centro 3
            Centro c3 = centros.get(2);
            Profesor p7 = nuevoProfesorBase.apply("Javier Torres", mapaEsp.get("Biología"));
            p7.setCentro(c3);
            em.persist(p7);

            Profesor p8 = nuevoProfesorBase.apply("Noelia Sánchez", mapaEsp.get("Matemáticas"));
            p8.setCentro(c3);
            p8.setJefeDepartamento(p7);
            em.persist(p8);

            Profesor p9 = nuevoProfesorBase.apply("Pablo Romero", mapaEsp.get("Inglés"));
            p9.setCentro(c3);
            p9.setJefeDepartamento(p7);
            em.persist(p9);

            // Centro 4
            Centro c4 = centros.get(3);
            Profesor p10 = nuevoProfesorBase.apply("Elena Navarro", mapaEsp.get("Educación Física"));
            p10.setCentro(c4);
            em.persist(p10);

            Profesor p11 = nuevoProfesorBase.apply("Raúl Vega", mapaEsp.get("Matemáticas"));
            p11.setCentro(c4);
            p11.setJefeDepartamento(p10);
            em.persist(p11);

            Profesor p12 = nuevoProfesorBase.apply("Claudia Gil", mapaEsp.get("Geografía e Historia"));
            p12.setCentro(c4);
            p12.setJefeDepartamento(p10);
            em.persist(p12);

            // Centro 5
            Centro c5 = centros.get(4);
            Profesor p13 = nuevoProfesorBase.apply("David Molina", mapaEsp.get("Biología"));
            p13.setCentro(c5);
            em.persist(p13);

            Profesor p14 = nuevoProfesorBase.apply("Lucía Herrera", mapaEsp.get("Física y Química"));
            p14.setCentro(c5);
            p14.setJefeDepartamento(p13);
            em.persist(p14);

            Profesor p15 = nuevoProfesorBase.apply("Marcos Cano", mapaEsp.get("Tecnología"));
            p15.setCentro(c5);
            p15.setJefeDepartamento(p13);
            em.persist(p15);

            todos.addAll(List.of(p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15));

            // ============ 5) DIRECTORES (uno por centro) ============
            // (Como OneToOne: cada centro tiene 1 director opcional)
            c1.setDirector(p1);
            c2.setDirector(p4);
            c3.setDirector(p7);
            c4.setDirector(p10);
            c5.setDirector(p13);

            em.merge(c1);
            em.merge(c2);
            em.merge(c3);
            em.merge(c4);
            em.merge(c5);

            // ============ 6) ASIGNAR ASIGNATURAS A PROFES ============
            // Usamos tu método auxiliar p.añadirAsignatura(a) (añade en ambos lados)
            // (IMPORTANTE: por eso inicialicé listas arriba)
            // Centro 1
            p1.añadirAsignatura(mapaAsig.get("Matemáticas I"));
            p1.añadirAsignatura(mapaAsig.get("Matemáticas II"));

            p2.añadirAsignatura(mapaAsig.get("Lengua Castellana"));
            p2.añadirAsignatura(mapaAsig.get("Filosofía"));

            p3.añadirAsignatura(mapaAsig.get("Inglés"));

            // Centro 2
            p4.añadirAsignatura(mapaAsig.get("Física"));
            p4.añadirAsignatura(mapaAsig.get("Química"));

            p5.añadirAsignatura(mapaAsig.get("Biología"));
            p5.añadirAsignatura(mapaAsig.get("Geología"));

            p6.añadirAsignatura(mapaAsig.get("Historia"));
            p6.añadirAsignatura(mapaAsig.get("Biología"));
            p6.añadirAsignatura(mapaAsig.get("Economía"));

            // Centro 3
            p7.añadirAsignatura(mapaAsig.get("Tecnología"));
            p7.añadirAsignatura(mapaAsig.get("Dibujo Técnico"));

            p8.añadirAsignatura(mapaAsig.get("Matemáticas I"));
            p8.añadirAsignatura(mapaAsig.get("Matemáticas II"));

            p9.añadirAsignatura(mapaAsig.get("Inglés"));

            // Centro 4
            p10.añadirAsignatura(mapaAsig.get("Educación Física"));

            p11.añadirAsignatura(mapaAsig.get("Lengua Castellana"));
            p11.añadirAsignatura(mapaAsig.get("Filosofía"));

            p12.añadirAsignatura(mapaAsig.get("Historia"));
            p12.añadirAsignatura(mapaAsig.get("Geografía"));

            // Centro 5
            p13.añadirAsignatura(mapaAsig.get("Biología"));
            p13.añadirAsignatura(mapaAsig.get("Geología"));

            p14.añadirAsignatura(mapaAsig.get("Física"));
            p14.añadirAsignatura(mapaAsig.get("Química"));

            p15.añadirAsignatura(mapaAsig.get("Tecnología"));
            p15.añadirAsignatura(mapaAsig.get("Dibujo Técnico"));

            // Como la tabla intermedia la gobierna Profesor (JoinTable está en Profesor),
            // con hacer merge de los profesores ya queda guardada la relación.
            for (Profesor p : todos) {
                em.merge(p);
            }

            em.getTransaction().commit();
            return "OK: datos completos insertados (5 centros, 8 especialidades, 15 asignaturas, 15 profesores + directores + jefes + asignaciones).";

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            return "ERROR cargando datos completos: " + ex.getClass().getSimpleName() + " -> " + ex.getMessage();
        }
    }
}
