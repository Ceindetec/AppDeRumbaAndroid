package org.ceindetec.d3rumb4;

public class CancionOnline {

    String nombre;
    String duracion;
    String agregadoPor;

    CancionOnline(String nombre, String duracion, String agregadoPor) {
        this.nombre = nombre;
        this.duracion = duracion;
        this.agregadoPor = agregadoPor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public String getAgregadoPor() {
        return agregadoPor;
    }

    public void setAgregadoPor(String agregadoPor) {
        this.agregadoPor = agregadoPor;
    }

}
