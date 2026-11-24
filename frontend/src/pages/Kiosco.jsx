import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../api.js';
import { Aviso, Cargando, SelectorTramite } from '../components/Comunes.jsx';
import { hora } from '../util.js';

/** Kiosco de autoatención de la sucursal (HU_009): el visitante toma turno con su cédula, sin cuenta. */
export default function Kiosco() {
  const { id } = useParams();
  const [s, setS] = useState(null);
  const [paso, setPaso] = useState(1);
  const [tramite, setTramite] = useState('');
  const [doc, setDoc] = useState('');
  const [preferencial, setPreferencial] = useState(false);
  const [tiquete, setTiquete] = useState(null);
  const [error, setError] = useState('');

  const cargar = () => api.get(`/api/sucursales/${id}`).then(setS).catch((e) => setError(e.message));
  useEffect(() => { cargar(); const i = setInterval(cargar, 15000); return () => clearInterval(i); // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const reiniciar = () => { setPaso(1); setTramite(''); setDoc(''); setPreferencial(false); setTiquete(null); setError(''); cargar(); };

  useEffect(() => {
    if (!tiquete) return undefined;
    const t = setTimeout(reiniciar, 20000); // vuelve al inicio para el siguiente cliente
    return () => clearTimeout(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tiquete]);

  const tecla = (k) => {
    if (k === '⌫') setDoc((d) => d.slice(0, -1));
    else if (k === 'C') setDoc('');
    else if (doc.length < 12) setDoc((d) => d + k);
  };

  const imprimir = async () => {
    setError('');
    try {
      setTiquete(await api.post('/api/kiosco/turnos', { sucursalId: id, tramiteId: tramite, documento: doc, preferencial }));
      setPaso(3);
    } catch (e) { setError(e.message); }
  };

  if (!s) return <div className="kiosco">{error ? <Aviso>{error}</Aviso> : <Cargando />}</div>;

  return (
    <div className="kiosco">
      <div className="fila entre">
        <div className="fila"><img src="/icono.svg" width="48" height="48" alt="" /><div><b style={{ fontSize: '1.3rem' }}>Banco del Galeras</b><br /><small style={{ color: '#9fbac3' }}>{s.nombre}</small></div></div>
        <b style={{ fontSize: '1.6rem' }}>{new Date().toLocaleTimeString('es-CO', { hour: 'numeric', minute: '2-digit' })}</b>
      </div>
      <div style={{ flex: 1, display: 'grid', placeItems: 'center', padding: '24px 0' }}>
        <div style={{ width: 'min(720px, 100%)' }}>
          {error && <Aviso>{error}</Aviso>}
          {paso === 1 && <>
            <h1>Hola, ¿qué vas a hacer hoy?</h1>
            <SelectorTramite filas={s.filas} elegido={tramite} onElegir={(t) => { setTramite(t); setPaso(2); }} />
            {!s.abiertaAhora && <Aviso tipo="info">La sucursal está fuera de horario.</Aviso>}
          </>}
          {paso === 2 && <div className="dos">
            <div>
              <h1>Digita tu cédula</h1>
              <div className="pantalla-doc">{doc || <span style={{ opacity: 0.3 }}>__________</span>}</div>
              <div className="teclado">
                {['1', '2', '3', '4', '5', '6', '7', '8', '9', 'C', '0', '⌫'].map((k) => <button key={k} onClick={() => tecla(k)}>{k}</button>)}
              </div>
            </div>
            <div className="columna" style={{ justifyContent: 'flex-end' }}>
              <button className={`opcion ${preferencial ? 'elegida' : ''}`} onClick={() => setPreferencial(!preferencial)}>
                <span className="letra">♿</span><span><b>Atención preferencial</b><br /><small>Adulto mayor, embarazo o discapacidad</small></span><span>{preferencial ? '✔' : ''}</span>
              </button>
              <button className="boton ambar grande" disabled={doc.length < 6} onClick={imprimir}>Imprimir turno</button>
              <button className="boton claro" onClick={reiniciar}>Volver</button>
            </div>
          </div>}
          {paso === 3 && tiquete && <div style={{ display: 'grid', placeItems: 'center' }}>
            <div className="papel-tiquete">
              <small>{tiquete.sucursalNombre}</small>
              <div style={{ font: '800 5rem var(--titulo)', letterSpacing: '-0.04em' }}>{tiquete.codigo}</div>
              <b>{tiquete.tramiteNombre}</b>
              <p style={{ marginTop: 10 }}>{tiquete.personasAntes} persona(s) antes · ~{tiquete.tiempoEstimadoMin} min</p>
              <small>{hora(tiquete.horaCreacion)} · Atento a la pantalla de llamado</small>
            </div>
            <button className="boton claro" style={{ marginTop: 20 }} onClick={reiniciar}>Listo</button>
          </div>}
        </div>
      </div>
    </div>
  );
}
