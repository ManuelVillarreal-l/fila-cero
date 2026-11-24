# 07 · Pruebas y despliegue

## Pruebas
- **Unitarias (JUnit 5 + Mockito):** reglas de prioridad, cola y tiempo estimado, ciclo de vida del turno, citas, módulo, tablero y servicios de turnos y atención.
- **API (Postman):** 71 peticiones por historia de usuario con aserciones (`postman/`).
- **CI (GitHub Actions):** compila, corre las pruebas, levanta la API con MongoDB real y ejecuta Postman con Newman en cada push.

## Despliegue
| Parte | Plataforma | URL |
|---|---|---|
| Frontend | Vercel | https://fila-cero.vercel.app |
| Backend | Render (Docker) | https://filacero-backend.onrender.com |
| Base de datos | MongoDB Atlas M0 | (privada) |

![](https://github.com/ManuelVillarreal-l/fila-cero/raw/main/diagramas/vista-despliegue.png)

## Cómo probar la API desplegada
1. Abrir [Swagger](https://filacero-backend.onrender.com/swagger-ui.html) (la primera petición puede tardar ~1 minuto mientras Render despierta).
2. `POST /api/auth/login` con `admin@filacero.co` / `Admin123*`, copiar el token y pulsar **Authorize**.
3. O importar la colección de Postman con el entorno **Fila Cero - Render** y ejecutarla.
