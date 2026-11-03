package gest_cabinet_back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import gest_cabinet_back.model.Administrateur;

public interface AdministrateurRepository extends JpaRepository<Administrateur, Long> {
}
