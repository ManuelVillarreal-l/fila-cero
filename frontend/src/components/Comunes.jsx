import { ESTADOS } from '../util.js';

export const Cargando = ({ texto = 'Cargando…' }) => <div className="cargando">{texto}</div>;

export function Aviso({ tipo = 'error', children }) {
  if (!children) return null;
  return <div className={`aviso ${tipo}`} role={tipo === 'error' ? 'alert' : 'status'}>{children}</div>;
}

export function Estado({ valor }) {
  const e = ESTADOS[valor] || { texto: valor, clase: 'neutro' };
  return <span className={`chip ${e.clase}`}><i className="punto" />{e.texto}</span>;
}

export function Vacio({ titulo, children }) {
  return <div className="vacio"><b>{titulo}</b>{children}</div>;
}

export function Estrellas({ valor = 0, onElegir }) {
  return (
    <div className="estrellas" role="radiogroup" aria-label="Calificación">
      {[1, 2, 3, 4, 5].map((n) => (
        <button key={n} type="button" className={n <= valor ? 'on' : ''} onClick={() => onElegir?.(n)}
          aria-label={`${n} estrellas`} disabled={!onElegir}>★</button>
      ))}
    </div>
  );
}

export function Modal({ titulo, onCerrar, children }) {
  return (
    <div className="modal-fondo" onClick={onCerrar}>
      <div className="modal" onClick={(e) => e.stopPropagation()} role="dialog" aria-label={titulo}>
        <div className="fila entre"><h2>{titulo}</h2><button className="boton claro mini" onClick={onCerrar}>Cerrar</button></div>
        {children}
      </div>
    </div>
  );
}

/** Lista de trámites para elegir, con su fila actual. */
export function SelectorTramite({ filas, elegido, onElegir }) {
  return (
    <div className="opciones">
      {filas.map((f) => (
        <button key={f.tramiteId} type="button" className={`opcion ${elegido === f.tramiteId ? 'elegida' : ''}`}
          onClick={() => onElegir(f.tramiteId)}>
          <span className="letra">{f.prefijo}</span>
          <span><b>{f.tramiteNombre}</b><br /><small>{f.enEspera} en espera · {f.modulosAbiertos} módulo(s) abiertos</small></span>
          <span className="chip espera">~{f.tiempoEstimadoMin} min</span>
        </button>
      ))}
    </div>
  );
}
