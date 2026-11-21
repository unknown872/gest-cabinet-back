package gest_cabinet_back.controller;

import gest_cabinet_back.model.Specialite;
import gest_cabinet_back.service.SpecialiteService;
import java.util.*;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/specialites")
@RequiredArgsConstructor
public class SpecialiteController {

    private final SpecialiteService specialiteService;

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','MEDECIN','SECRETAIRE', 'PATIENT')")
    @GetMapping
    public List<Specialite> findAll() {
        return specialiteService.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','MEDECIN','SECRETAIRE', 'PATIENT')")
    @GetMapping("/{id}")
    public Specialite findById(@PathVariable Long id) {
        return specialiteService.findById(id);
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Specialite save(@RequestBody Specialite specialite) {
        return specialiteService.save(specialite);
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        specialiteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @PutMapping("/{id}")
    public Specialite updateSpecialite(@PathVariable Long id, @RequestBody Specialite specialite) {
        return specialiteService.updateSpecialite(id, specialite);
    }

}
