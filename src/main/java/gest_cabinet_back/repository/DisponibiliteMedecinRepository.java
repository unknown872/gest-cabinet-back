package gest_cabinet_back.repository;

import gest_cabinet_back.model.DisponibiliteMedecin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DisponibiliteMedecinRepository extends JpaRepository<DisponibiliteMedecin, Long> {

    List<DisponibiliteMedecin> findByMedecinIdOrderByDateJourAscHeureDebutAsc(Long medecinId);

    List<DisponibiliteMedecin> findByMedecinIdAndDateJourOrderByHeureDebutAsc(Long medecinId, LocalDate dateJour);

}
