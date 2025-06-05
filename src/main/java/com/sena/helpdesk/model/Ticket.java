package com.sena.helpdesk.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.persistence.CascadeType;

/**
 * Entidad que representa un ticket de soporte en el sistema.
 * Esta clase utiliza JPA para el mapeo objeto-relacional y Lombok para reducir el código boilerplate.
 */
@Data
@Entity
public class Ticket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Título descriptivo del ticket
     * No puede estar vacío (validación con @NotBlank)
     */
    @NotBlank(message = "El título es requerido")
    private String title;
    
    /**
     * Descripción detallada del problema o solicitud
     * No puede estar vacía (validación con @NotBlank)
     */
    @NotBlank(message = "La descripción es requerida")
    private String description;
    
    /**
     * Estado actual del ticket (OPEN, IN_PROGRESS, RESOLVED, CLOSED)
     * Se inicializa como OPEN por defecto
     */
    @Enumerated(EnumType.STRING)
    private TicketStatus status = TicketStatus.OPEN;
    
    /**
     * Nivel de prioridad del ticket (LOW, MEDIUM, HIGH)
     * Se inicializa como MEDIUM por defecto
     */
    @NotNull(message = "La prioridad es requerida")
    @Enumerated(EnumType.STRING)
    private TicketPriority priority = TicketPriority.MEDIUM;

    /**
     * Categoría del ticket (HARDWARE, SOFTWARE, NETWORK, OTHER)
     * Debe especificarse al crear el ticket
     */
    @NotNull(message = "La categoría es requerida")
    @Enumerated(EnumType.STRING)
    private TicketCategory category;
    
    /**
     * Usuario que creó el ticket
     * Relación muchos a uno con la entidad User
     */
    @ManyToOne
    private User createdBy;
    
    /**
     * Lista de comentarios del ticket
     */
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();
    
    /**
     * Fechas de creación y última actualización del ticket
     * Se manejan automáticamente con @PrePersist y @PreUpdate
     */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Se ejecuta antes de persistir un nuevo ticket
     * Inicializa las fechas de creación y actualización
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TicketStatus.OPEN;
        }
    }
    
    /**
     * Se ejecuta antes de actualizar un ticket existente
     * Actualiza la fecha de última modificación
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Estados posibles de un ticket
     */
    public enum TicketStatus {
        OPEN {
            @Override
            public String toString() {
                return "Abierto";
            }
        },
        IN_PROGRESS {
            @Override
            public String toString() {
                return "En Progreso";
            }
        },
        RESOLVED {
            @Override
            public String toString() {
                return "Resuelto";
            }
        },
        CLOSED {
            @Override
            public String toString() {
                return "Cerrado";
            }
        }
    }
    
    /**
     * Niveles de prioridad disponibles
     */
    public enum TicketPriority {
        LOW {
            @Override
            public String toString() {
                return "Baja";
            }
        },
        MEDIUM {
            @Override
            public String toString() {
                return "Media";
            }
        },
        HIGH {
            @Override
            public String toString() {
                return "Alta";
            }
        }
    }

    /**
     * Categorías de tickets disponibles
     */
    public enum TicketCategory {
        HARDWARE {
            @Override
            public String toString() {
                return "Hardware";
            }
        },
        SOFTWARE {
            @Override
            public String toString() {
                return "Software";
            }
        },
        NETWORK {
            @Override
            public String toString() {
                return "Red";
            }
        },
        OTHER {
            @Override
            public String toString() {
                return "Otro";
            }
        }
    }
} 