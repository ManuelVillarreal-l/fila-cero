import { Navigate, NavLink, Route, Routes, useLocation, useNavigate } from 'react-router-dom';
import { useSesion } from './sesion.jsx';
import Inicio from './pages/Inicio.jsx';
import SucursalDetalle from './pages/SucursalDetalle.jsx';
import Login from './pages/Login.jsx';
import Registro from './pages/Registro.jsx';
import MiTurno from './pages/MiTurno.jsx';
import Historial from './pages/Historial.jsx';
import Citas from './pages/Citas.jsx';
import Kiosco from './pages/Kiosco.jsx';
import Pantalla from './pages/Pantalla.jsx';
import Asesor from './pages/Asesor.jsx';
import Admin from './pages/Admin.jsx';

function Protegida({ rol, children }) {
  const { sesion } = useSesion();
  const ubicacion = useLocation();
  if (!sesion) return <Navigate to="/login" state={{ volverA: ubicacion.pathname }} replace />;
  if (rol && sesion.rol !== rol) return <Navigate to="/" replace />;
  return children;
}

function Barra() {
  const { sesion, salir } = useSesion();
  const navegar = useNavigate();
  const rol = sesion?.rol;
  return (
    <header className="barra">
      <div className="marco">
        <NavLink to="/" className="marca">
          <img src="/icono.svg" alt="" width="34" height="34" />
          <span>Fila Cero<small>Banco del Galeras</small></span>
        </NavLink>
        <nav>
          <NavLink to="/" end className="solo-escritorio">Sucursales</NavLink>
          {rol === 'CLIENTE' && <>
            <NavLink to="/mi-turno" className="solo-escritorio">Mi turno</NavLink>
            <NavLink to="/citas" className="solo-escritorio">Citas</NavLink>
            <NavLink to="/historial" className="solo-escritorio">Historial</NavLink>
          </>}
          {rol === 'ASESOR' && <NavLink to="/asesor">Mi módulo</NavLink>}
          {rol === 'ADMINISTRADOR' && <NavLink to="/admin">Administración</NavLink>}
          {sesion
            ? <button onClick={() => { salir(); navegar('/'); }} title={sesion.correo}>Salir</button>
            : <NavLink to="/login" className="entrar">Ingresar</NavLink>}
        </nav>
      </div>
    </header>
  );
}

function NavMovil() {
  const { sesion } = useSesion();
  if (sesion && sesion.rol !== 'CLIENTE') return null;
  return (
    <nav className="nav-movil">
      <NavLink to="/" end><span>🏦</span>Sucursales</NavLink>
      <NavLink to="/mi-turno"><span>🎫</span>Mi turno</NavLink>
      <NavLink to="/citas"><span>📅</span>Citas</NavLink>
      <NavLink to="/historial"><span>🕘</span>Historial</NavLink>
    </nav>
  );
}

function ConMarco({ children }) {
  return (
    <>
      <Barra />
      <main className="marco">{children}</main>
      <p className="pie">Fila Cero · Proyecto académico de Diseño de Software · Banco del Galeras es una entidad ficticia</p>
      <NavMovil />
    </>
  );
}

export default function App() {
  return (
    <Routes>
      {/* Pantallas de sucursal a pantalla completa, sin barra */}
      <Route path="/kiosco/:id" element={<Kiosco />} />
      <Route path="/pantalla/:id" element={<Pantalla />} />

      <Route path="/" element={<ConMarco><Inicio /></ConMarco>} />
      <Route path="/sucursal/:id" element={<ConMarco><SucursalDetalle /></ConMarco>} />
      <Route path="/login" element={<ConMarco><Login /></ConMarco>} />
      <Route path="/registro" element={<ConMarco><Registro /></ConMarco>} />
      <Route path="/mi-turno" element={<ConMarco><Protegida rol="CLIENTE"><MiTurno /></Protegida></ConMarco>} />
      <Route path="/historial" element={<ConMarco><Protegida rol="CLIENTE"><Historial /></Protegida></ConMarco>} />
      <Route path="/citas" element={<ConMarco><Protegida rol="CLIENTE"><Citas /></Protegida></ConMarco>} />
      <Route path="/asesor" element={<ConMarco><Protegida rol="ASESOR"><Asesor /></Protegida></ConMarco>} />
      <Route path="/admin" element={<ConMarco><Protegida rol="ADMINISTRADOR"><Admin /></Protegida></ConMarco>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
