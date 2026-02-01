package alber.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "asignaturas")
@NamedQuery(name = "Asignatura.findAll",
                query ="Select a from Asignatura a ORDER BY a.nombre")
public class Asignatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre" , nullable = false, unique = true)
    private String nombre;

    @ManyToMany(mappedBy = "asignaturas")
    private List<Profesor> profesores;

    public Asignatura() {
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
