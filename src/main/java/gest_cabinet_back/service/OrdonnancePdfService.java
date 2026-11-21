package gest_cabinet_back.service;

import gest_cabinet_back.model.LignePrescription;
import gest_cabinet_back.model.Medecin;
import gest_cabinet_back.model.Patient;
import gest_cabinet_back.model.Prescription;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrdonnancePdfService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Génère un document PDF 1.4 conforme pour l'ordonnance médicale.
     */
    public byte[] genererPdf(Prescription prescription) {
        StringBuilder stream = new StringBuilder();

        Medecin medecin = prescription.getConsultation().getRendezVous().getMedecin();
        Patient patient = prescription.getConsultation().getDossierMedical().getPatient();

        String dateEmissionStr = prescription.getDateEmission() != null
                ? prescription.getDateEmission().format(DATE_TIME_FORMATTER)
                : "Non validée";
        String dateExpStr = prescription.getDateExpiration() != null
                ? prescription.getDateExpiration().format(DATE_FORMATTER)
                : "Non renseignée";

        // Dessin des bordures et séparateurs graphiques
        stream.append("0.2 w\n");
        stream.append("0.1 0.4 0.7 RG\n"); // Bleu médical
        stream.append("40 795 515 2 re f\n"); // Ligne sous en-tête

        // En-tête Cabinet Médical
        stream.append("BT\n");
        stream.append("/F2 18 Tf\n");
        stream.append("0.1 0.3 0.6 rg\n");
        stream.append("40 810 Td (CABINET MEDICAL FORCE-N) Tj\n");
        stream.append("/F1 9 Tf\n");
        stream.append("0.3 0.3 0.3 rg\n");
        stream.append("0 -15 Td (Clinique Medicale & Soins Specialises | Contact: +221 33 000 00 00) Tj\n");
        stream.append("ET\n");

        // Titre de l'ordonnance
        stream.append("BT\n");
        stream.append("/F2 16 Tf\n");
        stream.append("0 0 0 rg\n");
        stream.append("210 765 Td (ORDONNANCE MEDICALE) Tj\n");
        stream.append("/F1 10 Tf\n");
        stream.append("0 -16 Td (Prescription N. ").append(prescription.getId()).append(") Tj\n");
        stream.append("ET\n");

        // Bloc Médecin (Gauche)
        stream.append("BT\n");
        stream.append("/F2 11 Tf\n");
        stream.append("40 720 Td (MEDECIN PRESCRIPTEUR) Tj\n");
        stream.append("/F1 10 Tf\n");
        stream.append("0 -14 Td (Dr. ")
                .append(escapePdf(medecin.getPrenom())).append(" ")
                .append(escapePdf(medecin.getNom())).append(") Tj\n");
        String specialite = (medecin.getSpecialites() != null && !medecin.getSpecialites().isEmpty())
                ? medecin.getSpecialites().get(0).getLibelle()
                : "Medecine Generale";
        stream.append("0 -13 Td (Specialite : ").append(escapePdf(specialite)).append(") Tj\n");
        stream.append("0 -13 Td (Email : ").append(escapePdf(medecin.getEmail())).append(") Tj\n");
        stream.append("ET\n");

        // Bloc Patient (Droite)
        stream.append("BT\n");
        stream.append("/F2 11 Tf\n");
        stream.append("320 720 Td (PATIENT) Tj\n");
        stream.append("/F1 10 Tf\n");
        stream.append("0 -14 Td (Nom : ").append(escapePdf(patient.getNom())).append(" ")
                .append(escapePdf(patient.getPrenom())).append(") Tj\n");
        String numDossier = prescription.getConsultation().getDossierMedical().getNumeroDossier();
        stream.append("0 -13 Td (Dossier N. : ").append(escapePdf(numDossier)).append(") Tj\n");
        stream.append("0 -13 Td (Date d'emission : ").append(dateEmissionStr).append(") Tj\n");
        stream.append("0 -13 Td (Date d'expiration : ").append(dateExpStr).append(") Tj\n");
        stream.append("ET\n");

        // Séparateur
        stream.append("0.5 w\n");
        stream.append("0.7 0.7 0.7 RG\n");
        stream.append("40 655 m 555 655 l S\n");

        // Titre des prescriptions
        stream.append("BT\n");
        stream.append("/F2 12 Tf\n");
        stream.append("0.1 0.3 0.6 rg\n");
        stream.append("40 635 Td (PRESCRIPTIONS MEDICAMENTEUSES) Tj\n");
        stream.append("ET\n");

        // Liste des lignes de prescription
        int y = 610;
        int index = 1;
        for (LignePrescription ligne : prescription.getLignes()) {
            if (y < 160) {
                break; // Limite simple pour 1 page
            }
            stream.append("BT\n");
            stream.append("/F2 11 Tf\n");
            stream.append("0 0 0 rg\n");
            stream.append("50 ").append(y).append(" Td (").append(index).append(". ")
                    .append(escapePdf(ligne.getMedicament())).append(") Tj\n");

            stream.append("/F1 10 Tf\n");
            stream.append("0.2 0.2 0.2 rg\n");

            String posologie = (ligne.getPosologie() != null && !ligne.getPosologie().isBlank())
                    ? ligne.getPosologie() : "-";
            String duree = (ligne.getDureeTraitement() != null && !ligne.getDureeTraitement().isBlank())
                    ? ligne.getDureeTraitement() : "-";

            stream.append("15 -14 Td (Posologie : ").append(escapePdf(posologie))
                    .append(" | Duree : ").append(escapePdf(duree)).append(") Tj\n");

            if (ligne.getInstructions() != null && !ligne.getInstructions().isBlank()) {
                stream.append("0 -13 Td (Instructions : ").append(escapePdf(ligne.getInstructions())).append(") Tj\n");
                y -= 45;
            } else {
                y -= 32;
            }
            stream.append("ET\n");
            index++;
        }

        // Instructions générales
        if (prescription.getInstructionsGenerales() != null && !prescription.getInstructionsGenerales().isBlank()) {
            stream.append("BT\n");
            stream.append("/F2 11 Tf\n");
            stream.append("0.1 0.3 0.6 rg\n");
            stream.append("40 ").append(y - 10).append(" Td (RECOMMANDATIONS GENERALES) Tj\n");
            stream.append("/F1 10 Tf\n");
            stream.append("0.2 0.2 0.2 rg\n");
            stream.append("0 -15 Td (").append(escapePdf(prescription.getInstructionsGenerales())).append(") Tj\n");
            stream.append("ET\n");
            y -= 45;
        }

        // Pied de page & Signature
        stream.append("0.5 w\n");
        stream.append("0.7 0.7 0.7 RG\n");
        stream.append("40 100 m 555 100 l S\n");

        stream.append("BT\n");
        stream.append("/F2 10 Tf\n");
        stream.append("0 0 0 rg\n");
        stream.append("380 80 Td (Signature et Cachet du Medecin) Tj\n");
        stream.append("/F1 9 Tf\n");
        stream.append("0 -14 Td (Dr. ").append(escapePdf(medecin.getPrenom())).append(" ")
                .append(escapePdf(medecin.getNom())).append(") Tj\n");

        stream.append("/F1 8 Tf\n");
        stream.append("0.5 0.5 0.5 rg\n");
        stream.append("40 30 Td (Document medical officiel genere par Gest-Cabinet Force-N - Ne pas jeter sur la voie publique.) Tj\n");
        stream.append("ET\n");

        return buildPdfDocument(stream.toString());
    }

    private String escapePdf(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("(", "\\(")
                   .replace(")", "\\)")
                   .replaceAll("[\\r\\n]+", " ");
    }

    private byte[] buildPdfDocument(String contentStream) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            List<Integer> offsets = new ArrayList<>();

            out.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));

            // Objet 1 : Catalog
            offsets.add(out.size());
            out.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Objet 2 : Pages
            offsets.add(out.size());
            out.write("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Objet 3 : Page (format A4 : 595 x 842 points)
            offsets.add(out.size());
            out.write("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> /Contents 6 0 R >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Objet 4 : Police normale Helvetica
            offsets.add(out.size());
            out.write("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Objet 5 : Police grasse Helvetica-Bold
            offsets.add(out.size());
            out.write("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Objet 6 : Stream de contenu
            byte[] streamBytes = contentStream.getBytes(StandardCharsets.ISO_8859_1);
            offsets.add(out.size());
            out.write(("6 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n").getBytes(StandardCharsets.US_ASCII));
            out.write(streamBytes);
            out.write("\nendstream\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Table XREF
            int xrefOffset = out.size();
            out.write("xref\n".getBytes(StandardCharsets.US_ASCII));
            out.write(("0 " + (offsets.size() + 1) + "\n").getBytes(StandardCharsets.US_ASCII));
            out.write("0000000000 65535 f \n".getBytes(StandardCharsets.US_ASCII));
            for (int offset : offsets) {
                out.write(String.format("%010d 00000 n \n", offset).getBytes(StandardCharsets.US_ASCII));
            }

            // Trailer
            out.write("trailer\n".getBytes(StandardCharsets.US_ASCII));
            out.write(("<< /Size " + (offsets.size() + 1) + " /Root 1 0 R >>\n").getBytes(StandardCharsets.US_ASCII));
            out.write("startxref\n".getBytes(StandardCharsets.US_ASCII));
            out.write((xrefOffset + "\n%%EOF\n").getBytes(StandardCharsets.US_ASCII));

            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la construction du document PDF", e);
        }
    }
}
