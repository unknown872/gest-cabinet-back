package gest_cabinet_back.service.impl;

import gest_cabinet_back.dto.DisponibiliteRequestDTO;
import gest_cabinet_back.dto.DisponibiliteResponseDTO;
import gest_cabinet_back.exception.RessourceNotFoundException;
import gest_cabinet_back.model.DisponibiliteMedecin;
import gest_cabinet_back.model.Medecin;
import gest_cabinet_back.repository.DisponibiliteMedecinRepository;
import gest_cabinet_back.repository.MedecinRepository;
import gest_cabinet_back.service.PlanningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PlanningServiceImpl implements PlanningService {

    private final DisponibiliteMedecinRepository disponibiliteRepository;
    private final MedecinRepository medecinRepository;

    @Override
    public DisponibiliteResponseDTO definirDisponibilite(String emailMedecin, DisponibiliteRequestDTO dto) {
        Medecin medecin = trouverMedecinParEmail(emailMedecin);
        validerPlageHoraire(dto, medecin.getId(), null);

        DisponibiliteMedecin dispo = new DisponibiliteMedecin();
        dispo.setMedecin(medecin);
        dispo.setDateJour(dto.getDateJour());
        dispo.setHeureDebut(dto.getHeureDebut());
        dispo.setHeureFin(dto.getHeureFin());
        dispo.setDureeCreneauMinutes(dto.getDureeCreneauMinutes());

        return versDispoDto(disponibiliteRepository.save(dispo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisponibiliteResponseDTO> listerMesDisponibilites(String emailMedecin, LocalDate dateJour) {
        Medecin medecin = trouverMedecinParEmail(emailMedecin);
        List<DisponibiliteMedecin> liste = (dateJour != null)
                ? disponibiliteRepository.findByMedecinIdAndDateJourOrderByHeureDebutAsc(medecin.getId(), dateJour)
                : disponibiliteRepository.findByMedecinIdOrderByDateJourAscHeureDebutAsc(medecin.getId());

        return liste.stream()
                .map(this::versDispoDto)
                .toList();
    }

    @Override
    public DisponibiliteResponseDTO modifierDisponibilite(
            String emailMedecin,
            Long disponibiliteId,
            DisponibiliteRequestDTO dto
    ) {
        Medecin medecin = trouverMedecinParEmail(emailMedecin);
        DisponibiliteMedecin dispo = trouverDisponibiliteDuMedecin(disponibiliteId, medecin.getId());

        validerPlageHoraire(dto, medecin.getId(), disponibiliteId);

        dispo.setDateJour(dto.getDateJour());
        dispo.setHeureDebut(dto.getHeureDebut());
        dispo.setHeureFin(dto.getHeureFin());
        dispo.setDureeCreneauMinutes(dto.getDureeCreneauMinutes());

        return versDispoDto(disponibiliteRepository.save(dispo));
    }

    @Override
    public void supprimerDisponibilite(String emailMedecin, Long disponibiliteId) {
        Medecin medecin = trouverMedecinParEmail(emailMedecin);
        DisponibiliteMedecin dispo = trouverDisponibiliteDuMedecin(disponibiliteId, medecin.getId());
        disponibiliteRepository.delete(dispo);
    }

    private Medecin trouverMedecinParEmail(String email) {
        return medecinRepository.findByEmail(email)
                .orElseThrow(() -> new RessourceNotFoundException("Aucun profil médecin trouvé pour : " + email));
    }

    private DisponibiliteMedecin trouverDisponibiliteDuMedecin(Long disponibiliteId, Long medecinId) {
        DisponibiliteMedecin dispo = disponibiliteRepository.findById(disponibiliteId)
                .orElseThrow(() -> new RessourceNotFoundException("Disponibilité introuvable : " + disponibiliteId));
        if (!dispo.getMedecin().getId().equals(medecinId)) {
            throw new IllegalArgumentException("Vous ne pouvez modifier ou supprimer que vos propres disponibilités.");
        }
        return dispo;
    }

    private void validerPlageHoraire(DisponibiliteRequestDTO dto, Long medecinId, Long excludeDispoId) {
        if (dto.getDateJour() == null || dto.getHeureDebut() == null || dto.getHeureFin() == null) {
            throw new IllegalArgumentException("La date, l'heure de début et l'heure de fin sont obligatoires.");
        }
        if (dto.getDureeCreneauMinutes() == null || dto.getDureeCreneauMinutes() <= 0) {
            throw new IllegalArgumentException("La durée d'un créneau doit être strictement supérieure à 0 minute.");
        }
        if (!dto.getHeureDebut().isBefore(dto.getHeureFin())) {
            throw new IllegalArgumentException("L'heure de début doit être strictement antérieure à l'heure de fin.");
        }

        List<DisponibiliteMedecin> existantes =
                disponibiliteRepository.findByMedecinIdAndDateJourOrderByHeureDebutAsc(medecinId, dto.getDateJour());

        for (DisponibiliteMedecin existante : existantes) {
            if (excludeDispoId != null && existante.getId().equals(excludeDispoId)) {
                continue;
            }
            boolean chevauchement = dto.getHeureDebut().isBefore(existante.getHeureFin())
                    && dto.getHeureFin().isAfter(existante.getHeureDebut());
            if (chevauchement) {
                throw new IllegalArgumentException(
                        "Cette plage horaire chevauche une disponibilité déjà définie ("
                                + existante.getHeureDebut() + " - " + existante.getHeureFin() + ")."
                );
            }
        }
    }

    private DisponibiliteResponseDTO versDispoDto(DisponibiliteMedecin d) {
        Medecin m = d.getMedecin();
        return new DisponibiliteResponseDTO(
                m.getPrenom() + " " + m.getNom(),
                d.getDateJour(),
                d.getHeureDebut(),
                d.getHeureFin(),
                d.getDureeCreneauMinutes()
        );
    }
    
}
