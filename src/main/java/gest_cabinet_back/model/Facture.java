package gest_cabinet_back.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

import lombok.*;

@Entity
@Table(name = "facture")
@Getter
@Setter
@NoArgsConstructor
public class Facture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String numeroFacture;
    private LocalDateTime dateEmission;

    @Column(nullable = false)
    private Double montantTotal;

    private Double montantRegle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutFacture statut = StatutFacture.NON_PAYEE;

    @OneToOne
    @JoinColumn(name = "consultation_id", nullable = false, unique = true)
    private Consultation consultation;

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneFacture> lignes = new ArrayList<>();

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reglement> reglements = new ArrayList<>();

}
