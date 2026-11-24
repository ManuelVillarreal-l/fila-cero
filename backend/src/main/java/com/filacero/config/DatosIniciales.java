package com.filacero.config;

import com.filacero.model.Administrador;
import com.filacero.model.Asesor;
import com.filacero.model.CanalTurno;
import com.filacero.model.Cliente;
import com.filacero.model.CondicionEspecial;
import com.filacero.model.EstadoTurno;
import com.filacero.model.Modulo;
import com.filacero.model.Rol;
import com.filacero.model.Sucursal;
import com.filacero.model.TipoPrioridad;
import com.filacero.model.Tramite;
import com.filacero.model.Turno;
import com.filacero.repository.ContadorTurnosRepository;
import com.filacero.repository.ModuloRepository;
import com.filacero.repository.SucursalRepository;
import com.filacero.repository.TramiteRepository;
import com.filacero.repository.TurnoRepository;
import com.filacero.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Datos de prueba del Banco del Galeras (banco ficticio). Si la base está vacía crea sucursales,
 * trámites, módulos y cuentas. En cada arranque completa el historial de los últimos 14 días y unos
 * turnos en espera de hoy, para que el tablero y la cola siempre tengan datos en la demostración.
 * Se desactiva con DATOS_PRUEBA=false.
 */
