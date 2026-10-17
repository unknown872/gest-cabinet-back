package gest_cabinet_back.service;

import gest_cabinet_back.repository.SpecialiteRepository;
import gest_cabinet_back.model.Specialite;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SpecialiteService {

    private SpecialiteRepository specialiteRepository;

    public List<Specialite> findAll() {
        return specialiteRepository.findAll();
    }

    public Specialite findById(Long id){
        return specialiteRepository.findById(id).orElse(null);
    }

    public Specialite save(Specialite specialite){
        return specialiteRepository.save(specialite);
    }

    public boolean delete(Long id){
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
