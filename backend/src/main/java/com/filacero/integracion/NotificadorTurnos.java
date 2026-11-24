package com.filacero.integracion;

import com.filacero.model.Notificacion;

/**
 * Interfaz propia hacia el servicio externo de notificaciones (SMS / correo / push).
 * GRASP Indirección y Variaciones protegidas: si mañana se usa Twilio o Firebase,
 * solo se cambia la implementación (RNF_012).
 */
public interface NotificadorTurnos {

    void enviar(Notificacion notificacion);
}
