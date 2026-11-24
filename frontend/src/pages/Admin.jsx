import { useEffect, useState } from 'react';
import { api } from '../api.js';
import { hhmm } from '../util.js';
import { Aviso, Cargando, Modal } from '../components/Comunes.jsx';

const hoy = () => new Date().toLocaleDateString('en-CA');
const haceDias = (n) => new Date(Date.now() - n * 86400000).toLocaleDateString('en-CA');

export default function Admin() {
  const [pestana, setPestana] = useState('tablero');
  const [sucursales, setSucursales] = useState([]);
  const [tramites, setTramites] = useState([]);
  const [error, setError] = useState('');

  const recargar = () => Promise.all([
    api.get('/api/sucursales?incluirInactivas=true').then(setSucursales),
    api.get('/api/tramites?incluirInactivos=true').then(setTramites),
  ]).catch((e) => setError(e.message));
  useEffect(() => { recargar(); }, []);

  const P = { tablero: 'Tablero', sucursales: 'Sucursales', tramites: 'Trámites', modulos: 'Módulos', asesores: 'Asesores' };
  return (
    <>
      <h1>Administración</h1>
      <div className="pestanas">
        {Object.entries(P).map(([k, v]) => <button key={k} className={pestana === k ? 'activa' : ''} onClick={() => setPestana(k)}>{v}</button>)}
      </div>
      <Aviso>{error}</Aviso>
      {pestana === 'tablero' && <Tablero sucursales={sucursales.filter((s) => s.activa)} />}
      {pestana === 'sucursales' && <Sucursales sucursales={sucursales} tramites={tramites} recargar={recargar} />}
      {pestana === 'tramites' && <Tramites tramites={tramites} recargar={recargar} />}
      {pestana === 'modulos' && <Modulos sucursales={sucursales.filter((s) => s.activa)} tramites={tramites} />}
      {pestana === 'asesores' && <Asesores sucursales={sucursales.filter((s) => s.activa)} />}
    </>
  );
}

