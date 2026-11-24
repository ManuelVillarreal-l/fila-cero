package com.filacero.service;

import com.filacero.dto.AsesorRequest;
import com.filacero.dto.AsesorResponse;
import com.filacero.exception.RecursoNoEncontradoException;
import com.filacero.exception.ReglaNegocioException;
import com.filacero.model.Asesor;
import com.filacero.model.Rol;
import com.filacero.model.Sucursal;
import com.filacero.model.Usuario;
import com.filacero.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/** El administrador crea y gestiona las cuentas de los asesores (HU_006). */
@Service
public class AsesorServicio {

    private final UsuarioRepository usuarios;
    private final SucursalServicio sucursales;
    private final PasswordEncoder codificador;

    public AsesorServicio(UsuarioRepository usuarios, SucursalServicio sucursales, PasswordEncoder codificador) {
        this.usuarios = usuarios;
        this.sucursales = sucursales;
        this.codificador = codificador;
    }

    public List<AsesorResponse> listar(String sucursalId) {
        return usuarios.findByRol(Rol.ASESOR).stream()
                .map(Asesor.class::cast)
                .filter(a -> sucursalId == null || sucursalId.equals(a.getSucursalId()))
                .map(this::respuesta)
                .toList();
    }

    public Asesor obtener(String id) {
        Usuario u = usuarios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Asesor", id));
        if (!(u instanceof Asesor asesor)) {
            throw new RecursoNoEncontradoException("Asesor", id);
        }
        return asesor;
    }

    public AsesorResponse crear(AsesorRequest req) {
        if (req.contrasena() == null || req.contrasena().isBlank()) {
            throw new ReglaNegocioException("La contraseña es obligatoria al crear un asesor");
        }
        sucursales.obtener(req.sucursalId());
        validarUnicos(null, req);
        Asesor a = new Asesor(req.nombre(), req.documento(), req.correo().toLowerCase(),
                codificador.encode(req.contrasena()), req.sucursalId());
        return respuesta(usuarios.save(a));
    }

    public AsesorResponse actualizar(String id, AsesorRequest req) {
        Asesor a = obtener(id);
        sucursales.obtener(req.sucursalId());
        validarUnicos(a, req);
        if (a.getModuloId() != null && !a.getSucursalId().equals(req.sucursalId())) {
            throw new ReglaNegocioException("El asesor tiene un módulo abierto: debe cerrarlo antes de cambiar de sucursal");
        }
        a.setNombre(req.nombre());
        a.setDocumento(req.documento());
        a.setCorreo(req.correo().toLowerCase());
        a.setSucursalId(req.sucursalId());
        a.setActivo(true);
        if (req.contrasena() != null && !req.contrasena().isBlank()) {
            a.setContrasena(codificador.encode(req.contrasena()));
        }
        return respuesta(usuarios.save(a));
    }

    public void desactivar(String id) {
        Asesor a = obtener(id);
        if (a.getModuloId() != null) {
            throw new ReglaNegocioException("El asesor tiene un módulo abierto");
        }
        a.setActivo(false);
        usuarios.save(a);
    }

    private void validarUnicos(Asesor actual, AsesorRequest req) {
        usuarios.findByCorreoIgnoreCase(req.correo()).filter(u -> actual == null || !Objects.equals(u.getId(), actual.getId()))
                .ifPresent(u -> { throw new ReglaNegocioException("Ya existe un usuario con ese correo"); });
        usuarios.findByDocumento(req.documento()).filter(u -> actual == null || !Objects.equals(u.getId(), actual.getId()))
                .ifPresent(u -> { throw new ReglaNegocioException("Ya existe un usuario con ese documento"); });
    }

    private AsesorResponse respuesta(Asesor a) {
        Sucursal s = sucursales.obtener(a.getSucursalId());
        return AsesorResponse.desde(a, s.getNombre());
    }
}
