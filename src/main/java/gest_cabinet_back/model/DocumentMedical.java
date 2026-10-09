package gest_cabinet_back.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter 
@Setter
@NoArgsConstructor
public class DocumentMedical {
    
    private Long id;
    private String titre;
    private String contenu;
    private TypeDocument typeDocument;
    private LocalDateTime dateTeleversement;
    private String cheminFichier;
    private String description;

    public DocumentMedical(Long id, String titre, String contenu, TypeDocument typeDocument, LocalDateTime dateTeleversement, String cheminFichier, String description) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.typeDocument = typeDocument;
        this.dateTeleversement = dateTeleversement;
        this.cheminFichier = cheminFichier;
        this.description = description;
    }

}