/* ---------------- HU_020 Tablero de indicadores ---------------- */
function Tablero({ sucursales }) {
  const [sucursal, setSucursal] = useState('');
  const [desde, setDesde] = useState(haceDias(13));
  const [hasta, setHasta] = useState(hoy());
  const [r, setR] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => { if (!sucursal && sucursales[0]) setSucursal(sucursales[0].id); }, [sucursales, sucursal]);
  useEffect(() => {
    if (!sucursal) return;
    setError('');
    api.get(`/api/tablero?sucursalId=${sucursal}&desde=${desde}&hasta=${hasta}`).then(setR).catch((e) => { setError(e.message); setR(null); });
  }, [sucursal, desde, hasta]);

  const maxTramite = Math.max(1, ...(r?.porTramite || []).map((l) => l.total));
  const maxHora = Math.max(1, ...(r?.porHora || []).map((l) => l.total));
  return (
    <>
      <div className="tarjeta fila" style={{ marginBottom: 16 }}>
        <label style={{ flex: 2, margin: 0 }}>Sucursal<select value={sucursal} onChange={(e) => setSucursal(e.target.value)}>{sucursales.map((s) => <option key={s.id} value={s.id}>{s.nombre}</option>)}</select></label>
        <label style={{ flex: 1, margin: 0 }}>Desde<input type="date" value={desde} onChange={(e) => setDesde(e.target.value)} /></label>
        <label style={{ flex: 1, margin: 0 }}>Hasta<input type="date" value={hasta} onChange={(e) => setHasta(e.target.value)} /></label>
      </div>
      <Aviso>{error}</Aviso>
      {!r && !error && <Cargando />}
      {r && <>
        <div className="kpis">
          <div className="kpi destacado"><b>{r.resumen.total}</b><span>Turnos en el período</span></div>
          <div className="kpi"><b>{r.resumen.esperaPromedioMin}′</b><span>Espera promedio</span></div>
          <div className="kpi"><b>{r.resumen.atencionPromedioMin}′</b><span>Atención promedio</span></div>
          <div className="kpi"><b>{r.porcentajeAtendidos}%</b><span>Atendidos</span></div>
          <div className="kpi"><b>{r.resumen.ausentes}</b><span>Ausentes</span></div>
          <div className="kpi"><b>{r.resumen.calificacionPromedio ?? '—'}★</b><span>Satisfacción</span></div>
        </div>
        <div className="dos">
          <div className="tarjeta">
            <h3>Turnos por trámite</h3>
            <div className="grafica-barras">
              {r.porTramite.map((l) => (
                <div className="b" key={l.nombre}><span>{l.nombre}</span><div className="pista"><i style={{ width: `${(l.total / maxTramite) * 100}%` }} /></div><b>{l.total}</b></div>
              ))}
            </div>
            <h3 style={{ marginTop: 22 }}>Espera promedio por trámite (min)</h3>
            <div className="grafica-barras">
              {r.porTramite.map((l) => (
                <div className="b" key={l.nombre}><span>{l.nombre}</span><div className="pista"><i className="ambar" style={{ width: `${Math.min(100, (l.esperaPromedioMin / 30) * 100)}%` }} /></div><b>{l.esperaPromedioMin}</b></div>
              ))}
            </div>
          </div>
          <div className="tarjeta">
            <h3>Horas pico</h3>
            <div className="columnas-hora">
              {r.porHora.map((l) => (
                <div key={l.nombre}><em>{l.total}</em><i style={{ height: `${(l.total / maxHora) * 100}%` }} />{l.nombre.slice(0, 2)}h</div>
              ))}
            </div>
          </div>
        </div>
        <div className="tarjeta" style={{ marginTop: 16 }}>
          <h3>Desempeño por asesor</h3>
          <div className="tabla-scroll"><table>
            <thead><tr><th>Asesor</th><th>Atendidos</th><th>Ausentes</th><th>Atención prom.</th><th>Satisfacción</th></tr></thead>
            <tbody>{r.porAsesor.map((l) => (
              <tr key={l.nombre}><td>{l.nombre}</td><td>{l.atendidos}</td><td>{l.ausentes}</td><td>{l.atencionPromedioMin} min</td><td>{l.calificacionPromedio ?? '—'} ★</td></tr>
            ))}</tbody>
          </table></div>
        </div>
      </>}
    </>
  );
}

/* ---------------- Utilidad para formularios de CRUD ---------------- */
function useFormulario(inicial) {
  const [abierto, setAbierto] = useState(null);
  const [f, setF] = useState(inicial);
  const [error, setError] = useState('');
  const abrir = (datos, id = null) => { setF(datos); setAbierto({ id }); setError(''); };
  const cerrar = () => setAbierto(null);
  const campo = (k) => ({ value: f[k] ?? '', onChange: (e) => setF({ ...f, [k]: e.target.value }) });
  return { abierto, f, setF, error, setError, abrir, cerrar, campo };
}

function Casillas({ opciones, valor, onCambio }) {
  return (
    <div className="columna" style={{ gap: 6, marginBottom: 12 }}>
      {opciones.map((o) => (
        <label key={o.id} className="check" style={{ margin: 0 }}>
          <input type="checkbox" checked={valor.includes(o.id)}
            onChange={(e) => onCambio(e.target.checked ? [...valor, o.id] : valor.filter((x) => x !== o.id))} />
          {o.prefijo ? `${o.prefijo} · ` : ''}{o.nombre}
        </label>
      ))}
    </div>
  );
}

