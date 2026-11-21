package gest_cabinet_back.service;

import gest_cabinet_back.repository.SpecialiteRepository;
import gest_cabinet_back.model.Specialite;
import gest_cabinet_back.exception.RessourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SpecialiteService {

    private final SpecialiteRepository specialiteRepository;

    public List<Specialite> findAll() {
        return specialiteRepository.findAll();
    }

    public Specialite findById(Long id){
        return specialiteRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Spécialité introuvable : " + id));
    }

    public Specialite save(Specialite specialite){
        return specialiteRepository.save(specialite);
    }

    public boolean delete(Long id){
        findById(id);
        specialiteRepository.deleteById(id);
        return true;
    }

    public Specialite updateSpecialite(Long id, Specialite specialite){
        Specialite specialiteToUpdate = findById(id);
        specialiteToUpdate.setLibelle(specialite.getLibelle());
        specialiteToUpdate.setDescription(specialite.getDescription());
        return save(specialiteToUpdate);
    }

}
