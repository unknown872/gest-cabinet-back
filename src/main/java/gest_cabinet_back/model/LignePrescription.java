package gest_cabinet_back.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ligne_prescription")
@Getter
@Setter
@NoArgsConstructor
public class LignePrescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String medicament;
    private String posologie;
    private String dureeTraitement;
    private String instructions;

    @ManyToOne
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

}
