package gest_cabinet_back.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "resultat_examen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultatExamen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateResultat;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String compteRendu;

    private String fichierJointUrl;

    @Column(nullable = false)
    private String valeursAnalyse;
    
}
