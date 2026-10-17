package gest_cabinet_back.controller;

import gest_cabinet_back.model.Specialite;
import gest_cabinet_back.service.SpecialiteService;
import java.util.*;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/specialites")
@RequiredArgsConstructor
public class SpecialiteController {

    private SpecialiteService specialiteService;

    @GetMapping
    public List<Specialite> findAll() {
        return specialiteService.findAll();
    }

    @GetMapping("/{id}")
    public Specialite findById(@PathVariable Long id) {
        return specialiteService.findById(id);
    }

    @PostMapping
    public Specialite save(@RequestBody Specialite specialite) {
        return specialiteService.save(specialite);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return specialiteService.delete(id);
    }

    @PutMapping("/{id}")
    public Specialite updateSpecialite(@PathVariable Long id, @RequestBody Specialite specialite) {
        return specialiteService.updateSpecialite(id, specialite);
    }

}
