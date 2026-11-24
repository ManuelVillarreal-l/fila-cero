package com.filacero.service;

import com.filacero.dto.TurnoResponse;
import com.filacero.exception.AccesoDenegadoException;
import com.filacero.exception.ReglaNegocioException;
import com.filacero.model.Asesor;
import com.filacero.model.CanalTurno;
import com.filacero.model.ColaAtencion;
import com.filacero.model.EstadoModulo;
import com.filacero.model.EstadoTurno;
import com.filacero.model.Modulo;
import com.filacero.model.Rol;
import com.filacero.model.Sucursal;
import com.filacero.model.TipoPrioridad;
import com.filacero.model.Tramite;
import com.filacero.model.Turno;
import com.filacero.repository.ContadorTurnosRepository;
import com.filacero.repository.ModuloRepository;
import com.filacero.repository.TramiteRepository;
import com.filacero.repository.TurnoRepository;
import com.filacero.repository.UsuarioRepository;
import com.filacero.security.UsuarioActual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtencionServicioTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 24, 10, 0);

    @Mock TurnoRepository turnos;
    @Mock ModuloRepository modulos;
    @Mock UsuarioRepository usuarios;
    @Mock TramiteRepository tramites;
    @Mock ContadorTurnosRepository contador;
    @Mock SucursalServicio sucursales;
    @Mock ColaServicio colas;
    @Mock ApplicationEventPublisher eventos;

    private AtencionServicio servicio;
    private Asesor asesor;
    private Modulo modulo;
    private Sucursal centro;
    private Tramite caja;
    private final UsuarioActual actual = new UsuarioActual("a1", "Laura", Rol.ASESOR);

    @BeforeEach
    void datos() {
        Clock reloj = Clock.fixed(ZonedDateTime.of(AHORA, BOGOTA).toInstant(), BOGOTA);
        servicio = new AtencionServicio(turnos, modulos, usuarios, tramites, contador, sucursales, colas, eventos, reloj);
        centro = new Sucursal("Centro", "x", "Pasto", "1", LocalTime.of(8, 0), LocalTime.of(16, 0), List.of("caja"));
        centro.setId("centro");
        caja = new Tramite("C", "Caja", "", 5);
        caja.setId("caja");
        asesor = new Asesor("Laura", "123456", "l@x.co", "h", "centro");
        asesor.setId("a1");
        modulo = new Modulo("centro", 1, "Caja 1", List.of("caja"));
        modulo.setId("m1");
        modulo.abrir(asesor);
        when(usuarios.findById("a1")).thenReturn(Optional.of(asesor));
    }

    private Turno turno(String codigo, TipoPrioridad p, int minutosEsperando) {
        Turno t = new Turno(codigo, centro, caja, "2026-11-24", p, CanalTurno.KIOSCO, AHORA.minusMinutes(minutosEsperando));
        t.setId(codigo);
        return t;
    }

    @Test
    void llamaAlDeMayorPuntajeYOcupaElModulo() {
        Turno general = turno("C-001", TipoPrioridad.GENERAL, 20);
        Turno preferencial = turno("C-002", TipoPrioridad.PREFERENCIAL, 8);
        when(modulos.findByAsesorId("a1")).thenReturn(Optional.of(modulo));
        when(turnos.findByModuloIdAndEstadoIn(eq("m1"), any())).thenReturn(List.of());
        when(colas.colaDeModulo(modulo)).thenReturn(new ColaAtencion(List.of(general, preferencial), AHORA));
        when(turnos.save(any(Turno.class))).thenAnswer(i -> i.getArgument(0));

        TurnoResponse r = servicio.llamarSiguiente(actual);

        assertEquals("C-002", r.codigo(), "8 + 15 = 23 > 20");
        assertEquals(EstadoTurno.LLAMADO, r.estado());
        assertEquals("Caja 1", r.moduloNombre());
        assertEquals(EstadoModulo.OCUPADO, modulo.getEstado());
        verify(eventos).publishEvent(any(Object.class));
    }

    @Test
    void sinModuloAbiertoNoSePuedeLlamar() {
        when(modulos.findByAsesorId("a1")).thenReturn(Optional.empty());
        assertThrows(ReglaNegocioException.class, () -> servicio.llamarSiguiente(actual));
    }

    @Test
    void noSeLlamaOtroTurnoSiHayUnoEnAtencion() {
        Turno enCurso = turno("C-003", TipoPrioridad.GENERAL, 5);
        when(modulos.findByAsesorId("a1")).thenReturn(Optional.of(modulo));
        when(turnos.findByModuloIdAndEstadoIn(eq("m1"), any())).thenReturn(List.of(enCurso));
        assertThrows(ReglaNegocioException.class, () -> servicio.llamarSiguiente(actual));
    }

    @Test
    void unAsesorNoPuedeFinalizarElTurnoDeOtroModulo() {
        Turno ajeno = turno("C-009", TipoPrioridad.GENERAL, 5);
        ajeno.setModuloId("otro-modulo");
        when(modulos.findByAsesorId("a1")).thenReturn(Optional.of(modulo));
        when(turnos.findById(anyString())).thenReturn(Optional.of(ajeno));
        assertThrows(AccesoDenegadoException.class, () -> servicio.finalizar(actual, "C-009", null));
    }
}
