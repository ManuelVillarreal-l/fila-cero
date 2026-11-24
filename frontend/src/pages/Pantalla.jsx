import { useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../api.js';
import { anunciar, codigoHablado, hora } from '../util.js';

/** Pantalla de llamado de la sucursal (HU_018). Pensada para un televisor. */
export default function Pantalla() {
  const { id } = useParams();
  const [datos, setDatos] = useState(null);
  const [nuevo, setNuevo] = useState(false);
  const [sonido, setSonido] = useState(false);
  const ultimo = useRef(null);
  const [ahora, setAhora] = useState(new Date());

  useEffect(() => {
    const cargar = () => api.get(`/api/pantalla/${id}`).then((d) => {
      setDatos(d);
      const primero = d.llamados[0];
      const clave = primero ? `${primero.id}-${primero.vecesLlamado}-${primero.horaLlamado}` : null;
      if (clave && ultimo.current && clave !== ultimo.current) {
        setNuevo(true);
        setTimeout(() => setNuevo(false), 2600);
        if (sonido) anunciar(`Turno ${codigoHablado(primero.codigo)}, ${primero.moduloNombre}`);
      }
      ultimo.current = clave;
    }).catch(() => {});
    cargar();
    const i = setInterval(cargar, 3000);
    const r = setInterval(() => setAhora(new Date()), 1000);
    return () => { clearInterval(i); clearInterval(r); };
  }, [id, sonido]);

  if (!datos) return <div className="tv"><header><h2>Cargando…</h2></header></div>;
  const [actual, ...anteriores] = datos.llamados;

  return (
    <div className="tv" onClick={() => setSonido(true)}>
      <header>
        <div className="fila"><img src="/icono.svg" width="44" height="44" alt="" /><div><b style={{ fontSize: '1.4rem' }}>Banco del Galeras</b><br /><span style={{ color: '#9fbac3' }}>{datos.sucursalNombre}</span></div></div>
        <div className="reloj">{ahora.toLocaleTimeString('es-CO', { hour: 'numeric', minute: '2-digit' })}</div>
      </header>
      <section className={`principal ${nuevo ? 'nuevo' : ''}`}>
        {actual ? <>
          <div style={{ color: '#9fbac3', fontWeight: 700, fontSize: '1.4rem' }}>TURNO</div>
          <div className="codigo">{actual.codigo}</div>
          <div className="destino">→ {actual.moduloNombre || actual.tramiteNombre}</div>
        </> : <div className="destino" style={{ color: '#9fbac3' }}>Aún no hay turnos llamados hoy</div>}
        {!sonido && <p style={{ color: '#5f8796', marginTop: 30 }}>Toca la pantalla para activar el sonido</p>}
      </section>
      <aside>
        <h3 style={{ color: '#9fbac3' }}>Últimos llamados</h3>
        {anteriores.map((t) => (
          <div className="item" key={t.id}>{t.codigo}<span>{t.moduloNombre || t.tramiteNombre} · {hora(t.horaLlamado)}</span></div>
        ))}
      </aside>
      <footer>
        {datos.filas.map((f) => <div key={f.tramiteId}>{f.tramiteNombre}: <b>{f.enEspera}</b> en espera · ~{f.tiempoEstimadoMin} min</div>)}
      </footer>
    </div>
  );
}
