package gest_cabinet_back.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
public class RendezVous {
    private Long id;
    private LocalDateTime dateHeure;
    private String motif;
    private String notes;
    private StatutRdv statut;

    public RendezVous(Long id, LocalDateTime dateHeure, String motif, String notes, StatutRdv statut) {
        this.id = id;
        this.dateHeure = dateHeure;
        this.motif = motif;
        this.notes = notes;
        this.statut = statut;
    }

}
