package alber.model;

import jakarta.persistence.*;

import java.util.List;

/*La dinamica de estas clases va a ser igual que en un programa normal, simplemente usando las anotaciones de JPA
* para que se cree la base de datos automaticamente
* Si el ejercicio no especifica que algo pueda ser null, como jefe de departamento o Director, lo marco como que no se puede nullable = false
* Por lo demás lo que hemos visto en clase, Entity, Column, el joinColumn si son FK...*/
@Entity
@Table(name = "profesores")
public class Profesor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    //un profe tiene una especialidad, una especialidad la pueden tener varios profes
    @ManyToOne(optional = false)
    @JoinColumn(name = "especialidad_id", nullable = false)
    private Especialidad especialidad;

    //un profe pertenece a un centro, un centro puede tener varios profes
    @ManyToOne(optional = false)
    @JoinColumn(name = "centro_id", nullable = false)
    private Centro centro;

    //igual que antes, asi vamos sacando anotaciones
    @ManyToOne
    @JoinColumn(name = "jefe_id")
    private Profesor jefeDepartamento;

    /*ManyToMany con las asignaturas, se hace una JoinTable en la que marcamos las columnas con los ids de ambos*/
    @ManyToMany()
    @JoinTable(name = "profesor_asignatura",
            joinColumns = @JoinColumn(name= "profesor_id"),
            inverseJoinColumns = @JoinColumn(name = "asignatura_id"))
    private List<Asignatura> asignaturas;


    public Profesor() {
    }


    /****MÉTODOS AUXILIARES****/
    /*para añadir una asignatura comprobamos que no sea null,
    * despues vemos si el profesor ya da la asignatura, si no la da la metemos
    * y por ultimo comprobamos la lista de profesores de la asignatura, y si no esta añadimos este profe a esa lista*/
    public void añadirAsignatura(Asignatura asignatura) {
        if(asignatura == null) return;
        if(!this.asignaturas.contains(asignatura)) {this.asignaturas.add(asignatura);}
        if(!asignatura.getProfesores().contains(this)) {asignatura.getProfesores().add(this);}
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
