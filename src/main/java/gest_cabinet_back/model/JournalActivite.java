package gest_cabinet_back.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JournalActivite {

    private Long id;
    private LocalDateTime dateHeure;
    private String action;
    private String adresseIp;
    private String details;

    public JournalActivite(Long id, LocalDateTime dateHeure, String action, String adresseIp, String details) {
        this.id = id;
        this.dateHeure = dateHeure;
        this.action = action;
        this.adresseIp = adresseIp;
        this.details = details;
    }

    
    
}
