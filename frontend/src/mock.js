// Datos de ejemplo para generar los mockups de las historias sin backend (solo con VITE_MOCK=1).
const hoy = new Date();
const dia = hoy.toLocaleDateString('en-CA');
const h = (hh, mm) => `${dia}T${String(hh).padStart(2, '0')}:${String(mm).padStart(2, '0')}:00`;
const T = { C: 't-c', A: 't-a', K: 't-k', E: 't-e', S: 't-s' };
const tramites = [
  { id: T.C, prefijo: 'C', nombre: 'Caja', descripcion: 'Depósitos, retiros, pagos de tarjetas y servicios', tiempoPromedioMin: 5, activo: true },
  { id: T.A, prefijo: 'A', nombre: 'Asesoría y productos', descripcion: 'Apertura de cuentas, CDT, tarjetas y seguros', tiempoPromedioMin: 15, activo: true },
  { id: T.K, prefijo: 'K', nombre: 'Créditos', descripcion: 'Libre inversión, vivienda y vehículo', tiempoPromedioMin: 20, activo: true },
  { id: T.E, prefijo: 'E', nombre: 'Empresas', descripcion: 'Nómina, recaudos y cuentas empresariales', tiempoPromedioMin: 12, activo: true },
  { id: T.S, prefijo: 'S', nombre: 'Servicio al cliente', descripcion: 'Certificados, reclamos y bloqueo de productos', tiempoPromedioMin: 10, activo: true },
];
const fila = (t, enEspera, modulosAbiertos) => {
  const tr = tramites.find((x) => x.id === t);
  return { tramiteId: t, tramiteNombre: tr.nombre, prefijo: tr.prefijo, enEspera, modulosAbiertos, tiempoEstimadoMin: Math.ceil((enEspera * tr.tiempoPromedioMin) / Math.max(1, modulosAbiertos)) };
};
const sucursales = [
  { id: 's1', nombre: 'Sucursal Centro', direccion: 'Calle 18 # 24-35, Centro', ciudad: 'Pasto', telefono: '6027201010', horaApertura: '06:00:00', horaCierre: '23:00:00', activa: true, abiertaAhora: true,
    filas: [fila(T.C, 6, 2), fila(T.A, 3, 1), fila(T.K, 2, 1), fila(T.E, 1, 1), fila(T.S, 0, 1)] },
  { id: 's2', nombre: 'Sucursal Panamericana', direccion: 'Av. Panamericana # 12-50', ciudad: 'Pasto', telefono: '6027202020', horaApertura: '08:00:00', horaCierre: '17:00:00', activa: true, abiertaAhora: true,
    filas: [fila(T.C, 3, 2), fila(T.A, 1, 1), fila(T.K, 0, 1), fila(T.S, 1, 1)] },
  { id: 's3', nombre: 'Sucursal Torobajo', direccion: 'Calle 50 # 2-10, Torobajo', ciudad: 'Pasto', telefono: '6027203030', horaApertura: '08:00:00', horaCierre: '16:00:00', activa: true, abiertaAhora: false,
    filas: [fila(T.C, 0, 0), fila(T.A, 0, 0), fila(T.S, 0, 0)] },
];
sucursales.forEach((s) => { s.totalEnEspera = s.filas.reduce((a, f) => a + f.enEspera, 0); });
const turno = (o) => ({ id: 'tu-' + o.codigo, sucursalId: 's1', sucursalNombre: 'Sucursal Centro', canal: 'APP', prioridad: 'GENERAL', vecesLlamado: 0, horaCreacion: h(9, 41), clienteNombre: 'Carlos Benavides', clienteDocumento: '****0001', ...o });
const miTurno = turno({ codigo: 'C-014', tramiteId: T.C, tramiteNombre: 'Caja', estado: 'EN_ESPERA', posicion: 3, personasAntes: 2, tiempoEstimadoMin: 5 });
const notifs = [
  { id: 'n2', tipo: 'TURNO_PROXIMO', titulo: 'Tu turno C-014 está cerca', mensaje: 'Falta 1 persona antes que tú en Sucursal Centro.', fecha: h(9, 52), leida: false },
  { id: 'n1', tipo: 'TURNO_CREADO', titulo: 'Turno C-014 confirmado', mensaje: 'Estás en la fila de Caja en Sucursal Centro.', fecha: h(9, 41), leida: true },
];
const historial = [
  turno({ codigo: 'A-006', tramiteId: T.A, tramiteNombre: 'Asesoría y productos', estado: 'ATENDIDO', asesorNombre: 'Diego Rivas', moduloNombre: 'Asesoría 1', horaCreacion: h(8, 20), calificacion: null }),
  turno({ codigo: 'C-031', tramiteId: T.C, tramiteNombre: 'Caja', estado: 'ATENDIDO', asesorNombre: 'Laura Caicedo', moduloNombre: 'Caja 1', calificacion: 5 }),
  turno({ codigo: 'K-004', tramiteId: T.K, tramiteNombre: 'Créditos', estado: 'CANCELADO' }),
  turno({ codigo: 'C-009', tramiteId: T.C, tramiteNombre: 'Caja', estado: 'AUSENTE', asesorNombre: 'Andrés Mora', moduloNombre: 'Caja 2' }),
];
const manana = new Date(hoy.getTime() + 86400000).toISOString().slice(0, 10);
const franjas = Array.from({ length: 16 }, (_, i) => {
  const hh = 8 + Math.floor(i / 2); const mm = i % 2 ? 30 : 0;
  const ocupados = [1, 2, 5, 8].includes(i) ? 2 : i % 3 === 0 ? 1 : 0;
  return { fechaHora: `${manana}T${String(hh).padStart(2, '0')}:${mm ? '30' : '00'}:00`, cupos: 2, ocupados, disponible: ocupados < 2 };
});
const citas = [
  { id: 'c1', sucursalId: 's1', sucursalNombre: 'Sucursal Centro', tramiteId: T.A, tramiteNombre: 'Asesoría y productos', fechaHora: h(10, 0), estado: 'PROGRAMADA', motivo: 'Abrir un CDT', puedeHacerCheckIn: true, puedeCancelarse: false },
  { id: 'c2', sucursalId: 's2', sucursalNombre: 'Sucursal Panamericana', tramiteId: T.K, tramiteNombre: 'Créditos', fechaHora: `${manana}T15:30:00`, estado: 'PROGRAMADA', motivo: 'Crédito de vehículo', puedeHacerCheckIn: false, puedeCancelarse: true },
];
const modulos = [
  { id: 'm1', sucursalId: 's1', numero: 1, nombre: 'Caja 1', tramitesIds: [T.C], estado: 'OCUPADO', asesorNombre: 'Laura Caicedo' },
  { id: 'm2', sucursalId: 's1', numero: 2, nombre: 'Caja 2', tramitesIds: [T.C], estado: 'DISPONIBLE', asesorNombre: 'Andrés Mora' },
  { id: 'm3', sucursalId: 's1', numero: 3, nombre: 'Caja 3', tramitesIds: [T.C, T.E], estado: 'CERRADO' },
  { id: 'm4', sucursalId: 's1', numero: 4, nombre: 'Asesoría 1', tramitesIds: [T.A, T.S], estado: 'DISPONIBLE', asesorNombre: 'Diego Rivas' },
  { id: 'm5', sucursalId: 's1', numero: 5, nombre: 'Asesoría 2', tramitesIds: [T.A, T.K], estado: 'CERRADO' },
  { id: 'm6', sucursalId: 's1', numero: 6, nombre: 'Créditos', tramitesIds: [T.K], estado: 'CERRADO' },
];
const actualAsesor = turno({ codigo: 'C-012', tramiteId: T.C, tramiteNombre: 'Caja', estado: 'EN_ATENCION', prioridad: 'PREFERENCIAL', canal: 'KIOSCO', clienteNombre: 'Rosa Elena Ortiz', clienteDocumento: '****0002', moduloNombre: 'Caja 1', asesorNombre: 'Laura Caicedo', vecesLlamado: 1, horaCreacion: h(9, 30), horaLlamado: h(9, 38), horaInicioAtencion: h(9, 39) });
const linea = (nombre, total, esp, at, cal) => ({ nombre, total, atendidos: Math.round(total * 0.9), ausentes: Math.round(total * 0.06), cancelados: Math.round(total * 0.04), esperaPromedioMin: esp, atencionPromedioMin: at, calificacionPromedio: cal });
const tablero = {
  resumen: linea('Total', 612, 11.4, 9.2, 4.6), porcentajeAtendidos: 90.2, enEsperaAhora: 12,
  porTramite: [linea('Asesoría y productos', 142, 14.1, 15.3, 4.7), linea('Caja', 301, 9.8, 5.1, 4.5), linea('Créditos', 74, 16.2, 19.8, 4.6), linea('Empresas', 51, 8.3, 11.6, 4.8), linea('Servicio al cliente', 44, 10.5, 9.7, 4.4)],
  porAsesor: [linea('Andrés Mora', 160, 10, 6.1, 4.5), linea('Diego Rivas', 141, 13, 14.2, 4.8), linea('Laura Caicedo', 172, 9, 5.4, 4.7), linea('Paola Jurado', 139, 12, 10.9, 4.6)],
  porHora: [['08:00', 52], ['09:00', 88], ['10:00', 121], ['11:00', 104], ['12:00', 97], ['13:00', 61], ['14:00', 48], ['15:00', 30], ['16:00', 11]].map(([n, t]) => linea(n, t, 10, 9, 4.6)),
};
const asesores = [
  ['Laura Caicedo', 'asesor1', 's1', 'm1'], ['Andrés Mora', 'asesor2', 's1', 'm2'], ['Paola Jurado', 'asesor3', 's1', null], ['Diego Rivas', 'asesor4', 's1', 'm4'],
  ['Natalia Erazo', 'asesor5', 's2', null], ['Felipe Timaná', 'asesor6', 's2', null], ['Carolina Yépez', 'asesor7', 's3', null],
].map(([nombre, c, s, m], i) => ({ id: 'as' + i, nombre, documento: '108500010' + (i + 1), correo: `${c}@filacero.co`, sucursalId: s, sucursalNombre: sucursales.find((x) => x.id === s).nombre, moduloId: m, activo: true }));

