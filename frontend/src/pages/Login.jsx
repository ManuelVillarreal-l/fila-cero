import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { api } from '../api.js';
import { Aviso } from '../components/Comunes.jsx';
import { inicioSegunRol, useSesion } from '../sesion.jsx';

const PRUEBA = [
  ['Cliente', 'cliente@filacero.co', 'Cliente123*'],
  ['Adulto mayor', 'rosa@filacero.co', 'Cliente123*'],
  ['Asesor (Centro)', 'asesor1@filacero.co', 'Asesor123*'],
  ['Administrador', 'admin@filacero.co', 'Admin123*'],
];

export default function Login() {
  const { entrar } = useSesion();
  const navegar = useNavigate();
  const { state } = useLocation();
  const [correo, setCorreo] = useState('');
  const [contrasena, setContrasena] = useState('');
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  const ingresar = async (e) => {
    e?.preventDefault();
    setEnviando(true);
    setError('');
    try {
      const datos = await api.post('/api/auth/login', { correo, contrasena });
      entrar(datos);
      navegar(datos.rol === 'CLIENTE' && state?.volverA ? state.volverA : inicioSegunRol(datos.rol), { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="dos">
      <form className="tarjeta" onSubmit={ingresar}>
        <h1>Ingresar</h1>
        <p className="suave">Clientes, asesores y administradores usan el mismo acceso.</p>
        <Aviso>{error}</Aviso>
        <label>Correo<input type="email" value={correo} onChange={(e) => setCorreo(e.target.value)} required autoComplete="email" /></label>
        <label>Contraseña<input type="password" value={contrasena} onChange={(e) => setContrasena(e.target.value)} required autoComplete="current-password" /></label>
        <button className="boton ambar bloque" disabled={enviando}>{enviando ? 'Ingresando…' : 'Ingresar'}</button>
        <p style={{ marginTop: 14 }}>¿No tienes cuenta? <Link to="/registro">Regístrate</Link></p>
      </form>
      <aside className="tarjeta">
        <h3>Cuentas de demostración</h3>
        <p className="suave">Toca una para llenar el formulario.</p>
        <div className="columna">
          {PRUEBA.map(([rol, c, p]) => (
            <button key={c} type="button" className="opcion" onClick={() => { setCorreo(c); setContrasena(p); }}>
              <span className="letra">{rol[0]}</span><span><b>{rol}</b><br /><small>{c}</small></span><span />
            </button>
          ))}
        </div>
      </aside>
    </div>
  );
}
