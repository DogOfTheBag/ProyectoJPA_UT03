package alber.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "centros")
@NamedQuery(name = "Centro.findAll",
            query =  "SELECT c FROM Centro c LEFT JOIN FETCH c.director ORDER BY c.nombre")
public class Centro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "localidad")
    private String localidad;

    @OneToMany(mappedBy = "centro")
    private List<Profesor> profesores;

    @OneToOne
    @JoinColumn(name = "id_director", unique = true)
    private Profesor director;

    /*en centro podriamos poner también asignaturas, pero como no lo piden las operaciones pues nada*/


    public Centro() {
    }

    public Centro(String nombre, String localidad) {
        this.nombre = nombre;
        this.localidad = localidad;
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

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public List<Profesor> getProfesores() {
        return profesores;
    }

    public void setProfesores(List<Profesor> profesores) {
        this.profesores = profesores;
    }

    public Profesor getDirector() {
        return director;
    }

    public void setDirector(Profesor director) {
        this.director = director;
    }
}
