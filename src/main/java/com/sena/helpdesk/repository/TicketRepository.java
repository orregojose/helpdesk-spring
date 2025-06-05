package com.sena.helpdesk.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sena.helpdesk.model.Ticket;
import com.sena.helpdesk.model.User;
import com.sena.helpdesk.model.Ticket.TicketStatus;
import com.sena.helpdesk.model.Ticket.TicketPriority;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCreatedBy(User user);
    long countByStatus(TicketStatus status);
    
    // Método para buscar por estado
    List<Ticket> findByStatus(TicketStatus status);
    
    // Método para buscar por prioridad
    List<Ticket> findByPriority(TicketPriority priority);
    
    // Método para buscar por estado y prioridad
    List<Ticket> findByStatusAndPriority(TicketStatus status, TicketPriority priority);
    
    // Método personalizado para filtrar con parámetros opcionales
    @Query("SELECT t FROM Ticket t WHERE " +
           "(:status is null OR t.status = :status) AND " +
           "(:priority is null OR t.priority = :priority)")
    List<Ticket> findByFilters(@Param("status") TicketStatus status, 
                              @Param("priority") TicketPriority priority);
} 