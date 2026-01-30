package alber.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "asignaturas")
public class Asignatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre" , nullable = false, unique = true)
    private String nombre;

    @ManyToMany(mappedBy = "asignaturas", cascade = CascadeType.PERSIST)
    private List<Profesor> profesores;

    public Asignatura() {
    }

    public Asignatura(Long id, String nombre, List<Profesor> profesores) {
        this.id = id;
        this.nombre = nombre;
        this.profesores = profesores;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Profesor> getProfesores() {
        return profesores;
    }

    public void setProfesores(List<Profesor> profesores) {
        this.profesores = profesores;
    }
}
