package gest_cabinet_back.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reglement")
@Getter
@Setter
@NoArgsConstructor
public class Reglement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateReglement;

    @Column(nullable = false)
    private double montant;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement", nullable = false)
    private ModePaiement modePaiement;

    private String reference;

    @ManyToOne
    @JoinColumn(name = "facture_id", nullable = false)
    private Facture facture;
    
}
