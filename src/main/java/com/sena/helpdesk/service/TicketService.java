package com.sena.helpdesk.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sena.helpdesk.model.Ticket;
import com.sena.helpdesk.model.User;
import com.sena.helpdesk.repository.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    public Ticket createTicket(Ticket ticket) {
        ticket.setStatus(Ticket.TicketStatus.OPEN);
        return ticketRepository.save(ticket);
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public List<Ticket> getTicketsByUser(User user) {
        return ticketRepository.findByCreatedBy(user);
    }

    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
    }

    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }
} 