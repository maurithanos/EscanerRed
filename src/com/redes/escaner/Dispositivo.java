package com.redes.escaner;

public class Dispositivo {

    private final String ip;
    private final String nombre;
    private final boolean activo;
    private final long tiempoMs;

    public Dispositivo(
            String ip,
            String nombre,
            boolean activo,
            long tiempoMs) {

        this.ip = ip;
        this.nombre = nombre;
        this.activo = activo;
        this.tiempoMs = tiempoMs;
    }

    public String getIp() {
        return ip;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isActivo() {
        return activo;
    }

    public long getTiempoMs() {
        return tiempoMs;
    }

    @Override
    public String toString() {
        return "Dispositivo{" +
                "ip='" + ip + '\'' +
                ", nombre='" + nombre + '\'' +
                ", activo=" + activo +
                ", tiempoMs=" + tiempoMs +
                '}';
    }
}
