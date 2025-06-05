package com.sena.helpdesk.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sena.helpdesk.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
} 