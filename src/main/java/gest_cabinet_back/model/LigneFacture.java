package gest_cabinet_back.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ligne_facture")
@Getter
@Setter
@NoArgsConstructor
public class LigneFacture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String designation;

    private Integer quantite;
    private Double prixUnitaire;
    private Double totalLigne;

    @ManyToOne
    @JoinColumn(name = "facture_id", nullable = false)
    private Facture facture;

    @ManyToOne
    @JoinColumn(name = "acte_medical_id")
    private ActeMedical acteMedical;

}
