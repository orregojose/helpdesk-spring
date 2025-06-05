package com.sena.helpdesk.model;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Entidad que representa un usuario en el sistema.
 * Implementa UserDetails para integrarse con Spring Security.
 */
@Data
@Entity
@Table(name = "users")
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Nombre completo del usuario
     * No puede estar vacío
     */
    @NotBlank(message = "El nombre es requerido")
    private String name;
    
    /**
     * Correo electrónico del usuario (usado como nombre de usuario)
     * Debe ser un email válido y no puede estar vacío
     */
    @NotBlank(message = "El email es requerido")
    @Email(message = "Debe ser un email válido")
    private String email;
    
    /**
     * Contraseña del usuario (se almacena encriptada)
     * No puede estar vacía
     */
    @NotBlank(message = "La contraseña es requerida")
    private String password;

    /**
     * Rol del usuario en el sistema
     */
    private String role = "ROLE_USER"; // Valor por defecto

    /**
     * Indica si la cuenta está habilitada
     */
    private boolean enabled = true; // Valor por defecto
    
    /**
     * Lista de tickets creados por el usuario
     * Relación uno a muchos con la entidad Ticket
     */
    @OneToMany(mappedBy = "createdBy")
    private List<Ticket> tickets;

    // Implementación de métodos de UserDetails

    /**
     * Obtiene los roles/autoridades del usuario basado en su rol asignado
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    /**
     * Obtiene el nombre de usuario (email en este caso)
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indica si la cuenta no ha expirado
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indica si la cuenta no está bloqueada
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indica si las credenciales no han expirado
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indica si el usuario está habilitado
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Obtiene el rol del usuario
     */
    public String getRole() {
        return role;
    }

    /**
     * Establece el rol del usuario
     */
    public void setRole(String role) {
        this.role = role;
    }
} 