// Cliente HTTP de la API de Fila Cero. Todas las pantallas pasan por aquí.
export const API_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '');

let tokenActual = null;
export function usarToken(token) {
  tokenActual = token;
}

export class ErrorApi extends Error {
  constructor(estado, mensaje, campos) {
    super(mensaje);
    this.estado = estado;
    this.campos = campos;
  }
}

async function pedir(metodo, ruta, cuerpo) {
  if (import.meta.env.VITE_MOCK === '1') {
    const { respuestaMock } = await import('./mock.js');
    return respuestaMock(metodo, ruta, cuerpo);
  }
  const cabeceras = { Accept: 'application/json' };
  if (cuerpo !== undefined) cabeceras['Content-Type'] = 'application/json';
  if (tokenActual) cabeceras.Authorization = `Bearer ${tokenActual}`;
  let resp;
  try {
    resp = await fetch(API_URL + ruta, {
      method: metodo,
      headers: cabeceras,
      body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
    });
  } catch {
    throw new ErrorApi(0, 'No hay conexión con el servidor. Si es la primera visita del día, espera un minuto mientras despierta.');
  }
  if (resp.status === 204) return null;
  const texto = await resp.text();
  const datos = texto ? JSON.parse(texto) : null;
  if (!resp.ok) {
    throw new ErrorApi(resp.status, datos?.mensaje || 'Ocurrió un error', datos?.campos);
  }
  return datos;
}

export const api = {
  get: (r) => pedir('GET', r),
  post: (r, c = {}) => pedir('POST', r, c),
  put: (r, c) => pedir('PUT', r, c),
  patch: (r, c) => pedir('PATCH', r, c),
  del: (r) => pedir('DELETE', r),
};