/* ---------------- HU_003 Sucursales ---------------- */
function Sucursales({ sucursales, tramites, recargar }) {
  const VACIO = { nombre: '', direccion: '', ciudad: 'Pasto', telefono: '', horaApertura: '08:00', horaCierre: '16:00', tramitesIds: [] };
  const fm = useFormulario(VACIO);
  const guardar = async () => {
    try {
      if (fm.abierto.id) await api.put(`/api/sucursales/${fm.abierto.id}`, fm.f); else await api.post('/api/sucursales', fm.f);
      fm.cerrar(); recargar();
    } catch (e) { fm.setError(e.message); }
  };
  return (
    <div className="tarjeta">
      <div className="fila entre"><h2>Sucursales</h2><button className="boton ambar" onClick={() => fm.abrir(VACIO)}>Nueva sucursal</button></div>
      <div className="tabla-scroll"><table>
        <thead><tr><th>Nombre</th><th>Dirección</th><th>Horario</th><th>Trámites</th><th>Estado</th><th /></tr></thead>
        <tbody>{sucursales.map((s) => (
          <tr key={s.id}>
            <td><b>{s.nombre}</b></td><td>{s.direccion}</td><td>{hhmm(s.horaApertura)}–{hhmm(s.horaCierre)}</td>
            <td>{s.filas.map((x) => x.prefijo).join(' ')}</td>
            <td><span className={`chip ${s.activa ? 'ok' : 'neutro'}`}>{s.activa ? 'Activa' : 'Inactiva'}</span></td>
            <td className="fila">
              <button className="boton claro mini" onClick={() => fm.abrir({ ...s, horaApertura: hhmm(s.horaApertura), horaCierre: hhmm(s.horaCierre), tramitesIds: s.filas.map((x) => x.tramiteId) }, s.id)}>Editar</button>
              {s.activa && <button className="boton peligro mini" onClick={() => window.confirm(`¿Desactivar ${s.nombre}?`) && api.del(`/api/sucursales/${s.id}`).then(recargar)}>Desactivar</button>}
            </td>
          </tr>
        ))}</tbody>
      </table></div>
      {fm.abierto && (
        <Modal titulo={fm.abierto.id ? 'Editar sucursal' : 'Nueva sucursal'} onCerrar={fm.cerrar}>
          <Aviso>{fm.error}</Aviso>
          <label>Nombre<input {...fm.campo('nombre')} /></label>
          <label>Dirección<input {...fm.campo('direccion')} /></label>
          <div className="dos"><label>Ciudad<input {...fm.campo('ciudad')} /></label><label>Teléfono<input {...fm.campo('telefono')} /></label></div>
          <div className="dos"><label>Abre<input type="time" {...fm.campo('horaApertura')} /></label><label>Cierra<input type="time" {...fm.campo('horaCierre')} /></label></div>
          <b>Trámites que ofrece</b>
          <Casillas opciones={tramites.filter((t) => t.activo)} valor={fm.f.tramitesIds} onCambio={(v) => fm.setF({ ...fm.f, tramitesIds: v })} />
          <button className="boton ambar bloque" onClick={guardar}>Guardar</button>
        </Modal>
      )}
    </div>
  );
}

/* ---------------- HU_004 Trámites ---------------- */
function Tramites({ tramites, recargar }) {
  const VACIO = { prefijo: '', nombre: '', descripcion: '', tiempoPromedioMin: 10 };
  const fm = useFormulario(VACIO);
  const guardar = async () => {
    try {
      const datos = { ...fm.f, prefijo: fm.f.prefijo.toUpperCase(), tiempoPromedioMin: Number(fm.f.tiempoPromedioMin) };
      if (fm.abierto.id) await api.put(`/api/tramites/${fm.abierto.id}`, datos); else await api.post('/api/tramites', datos);
      fm.cerrar(); recargar();
    } catch (e) { fm.setError(e.message); }
  };
  return (
    <div className="tarjeta">
      <div className="fila entre"><h2>Trámites</h2><button className="boton ambar" onClick={() => fm.abrir(VACIO)}>Nuevo trámite</button></div>
      <div className="tabla-scroll"><table>
        <thead><tr><th>Prefijo</th><th>Nombre</th><th>Descripción</th><th>Tiempo prom.</th><th>Estado</th><th /></tr></thead>
        <tbody>{tramites.map((t) => (
          <tr key={t.id}>
            <td><span className="letra">{t.prefijo}</span></td><td><b>{t.nombre}</b></td><td>{t.descripcion}</td><td>{t.tiempoPromedioMin} min</td>
            <td><span className={`chip ${t.activo ? 'ok' : 'neutro'}`}>{t.activo ? 'Activo' : 'Inactivo'}</span></td>
            <td className="fila">
              <button className="boton claro mini" onClick={() => fm.abrir(t, t.id)}>Editar</button>
              {t.activo && <button className="boton peligro mini" onClick={() => window.confirm(`¿Desactivar ${t.nombre}?`) && api.del(`/api/tramites/${t.id}`).then(recargar)}>Desactivar</button>}
            </td>
          </tr>
        ))}</tbody>
      </table></div>
      {fm.abierto && (
        <Modal titulo={fm.abierto.id ? 'Editar trámite' : 'Nuevo trámite'} onCerrar={fm.cerrar}>
          <Aviso>{fm.error}</Aviso>
          <div className="dos"><label>Prefijo (una letra)<input maxLength="1" {...fm.campo('prefijo')} /></label><label>Tiempo promedio (min)<input type="number" min="1" {...fm.campo('tiempoPromedioMin')} /></label></div>
          <label>Nombre<input {...fm.campo('nombre')} /></label>
          <label>Descripción<input {...fm.campo('descripcion')} /></label>
          <button className="boton ambar bloque" onClick={guardar}>Guardar</button>
        </Modal>
      )}
    </div>
  );
}

