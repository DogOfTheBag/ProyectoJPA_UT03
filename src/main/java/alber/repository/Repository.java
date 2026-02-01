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

            // Evitar duplicados: si ya hay algo, no insertamos
            Long hayE = em.createQuery("SELECT COUNT(e.id) FROM Especialidad e", Long.class).getSingleResult();
            Long hayA = em.createQuery("SELECT COUNT(a.id) FROM Asignatura a", Long.class).getSingleResult();
            Long hayC = em.createQuery("SELECT COUNT(c.id) FROM Centro c", Long.class).getSingleResult();
            Long hayP = em.createQuery("SELECT COUNT(p.id) FROM Profesor p", Long.class).getSingleResult();

            if (hayE > 0 || hayA > 0 || hayC > 0 || hayP > 0) {
                em.getTransaction().rollback();
                return "Ya hay datos en la BD. No se insertó nada.";
            }

            // =========================
            // 1) ESPECIALIDADES (8)
            // =========================
            Especialidad espMat = new Especialidad();
            espMat.setNombre("Matemáticas");
            em.persist(espMat);

            Especialidad espLen = new Especialidad();
            espLen.setNombre("Lengua");
            em.persist(espLen);

            Especialidad espIng = new Especialidad();
            espIng.setNombre("Inglés");
            em.persist(espIng);

            Especialidad espBio = new Especialidad();
            espBio.setNombre("Biología");
            em.persist(espBio);

            Especialidad espGHy = new Especialidad();
            espGHy.setNombre("Geografía e Historia");
            em.persist(espGHy);

            Especialidad espFyQ = new Especialidad();
            espFyQ.setNombre("Física y Química");
            em.persist(espFyQ);

            Especialidad espTec = new Especialidad();
            espTec.setNombre("Tecnología");
            em.persist(espTec);

            Especialidad espEF = new Especialidad();
            espEF.setNombre("Educación Física");
            em.persist(espEF);

            // =========================
            // 2) ASIGNATURAS (15)
            // =========================
            Asignatura asMatI = new Asignatura();
            asMatI.setNombre("Matemáticas I");
            asMatI.setProfesores(new ArrayList<>());
            em.persist(asMatI);

            Asignatura asMatII = new Asignatura();
            asMatII.setNombre("Matemáticas II");
            asMatII.setProfesores(new ArrayList<>());
            em.persist(asMatII);

            Asignatura asLengua = new Asignatura();
            asLengua.setNombre("Lengua Castellana");
            asLengua.setProfesores(new ArrayList<>());
            em.persist(asLengua);

            Asignatura asIngles = new Asignatura();
            asIngles.setNombre("Inglés");
            asIngles.setProfesores(new ArrayList<>());
            em.persist(asIngles);

            Asignatura asBio = new Asignatura();
            asBio.setNombre("Biología");
            asBio.setProfesores(new ArrayList<>());
            em.persist(asBio);

            Asignatura asGeoLog = new Asignatura();
            asGeoLog.setNombre("Geología");
            asGeoLog.setProfesores(new ArrayList<>());
            em.persist(asGeoLog);

            Asignatura asFisica = new Asignatura();
            asFisica.setNombre("Física");
            asFisica.setProfesores(new ArrayList<>());
            em.persist(asFisica);

            Asignatura asQuimica = new Asignatura();
            asQuimica.setNombre("Química");
            asQuimica.setProfesores(new ArrayList<>());
            em.persist(asQuimica);

            Asignatura asTecnologia = new Asignatura();
            asTecnologia.setNombre("Tecnología");
            asTecnologia.setProfesores(new ArrayList<>());
            em.persist(asTecnologia);

            Asignatura asHistoria = new Asignatura();
            asHistoria.setNombre("Historia");
            asHistoria.setProfesores(new ArrayList<>());
            em.persist(asHistoria);

            Asignatura asGeografia = new Asignatura();
            asGeografia.setNombre("Geografía");
            asGeografia.setProfesores(new ArrayList<>());
            em.persist(asGeografia);

            Asignatura asEconomia = new Asignatura();
            asEconomia.setNombre("Economía");
            asEconomia.setProfesores(new ArrayList<>());
            em.persist(asEconomia);

            Asignatura asFilosofia = new Asignatura();
            asFilosofia.setNombre("Filosofía");
            asFilosofia.setProfesores(new ArrayList<>());
            em.persist(asFilosofia);

            Asignatura asEF = new Asignatura();
            asEF.setNombre("Educación Física");
            asEF.setProfesores(new ArrayList<>());
            em.persist(asEF);

            Asignatura asDibujoTec = new Asignatura();
            asDibujoTec.setNombre("Dibujo Técnico");
            asDibujoTec.setProfesores(new ArrayList<>());
            em.persist(asDibujoTec);

            // =========================
            // 3) CENTROS (5)
            // =========================
            Centro c1 = new Centro();
            c1.setNombre("IES Sierra Norte");
            c1.setLocalidad("Madrid");
            // c1.setProfesores(new ArrayList<>()); // SOLO si tu Centro tiene esa lista bien anotada
            em.persist(c1);

            Centro c2 = new Centro();
            c2.setNombre("IES Valle Verde");
            c2.setLocalidad("Alcalá de Henares");
            em.persist(c2);

            Centro c3 = new Centro();
            c3.setNombre("IES Mar Azul");
            c3.setLocalidad("Getafe");
            em.persist(c3);

            Centro c4 = new Centro();
            c4.setNombre("IES Camino Real");
            c4.setLocalidad("Móstoles");
            em.persist(c4);

            Centro c5 = new Centro();
            c5.setNombre("IES Puerta del Sol");
            c5.setLocalidad("Leganés");
            em.persist(c5);

            // =========================
            // 4) PROFESORES (15, 3 por centro)
            // (1º de cada centro será “jefe” y además director)
            // =========================
            Profesor p1 = new Profesor();
            p1.setNombre("Ana Martín");
            p1.setCentro(c1);
            p1.setEspecialidad(espMat);
            p1.setAsignaturas(new ArrayList<>());
            em.persist(p1);

            Profesor p2 = new Profesor();
            p2.setNombre("Luis García");
            p2.setCentro(c1);
            p2.setEspecialidad(espLen);
            p2.setJefeDepartamento(p1);
            p2.setAsignaturas(new ArrayList<>());
            em.persist(p2);

            Profesor p3 = new Profesor();
            p3.setNombre("Marta López");
            p3.setCentro(c1);
            p3.setEspecialidad(espIng);
            p3.setJefeDepartamento(p1);
            p3.setAsignaturas(new ArrayList<>());
            em.persist(p3);

            Profesor p4 = new Profesor();
            p4.setNombre("Carlos Pérez");
            p4.setCentro(c2);
            p4.setEspecialidad(espFyQ);
            p4.setAsignaturas(new ArrayList<>());
            em.persist(p4);

            Profesor p5 = new Profesor();
            p5.setNombre("Sara Díaz");
            p5.setCentro(c2);
            p5.setEspecialidad(espBio);
            p5.setJefeDepartamento(p4);
            p5.setAsignaturas(new ArrayList<>());
            em.persist(p5);

            Profesor p6 = new Profesor();
            p6.setNombre("Irene Ruiz");
            p6.setCentro(c2);
            p6.setEspecialidad(espGHy);
            p6.setJefeDepartamento(p4);
            p6.setAsignaturas(new ArrayList<>());
            em.persist(p6);

            Profesor p7 = new Profesor();
            p7.setNombre("Javier Torres");
            p7.setCentro(c3);
            p7.setEspecialidad(espTec);
            p7.setAsignaturas(new ArrayList<>());
            em.persist(p7);

            Profesor p8 = new Profesor();
            p8.setNombre("Noelia Sánchez");
            p8.setCentro(c3);
            p8.setEspecialidad(espMat);
            p8.setJefeDepartamento(p7);
            p8.setAsignaturas(new ArrayList<>());
            em.persist(p8);

            Profesor p9 = new Profesor();
            p9.setNombre("Pablo Romero");
            p9.setCentro(c3);
            p9.setEspecialidad(espIng);
            p9.setJefeDepartamento(p7);
            p9.setAsignaturas(new ArrayList<>());
            em.persist(p9);

            Profesor p10 = new Profesor();
            p10.setNombre("Elena Navarro");
            p10.setCentro(c4);
            p10.setEspecialidad(espEF);
            p10.setAsignaturas(new ArrayList<>());
            em.persist(p10);

            Profesor p11 = new Profesor();
            p11.setNombre("Raúl Vega");
            p11.setCentro(c4);
            p11.setEspecialidad(espLen);
            p11.setJefeDepartamento(p10);
            p11.setAsignaturas(new ArrayList<>());
            em.persist(p11);

            Profesor p12 = new Profesor();
            p12.setNombre("Claudia Gil");
            p12.setCentro(c4);
            p12.setEspecialidad(espGHy);
            p12.setJefeDepartamento(p10);
            p12.setAsignaturas(new ArrayList<>());
            em.persist(p12);

            Profesor p13 = new Profesor();
            p13.setNombre("David Molina");
            p13.setCentro(c5);
            p13.setEspecialidad(espBio);
            p13.setAsignaturas(new ArrayList<>());
            em.persist(p13);

            Profesor p14 = new Profesor();
            p14.setNombre("Lucía Herrera");
            p14.setCentro(c5);
            p14.setEspecialidad(espFyQ);
            p14.setJefeDepartamento(p13);
            p14.setAsignaturas(new ArrayList<>());
            em.persist(p14);

            Profesor p15 = new Profesor();
            p15.setNombre("Marcos Cano");
            p15.setCentro(c5);
            p15.setEspecialidad(espTec);
            p15.setJefeDepartamento(p13);
            p15.setAsignaturas(new ArrayList<>());
            em.persist(p15);

            // =========================
            // 5) DIRECTORES (uno por centro)
            // =========================
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

            // =========================
            // 6) ASIGNAR ASIGNATURAS A PROFES
            // (usa tu método auxiliar para mantener ambos lados)
            // =========================
            p1.añadirAsignatura(asMatI);
            p1.añadirAsignatura(asMatII);

            p2.añadirAsignatura(asLengua);
            p2.añadirAsignatura(asFilosofia);

            p3.añadirAsignatura(asIngles);

            p4.añadirAsignatura(asFisica);
            p4.añadirAsignatura(asQuimica);

            p5.añadirAsignatura(asBio);
            p5.añadirAsignatura(asGeoLog);

            p6.añadirAsignatura(asHistoria);
            p6.añadirAsignatura(asGeografia);
            p6.añadirAsignatura(asEconomia);

            p7.añadirAsignatura(asTecnologia);
            p7.añadirAsignatura(asDibujoTec);

            p8.añadirAsignatura(asMatI);
            p8.añadirAsignatura(asMatII);

            p9.añadirAsignatura(asIngles);

            p10.añadirAsignatura(asEF);

            p11.añadirAsignatura(asLengua);
            p11.añadirAsignatura(asFilosofia);

            p12.añadirAsignatura(asHistoria);
            p12.añadirAsignatura(asGeografia);

            p13.añadirAsignatura(asBio);
            p13.añadirAsignatura(asGeoLog);

            p14.añadirAsignatura(asFisica);
            p14.añadirAsignatura(asQuimica);

            p15.añadirAsignatura(asTecnologia);
            p15.añadirAsignatura(asDibujoTec);

            // Guardar la tabla intermedia (owning side = Profesor)
            em.merge(p1);
            em.merge(p2);
            em.merge(p3);
            em.merge(p4);
            em.merge(p5);
            em.merge(p6);
            em.merge(p7);
            em.merge(p8);
            em.merge(p9);
            em.merge(p10);
            em.merge(p11);
            em.merge(p12);
            em.merge(p13);
            em.merge(p14);
            em.merge(p15);

            em.getTransaction().commit();
            return "OK: datos completos insertados (8 esp, 15 asig, 5 centros, 15 profes, directores, jefes, asignaciones).";

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            return "Error cargando datos: " +  ex.getMessage();
        }
    }
}
