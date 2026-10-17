package gest_cabinet_back.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.*;

@Entity
@Table(name = "rendez_vous")
@Getter 
@Setter
@NoArgsConstructor
public class RendezVous {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateHeure;
    private String motif;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutRdv statut;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "medecin_id", nullable = false)
    private Medecin medecin;

}
