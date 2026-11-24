import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api.js';
import { hhmm, useCada } from '../util.js';
import { Aviso, Cargando, SelectorTramite } from '../components/Comunes.jsx';
import { useSesion } from '../sesion.jsx';

export default function SucursalDetalle() {
  const { id } = useParams();
  const { sesion } = useSesion();
  const navegar = useNavigate();
  const [s, setS] = useState(null);
  const [tramite, setTramite] = useState('');
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  useCada(() => {
    api.get(`/api/sucursales/${id}`).then(setS).catch((e) => setError(e.message));
  }, 10000, [id]);

  const tomar = async () => {
    setEnviando(true);
    setError('');
    try {
      await api.post('/api/turnos', { sucursalId: id, tramiteId: tramite });
      navegar('/mi-turno');
    } catch (e) {
      setError(e.message);
    } finally {
      setEnviando(false);
    }
  };

  if (!s) return error ? <Aviso>{error}</Aviso> : <Cargando />;
  const elegido = s.filas.find((f) => f.tramiteId === tramite);

  return (
    <div className="dos">
      <section>
        <Link to="/" className="suave">← Sucursales</Link>
        <h1 style={{ marginTop: 10 }}>{s.nombre}</h1>
        <p className="suave">{s.direccion}, {s.ciudad} · Tel. {s.telefono} · Atiende de {hhmm(s.horaApertura)} a {hhmm(s.horaCierre)}</p>
        <h3>¿Qué vas a hacer hoy?</h3>
        <SelectorTramite filas={s.filas} elegido={tramite} onElegir={setTramite} />
      </section>
      <aside className="columna">
        <div className="tarjeta">
          <span className={`chip ${s.abiertaAhora ? 'ok' : 'neutro'}`}><i className="punto" />{s.abiertaAhora ? 'Abierta ahora' : 'Cerrada ahora'}</span>
          <h2 style={{ marginTop: 12 }}>{elegido ? elegido.tramiteNombre : 'Elige un trámite'}</h2>
          {elegido && <p>Hay <b>{elegido.enEspera}</b> persona(s) antes que tú. Tiempo estimado: <b>{elegido.tiempoEstimadoMin} min</b>.</p>}
          <Aviso>{error}</Aviso>
          {!s.abiertaAhora && <Aviso tipo="info">La sucursal está cerrada. Puedes agendar una cita para otro momento.</Aviso>}
          {sesion?.rol === 'CLIENTE' ? (
            <div className="columna">
              <button className="boton ambar grande bloque" disabled={!tramite || enviando || !s.abiertaAhora} onClick={tomar}>
                {enviando ? 'Tomando turno…' : 'Tomar turno'}
              </button>
              <Link className="boton claro bloque" to={`/citas?sucursal=${id}`}>Agendar una cita</Link>
            </div>
          ) : !sesion ? (
            <div className="columna">
              <Link className="boton ambar bloque" to="/login" state={{ volverA: `/sucursal/${id}` }}>Ingresa para tomar turno</Link>
              <small>¿Estás en la sucursal? Usa el kiosco con tu cédula, sin cuenta.</small>
            </div>
          ) : <small>Inicia sesión como cliente para tomar turnos.</small>}
        </div>
        <div className="tarjeta">
          <h3>Pantallas de la sucursal</h3>
          <p className="suave">Para mostrar en la oficina (abre en otra pestaña).</p>
          <div className="fila">
            <a className="boton claro mini" href={`/kiosco/${id}`} target="_blank" rel="noreferrer">Kiosco de turnos</a>
            <a className="boton claro mini" href={`/pantalla/${id}`} target="_blank" rel="noreferrer">Pantalla de llamado</a>
          </div>
        </div>
      </aside>
    </div>
  );
}
