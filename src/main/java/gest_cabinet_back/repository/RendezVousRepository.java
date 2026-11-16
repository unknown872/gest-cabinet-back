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
    boolean existsByPatientIdAndMedecinId(Long patientId, Long medecinId);

    // --- REGLES RG05 & RG07 ---
    // RG05 : Compter les RDV futurs actifs (non annulés, non passés) du patient
    long countByPatientIdAndDateHeureAfterAndStatutIn(
            Long patientId, LocalDateTime now, List<StatutRdv> statuts);

    // RG07 : Compter les absences non justifiées du patient sur les 12 derniers mois
    long countByPatientIdAndStatutAndDateHeureBetween(
            Long patientId, StatutRdv statut, LocalDateTime debut, LocalDateTime fin);

    // Recherche par médecin et plage de date (planning/journée)
    List<RendezVous> findByMedecinIdAndDateHeureBetweenOrderByDateHeureAsc(
            Long medecinId, LocalDateTime debut, LocalDateTime fin);

    // Recherche de tous les RDV sur une plage (secrétaire - vue journalière)
    List<RendezVous> findByDateHeureBetweenOrderByDateHeureAsc(
            LocalDateTime debut, LocalDateTime fin);

    // --- CALCUL DES CRÉNEAUX DISPONIBLES ---
    // Toutes les RDV d'un médecin sur une journée donnée (bornes incluses/exclues
    // gérées par le Service, qui passe le début et la fin exacts de la journée).
    List<RendezVous> findByMedecinIdAndDateHeureBetween(
            Long medecinId, LocalDateTime debut, LocalDateTime fin);
}