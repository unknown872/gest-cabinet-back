package gest_cabinet_back.service.impl;

import gest_cabinet_back.dto.UtilisateurRequestDTO;
import gest_cabinet_back.dto.UtilisateurResponseDTO;
import gest_cabinet_back.model.Administrateur;
import gest_cabinet_back.model.Medecin;
import gest_cabinet_back.model.Patient;
import gest_cabinet_back.model.Secretaire;
import gest_cabinet_back.model.Utilisateur;
import gest_cabinet_back.repository.UtilisateurRepository;
import gest_cabinet_back.service.UtilisateurService;
import gest_cabinet_back.exception.RessourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurServiceImpl(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilisateurResponseDTO> listerTous() {
        return utilisateurRepository.findAll().stream()
                .map(this::versDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurResponseDTO consulterParId(Long id) {
        return versDto(trouverOuLeverException(id));
    }


    @Override
    @Transactional
    public UtilisateurResponseDTO creer(UtilisateurRequestDTO dto) {

        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException(
                    "Un compte existe déjà avec cet email."
            );
        }

        Utilisateur utilisateur = switch (dto.getRole()) {
            case ADMINISTRATEUR -> new Administrateur();

            case MEDECIN -> {
                Medecin medecin = new Medecin();
                medecin.setSignatureNumerique(dto.getSignatureNumerique());
                yield medecin;
            }

            case SECRETAIRE -> new Secretaire();

            case PATIENT -> new Patient();
        };

        // Informations communes
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setMotDePasse(
                passwordEncoder.encode(dto.getMotDePasse())
        );
        utilisateur.setTelephone(dto.getTelephone());
        utilisateur.setRole(dto.getRole());
        utilisateur.setActif(true);

        utilisateur.setGenre(dto.getGenre());
        utilisateur.setAdresse(dto.getAdresse());
        utilisateur.setDateNaissance(dto.getDateNaissance());

        // Première sauvegarde pour récupérer l'identifiant
        utilisateur = utilisateurRepository.saveAndFlush(utilisateur);

        // Génération du code général
        utilisateur.setCode(
                String.format("USER-%06d", utilisateur.getId())
        );

        // Génération du code spécifique
        switch (utilisateur) {
            case Medecin medecin ->
                    medecin.setCodeMedecin(String.format("MED-%06d", utilisateur.getId()));

            case Secretaire secretaire ->
                    secretaire.setMatricule(String.format("SEC-%06d", utilisateur.getId()));

            case Patient patient ->
                    patient.setCodePatient(String.format("PAT-%06d", utilisateur.getId()));

            case Administrateur administrateur ->
                    administrateur.setCodeAdministrateur(String.format("ADM-%06d", utilisateur.getId()));

            default -> throw new IllegalStateException("Type utilisateur inconnu");
        }

        // Sauvegarde des modifications
        utilisateur = utilisateurRepository.save(utilisateur);

        return versDto(utilisateur);
    }


    @Override
    public UtilisateurResponseDTO modifier(Long id, UtilisateurRequestDTO dto) {
        Utilisateur utilisateur = trouverOuLeverException(id);

        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setTelephone(dto.getTelephone());

        return versDto(utilisateurRepository.save(utilisateur));
    }

    @Override
    public void desactiver(Long id) {
        Utilisateur utilisateur = trouverOuLeverException(id);
        utilisateur.setActif(false);
        utilisateurRepository.save(utilisateur);
    }

    private Utilisateur trouverOuLeverException(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur introuvable : " + id));
    }

    private UtilisateurResponseDTO versDto(Utilisateur u) {
        return new UtilisateurResponseDTO(
                u.getId(), u.getCode(), u.getNom(), u.getPrenom(), u.getEmail(),
                u.getTelephone(), u.isActif(), u.getRole()
        );
    }

}
