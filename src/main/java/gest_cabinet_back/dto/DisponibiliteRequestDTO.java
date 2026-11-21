package gest_cabinet_back.dto;

import jakarta.validation.constraints.NotNull;
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
public class DisponibiliteRequestDTO {
    @NotNull(message = "La date est obligatoire.")
    private LocalDate dateJour;
    @NotNull(message = "L'heure de début est obligatoire.")
    private LocalTime heureDebut;
    @NotNull(message = "L'heure de fin est obligatoire.")
    private LocalTime heureFin;
    @NotNull(message = "La durée du créneau est obligatoire.")
    private Integer dureeCreneauMinutes;
}
