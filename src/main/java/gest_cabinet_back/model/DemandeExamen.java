package gest_cabinet_back.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Getter 
@Setter
@NoArgsConstructor
@Table(name = "demande_examen")
public class DemandeExamen {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String typeExamen;

    private LocalDateTime dateDemande;

    @Column(columnDefinition = "TEXT")
    private String consignes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutExamen statut = StatutExamen.EN_ATTENTE;

    

}
