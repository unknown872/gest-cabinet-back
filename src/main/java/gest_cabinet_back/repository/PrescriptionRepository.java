package gest_cabinet_back.repository;

import gest_cabinet_back.model.Prescription;
import gest_cabinet_back.model.StatutPrescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByConsultationId(Long consultationId);

    @Query("SELECT p FROM Prescription p WHERE p.consultation.dossierMedical.patient.id = :patientId ORDER BY p.dateEmission DESC")
    List<Prescription> findByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT p FROM Prescription p WHERE p.consultation.dossierMedical.patient.id = :patientId AND p.statut = :statut ORDER BY p.dateEmission DESC")
    List<Prescription> findByPatientIdAndStatut(@Param("patientId") Long patientId, @Param("statut") StatutPrescription statut);

    @Query("SELECT p FROM Prescription p WHERE p.consultation.rendezVous.medecin.id = :medecinId ORDER BY p.dateEmission DESC")
    List<Prescription> findByMedecinId(@Param("medecinId") Long medecinId);
}
