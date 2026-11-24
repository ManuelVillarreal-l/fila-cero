package com.filacero.service;

import com.filacero.dto.AuthResponse;
import com.filacero.dto.LoginRequest;
import com.filacero.dto.RegistroRequest;
import com.filacero.exception.CredencialesInvalidasException;
import com.filacero.exception.ReglaNegocioException;
import com.filacero.model.Cliente;
import com.filacero.model.Usuario;
import com.filacero.repository.UsuarioRepository;
import com.filacero.security.JwtServicio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Registro de clientes e inicio de sesión (HU_001, HU_002). */
@Service
public class AuthServicio {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder codificador;
    private final JwtServicio jwt;

    public AuthServicio(UsuarioRepository usuarios, PasswordEncoder codificador, JwtServicio jwt) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.jwt = jwt;
    }

    public AuthResponse registrar(RegistroRequest req) {
        String correo = req.correo().trim().toLowerCase();
        if (usuarios.existsByCorreoIgnoreCase(correo)) {
            throw new ReglaNegocioException("Ya existe una cuenta con el correo " + correo);
        }
        if (usuarios.existsByDocumento(req.documento())) {
            throw new ReglaNegocioException("Ya existe una cuenta con ese documento");
        }
        Cliente cliente = new Cliente(req.nombre().trim(), req.documento(), correo,
                codificador.encode(req.contrasena()), req.telefono(), req.fechaNacimiento(), req.condicion());
        return respuesta(usuarios.save(cliente));
    }

    public AuthResponse iniciarSesion(LoginRequest req) {
        Usuario usuario = usuarios.findByCorreoIgnoreCase(req.correo().trim())
                .orElseThrow(CredencialesInvalidasException::new);
        if (!usuario.verificarContrasena(req.contrasena(), codificador)) {
            throw new CredencialesInvalidasException();
        }
        return respuesta(usuario);
    }

    private AuthResponse respuesta(Usuario u) {
        return new AuthResponse(jwt.generar(u), u.getId(), u.getNombre(), u.getCorreo(), u.getRol(),
                u.descripcionPerfil());
    }
}
