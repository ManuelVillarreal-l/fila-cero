package com.filacero.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * Usuario registrado del sistema. Clase abstracta: Cliente, Asesor y Administrador heredan de ella
 * y se guardan en la misma colección "usuarios" (MongoDB guarda el tipo en el campo _class).
 */
@Document(collection = "usuarios")
public abstract class Usuario {

    @Id
    private String id;
    private String nombre;
    @Indexed(unique = true)
    private String documento;
    @Indexed(unique = true)
    private String correo;
    private String contrasena;
    private Rol rol;
    private boolean activo = true;
    private LocalDateTime fechaRegistro;

    protected Usuario() {
    }

    protected Usuario(String nombre, String documento, String correo, String contrasena, Rol rol) {
        this.nombre = nombre;
        this.documento = documento;
        this.correo = correo;
        this.contrasena = contrasena;
        this.rol = rol;
        this.fechaRegistro = LocalDateTime.now();
    }

    /** GRASP Experto: el usuario conoce su contraseña cifrada y sabe verificarla. */
    public boolean verificarContrasena(String contrasenaPlana, PasswordEncoder codificador) {
        return activo && contrasenaPlana != null && codificador.matches(contrasenaPlana, contrasena);
    }

    /** Descripción corta que cada tipo de usuario muestra en la app (polimorfismo). */
    public abstract String descripcionPerfil();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
