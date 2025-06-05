package com.sena.helpdesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sena.helpdesk.model.Comment;
import com.sena.helpdesk.model.Ticket;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTicketOrderByCreatedAtDesc(Ticket ticket);
} 