package org.ceindetec.d3rumb4;

public class CancionBiblioteca {

    String nombre;
    String id;

    CancionBiblioteca(String nombre, String id) {
        this.nombre = nombre;
        this.id = id;

    }
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
