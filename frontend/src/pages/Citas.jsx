import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api } from '../api.js';
import { fechaHora, hora } from '../util.js';
import { Aviso, Cargando, Estado, Vacio } from '../components/Comunes.jsx';
import { useNavigate } from 'react-router-dom';

const manana = () => {
  const d = new Date(Date.now() + 86400000);
  return d.toLocaleDateString('en-CA');
};

export default function Citas() {
  const [params] = useSearchParams();
  const navegar = useNavigate();
  const [sucursales, setSucursales] = useState([]);
  const [sucursal, setSucursal] = useState(params.get('sucursal') || '');
  const [tramite, setTramite] = useState('');
  const [fecha, setFecha] = useState(manana());
  const [franjas, setFranjas] = useState(null);
  const [franja, setFranja] = useState('');
  const [motivo, setMotivo] = useState('');
  const [citas, setCitas] = useState(null);
  const [error, setError] = useState('');
  const [ok, setOk] = useState('');

  const cargarCitas = () => api.get('/api/citas').then(setCitas).catch((e) => setError(e.message));
  useEffect(() => {
    api.get('/api/sucursales').then((s) => { setSucursales(s); if (!sucursal && s[0]) setSucursal(s[0].id); });
    cargarCitas();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const tramites = sucursales.find((s) => s.id === sucursal)?.filas || [];
  useEffect(() => {
    setFranja('');
    if (!sucursal || !tramite || !fecha) { setFranjas(null); return; }
    api.get(`/api/citas/franjas?sucursalId=${sucursal}&tramiteId=${tramite}&fecha=${fecha}`)
      .then(setFranjas).catch((e) => setError(e.message));
  }, [sucursal, tramite, fecha]);

  const agendar = async () => {
    setError(''); setOk('');
    try {
      await api.post('/api/citas', { sucursalId: sucursal, tramiteId: tramite, fechaHora: franja, motivo });
      setOk('¡Cita agendada! El día de la cita haz check-in al llegar a la sucursal.');
      setFranja(''); setMotivo('');
      cargarCitas();
      setFecha((f) => f);
    } catch (e) { setError(e.message); }
  };

  const accion = async (ruta, metodo, irATurno) => {
    setError(''); setOk('');
    try {
      await api[metodo](ruta);
      if (irATurno) navegar('/mi-turno'); else cargarCitas();
    } catch (e) { setError(e.message); }
  };

  return (
    <div className="dos">
      <section className="tarjeta">
        <h1>Agendar cita</h1>
        <p className="suave">Las citas tienen la mayor prioridad en la fila. Haz check-in entre 15 minutos antes y 10 minutos después de tu hora.</p>
        <Aviso>{error}</Aviso>
        <Aviso tipo="ok">{ok}</Aviso>
        <label>Sucursal
          <select value={sucursal} onChange={(e) => { setSucursal(e.target.value); setTramite(''); }}>
            {sucursales.map((s) => <option key={s.id} value={s.id}>{s.nombre}</option>)}
          </select>
        </label>
        <label>Trámite
          <select value={tramite} onChange={(e) => setTramite(e.target.value)}>
            <option value="">Elige…</option>
            {tramites.map((t) => <option key={t.tramiteId} value={t.tramiteId}>{t.tramiteNombre}</option>)}
          </select>
        </label>
        <label>Fecha<input type="date" value={fecha} min={new Date().toLocaleDateString('en-CA')} onChange={(e) => setFecha(e.target.value)} /></label>
        {franjas && <>
          <b>Hora</b>
          <div className="franjas" style={{ margin: '8px 0 14px' }}>
            {franjas.map((f) => (
              <button key={f.fechaHora} type="button" disabled={!f.disponible}
                className={`franja ${franja === f.fechaHora ? 'elegida' : ''}`} onClick={() => setFranja(f.fechaHora)}>
                {hora(f.fechaHora)}<small>{f.cupos - f.ocupados} cupo(s)</small>
              </button>
            ))}
          </div>
        </>}
        <label>Motivo (opcional)<input value={motivo} onChange={(e) => setMotivo(e.target.value)} placeholder="Ej.: abrir un CDT" /></label>
        <button className="boton ambar bloque" disabled={!franja} onClick={agendar}>Agendar cita</button>
      </section>
      <aside>
        <h2>Mis citas</h2>
        {!citas && <Cargando />}
        {citas?.length === 0 && <div className="tarjeta"><Vacio titulo="Sin citas">Agenda una y evita la fila.</Vacio></div>}
        {citas?.map((c) => (
          <div key={c.id} className="tarjeta">
            <div className="fila entre"><b>{c.tramiteNombre}</b><Estado valor={c.estado} /></div>
            <p className="suave">{c.sucursalNombre}<br />{fechaHora(c.fechaHora)}{c.motivo ? ` · ${c.motivo}` : ''}</p>
            <div className="fila">
              {c.puedeHacerCheckIn && <button className="boton exito mini" onClick={() => accion(`/api/citas/${c.id}/check-in`, 'post', true)}>Ya llegué (check-in)</button>}
              {c.puedeCancelarse && <button className="boton peligro mini" onClick={() => accion(`/api/citas/${c.id}/cancelar`, 'patch')}>Cancelar</button>}
            </div>
          </div>
        ))}
      </aside>
    </div>
  );
}
