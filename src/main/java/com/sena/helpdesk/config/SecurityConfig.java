package com.sena.helpdesk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import lombok.RequiredArgsConstructor;

/**
 * Configuración de seguridad de la aplicación.
 * Define las reglas de acceso, autenticación y autorización.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final PasswordConfig passwordConfig;

    /**
     * Configura la cadena de filtros de seguridad
     * Define las reglas de acceso a las diferentes rutas de la aplicación
     * @param http Configuración de seguridad HTTP
     * @return Cadena de filtros de seguridad configurada
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Configuración de autorización de rutas
            .authorizeHttpRequests(auth -> auth
                // Recursos estáticos accesibles sin autenticación
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/fonts/**", "/favicon.ico").permitAll()
                // Rutas públicas
                .requestMatchers("/register", "/login", "/", "/error", "/.well-known/**").permitAll()
                // Todas las demás rutas requieren autenticación
                .anyRequest().authenticated()
            )
            // Configuración del formulario de login
            .formLogin(form -> form
                .loginPage("/login")              // Página de login personalizada
                .usernameParameter("username")     // Campo de usuario en el formulario
                .passwordParameter("password")     // Campo de contraseña en el formulario
                .defaultSuccessUrl("/tickets")     // Redirección después de login exitoso
                .failureUrl("/login?error")       // Redirección en caso de error
                .permitAll()
            )
            // Configuración del logout
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))  // URL para cerrar sesión
                .logoutSuccessUrl("/login?logout")                          // Redirección después de logout
                .permitAll()
            )
            // Deshabilitamos CSRF para simplificar las peticiones
            .csrf(csrf -> csrf.disable());
        
        return http.build();
    }

    /**
     * Configura el administrador de autenticación
     * Define cómo se validan las credenciales de los usuarios
     * @param http Configuración de seguridad HTTP
     * @return Administrador de autenticación configurado
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
            .userDetailsService(userDetailsService)    // Servicio para cargar los detalles del usuario
            .passwordEncoder(passwordConfig.passwordEncoder());  // Codificador de contraseñas
        return authenticationManagerBuilder.build();
    }
} 