export function respuestaMock(metodo, ruta) {
  const r = ruta.split('?')[0];
  const sesion = JSON.parse(localStorage.getItem('filacero.sesion') || 'null');
  if (r === '/api/sucursales') return sucursales;
  if (r.startsWith('/api/sucursales/')) return sucursales.find((s) => s.id === r.split('/')[3]) || sucursales[0];
  if (r === '/api/tramites') return tramites;
  if (r === '/api/turnos/actual') return window.__estadoMock === 'llamado' ? { ...miTurno, estado: 'LLAMADO', moduloNombre: 'Caja 2', asesorNombre: 'Andrés Mora', vecesLlamado: 1, horaLlamado: h(9, 58) } : miTurno;
  if (r === '/api/notificaciones') return notifs;
  if (r === '/api/turnos/historial') return historial;
  if (r === '/api/citas/franjas') return franjas;
  if (r === '/api/citas') return citas;
  if (r === '/api/kiosco/turnos' && metodo === 'POST') return turno({ codigo: 'C-015', tramiteId: T.C, tramiteNombre: 'Caja', estado: 'EN_ESPERA', canal: 'KIOSCO', personasAntes: 6, tiempoEstimadoMin: 15, horaCreacion: h(10, 2) });
  if (r.startsWith('/api/pantalla/')) return {
    sucursalId: 's1', sucursalNombre: 'Sucursal Centro', hora: h(10, 0), filas: sucursales[0].filas,
    llamados: [
      turno({ codigo: 'C-013', moduloNombre: 'Caja 2', tramiteNombre: 'Caja', horaLlamado: h(9, 59), estado: 'LLAMADO' }),
      turno({ codigo: 'A-007', moduloNombre: 'Asesoría 1', tramiteNombre: 'Asesoría', horaLlamado: h(9, 55), estado: 'EN_ATENCION' }),
      turno({ codigo: 'C-012', moduloNombre: 'Caja 1', tramiteNombre: 'Caja', horaLlamado: h(9, 38), estado: 'EN_ATENCION' }),
      turno({ codigo: 'E-003', moduloNombre: 'Caja 3', tramiteNombre: 'Empresas', horaLlamado: h(9, 31), estado: 'ATENDIDO' }),
      turno({ codigo: 'C-011', moduloNombre: 'Caja 2', tramiteNombre: 'Caja', horaLlamado: h(9, 29), estado: 'ATENDIDO' }),
    ] };
  if (r === '/api/atencion/panel') {
    const sinModulo = window.__estadoMock === 'sin-modulo';
    if (window.__estadoMock === 'libre') return { asesorNombre: 'Andrés Mora', sucursalId: 's1', sucursalNombre: 'Sucursal Centro', modulo: modulos[1], turnoActual: null, filas: [sucursales[0].filas[0]], atendidosHoy: 18, modulosSucursal: modulos, tramitesSucursal: tramites };
    return { asesorNombre: 'Laura Caicedo', sucursalId: 's1', sucursalNombre: 'Sucursal Centro', modulo: sinModulo ? null : modulos[0], turnoActual: sinModulo ? null : actualAsesor,
      filas: [sucursales[0].filas[0]], atendidosHoy: 23, modulosSucursal: sinModulo ? modulos.map((m, i) => (i < 2 || i === 3 ? m : { ...m, estado: 'CERRADO' })) : modulos, tramitesSucursal: tramites };
  }
  if (r === '/api/tablero') return { sucursalId: 's1', sucursalNombre: 'Sucursal Centro', ...tablero };
  if (r === '/api/modulos') return modulos;
  if (r === '/api/asesores') return asesores;
  if (r === '/api/auth/login' || r === '/api/auth/registro') return sesion;
  return {};
}
