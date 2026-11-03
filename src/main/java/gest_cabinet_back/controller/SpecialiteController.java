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

@RestController
@RequestMapping("/api/specialites")
@RequiredArgsConstructor
public class SpecialiteController {

    private SpecialiteService specialiteService;

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','MEDECIN','SECRETAIRE')")
    @GetMapping
    public List<Specialite> findAll() {
        return specialiteService.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','MEDECIN','SECRETAIRE')")
    @GetMapping("/{id}")
    public Specialite findById(@PathVariable Long id) {
        return specialiteService.findById(id);
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @PostMapping
    public Specialite save(@RequestBody Specialite specialite) {
        return specialiteService.save(specialite);
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return specialiteService.delete(id);
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @PutMapping("/{id}")
    public Specialite updateSpecialite(@PathVariable Long id, @RequestBody Specialite specialite) {
        return specialiteService.updateSpecialite(id, specialite);
    }

}
