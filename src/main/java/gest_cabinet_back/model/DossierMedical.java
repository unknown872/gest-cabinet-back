package gest_cabinet_back.model;

import jakarta.persistence.*;
import java.util.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "dossier_medical")
@Getter
@Setter
@NoArgsConstructor
public class DossierMedical {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String numeroDossier;
 
    private LocalDateTime dateCreation;
 
    private String groupeSanguin;

    @Column(columnDefinition = "TEXT")
    private String antecedents;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(columnDefinition = "TEXT")
    private String traitementsEnCours;

    @OneToOne
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private Patient patient;

    @OneToMany(mappedBy = "dossierMedical", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Consultation> consultations = new ArrayList<>();

    @OneToMany(mappedBy = "dossierMedical", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentMedical> documents = new ArrayList<>();

}
