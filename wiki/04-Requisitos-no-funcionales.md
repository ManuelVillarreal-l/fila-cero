# 04 · Requisitos no funcionales

| Código | Atributo | Requisito |
|---|---|---|
| RNF_001 | Tecnología | Backend en Java 17 con Spring Boot 3.3. |
| RNF_002 | Persistencia | Base de datos no relacional MongoDB (Atlas en la nube). |
| RNF_003 | Arquitectura | Arquitectura por capas Controller → Service → Repository con DTO; los controladores no acceden a repositorios. |
| RNF_004 | Seguridad | Contraseñas con BCrypt, JWT con expiración, permisos por rol y ninguna credencial en el repositorio. |
| RNF_005 | Mantenibilidad | Las reglas que cambian (bonificaciones, tiempos, llamados máximos) están en constantes con nombre y en estrategias intercambiables. |
| RNF_006 | Usabilidad de la API | Errores con código HTTP correcto (400, 401, 403, 404, 409, 422) y mensaje en español. |
| RNF_007 | Rendimiento | Consultar el estado de las filas responde en menos de 2 segundos con índices en MongoDB. |
| RNF_008 | Calidad | Pruebas unitarias JUnit 5 + Mockito y pruebas de API con Postman ejecutadas en CI (GitHub Actions). |
| RNF_009 | Portabilidad | Backend en contenedor Docker desplegado en la nube (Render) y frontend en Vercel. |
| RNF_010 | Documentación | Swagger/OpenAPI, README, wiki tipo SRS y diagramas UML en Visual Paradigm. |
| RNF_011 | Plataforma | Interfaz web responsive pensada primero para celular, más vistas de kiosco y pantalla de TV. |
| RNF_012 | Integración | El servicio de notificaciones externo se usa a través de una interfaz propia (NotificadorTurnos). |
| RNF_013 | Concurrencia | Los códigos de turno se generan de forma atómica y los turnos usan bloqueo optimista (@Version). |
| RNF_014 | Privacidad | El documento del cliente se muestra enmascarado en las respuestas públicas. |
