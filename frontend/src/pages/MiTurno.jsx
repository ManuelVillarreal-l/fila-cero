import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api.js';
import { anunciar, hora, PRIORIDAD, useCada } from '../util.js';
import { Aviso, Cargando, Estado, Estrellas, Vacio } from '../components/Comunes.jsx';

const ICONOS = { TURNO_CREADO: '🎫', TURNO_PROXIMO: '⏳', TURNO_LLAMADO: '📣', TURNO_ATENDIDO: '✅', TURNO_AUSENTE: '⚠️', TURNO_TRANSFERIDO: '↪️' };

export default function MiTurno() {
  const [turno, setTurno] = useState(undefined);
  const [notifs, setNotifs] = useState([]);
  const [error, setError] = useState('');
  const [ultimo, setUltimo] = useState(null);
  const [estrellas, setEstrellas] = useState(0);
  const [comentario, setComentario] = useState('');
  const [gracias, setGracias] = useState(false);
  const estadoPrevio = useRef(null);

  const cargar = async () => {
    try {
      const [t, n] = await Promise.all([api.get('/api/turnos/actual'), api.get('/api/notificaciones')]);
      setTurno(t);
      setNotifs(n);
      setError('');
      if (t) setUltimo(t);
      // Aviso sonoro cuando pasa de "en espera" a "llamado".
      if (t?.estado === 'LLAMADO' && estadoPrevio.current !== 'LLAMADO') {
        anunciar(`Es tu turno. Acércate a ${t.moduloNombre}`);
        if ('vibrate' in navigator) navigator.vibrate?.([300, 120, 300]);
      }
      estadoPrevio.current = t?.estado ?? null;
      if (!t && ultimo) {
        // El turno terminó: traemos su estado final del historial.
        const h = await api.get('/api/turnos/historial');
        setUltimo(h.find((x) => x.id === ultimo.id) || null);
      }
    } catch (e) {
      setError(e.message);
    }
  };
  useCada(cargar, 5000);

  useEffect(() => {
    const sinLeer = notifs.some((n) => !n.leida);
    if (sinLeer) {
      const id = setTimeout(() => api.post('/api/notificaciones/leidas').catch(() => {}), 4000);
      return () => clearTimeout(id);
    }
  }, [notifs]);

  const cancelar = async () => {
    if (!window.confirm('¿Seguro que quieres cancelar tu turno? Perderás tu lugar en la fila.')) return;
    try { await api.patch(`/api/turnos/${turno.id}/cancelar`); cargar(); } catch (e) { setError(e.message); }
  };

  const calificar = async () => {
    try {
      await api.post(`/api/turnos/${ultimo.id}/calificacion`, { estrellas, comentario });
      setGracias(true);
    } catch (e) { setError(e.message); }
  };

  if (turno === undefined) return <Cargando texto="Buscando tu turno…" />;
  const t = turno;
  const total = t?.posicion ? Math.min(t.posicion + 1, 8) : 0;

  return (
    <div className="dos">
      <section>
        <Aviso>{error}</Aviso>
        {t ? (
          <article className={`tiquete ${t.estado === 'LLAMADO' ? 'llamado' : ''}`}>
            <div className="cabeza">
              <div className="fila entre"><small>{t.sucursalNombre}</small><Estado valor={t.estado} /></div>
              <div className="codigo">{t.codigo}</div>
              <div><b>{t.tramiteNombre}</b> · <small>Prioridad {PRIORIDAD[t.prioridad]}</small></div>
            </div>
            <div className="corte" />
            <div className="cuerpo">
              {t.estado === 'EN_ESPERA' && <>
                <div className="datos">
                  <div className="dato"><b>{t.posicion}</b><span>Tu posición</span></div>
                  <div className="dato"><b>{t.personasAntes}</b><span>Personas antes</span></div>
                  <div className="dato"><b>{t.tiempoEstimadoMin}′</b><span>Tiempo estimado</span></div>
                </div>
                <div className="progreso" aria-hidden>
                  {Array.from({ length: total }, (_, i) => <i key={i} className={i === total - 1 ? 'actual' : i < total - t.posicion ? 'lleno' : ''} />)}
                </div>
                <p className="suave">Tomado a las {hora(t.horaCreacion)}. Te avisaremos cuando falten 2 turnos. Esta pantalla se actualiza sola.</p>
                {t.tramiteOrigenNombre && <Aviso tipo="info">Te transfirieron desde {t.tramiteOrigenNombre}: conservas tu tiempo de espera.</Aviso>}
                <button className="boton peligro bloque" onClick={cancelar}>Cancelar turno</button>
              </>}
              {t.estado === 'LLAMADO' && <>
                <h2>Acércate a {t.moduloNombre}</h2>
                <p>Te atiende {t.asesorNombre}. Llamado {t.vecesLlamado} de 3: si no te presentas, el turno se pierde.</p>
              </>}
              {t.estado === 'EN_ATENCION' && <>
                <h2>Te están atendiendo en {t.moduloNombre}</h2>
                <p className="suave">Asesor: {t.asesorNombre}</p>
              </>}
            </div>
          </article>
        ) : ultimo && ['ATENDIDO', 'AUSENTE', 'CANCELADO'].includes(ultimo.estado) ? (
          <div className="tarjeta">
            <div className="fila entre"><h2>Turno {ultimo.codigo}</h2><Estado valor={ultimo.estado} /></div>
            {ultimo.estado === 'ATENDIDO' && !gracias && ultimo.calificacion == null && <>
              <p>¿Cómo te atendió {ultimo.asesorNombre}?</p>
              <Estrellas valor={estrellas} onElegir={setEstrellas} />
              <label style={{ marginTop: 12 }}>Comentario (opcional)<textarea rows="2" value={comentario} onChange={(e) => setComentario(e.target.value)} /></label>
              <button className="boton ambar" disabled={!estrellas} onClick={calificar}>Enviar calificación</button>
            </>}
            {gracias && <Aviso tipo="ok">¡Gracias por tu calificación!</Aviso>}
            <div className="fila" style={{ marginTop: 14 }}><Link className="boton claro" to="/">Tomar otro turno</Link></div>
          </div>
        ) : (
          <div className="tarjeta">
            <Vacio titulo="No tienes un turno activo">Elige una sucursal y toma tu turno desde aquí.</Vacio>
            <div className="fila" style={{ justifyContent: 'center' }}>
              <Link className="boton ambar" to="/">Ver sucursales</Link>
              <Link className="boton claro" to="/citas">Agendar cita</Link>
            </div>
          </div>
        )}
      </section>
      <aside className="tarjeta">
        <h3>Avisos</h3>
        {notifs.length === 0 && <p className="suave">Aquí verás los avisos de tus turnos.</p>}
        {notifs.slice(0, 8).map((n) => (
          <div key={n.id} className={`notif ${n.leida ? '' : 'nueva'}`}>
            <span className="icono">{ICONOS[n.tipo] || '🔔'}</span>
            <div><b>{n.titulo}</b><br /><small>{n.mensaje} · {hora(n.fecha)}</small></div>
          </div>
        ))}
        <p className="suave" style={{ marginTop: 10 }}>En una versión real estos avisos llegarían por SMS.</p>
      </aside>
    </div>
  );
}
