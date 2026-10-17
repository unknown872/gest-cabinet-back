package gest_cabinet_back.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.*;

@Entity
@Table(name = "document_medical")
@Getter 
@Setter
@NoArgsConstructor
public class DocumentMedical {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_document", nullable = false)
    private TypeDocument typeDocument;

    @Column(name = "date_televersement", nullable = false)
    private LocalDateTime dateTeleversement;

    @Column(name = "chemin_fichier", nullable = false)
    private String cheminFichier;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "dossier_medical_id", nullable = false)
    private DossierMedical dossierMedical;

}
