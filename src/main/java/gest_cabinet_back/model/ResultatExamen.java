package gest_cabinet_back.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResultatExamen {
    private Long id;

    private LocalDateTime dateResultat;
    private String compteRendu;
    private String fichierJointUrl;
    private String valeursAnalyse;

    public ResultatExamen(Long id, LocalDateTime dateResultat, String compteRendu, String fichierJointUrl, String valeursAnalyse) {
        this.id = id;
        this.dateResultat = dateResultat;
        this.compteRendu = compteRendu;
        this.fichierJointUrl = fichierJointUrl;
        this.valeursAnalyse = valeursAnalyse;
    }
    
}
