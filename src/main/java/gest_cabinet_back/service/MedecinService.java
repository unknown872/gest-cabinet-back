package gest_cabinet_back.service;

import gest_cabinet_back.model.Medecin;
import gest_cabinet_back.repository.MedecinRepository;
import gest_cabinet_back.exception.RessourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MedecinService {

    private final MedecinRepository medecinRepository;

    public List<Medecin> findAll() {
        return medecinRepository.findAll();
    }

    public Medecin findById(Long id) {
        return medecinRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Médecin introuvable : " + id));
    }

    public Medecin save(Medecin medecin) {
        return medecinRepository.save(medecin);
    }

    public boolean delete(Long id) {
        findById(id);
        medecinRepository.deleteById(id);
        return true;
    }

    public Medecin updateMedecin(Long id, Medecin medecin){
        Medecin medecinToUpdate = findById(id);
        medecinToUpdate.setNom(medecin.getNom());
        medecinToUpdate.setPrenom(medecin.getPrenom());
        return save(medecinToUpdate);
    }

}
