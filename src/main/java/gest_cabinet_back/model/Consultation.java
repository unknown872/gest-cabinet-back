package gest_cabinet_back.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "consultation")
@Getter 
@Setter 
@NoArgsConstructor 
public class Consultation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateHeure;
    private String motif;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column(columnDefinition = "TEXT")
    private String diagnostic;
    private double tension;
    private double poids;
    private double temperature;

    @Enumerated(EnumType.STRING)
    private StatutConsultation statut;

    @OneToOne
    @JoinColumn(name = "rendez_vous_id", nullable = false, unique = true)
    private RendezVous rendezVous;

    @ManyToOne
    @JoinColumn(name = "dossier_medical_id", nullable = false)
    private DossierMedical dossierMedical;


}
