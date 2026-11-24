package com.filacero.service;

import com.filacero.dto.TomarTurnoRequest;
import com.filacero.dto.TurnoResponse;
import com.filacero.exception.ReglaNegocioException;
import com.filacero.exception.SucursalCerradaException;
import com.filacero.model.CanalTurno;
import com.filacero.model.Cliente;
import com.filacero.model.CondicionEspecial;
import com.filacero.model.EventoTurno;
import com.filacero.model.Rol;
import com.filacero.model.Sucursal;
import com.filacero.model.TipoPrioridad;
import com.filacero.model.Tramite;
import com.filacero.model.Turno;
import com.filacero.repository.ContadorTurnosRepository;
import com.filacero.repository.SucursalRepository;
import com.filacero.repository.TramiteRepository;
import com.filacero.repository.TurnoRepository;
import com.filacero.repository.UsuarioRepository;
import com.filacero.security.UsuarioActual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.LocalDate;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TurnoServicioTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    @Mock TurnoRepository turnos;
    @Mock SucursalRepository sucursales;
    @Mock TramiteRepository tramites;
    @Mock UsuarioRepository usuarios;
    @Mock ContadorTurnosRepository contador;
    @Mock ColaServicio colas;
    @Mock ApplicationEventPublisher eventos;

    private Sucursal centro;
    private Tramite caja;
    private final UsuarioActual actual = new UsuarioActual("cli", "Rosa", Rol.CLIENTE);

    private TurnoServicio servicio(int hora) {
        Clock reloj = Clock.fixed(ZonedDateTime.of(2026, 11, 24, hora, 0, 0, 0, BOGOTA).toInstant(), BOGOTA);
        return new TurnoServicio(turnos, sucursales, tramites, usuarios, contador, colas, eventos, reloj);
    }

    @BeforeEach
    void datos() {
        centro = new Sucursal("Sucursal Centro", "Calle 18", "Pasto", "1", LocalTime.of(8, 0), LocalTime.of(16, 0),
                List.of("caja"));
        centro.setId("centro");
        caja = new Tramite("C", "Caja", "", 5);
        caja.setId("caja");
    }

    private Cliente cliente(LocalDate nacimiento) {
        Cliente c = new Cliente("Rosa", "1085100002", "rosa@x.co", "h", "3001234567", nacimiento,
                CondicionEspecial.NINGUNA);
        c.setId("cli");
        return c;
    }

    @Test
    void creaElTurnoConCodigoDelDiaYPrioridadPreferencialParaAdultoMayor() {
        when(usuarios.findById("cli")).thenReturn(Optional.of(cliente(LocalDate.of(1950, 5, 5))));
        when(sucursales.findById("centro")).thenReturn(Optional.of(centro));
        when(tramites.findById("caja")).thenReturn(Optional.of(caja));
        when(turnos.findByClienteIdAndFechaAndEstadoIn(eq("cli"), eq("2026-11-24"), any())).thenReturn(List.of());
        when(contador.siguiente("centro", "caja", "2026-11-24")).thenReturn(7);
        when(turnos.save(any(Turno.class))).thenAnswer(i -> i.getArgument(0));
        when(colas.enriquecer(any())).thenAnswer(i -> TurnoResponse.desde(i.getArgument(0)));

        TurnoResponse r = servicio(10).tomarTurno(actual, new TomarTurnoRequest("centro", "caja"));

        assertEquals("C-007", r.codigo());
        assertEquals(TipoPrioridad.PREFERENCIAL, r.prioridad());
        assertEquals(CanalTurno.APP, r.canal());
        ArgumentCaptor<EventoTurno> evento = ArgumentCaptor.forClass(EventoTurno.class);
        verify(eventos).publishEvent(evento.capture());
        assertEquals("C-007", evento.getValue().turno().getCodigo());
    }

    @Test
    void rechazaUnSegundoTurnoActivo() {
        Turno activo = new Turno("C-001", centro, caja, "2026-11-24", TipoPrioridad.GENERAL, CanalTurno.APP,
                ZonedDateTime.now().toLocalDateTime());
        when(usuarios.findById("cli")).thenReturn(Optional.of(cliente(LocalDate.of(2000, 1, 1))));
        when(sucursales.findById("centro")).thenReturn(Optional.of(centro));
        when(tramites.findById("caja")).thenReturn(Optional.of(caja));
        when(turnos.findByClienteIdAndFechaAndEstadoIn(eq("cli"), anyString(), any())).thenReturn(List.of(activo));

        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> servicio(10).tomarTurno(actual, new TomarTurnoRequest("centro", "caja")));
        assertEquals(true, e.getMessage().contains("C-001"));
        verify(turnos, never()).save(any());
    }

    @Test
    void rechazaTurnosFueraDelHorario() {
        when(usuarios.findById("cli")).thenReturn(Optional.of(cliente(LocalDate.of(2000, 1, 1))));
        when(sucursales.findById("centro")).thenReturn(Optional.of(centro));
        assertThrows(SucursalCerradaException.class,
                () -> servicio(19).tomarTurno(actual, new TomarTurnoRequest("centro", "caja")));
        verify(contador, never()).siguiente(anyString(), anyString(), anyString());
    }

    @Test
    void rechazaTramitesQueLaSucursalNoOfrece() {
        Tramite creditos = new Tramite("K", "Créditos", "", 20);
        creditos.setId("creditos");
        when(usuarios.findById("cli")).thenReturn(Optional.of(cliente(LocalDate.of(2000, 1, 1))));
        when(sucursales.findById("centro")).thenReturn(Optional.of(centro));
        when(tramites.findById("creditos")).thenReturn(Optional.of(creditos));
        assertThrows(ReglaNegocioException.class,
                () -> servicio(10).tomarTurno(actual, new TomarTurnoRequest("centro", "creditos")));
    }
}
