package gest_cabinet_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DisponibiliteResponseDTO {
    private Long id;
    private Long medecinId;
    private String medecinNomComplet;
    private LocalDate dateJour;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Integer dureeCreneauMinutes;
}
