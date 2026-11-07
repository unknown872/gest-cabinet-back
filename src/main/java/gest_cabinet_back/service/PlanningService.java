package gest_cabinet_back.service;

import gest_cabinet_back.dto.DisponibiliteRequestDTO;
import gest_cabinet_back.dto.DisponibiliteResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface PlanningService {

    DisponibiliteResponseDTO definirDisponibilite(String emailMedecin, DisponibiliteRequestDTO dto);

    List<DisponibiliteResponseDTO> listerMesDisponibilites(String emailMedecin, LocalDate dateJour);

    DisponibiliteResponseDTO modifierDisponibilite(String emailMedecin, Long disponibiliteId, DisponibiliteRequestDTO dto);

    void supprimerDisponibilite(String emailMedecin, Long disponibiliteId);

}
