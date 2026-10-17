package gest_cabinet_back.controller;

import gest_cabinet_back.model.Medecin;
import gest_cabinet_back.service.MedecinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/medecins")
@RequiredArgsConstructor
public class MedecinController {

    private MedecinService medecinService;

    @GetMapping
    public List<Medecin> findAll() {
        return medecinService.findAll();
    }

    @PostMapping
    public Medecin save(@RequestBody Medecin medecin) {
        return medecinService.save(medecin);
    }

    @DeleteMapping
    public boolean delete(@PathVariable Long id) {
        return medecinService.delete(id);
    }

}
