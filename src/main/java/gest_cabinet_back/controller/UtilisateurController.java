package gest_cabinet_back.controller;

import gest_cabinet_back.dto.UtilisateurRequestDTO;
import gest_cabinet_back.dto.UtilisateurResponseDTO;
import gest_cabinet_back.service.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {
    private final UtilisateurService utilisateurService;

    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @GetMapping
    public List<UtilisateurResponseDTO> listerTous() {
        return utilisateurService.listerTous();
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR') or #id == authentication.principal.utilisateur.id")
    @GetMapping("/{id}")
    public UtilisateurResponseDTO consulterParId(@PathVariable Long id) {
        return utilisateurService.consulterParId(id);
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UtilisateurResponseDTO creer(@RequestBody UtilisateurRequestDTO dto) {
        return utilisateurService.creer(dto);
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR') or #id == authentication.principal.utilisateur.id")
    @PutMapping("/{id}")
    public UtilisateurResponseDTO modifier(@PathVariable Long id, @RequestBody UtilisateurRequestDTO dto) {
        return utilisateurService.modifier(id, dto);
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        utilisateurService.desactiver(id);
        return ResponseEntity.noContent().build();
    }

}
