package gest_cabinet_back.service.impl;

import gest_cabinet_back.dto.ProfileResponseDTO;
import gest_cabinet_back.model.Utilisateur;
import gest_cabinet_back.repository.UtilisateurRepository;
import gest_cabinet_back.service.ProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {

    private final UtilisateurRepository utilisateurRepository;

    public ProfileServiceImpl(
            UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public ProfileResponseDTO getProfile(String email) {

        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Utilisateur introuvable"));

        return new ProfileResponseDTO(
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getEmail(),
                utilisateur.getRole()
        );
    }
}
