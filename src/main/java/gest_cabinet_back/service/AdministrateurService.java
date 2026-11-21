package gest_cabinet_back.service;

import gest_cabinet_back.model.Administrateur;
import gest_cabinet_back.repository.AdministrateurRepository;
import gest_cabinet_back.exception.RessourceNotFoundException;

import java.util.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdministrateurService {

    private final AdministrateurRepository administrateurRepository;

    public List<Administrateur> findAll() {
        return administrateurRepository.findAll();
    }

    public Administrateur findById(Long id) {
        return administrateurRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Administrateur introuvable : " + id));
    }

    public Administrateur save(Administrateur administrateur) {
        return administrateurRepository.save(administrateur);
    }

    public boolean delete(Long id) {
        findById(id);
        administrateurRepository.deleteById(id);
        return true;
    }

}
