package com.sena.helpdesk.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/.well-known")
public class WellKnownController {

    @GetMapping("/appspecific/**")
    public ResponseEntity<Void> handleChromeDevTools() {
        // Retorna 204 No Content en lugar de 404
        return ResponseEntity.noContent().build();
    }
} 