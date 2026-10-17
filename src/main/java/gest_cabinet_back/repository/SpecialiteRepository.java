package gest_cabinet_back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import gest_cabinet_back.model.Specialite;

public interface SpecialiteRepository extends JpaRepository<Specialite, Long> {
}
