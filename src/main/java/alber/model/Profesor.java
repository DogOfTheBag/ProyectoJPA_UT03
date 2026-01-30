package alber.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "profesores")
public class Profesor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @ManyToOne(optional = false)
    @JoinColumn(name = "especialidad_id", nullable = false)
    private Especialidad especialidad;

    @ManyToOne(optional = false)
    @JoinColumn(name = "centro_id", nullable = false)
    private Centro centro;

    @ManyToOne
    @JoinColumn(name = "jefe_id")
    private Profesor jefeDepartamento;


    @ManyToMany()
    @JoinTable(name = "profesor_asignatura",
            joinColumns = @JoinColumn(name= "profesor_id"),
            inverseJoinColumns = @JoinColumn(name = "asignatura_id"))
    private List<Asignatura> asignaturas;


    public Profesor() {
    }

    public Profesor(String nombre, Especialidad especialidad) {
        this.nombre = nombre;
        this.especialidad = especialidad;
    }

    /****MÉTODOS AUXILIARES****/
    public void añadirAsignatura(Asignatura asignatura) {
        if(asignatura == null) return;
        if(!this.asignaturas.contains(asignatura)) {this.asignaturas.add(asignatura);}
        if(!asignatura.getProfesores().contains(this)) {asignatura.getProfesores().add(this);}
    }

    public void eliminarAsignatura(Asignatura asignatura) {
        if(asignatura == null) return;
        this.asignaturas.remove(asignatura);
        asignatura.getProfesores().remove(this);
    }

    /****MÉTODOS AUXILIARES****/

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

    public Especialidad getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(Especialidad especialidad) {
        this.especialidad = especialidad;
    }

    public Centro getCentro() {
        return centro;
    }

    public void setCentro(Centro centro) {
        this.centro = centro;
    }

    public Profesor getJefeDepartamento() {
        return jefeDepartamento;
    }

    public void setJefeDepartamento(Profesor jefeDepartamento) {
        this.jefeDepartamento = jefeDepartamento;
    }

    public List<Asignatura> getAsignaturas() {
        return asignaturas;
    }

    public void setAsignaturas(List<Asignatura> asignaturas) {
        this.asignaturas = asignaturas;
    }
}