@Component
public class DatosIniciales implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);
    private static final int DIAS_HISTORIAL = 14;
    private static final String[] NOMBRES = {"Camila Rosero", "Juan Pablo Eraso", "Valentina Benavides", "Santiago Burbano",
            "Daniela Delgado", "Mateo Guerrero", "Isabella Muñoz", "Sebastián Ortega", "Mariana Insuasty", "Nicolás Bravo",
            "Gabriela Chamorro", "Samuel Narváez", "Sara Villota", "Tomás Enríquez", "Luciana Zarama", "Emilio Pantoja"};
    private static final String[] COMENTARIOS = {"Muy amable", "Rápido y claro", "Me explicaron todo", null, null,
            "Demoró un poco", "Excelente atención", null};

    private final UsuarioRepository usuarios;
    private final SucursalRepository sucursales;
    private final TramiteRepository tramites;
    private final ModuloRepository modulos;
    private final TurnoRepository turnos;
    private final ContadorTurnosRepository contador;
    private final PasswordEncoder codificador;
    private final Clock reloj;
    private final boolean activo;

    public DatosIniciales(UsuarioRepository usuarios, SucursalRepository sucursales, TramiteRepository tramites,
                          ModuloRepository modulos, TurnoRepository turnos, ContadorTurnosRepository contador,
                          PasswordEncoder codificador, Clock reloj,
                          @Value("${filacero.datos-prueba:true}") boolean activo) {
        this.usuarios = usuarios;
        this.sucursales = sucursales;
        this.tramites = tramites;
        this.modulos = modulos;
        this.turnos = turnos;
        this.contador = contador;
        this.codificador = codificador;
        this.reloj = reloj;
        this.activo = activo;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!activo) {
            return;
        }
        if (sucursales.count() == 0) {
            crearCatalogo();
        }
        completarHistorial();
    }

    private void crearCatalogo() {
        log.info("Base vacía: cargando datos de prueba del Banco del Galeras");
        Tramite caja = tramites.save(new Tramite("C", "Caja", "Depósitos, retiros, pagos de tarjetas y servicios", 5));
        Tramite asesoria = tramites.save(new Tramite("A", "Asesoría y productos",
                "Apertura de cuentas, CDT, tarjetas y seguros", 15));
        Tramite creditos = tramites.save(new Tramite("K", "Créditos", "Libre inversión, vivienda y vehículo", 20));
        Tramite empresas = tramites.save(new Tramite("E", "Empresas", "Nómina, recaudos y cuentas empresariales", 12));
        Tramite servicio = tramites.save(new Tramite("S", "Servicio al cliente",
                "Certificados, reclamos y bloqueo de productos", 10));

        List<String> todos = List.of(caja.getId(), asesoria.getId(), creditos.getId(), empresas.getId(), servicio.getId());
        Sucursal centro = sucursales.save(new Sucursal("Sucursal Centro", "Calle 18 # 24-35, Centro", "Pasto",
                "6027201010", LocalTime.of(6, 0), LocalTime.of(23, 0), todos));
        Sucursal panamericana = sucursales.save(new Sucursal("Sucursal Panamericana", "Av. Panamericana # 12-50",
                "Pasto", "6027202020", LocalTime.of(8, 0), LocalTime.of(17, 0),
                List.of(caja.getId(), asesoria.getId(), creditos.getId(), servicio.getId())));
        Sucursal torobajo = sucursales.save(new Sucursal("Sucursal Torobajo", "Calle 50 # 2-10, Torobajo", "Pasto",
                "6027203030", LocalTime.of(8, 0), LocalTime.of(16, 0),
                List.of(caja.getId(), asesoria.getId(), servicio.getId())));

        modulos.saveAll(List.of(
                new Modulo(centro.getId(), 1, "Caja 1", List.of(caja.getId())),
                new Modulo(centro.getId(), 2, "Caja 2", List.of(caja.getId())),
                new Modulo(centro.getId(), 3, "Caja 3", List.of(caja.getId(), empresas.getId())),
                new Modulo(centro.getId(), 4, "Asesoría 1", List.of(asesoria.getId(), servicio.getId())),
                new Modulo(centro.getId(), 5, "Asesoría 2", List.of(asesoria.getId(), creditos.getId())),
                new Modulo(centro.getId(), 6, "Créditos", List.of(creditos.getId())),
                new Modulo(panamericana.getId(), 1, "Caja 1", List.of(caja.getId())),
                new Modulo(panamericana.getId(), 2, "Caja 2", List.of(caja.getId())),
                new Modulo(panamericana.getId(), 3, "Asesoría", List.of(asesoria.getId(), creditos.getId(), servicio.getId())),
                new Modulo(torobajo.getId(), 1, "Caja 1", List.of(caja.getId())),
                new Modulo(torobajo.getId(), 2, "Asesoría", List.of(asesoria.getId(), servicio.getId()))));

        String claveAsesor = codificador.encode("Asesor123*");
        usuarios.saveAll(List.of(
                new Administrador("Manuel Villarreal", "1085000001", "admin@filacero.co",
                        codificador.encode("Admin123*"), "Gerente de operaciones"),
                new Asesor("Laura Caicedo", "1085000101", "asesor1@filacero.co", claveAsesor, centro.getId()),
                new Asesor("Andrés Mora", "1085000102", "asesor2@filacero.co", claveAsesor, centro.getId()),
                new Asesor("Paola Jurado", "1085000103", "asesor3@filacero.co", claveAsesor, centro.getId()),
                new Asesor("Diego Rivas", "1085000104", "asesor4@filacero.co", claveAsesor, centro.getId()),
                new Asesor("Natalia Erazo", "1085000105", "asesor5@filacero.co", claveAsesor, panamericana.getId()),
                new Asesor("Felipe Timaná", "1085000106", "asesor6@filacero.co", claveAsesor, panamericana.getId()),
                new Asesor("Carolina Yépez", "1085000107", "asesor7@filacero.co", claveAsesor, torobajo.getId())));

        String claveCliente = codificador.encode("Cliente123*");
        usuarios.save(new Cliente("Carlos Benavides", "1085100001", "cliente@filacero.co", claveCliente,
                "3001234567", LocalDate.of(1998, 3, 14), CondicionEspecial.NINGUNA));
        usuarios.save(new Cliente("Rosa Elena Ortiz", "1085100002", "rosa@filacero.co", claveCliente,
                "3007654321", LocalDate.of(1954, 8, 2), CondicionEspecial.NINGUNA));
        usuarios.save(new Cliente("Ana Lucía Paz", "1085100003", "ana@filacero.co", claveCliente,
                "3015558899", LocalDate.of(1994, 11, 20), CondicionEspecial.EMBARAZO));
        for (int i = 0; i < NOMBRES.length; i++) {
            usuarios.save(new Cliente(NOMBRES[i], String.valueOf(1085200000L + i), "cliente" + (i + 1) + "@filacero.co",
                    claveCliente, "300" + (1000000 + i * 7919), LocalDate.of(1960 + (i * 3) % 45, 1 + i % 12, 1 + i),
                    CondicionEspecial.NINGUNA));
        }
    }

    /** Genera turnos atendidos de los días que falten y algunos en espera para hoy. */
    private void completarHistorial() {
        LocalDate hoy = LocalDate.now(reloj);
        Map<String, Tramite> porId = new HashMap<>();
        tramites.findAll().forEach(t -> porId.put(t.getId(), t));
        List<Cliente> clientes = usuarios.findByRol(Rol.CLIENTE).stream()
                .map(Cliente.class::cast).filter(c -> c.getCorreo().startsWith("cliente") && !c.getCorreo().startsWith("cliente@"))
                .toList();
        if (clientes.isEmpty()) {
            return;
        }
        int generados = 0;
        for (Sucursal s : sucursales.findByActivaTrueOrderByNombre()) {
            List<Modulo> mods = modulos.findBySucursalIdOrderByNumero(s.getId());
            List<Asesor> asesores = usuarios.findByRol(Rol.ASESOR).stream().map(Asesor.class::cast)
                    .filter(a -> a.perteneceA(s.getId())).toList();
            if (mods.isEmpty() || asesores.isEmpty()) {
                continue;
            }
            for (int d = DIAS_HISTORIAL; d >= 1; d--) {
                LocalDate dia = hoy.minusDays(d);
                if (dia.getDayOfWeek().getValue() == 7) {
                    continue; // los domingos no se atiende
                }
                if (!turnos.findBySucursalIdAndFechaBetween(s.getId(), dia.minusDays(1).toString(),
                        dia.plusDays(1).toString()).isEmpty()) {
                    continue;
                }
                generados += generarDia(s, dia, mods, asesores, porId, clientes);
            }
            if (s.getNombre().contains("Centro") && turnos.findBySucursalIdAndFechaAndEstado(s.getId(), hoy.toString(),
                    EstadoTurno.EN_ESPERA).isEmpty()) {
                generados += generarEsperaHoy(s, porId, clientes);
            }
        }
        if (generados > 0) {
            log.info("Datos de prueba: {} turnos generados", generados);
        }
    }

    private int generarDia(Sucursal s, LocalDate dia, List<Modulo> mods, List<Asesor> asesores,
                           Map<String, Tramite> tramitesPorId, List<Cliente> clientes) {
        Random r = new Random(dia.toEpochDay() * 31 + s.getNombre().hashCode());
        boolean sabado = dia.getDayOfWeek().getValue() == 6;
        int cantidad = (sabado ? 12 : 25) + r.nextInt(sabado ? 10 : 25);
        Map<String, Integer> consecutivos = new HashMap<>();
        List<Turno> lista = new ArrayList<>();
        int apertura = Math.max(8, s.getHoraApertura().getHour());
        int cierre = Math.min(sabado ? 12 : 17, s.getHoraCierre().getHour());
        for (int i = 0; i < cantidad; i++) {
            Modulo m = mods.get(r.nextInt(mods.size()));
            Tramite t = tramitesPorId.get(m.getTramitesIds().get(r.nextInt(m.getTramitesIds().size())));
            // Picos de clientes a media mañana y a mediodía.
            int hora = apertura + (int) Math.min(cierre - apertura - 1, Math.abs(r.nextGaussian() * 2.2 + 2.5));
            LocalDateTime creado = dia.atTime(hora, r.nextInt(60));
            int consecutivo = consecutivos.merge(t.getId(), 1, Integer::sum);
            double dado = r.nextDouble();
            TipoPrioridad prioridad = dado < 0.12 ? TipoPrioridad.PREFERENCIAL : dado < 0.2 ? TipoPrioridad.CITA
                    : TipoPrioridad.GENERAL;
            CanalTurno canal = prioridad == TipoPrioridad.CITA ? CanalTurno.CITA
                    : r.nextDouble() < 0.55 ? CanalTurno.APP : CanalTurno.KIOSCO;
            Turno turno = new Turno(t.codigoTurno(consecutivo), s, t, dia.toString(), prioridad, canal, creado);
            Cliente c = clientes.get(r.nextInt(clientes.size()));
            turno.asignarCliente(canal == CanalTurno.KIOSCO ? null : c.getId(), c.getNombre(), c.getDocumento());
            Asesor a = asesores.get(r.nextInt(asesores.size()));
            int espera = prioridad == TipoPrioridad.GENERAL ? 4 + r.nextInt(22) : 1 + r.nextInt(8);
            LocalDateTime llamado = creado.plusMinutes(espera);
            double destino = r.nextDouble();
            turno.llamar(m, a, llamado);
            if (destino < 0.07) {
                turno.marcarAusente(llamado.plusMinutes(3));
            } else {
                LocalDateTime inicio = llamado.plusMinutes(1);
                turno.iniciarAtencion(inicio);
                int dur = Math.max(2, (int) Math.round(t.getTiempoPromedioMin() * (0.6 + r.nextDouble() * 0.9)));
                turno.finalizar(null, inicio.plusMinutes(dur));
                if (turno.getClienteId() != null && r.nextDouble() < 0.6) {
                    int estrellas = r.nextDouble() < 0.7 ? 5 : r.nextDouble() < 0.7 ? 4 : 3;
                    turno.calificar(estrellas, COMENTARIOS[r.nextInt(COMENTARIOS.length)]);
                }
            }
            lista.add(turno);
        }
        // Algunos clientes cancelan desde la app antes de ser llamados.
        for (int i = 0; i < 1 + r.nextInt(3); i++) {
            Tramite t = tramitesPorId.get(s.getTramitesIds().get(0));
            Turno cancelado = new Turno(t.codigoTurno(consecutivos.merge(t.getId(), 1, Integer::sum)), s, t,
                    dia.toString(), TipoPrioridad.GENERAL, CanalTurno.APP, dia.atTime(apertura + 1 + i, 15));
            Cliente c = clientes.get(r.nextInt(clientes.size()));
            cancelado.asignarCliente(c.getId(), c.getNombre(), c.getDocumento());
            cancelado.cancelar(cancelado.getHoraCreacion().plusMinutes(6));
            lista.add(cancelado);
        }
        turnos.saveAll(lista);
        return lista.size();
    }

    /** Unos turnos de kiosco en espera para que la cola de hoy no esté vacía en la demo. */
    private int generarEsperaHoy(Sucursal s, Map<String, Tramite> tramitesPorId, List<Cliente> clientes) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        if (!s.estaAbierta(ahora.toLocalTime())) {
            return 0;
        }
        String hoy = ahora.toLocalDate().toString();
        Random r = new Random(ahora.toLocalDate().toEpochDay());
        List<String> ids = s.getTramitesIds();
        String[] prefijosDemo = {"C", "C", "C", "A", "C", "A", "S"};
        int creados = 0;
        for (int i = 0; i < prefijosDemo.length; i++) {
            String prefijo = prefijosDemo[i];
            Tramite t = ids.stream().map(tramitesPorId::get).filter(x -> x != null && x.getPrefijo().equals(prefijo))
                    .findFirst().orElse(null);
            if (t == null) {
                continue;
            }
            TipoPrioridad p = i == 4 ? TipoPrioridad.PREFERENCIAL : TipoPrioridad.GENERAL;
            Turno turno = new Turno(t.codigoTurno(contador.siguiente(s.getId(), t.getId(), hoy)), s, t, hoy, p,
                    CanalTurno.KIOSCO, ahora.minusMinutes(25L - i * 3L));
            Cliente c = clientes.get(r.nextInt(clientes.size()));
            turno.asignarCliente(null, c.getNombre(), c.getDocumento());
            turnos.save(turno);
            creados++;
        }
        return creados;
    }

}
