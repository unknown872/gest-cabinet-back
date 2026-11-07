package gest_cabinet_back.controller;

import gest_cabinet_back.model.Administrateur;
import gest_cabinet_back.service.AdministrateurService;

import java.util.*;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/administrateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATEUR')")
public class AdministrateurController {

    private final AdministrateurService administrateurService;

    @Operation(summary = "Find all administrateurs")
    @GetMapping
    public List<Administrateur> findAll() {
        return administrateurService.findAll();
    }

    @Operation(summary = "Find an administrateur by id")
    @GetMapping("/{id}")
    public Administrateur findById(@PathVariable Long id) {
        return administrateurService.findById(id);
    }

}
