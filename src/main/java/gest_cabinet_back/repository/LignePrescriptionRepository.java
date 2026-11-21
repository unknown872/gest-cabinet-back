package gest_cabinet_back.repository;

import gest_cabinet_back.model.LignePrescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LignePrescriptionRepository extends JpaRepository<LignePrescription, Long> {
    List<LignePrescription> findByPrescriptionId(Long prescriptionId);
}
