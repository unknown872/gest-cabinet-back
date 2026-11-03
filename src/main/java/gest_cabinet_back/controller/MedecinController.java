package gest_cabinet_back.controller;

import gest_cabinet_back.model.Medecin;
import gest_cabinet_back.service.MedecinService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/medecins")
@RequiredArgsConstructor
public class MedecinController {

    private MedecinService medecinService;

    @Operation(summary = "Find all medecins")
    @GetMapping
    public List<Medecin> findAll() {
        return medecinService.findAll();
    }

    @Operation(summary = "Save a new medecin")
    @PostMapping
    public Medecin save(@RequestBody Medecin medecin) {
        return medecinService.save(medecin);
    }

    @Operation(summary = "Delete a medecin by id")
    @DeleteMapping
    public boolean delete(@PathVariable Long id) {
        return medecinService.delete(id);
    }

}
