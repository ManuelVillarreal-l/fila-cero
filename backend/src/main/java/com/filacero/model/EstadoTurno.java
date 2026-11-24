package com.filacero.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Estados del turno (RN6) implementados con el patrón State sobre un enum:
 * cada estado sabe a qué estados puede pasar. La clase Turno delega en él
 * la validación de cada transición.
 *
 * EN_ESPERA -> LLAMADO | CANCELADO
 * LLAMADO -> LLAMADO (volver a llamar) | EN_ATENCION | AUSENTE
 * EN_ATENCION -> ATENDIDO | EN_ESPERA (transferido a otro trámite)
 * ATENDIDO, AUSENTE, CANCELADO: finales
 */
public enum EstadoTurno {

    EN_ESPERA {
        @Override
        public Set<EstadoTurno> siguientes() {
            return EnumSet.of(LLAMADO, CANCELADO);
        }
    },
    LLAMADO {
        @Override
        public Set<EstadoTurno> siguientes() {
            return EnumSet.of(LLAMADO, EN_ATENCION, AUSENTE);
        }
    },
    EN_ATENCION {
        @Override
        public Set<EstadoTurno> siguientes() {
            return EnumSet.of(ATENDIDO, EN_ESPERA);
        }
    },
    ATENDIDO {
        @Override
        public Set<EstadoTurno> siguientes() {
            return EnumSet.noneOf(EstadoTurno.class);
        }
    },
    AUSENTE {
        @Override
        public Set<EstadoTurno> siguientes() {
            return EnumSet.noneOf(EstadoTurno.class);
        }
    },
    CANCELADO {
        @Override
        public Set<EstadoTurno> siguientes() {
            return EnumSet.noneOf(EstadoTurno.class);
        }
    };

    /** Estados a los que se puede pasar desde este. */
    public abstract Set<EstadoTurno> siguientes();

    public boolean puedePasarA(EstadoTurno destino) {
        return siguientes().contains(destino);
    }

    /** Un turno activo ocupa la cola o una ventanilla. */
    public boolean esActivo() {
        return this == EN_ESPERA || this == LLAMADO || this == EN_ATENCION;
    }

    public boolean esFinal() {
        return siguientes().isEmpty();
    }
}
