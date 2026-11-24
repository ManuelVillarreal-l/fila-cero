package com.filacero.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * El reloj se inyecta (inyección de dependencias) para que las reglas de horario y tiempos
 * usen la hora de Colombia aunque el servidor esté en otra zona, y para poder fijarlo en las pruebas.
 */
@Configuration
public class RelojConfig {

    public static final ZoneId ZONA = ZoneId.of("America/Bogota");

    @Bean
    public Clock reloj() {
        return Clock.system(ZONA);
    }
}
