import { useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api.js';
import { hhmm, useCada } from '../util.js';
import { Aviso, Cargando } from '../components/Comunes.jsx';
import { useSesion } from '../sesion.jsx';

export default function Inicio() {
  const { sesion } = useSesion();
  const [sucursales, setSucursales] = useState(null);
  const [error, setError] = useState('');

  useCada(() => {
    api.get('/api/sucursales').then((s) => { setSucursales(s); setError(''); }).catch((e) => setError(e.message));
  }, 15000);

  return (
    <>
      <section className="portada">
        <h1>Tu turno en el banco, <em>sin hacer fila</em>.</h1>
        <p>Mira cuánta gente hay en cada sucursal del Banco del Galeras, toma tu turno desde el celular
          y llega cuando te falte poco. Te avisamos cuando estés cerca.</p>
        <div className="pasos">
          <span>1 · Elige sucursal</span><span>2 · Toma tu turno</span><span>3 · Llega justo a tiempo</span>
        </div>
        {!sesion && <div className="fila" style={{ marginTop: 20, position: 'relative', zIndex: 1 }}>
          <Link className="boton ambar" to="/registro">Crear mi cuenta</Link>
          <Link className="boton claro" to="/login">Ya tengo cuenta</Link>
        </div>}
      </section>

      <div className="fila entre"><h2>Sucursales en Pasto</h2><small>Se actualiza cada 15 s</small></div>
      <Aviso>{error}</Aviso>
      {!sucursales && !error && <Cargando texto="Consultando las sucursales…" />}
      <div className="rejilla">
        {sucursales?.map((s) => {
          const max = Math.max(5, ...s.filas.map((f) => f.enEspera));
          return (
            <article key={s.id} className="tarjeta sucursal">
              <div className="fila entre">
                <h3>{s.nombre}</h3>
                <span className={`chip ${s.abiertaAhora ? 'ok' : 'neutro'}`}><i className="punto" />{s.abiertaAhora ? 'Abierta' : 'Cerrada'}</span>
              </div>
              <div className="direccion">{s.direccion} · {hhmm(s.horaApertura)} a {hhmm(s.horaCierre)}</div>
              <div className="filas-mini">
                {s.filas.map((f) => (
                  <div className="fila-mini" key={f.tramiteId}>
                    <span className="letra">{f.prefijo}</span>
                    <div>
                      <div className="fila entre"><span>{f.tramiteNombre}</span><small>{f.enEspera} en espera</small></div>
                      <div className="barra-espera"><i style={{ width: `${(f.enEspera / max) * 100}%` }} /></div>
                    </div>
                    <b title="Tiempo estimado para un turno nuevo">{f.tiempoEstimadoMin}′</b>
                  </div>
                ))}
              </div>
              <Link className="boton bloque" to={`/sucursal/${s.id}`}>Ver y tomar turno</Link>
            </article>
          );
        })}
      </div>
    </>
  );
}
