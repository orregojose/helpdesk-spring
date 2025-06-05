package com.sena.helpdesk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sena.helpdesk.model.Ticket;
import com.sena.helpdesk.model.Comment;
import com.sena.helpdesk.model.Ticket.TicketPriority;
import com.sena.helpdesk.model.Ticket.TicketStatus;
import com.sena.helpdesk.model.User;
import com.sena.helpdesk.repository.TicketRepository;
import com.sena.helpdesk.repository.CommentRepository;
import com.sena.helpdesk.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Controlador que maneja todas las operaciones relacionadas con tickets.
 * Implementa operaciones CRUD (Crear, Leer, Actualizar, Eliminar) para tickets.
 */
@Controller
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    /**
     * Muestra la lista de todos los tickets y estadísticas en el dashboard
     * @param model Modelo para pasar datos a la vista
     * @param authentication Información del usuario autenticado
     * @param status Estado para filtrar (opcional)
     * @param priority Prioridad para filtrar (opcional)
     * @return Vista de lista de tickets
     */
    @GetMapping
    public String listTickets(Model model, Authentication authentication,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) String priority) {
        
        logger.info("Filtros recibidos - Status: {}, Priority: {}", status, priority);
        
        // Convertir los parámetros String a enums
        TicketStatus ticketStatus = null;
        TicketPriority ticketPriority = null;
        
        try {
            if (status != null && !status.isEmpty()) {
                ticketStatus = TicketStatus.valueOf(status);
            }
        } catch (IllegalArgumentException e) {
            logger.error("Error al convertir status: {}", e.getMessage());
        }
        
        try {
            if (priority != null && !priority.isEmpty()) {
                ticketPriority = TicketPriority.valueOf(priority);
            }
        } catch (IllegalArgumentException e) {
            logger.error("Error al convertir priority: {}", e.getMessage());
        }
        
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Obtener tickets aplicando los filtros
        List<Ticket> tickets;
        if (ticketStatus != null && ticketPriority != null) {
            tickets = ticketRepository.findByStatusAndPriority(ticketStatus, ticketPriority);
            logger.info("Buscando por estado y prioridad");
        } else if (ticketStatus != null) {
            tickets = ticketRepository.findByStatus(ticketStatus);
            logger.info("Buscando solo por estado");
        } else if (ticketPriority != null) {
            tickets = ticketRepository.findByPriority(ticketPriority);
            logger.info("Buscando solo por prioridad");
        } else {
            tickets = ticketRepository.findAll();
            logger.info("Buscando todos los tickets");
        }
        
        logger.info("Tickets encontrados: {}", tickets.size());
        
        // Agregar datos al modelo
        model.addAttribute("tickets", tickets);
        model.addAttribute("selectedStatus", ticketStatus);
        model.addAttribute("selectedPriority", ticketPriority);
        model.addAttribute("statuses", TicketStatus.values());
        model.addAttribute("priorities", TicketPriority.values());
        
        // Agregar contadores para el dashboard
        long totalTickets = ticketRepository.count();
        long inProgressTickets = ticketRepository.countByStatus(Ticket.TicketStatus.IN_PROGRESS);
        long resolvedTickets = ticketRepository.countByStatus(Ticket.TicketStatus.RESOLVED);
        long pendingTickets = ticketRepository.countByStatus(Ticket.TicketStatus.OPEN);
        
        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("inProgressTickets", inProgressTickets);
        model.addAttribute("resolvedTickets", resolvedTickets);
        model.addAttribute("pendingTickets", pendingTickets);
        
        return "tickets/list";
    }

    /**
     * Muestra el formulario para crear un nuevo ticket
     * @param model Modelo para pasar datos a la vista
     * @return Vista del formulario de ticket
     */
    @GetMapping("/new")
    public String showTicketForm(Model model) {
        model.addAttribute("ticket", new Ticket());
        return "tickets/form";
    }

    /**
     * Procesa la creación de un nuevo ticket
     * @param ticket Datos del ticket a crear (validados)
     * @param result Resultado de la validación
     * @param authentication Información del usuario autenticado
     * @param redirectAttributes Para mensajes flash
     * @param model Modelo para pasar datos a la vista
     * @return Redirección a la lista de tickets o al formulario si hay errores
     */
    @PostMapping
    public String createTicket(@Valid @ModelAttribute Ticket ticket, BindingResult result, 
                             Authentication authentication, RedirectAttributes redirectAttributes,
                             Model model) {
        if (result.hasErrors()) {
            return "tickets/form";
        }
        
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            ticket.setCreatedBy(user);
            ticket.setStatus(Ticket.TicketStatus.OPEN);
            ticketRepository.save(ticket);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket creado exitosamente");
            return "redirect:/tickets";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear el ticket: " + e.getMessage());
            return "redirect:/tickets/new";
        }
    }

    /**
     * Muestra los detalles de un ticket específico
     * @param id ID del ticket a mostrar
     * @param model Modelo para pasar datos a la vista
     * @return Vista de detalles del ticket
     */
    @GetMapping("/{id}")
    public String viewTicket(@PathVariable Long id, Model model) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        
        // Obtener comentarios del ticket
        List<Comment> comments = commentRepository.findByTicketOrderByCreatedAtDesc(ticket);
        
        model.addAttribute("ticket", ticket);
        model.addAttribute("comments", comments);
        return "tickets/view";
    }

    /**
     * Muestra el formulario para editar un ticket existente
     * @param id ID del ticket a editar
     * @param model Modelo para pasar datos a la vista
     * @return Vista del formulario de edición
     */
    @GetMapping("/{id}/edit")
    public String editTicketForm(@PathVariable Long id, Model model) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        
        // Obtener comentarios del ticket
        List<Comment> comments = commentRepository.findByTicketOrderByCreatedAtDesc(ticket);
        
        model.addAttribute("ticket", ticket);
        model.addAttribute("comments", comments);
        return "tickets/form";
    }

    /**
     * Procesa la actualización de un ticket existente
     * @param id ID del ticket a actualizar
     * @param ticket Datos actualizados del ticket (validados)
     * @param result Resultado de la validación
     * @param redirectAttributes Para mensajes flash
     * @param model Modelo para pasar datos a la vista
     * @return Redirección a la lista de tickets o al formulario si hay errores
     */
    @PostMapping("/{id}")
    public String updateTicket(@PathVariable Long id, @Valid @ModelAttribute Ticket ticket, 
                             BindingResult result, RedirectAttributes redirectAttributes,
                             Model model) {
        if (result.hasErrors()) {
            return "tickets/form";
        }
        
        try {
            Ticket existingTicket = ticketRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
            existingTicket.setTitle(ticket.getTitle());
            existingTicket.setDescription(ticket.getDescription());
            existingTicket.setStatus(ticket.getStatus());
            existingTicket.setPriority(ticket.getPriority());
            existingTicket.setCategory(ticket.getCategory());
            ticketRepository.save(existingTicket);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket actualizado exitosamente");
            return "redirect:/tickets";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el ticket: " + e.getMessage());
            return "redirect:/tickets/" + id + "/edit";
        }
    }

    /**
     * Elimina un ticket específico
     * @param id ID del ticket a eliminar
     * @param redirectAttributes Para mensajes flash
     * @return Redirección a la lista de tickets
     */
    @DeleteMapping("/{id}")
    public String deleteTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ticketRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el ticket: " + e.getMessage());
        }
        return "redirect:/tickets";
    }

    @PostMapping("/{id}/comments")
    public String addComment(@PathVariable Long id, 
                           @RequestParam String content,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        try {
            Ticket ticket = ticketRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
            
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setTicket(ticket);
            comment.setCreatedBy(user);
            
            commentRepository.save(comment);
            redirectAttributes.addFlashAttribute("successMessage", "Comentario agregado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al agregar el comentario: " + e.getMessage());
        }
        
        return "redirect:/tickets/" + id + "/edit";
    }
} 