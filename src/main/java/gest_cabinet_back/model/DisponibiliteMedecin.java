package gest_cabinet_back.model;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
public class DisponibiliteMedecin {
    private Long id;

    private LocalDate dateJour;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Integer dureeCreneauMinutes;

    public DisponibiliteMedecin(Long id, LocalDate dateJour, LocalTime heureDebut, LocalTime heureFin, Integer dureeCreneauMinutes) {
        this.id = id;
        this.dateJour = dateJour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.dureeCreneauMinutes = dureeCreneauMinutes;
    }
}
