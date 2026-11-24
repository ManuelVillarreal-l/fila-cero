package com.filacero.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filacero.security.FiltroJwt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Seguridad: API sin estado con JWT, BCrypt y permisos por rol (RNF_004). */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${filacero.cors.origenes}")
    private String origenes;

    @Bean
    public SecurityFilterChain cadena(HttpSecurity http, FiltroJwt filtroJwt, ObjectMapper json) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(c -> c.configurationSource(cors()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api/estado").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/sucursales/**", "/api/tramites/**", "/api/pantalla/**").permitAll()
                        .requestMatchers("/api/kiosco/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/turnos/**", "/api/citas/**", "/api/notificaciones/**").hasRole("CLIENTE")
                        .requestMatchers("/api/atencion/**").hasRole("ASESOR")
                        .requestMatchers("/api/asesores/**", "/api/tablero/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/modulos/**").hasAnyRole("ADMINISTRADOR", "ASESOR")
                        .anyRequest().hasRole("ADMINISTRADOR"))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> escribir(res, json, 401,
                                "Debes iniciar sesión para usar esta función"))
                        .accessDeniedHandler((req, res, ex) -> escribir(res, json, 403,
                                "Tu rol no tiene permiso para esta operación")))
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void escribir(jakarta.servlet.http.HttpServletResponse res, ObjectMapper json, int estado, String mensaje)
            throws java.io.IOException {
        res.setStatus(estado);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        json.writeValue(res.getWriter(), Map.of(
                "fecha", LocalDateTime.now().toString(),
                "estado", estado,
                "mensaje", mensaje));
    }

    @Bean
    public CorsConfigurationSource cors() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.stream(origenes.split(",")).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", config);
        return fuente;
    }

    @Bean
    public PasswordEncoder codificador() {
        return new BCryptPasswordEncoder();
    }
}
