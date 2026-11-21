package gest_cabinet_back.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "prescription")
@Getter
@Setter
@NoArgsConstructor
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutPrescription statut = StatutPrescription.BROUILLON;

    private LocalDateTime dateEmission;
    private LocalDate dateExpiration;

    @Column(columnDefinition = "TEXT", name = "instructions_generales")
    private String instructionsGenerales;

    private String pdfUrl;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LignePrescription> lignes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;

    public void addLigne(LignePrescription ligne) {
        if (ligne != null) {
            lignes.add(ligne);
            ligne.setPrescription(this);
        }
    }

    public void removeLigne(LignePrescription ligne) {
        if (ligne != null) {
            lignes.remove(ligne);
            ligne.setPrescription(null);
        }
    }
}
