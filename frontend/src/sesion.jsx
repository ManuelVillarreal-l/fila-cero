import { createContext, useContext, useState } from 'react';
import { usarToken } from './api.js';

const Contexto = createContext(null);
const CLAVE = 'filacero.sesion';

function leer() {
  try {
    const s = JSON.parse(localStorage.getItem(CLAVE));
    if (s?.token) usarToken(s.token);
    return s;
  } catch {
    return null;
  }
}

export function ProveedorSesion({ children }) {
  const [sesion, setSesion] = useState(leer);

  const entrar = (datos) => {
    usarToken(datos.token);
    try { localStorage.setItem(CLAVE, JSON.stringify(datos)); } catch { /* navegador sin almacenamiento */ }
    setSesion(datos);
  };
  const salir = () => {
    usarToken(null);
    try { localStorage.removeItem(CLAVE); } catch { /* nada */ }
    setSesion(null);
  };

  return <Contexto.Provider value={{ sesion, entrar, salir }}>{children}</Contexto.Provider>;
}

export const useSesion = () => useContext(Contexto);

export function inicioSegunRol(rol) {
  if (rol === 'ASESOR') return '/asesor';
  if (rol === 'ADMINISTRADOR') return '/admin';
  return '/mi-turno';
}
