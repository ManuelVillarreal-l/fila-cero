import { useState } from 'react';
import { api } from '../api.js';
import { hora, minutos, PRIORIDAD, useCada } from '../util.js';
import { Aviso, Cargando, Estado, Modal, Vacio } from '../components/Comunes.jsx';

/** Panel del asesor de ventanilla (HU_012 a HU_015). */
export default function Asesor() {
  const [p, setP] = useState(null);
  const [error, setError] = useState('');
  const [ocupado, setOcupado] = useState(false);
  const [transferir, setTransferir] = useState(false);
  const [obs, setObs] = useState('');

  const cargar = () => api.get('/api/atencion/panel').then(setP).catch((e) => setError(e.message));
  useCada(cargar, 5000);

  const hacer = async (ruta, cuerpo) => {
    setOcupado(true); setError('');
    try {
      const r = await api.post(ruta, cuerpo);
      if (r && r.modulosSucursal) setP(r); else await cargar();
      return true;
    } catch (e) { setError(e.message); return false; } finally { setOcupado(false); }
  };

  if (!p) return error ? <Aviso>{error}</Aviso> : <Cargando />;
  const m = p.modulo;
  const t = p.turnoActual;

  if (!m) {
    return (
      <>
        <h1>Hola, {p.asesorNombre.split(' ')[0]}</h1>
        <p className="suave">{p.sucursalNombre} · Elige el módulo donde vas a atender hoy.</p>
        <Aviso>{error}</Aviso>
        <div className="rejilla">
          {p.modulosSucursal.map((mod) => {
            const nombres = p.tramitesSucursal.filter((x) => mod.tramitesIds.includes(x.id)).map((x) => x.nombre).join(', ');
            const libre = mod.estado === 'CERRADO';
            return (
              <div key={mod.id} className="tarjeta">
                <div className="fila entre"><h3>{mod.nombre}</h3><span className={`chip ${libre ? 'neutro' : 'espera'}`}>{libre ? 'Libre' : `Lo usa ${mod.asesorNombre}`}</span></div>
                <p className="suave">{nombres}</p>
                <button className="boton bloque" disabled={!libre || ocupado} onClick={() => hacer('/api/atencion/modulo/abrir', { moduloId: mod.id })}>Abrir módulo</button>
              </div>
            );
          })}
        </div>
      </>
    );
  }

  const espera = t ? minutos(Math.round((new Date(t.horaLlamado) - new Date(t.horaCreacion)) / 60000)) : '';

  return (
    <div className="dos">
      <section className="columna">
        <div className="fila entre">
          <div><h1 style={{ marginBottom: 0 }}>{m.nombre}</h1><small>{p.sucursalNombre} · {p.atendidosHoy} atendidos hoy</small></div>
          <span className="estado-modulo"><Estado valor={m.estado === 'EN_PAUSA' ? 'CANCELADO' : t ? 'EN_ATENCION' : 'ATENDIDO'} /></span>
        </div>
        <Aviso>{error}</Aviso>
        <div className="mostrador">
          {t ? <>
            <div className="fila entre"><small>Turno actual · {t.tramiteNombre}</small><Estado valor={t.estado} /></div>
            <div className="codigo">{t.codigo}</div>
            <p><b>{t.clienteNombre}</b> <small>{t.clienteDocumento} · {PRIORIDAD[t.prioridad]} · canal {t.canal.toLowerCase()} · esperó {espera}</small></p>
            {t.tramiteOrigenNombre && <p className="suave">Transferido desde {t.tramiteOrigenNombre}</p>}
            {t.estado === 'LLAMADO' ? <>
              <p className="suave">Llamado {t.vecesLlamado} de 3 a las {hora(t.horaLlamado)}</p>
              <div className="fila">
                <button className="boton ambar grande" disabled={ocupado} onClick={() => hacer(`/api/atencion/turnos/${t.id}/iniciar`)}>El cliente llegó</button>
                <button className="boton claro" disabled={ocupado || t.vecesLlamado >= 3} onClick={() => hacer(`/api/atencion/turnos/${t.id}/rellamar`)}>Volver a llamar</button>
                <button className="boton peligro" disabled={ocupado} onClick={() => hacer(`/api/atencion/turnos/${t.id}/ausente`)}>No se presentó</button>
              </div>
            </> : <>
              <p className="suave">En atención desde las {hora(t.horaInicioAtencion)}</p>
              <label>Observaciones<textarea rows="2" value={obs} onChange={(e) => setObs(e.target.value)} placeholder="Qué se hizo (opcional)" /></label>
              <div className="fila">
                <button className="boton ambar grande" disabled={ocupado} onClick={async () => { if (await hacer(`/api/atencion/turnos/${t.id}/finalizar`, { observaciones: obs })) setObs(''); }}>Finalizar atención</button>
                <button className="boton claro" disabled={ocupado} onClick={() => setTransferir(true)}>Transferir</button>
              </div>
            </>}
          </> : <>
            <small>{m.estado === 'EN_PAUSA' ? 'Módulo en pausa' : 'Listo para atender'}</small>
            <div className="codigo" style={{ color: '#47707f' }}>— —</div>
            <button className="boton ambar grande bloque" disabled={ocupado || m.estado === 'EN_PAUSA'} onClick={() => hacer('/api/atencion/llamar-siguiente')}>
              Llamar siguiente
            </button>
          </>}
        </div>
        <div className="fila">
          {m.estado === 'EN_PAUSA'
            ? <button className="boton claro" onClick={() => hacer('/api/atencion/modulo/reanudar')}>Reanudar</button>
            : <button className="boton claro" disabled={!!t} onClick={() => hacer('/api/atencion/modulo/pausar')}>Pausa</button>}
          <button className="boton peligro" disabled={!!t} onClick={() => hacer('/api/atencion/modulo/cerrar')}>Cerrar módulo</button>
        </div>
      </section>
      <aside className="tarjeta">
        <h3>Filas de mi módulo</h3>
        {p.filas.length === 0 && <Vacio titulo="Sin trámites" />}
        {p.filas.map((f) => (
          <div key={f.tramiteId} className="fila-mini" style={{ marginBottom: 12 }}>
            <span className="letra">{f.prefijo}</span><span>{f.tramiteNombre}</span><b>{f.enEspera}</b>
          </div>
        ))}
        <p className="suave">El siguiente se elige por puntaje: minutos de espera + 15 si es preferencial + 30 si tiene cita.</p>
      </aside>
      {transferir && t && (
        <Modal titulo={`Transferir ${t.codigo}`} onCerrar={() => setTransferir(false)}>
          <p className="suave">El cliente vuelve a la fila del trámite elegido y conserva su tiempo de espera.</p>
          <div className="opciones">
            {p.tramitesSucursal.filter((x) => x.id !== t.tramiteId).map((x) => (
              <button key={x.id} className="opcion" onClick={async () => { if (await hacer(`/api/atencion/turnos/${t.id}/transferir`, { tramiteId: x.id })) setTransferir(false); }}>
                <span className="letra">{x.prefijo}</span><span><b>{x.nombre}</b></span><span />
              </button>
            ))}
          </div>
        </Modal>
      )}
    </div>
  );
}