/* ---------------- HU_005 Módulos ---------------- */
function Modulos({ sucursales, tramites }) {
  const [sucursal, setSucursal] = useState('');
  const [lista, setLista] = useState(null);
  const fm = useFormulario({});
  useEffect(() => { if (!sucursal && sucursales[0]) setSucursal(sucursales[0].id); }, [sucursales, sucursal]);
  const cargar = () => sucursal && api.get(`/api/modulos?sucursalId=${sucursal}`).then(setLista).catch((e) => fm.setError(e.message));
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => { cargar(); }, [sucursal]);
  const ofrecidos = tramites.filter((t) => sucursales.find((s) => s.id === sucursal)?.filas.some((f) => f.tramiteId === t.id));
  const nombre = (ids) => tramites.filter((t) => ids.includes(t.id)).map((t) => t.nombre).join(', ');
  const guardar = async () => {
    try {
      const datos = { ...fm.f, sucursalId: sucursal, numero: Number(fm.f.numero) };
      if (fm.abierto.id) await api.put(`/api/modulos/${fm.abierto.id}`, datos); else await api.post('/api/modulos', datos);
      fm.cerrar(); cargar();
    } catch (e) { fm.setError(e.message); }
  };
  return (
    <div className="tarjeta">
      <div className="fila entre">
        <h2>Módulos</h2>
        <div className="fila">
          <select value={sucursal} onChange={(e) => setSucursal(e.target.value)} style={{ width: 'auto', margin: 0 }}>{sucursales.map((s) => <option key={s.id} value={s.id}>{s.nombre}</option>)}</select>
          <button className="boton ambar" onClick={() => fm.abrir({ numero: (lista?.length || 0) + 1, nombre: '', tramitesIds: [] })}>Nuevo módulo</button>
        </div>
      </div>
      {!lista ? <Cargando /> : <div className="tabla-scroll"><table>
        <thead><tr><th>#</th><th>Nombre</th><th>Atiende</th><th>Estado</th><th>Asesor</th><th /></tr></thead>
        <tbody>{lista.map((m) => (
          <tr key={m.id}>
            <td>{m.numero}</td><td><b>{m.nombre}</b></td><td>{nombre(m.tramitesIds)}</td>
            <td><span className={`chip ${m.estado === 'CERRADO' ? 'neutro' : 'ok'}`}>{m.estado.replace('_', ' ').toLowerCase()}</span></td>
            <td>{m.asesorNombre || '—'}</td>
            <td className="fila">
              <button className="boton claro mini" onClick={() => fm.abrir(m, m.id)}>Editar</button>
              <button className="boton peligro mini" onClick={() => window.confirm(`¿Eliminar ${m.nombre}?`) && api.del(`/api/modulos/${m.id}`).then(cargar).catch((e) => alert(e.message))}>Eliminar</button>
            </td>
          </tr>
        ))}</tbody>
      </table></div>}
      {fm.abierto && (
        <Modal titulo={fm.abierto.id ? 'Editar módulo' : 'Nuevo módulo'} onCerrar={fm.cerrar}>
          <Aviso>{fm.error}</Aviso>
          <div className="dos"><label>Número<input type="number" min="1" {...fm.campo('numero')} /></label><label>Nombre<input {...fm.campo('nombre')} placeholder="Caja 4" /></label></div>
          <b>Trámites que atiende</b>
          <Casillas opciones={ofrecidos} valor={fm.f.tramitesIds || []} onCambio={(v) => fm.setF({ ...fm.f, tramitesIds: v })} />
          <button className="boton ambar bloque" onClick={guardar}>Guardar</button>
        </Modal>
      )}
    </div>
  );
}

