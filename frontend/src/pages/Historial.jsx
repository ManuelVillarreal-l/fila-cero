import { useEffect, useState } from 'react';
import { api } from '../api.js';
import { fechaHora, PRIORIDAD } from '../util.js';
import { Aviso, Cargando, Estado, Estrellas, Vacio } from '../components/Comunes.jsx';

export default function Historial() {
  const [turnos, setTurnos] = useState(null);
  const [error, setError] = useState('');

  const cargar = () => api.get('/api/turnos/historial').then(setTurnos).catch((e) => setError(e.message));
  useEffect(() => { cargar(); }, []);

  const calificar = async (id, estrellas) => {
    try { await api.post(`/api/turnos/${id}/calificacion`, { estrellas }); cargar(); } catch (e) { setError(e.message); }
  };

  if (!turnos) return error ? <Aviso>{error}</Aviso> : <Cargando />;
  return (
    <>
      <h1>Mis turnos</h1>
      <Aviso>{error}</Aviso>
      {turnos.length === 0 && <div className="tarjeta"><Vacio titulo="Todavía no tienes turnos">Cuando tomes uno aparecerá aquí.</Vacio></div>}
      <div className="rejilla">
        {turnos.map((t) => (
          <article key={t.id} className="tarjeta">
            <div className="fila entre"><h2 style={{ margin: 0 }}>{t.codigo}</h2><Estado valor={t.estado} /></div>
            <p className="suave">{t.tramiteNombre} · {t.sucursalNombre}<br />{fechaHora(t.horaCreacion)} · {PRIORIDAD[t.prioridad]}</p>
            {t.asesorNombre && <p>Atendió: <b>{t.asesorNombre}</b> en {t.moduloNombre}</p>}
            {t.estado === 'ATENDIDO' && (
              t.calificacion
                ? <Estrellas valor={t.calificacion} />
                : <div><small>Califica la atención:</small><Estrellas valor={0} onElegir={(n) => calificar(t.id, n)} /></div>
            )}
          </article>
        ))}
      </div>
    </>
  );
}
