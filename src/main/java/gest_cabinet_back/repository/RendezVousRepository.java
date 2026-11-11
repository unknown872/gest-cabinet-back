package gest_cabinet_back.repository;

import gest_cabinet_back.model.RendezVous;
import gest_cabinet_back.model.StatutRdv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {
    // --- PATIENT ---
    List<RendezVous> findByPatientId(Long patientId);
    Optional<RendezVous> findByIdAndPatientId(Long id, Long patientId);

    // --- MEDECIN ---
    List<RendezVous> findByMedecinId(Long medecinId);
    Optional<RendezVous> findByIdAndMedecinId(Long id, Long medecinId);
    List<RendezVous> findByMedecinIdAndStatut(Long medecinId, StatutRdv statut);
    Optional<RendezVous> findByIdAndMedecinIdAndStatut(Long id, Long medecinId, StatutRdv statut);

    // --- CALCUL DES CRÉNEAUX DISPONIBLES ---
    // Toutes les RDV d'un médecin sur une journée donnée (bornes incluses/exclues
    // gérées par le Service, qui passe le début et la fin exacts de la journée).
    List<RendezVous> findByMedecinIdAndDateHeureBetween(
            Long medecinId, LocalDateTime debut, LocalDateTime fin);
}