package gest_cabinet_back.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "disponibilite_medecin")
@Getter 
@Setter
@NoArgsConstructor
public class DisponibiliteMedecin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateJour;

    private LocalTime heureDebut;

    private LocalTime heureFin;

    private Integer dureeCreneauMinutes;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "medecin_id", nullable = false)
    private Medecin medecin;

}
