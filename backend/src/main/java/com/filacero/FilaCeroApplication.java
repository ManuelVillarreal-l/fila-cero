package com.filacero;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class FilaCeroApplication {

    public static void main(String[] args) {
        // Fechas y horas siempre en hora de Colombia, también en Render (que corre en UTC).
        TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
        SpringApplication.run(FilaCeroApplication.class, args);
    }
}
