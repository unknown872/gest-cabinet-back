package gest_cabinet_back.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Prescription {
    private Long id;

    private LocalDateTime dateEmission;
    private LocalDate dateExpiration;
    private String instructionsGenerales;
    private String pdfUrl;

    public Prescription(Long id, LocalDateTime dateEmission, LocalDate dateExpiration, String instructionsGenerales, String pdfUrl) {
        this.id = id;
        this.dateEmission = dateEmission;
        this.dateExpiration = dateExpiration;
        this.instructionsGenerales = instructionsGenerales;
        this.pdfUrl = pdfUrl;
    }
}
