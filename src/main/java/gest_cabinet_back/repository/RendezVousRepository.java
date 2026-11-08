package gest_cabinet_back.repository;

import gest_cabinet_back.model.RendezVous;
import gest_cabinet_back.model.StatutRdv;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
