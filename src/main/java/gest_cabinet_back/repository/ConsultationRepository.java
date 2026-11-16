package gest_cabinet_back.repository;

import gest_cabinet_back.model.Consultation;
import gest_cabinet_back.model.StatutConsultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    Optional<Consultation> findByRendezVousId(Long rendezVousId);

    boolean existsByRendezVousId(Long rendezVousId);

    List<Consultation> findByRendezVousMedecinIdOrderByDateHeureDesc(Long medecinId);

    List<Consultation> findByRendezVousMedecinIdAndStatutOrderByDateHeureDesc(Long medecinId, StatutConsultation statut);

    Optional<Consultation> findByIdAndRendezVousMedecinId(Long id, Long medecinId);

    List<Consultation> findByDossierMedicalPatientIdOrderByDateHeureDesc(Long patientId);

    Optional<Consultation> findByIdAndDossierMedicalPatientId(Long id, Long patientId);
}
