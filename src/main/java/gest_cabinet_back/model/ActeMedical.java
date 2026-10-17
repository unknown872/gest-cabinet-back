package gest_cabinet_back.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "acte_medical")
@Getter
@Setter
@NoArgsConstructor
public class ActeMedical {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String libelle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double tarifBase;

    private Boolean remboursable;


}