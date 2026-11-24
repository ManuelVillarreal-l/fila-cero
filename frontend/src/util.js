import { useEffect, useRef } from 'react';

export const hora = (iso) =>
  iso ? new Date(iso).toLocaleTimeString('es-CO', { hour: 'numeric', minute: '2-digit' }) : '—';

export const fechaHora = (iso) =>
  iso ? new Date(iso).toLocaleString('es-CO', { weekday: 'short', day: 'numeric', month: 'short', hour: 'numeric', minute: '2-digit' }) : '—';

export const hhmm = (t) => (t ? t.slice(0, 5) : '');

export const minutos = (m) => {
  if (m === null || m === undefined) return '—';
  if (m < 1) return 'menos de 1 min';
  if (m < 60) return `${m} min`;
  return `${Math.floor(m / 60)} h ${m % 60} min`;
};

export const ESTADOS = {
  EN_ESPERA: { texto: 'En espera', clase: 'espera' },
  LLAMADO: { texto: '¡Te están llamando!', clase: 'llamado' },
  EN_ATENCION: { texto: 'En atención', clase: 'atencion' },
  ATENDIDO: { texto: 'Atendido', clase: 'ok' },
  AUSENTE: { texto: 'Ausente', clase: 'mal' },
  CANCELADO: { texto: 'Cancelado', clase: 'neutro' },
  PROGRAMADA: { texto: 'Programada', clase: 'espera' },
  REGISTRADA: { texto: 'Check-in hecho', clase: 'ok' },
  CANCELADA: { texto: 'Cancelada', clase: 'neutro' },
  VENCIDA: { texto: 'Vencida', clase: 'mal' },
};

export const PRIORIDAD = { GENERAL: 'General', PREFERENCIAL: 'Preferencial', CITA: 'Cita' };

/** Ejecuta fn ahora y cada `ms` milisegundos mientras el componente esté montado. */
export function useCada(fn, ms, deps = []) {
  const ref = useRef(fn);
  ref.current = fn;
  useEffect(() => {
    ref.current();
    const id = setInterval(() => ref.current(), ms);
    return () => clearInterval(id);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);
}

/** Sonido de "ding" y anuncio por voz para los llamados. */
export function anunciar(texto) {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    [880, 1320].forEach((f, i) => {
      const o = ctx.createOscillator();
      const g = ctx.createGain();
      o.frequency.value = f;
      g.gain.setValueAtTime(0.25, ctx.currentTime + i * 0.25);
      g.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + i * 0.25 + 0.6);
      o.connect(g).connect(ctx.destination);
      o.start(ctx.currentTime + i * 0.25);
      o.stop(ctx.currentTime + i * 0.25 + 0.6);
    });
  } catch { /* sin audio */ }
  try {
    if (texto && 'speechSynthesis' in window) {
      const u = new SpeechSynthesisUtterance(texto);
      u.lang = 'es-CO';
      u.rate = 0.95;
      setTimeout(() => window.speechSynthesis.speak(u), 700);
    }
  } catch { /* sin voz */ }
}

/** "C-014" -> "C, cero catorce" para la voz. */
export const codigoHablado = (c) => (c ? c.replace('-', ', ').replace(/0(\d)/g, 'cero $1') : '');
