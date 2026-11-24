import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api.js';
import { Aviso } from '../components/Comunes.jsx';
import { useSesion } from '../sesion.jsx';

const VACIO = { nombre: '', documento: '', correo: '', contrasena: '', telefono: '', fechaNacimiento: '', condicion: 'NINGUNA' };

export default function Registro() {
  const { entrar } = useSesion();
  const navegar = useNavigate();
  const [f, setF] = useState(VACIO);
  const [error, setError] = useState('');
  const [campos, setCampos] = useState({});
  const cambiar = (k) => (e) => setF({ ...f, [k]: e.target.value });

  const enviar = async (e) => {
    e.preventDefault();
    setError('');
    setCampos({});
    try {
      const datos = await api.post('/api/auth/registro', {
        ...f, telefono: f.telefono || null, fechaNacimiento: f.fechaNacimiento || null,
      });
      entrar(datos);
      navegar('/');
    } catch (err) {
      setError(err.message);
      setCampos(err.campos || {});
    }
  };

  const Campo = ({ k, etiqueta, ...props }) => (
    <label>{etiqueta}<input value={f[k]} onChange={cambiar(k)} {...props} />
      {campos[k] && <span className="error-campo">{campos[k]}</span>}</label>
  );

  return (
    <form className="tarjeta" style={{ maxWidth: 620, margin: '0 auto' }} onSubmit={enviar}>
      <h1>Crear cuenta</h1>
      <p className="suave">Con tu cuenta tomas turnos desde el celular, agendas citas y recibes avisos.</p>
      <Aviso>{error}</Aviso>
      {Campo({ k: 'nombre', etiqueta: 'Nombre completo', required: true })}
      <div className="dos">
        {Campo({ k: 'documento', etiqueta: 'Cédula', inputMode: 'numeric', required: true })}
        {Campo({ k: 'telefono', etiqueta: 'Celular (opcional)', inputMode: 'tel', placeholder: '3001234567' })}
      </div>
      {Campo({ k: 'correo', etiqueta: 'Correo', type: 'email', required: true })}
      {Campo({ k: 'contrasena', etiqueta: 'Contraseña (mínimo 8 caracteres)', type: 'password', required: true })}
      <div className="dos">
        {Campo({ k: 'fechaNacimiento', etiqueta: 'Fecha de nacimiento', type: 'date' })}
        <label>Atención preferencial
          <select value={f.condicion} onChange={cambiar('condicion')}>
            <option value="NINGUNA">No aplica</option>
            <option value="ADULTO_MAYOR">Adulto mayor</option>
            <option value="EMBARAZO">Embarazo</option>
            <option value="DISCAPACIDAD">Discapacidad</option>
          </select>
        </label>
      </div>
      <p className="suave">Los mayores de 60 años, las mujeres embarazadas y las personas con discapacidad tienen prioridad en la fila.</p>
      <button className="boton ambar bloque">Crear cuenta</button>
      <p style={{ marginTop: 14 }}>¿Ya tienes cuenta? <Link to="/login">Ingresa</Link></p>
    </form>
  );
}