/* ---------------- HU_006 Asesores ---------------- */
function Asesores({ sucursales }) {
  const [lista, setLista] = useState(null);
  const VACIO = { nombre: '', documento: '', correo: '', contrasena: '', sucursalId: '' };
  const fm = useFormulario(VACIO);
  const cargar = () => api.get('/api/asesores').then(setLista).catch((e) => fm.setError(e.message));
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => { cargar(); }, []);
  const guardar = async () => {
    try {
      const datos = { ...fm.f, contrasena: fm.f.contrasena || null };
      if (fm.abierto.id) await api.put(`/api/asesores/${fm.abierto.id}`, datos); else await api.post('/api/asesores', datos);
      fm.cerrar(); cargar();
    } catch (e) { fm.setError(e.message); }
  };
  return (
    <div className="tarjeta">
      <div className="fila entre"><h2>Asesores</h2><button className="boton ambar" onClick={() => fm.abrir({ ...VACIO, sucursalId: sucursales[0]?.id || '' })}>Nuevo asesor</button></div>
      {!lista ? <Cargando /> : <div className="tabla-scroll"><table>
        <thead><tr><th>Nombre</th><th>Correo</th><th>Sucursal</th><th>Estado</th><th /></tr></thead>
        <tbody>{lista.map((a) => (
          <tr key={a.id}>
            <td><b>{a.nombre}</b><br /><small>{a.documento}</small></td><td>{a.correo}</td><td>{a.sucursalNombre}</td>
            <td><span className={`chip ${a.activo ? (a.moduloId ? 'espera' : 'ok') : 'neutro'}`}>{!a.activo ? 'Inactivo' : a.moduloId ? 'Atendiendo' : 'Activo'}</span></td>
            <td className="fila">
              <button className="boton claro mini" onClick={() => fm.abrir({ ...a, contrasena: '' }, a.id)}>Editar</button>
              {a.activo && <button className="boton peligro mini" onClick={() => window.confirm(`¿Desactivar a ${a.nombre}?`) && api.del(`/api/asesores/${a.id}`).then(cargar).catch((e) => alert(e.message))}>Desactivar</button>}
            </td>
          </tr>
        ))}</tbody>
      </table></div>}
      {fm.abierto && (
        <Modal titulo={fm.abierto.id ? 'Editar asesor' : 'Nuevo asesor'} onCerrar={fm.cerrar}>
          <Aviso>{fm.error}</Aviso>
          <label>Nombre<input {...fm.campo('nombre')} /></label>
          <div className="dos"><label>Cédula<input {...fm.campo('documento')} /></label><label>Correo<input type="email" {...fm.campo('correo')} /></label></div>
          <label>Contraseña {fm.abierto.id && <small>(vacía para no cambiarla)</small>}<input type="password" {...fm.campo('contrasena')} /></label>
          <label>Sucursal<select {...fm.campo('sucursalId')}>{sucursales.map((s) => <option key={s.id} value={s.id}>{s.nombre}</option>)}</select></label>
          <button className="boton ambar bloque" onClick={guardar}>Guardar</button>
        </Modal>
      )}
    </div>
  );
}
