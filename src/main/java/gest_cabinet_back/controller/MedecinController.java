package gest_cabinet_back.controller;

import gest_cabinet_back.model.Medecin;
import gest_cabinet_back.service.MedecinService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/medecins")
@RequiredArgsConstructor
public class MedecinController {

    private final MedecinService medecinService;

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','MEDECIN','SECRETAIRE','PATIENT')")
    @Operation(summary = "Find all medecins")
    @GetMapping
    public List<Medecin> findAll() {
        return medecinService.findAll();
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @Operation(summary = "Save a new medecin")
    @PostMapping
    public Medecin save(@RequestBody Medecin medecin) {
        return medecinService.save(medecin);
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @Operation(summary = "Delete a medecin by id")
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return medecinService.delete(id);
    }

}
