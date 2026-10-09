package gest_cabinet_back.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StatutConsultation {
    private Long id;

    private LocalDateTime dateHeure;
    private String motif;
    private String observations;
    private String diagnostic;
    private Float tension;
    private Float poids;
    private Float temperature;
    private StatutConsultation statut;

    public StatutConsultation(Long id, LocalDateTime dateHeure, String motif, String observations, String diagnostic, Float tension, Float poids, Float temperature, StatutConsultation statut) {
        this.id = id;
        this.dateHeure = dateHeure;
        this.motif = motif;
        this.observations = observations;
        this.diagnostic = diagnostic;
        this.tension = tension;
        this.poids = poids;
        this.temperature = temperature;
        this.statut = statut;
    }
}
