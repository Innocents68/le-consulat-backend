package com.leconsulat.avoir.util;

import com.leconsulat.avoir.entity.Avoir;
import com.leconsulat.avoir.entity.LigneAvoir;
import com.leconsulat.parametres.entity.FormatTicket;
import com.leconsulat.parametres.entity.ParametresGeneraux;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Même gabarit que le ticket de caisse, titre "AVOIR" et montants négatifs (RG-116) — copie
 * adaptée de {@link com.leconsulat.vente.util.TicketGenerator} plutôt qu'une factorisation
 * prématurée : les lignes d'un avoir n'ont pas la même forme que les lignes d'une commande.
 * En-tête/pied de page paramétrés (§6.10.1, RG-103, Lot 6a) comme le ticket de caisse.
 */
public final class AvoirGenerator {

    private static final float LARGEUR_58MM = 164f;
    private static final float LARGEUR_80MM = 227f;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private AvoirGenerator() {
    }

    public static byte[] genererPdf(Avoir avoir, ParametresGeneraux parametres) {
        float largeur = parametres.getFormatTicket() == FormatTicket.MM_58 ? LARGEUR_58MM : LARGEUR_80MM;
        Document document = new Document(new Rectangle(largeur, 1000f), 10, 10, 10, 10);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 8);
            Font gras = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
            Font petit = FontFactory.getFont(FontFactory.HELVETICA, 7);

            ajouterCentre(document, avoir.getEtablissement().getNom().toUpperCase(), titre);
            if (parametres.getAdresse() != null && !parametres.getAdresse().isBlank()) {
                ajouterCentre(document, parametres.getAdresse(), petit);
            }
            if (parametres.getTelephone() != null && !parametres.getTelephone().isBlank()) {
                ajouterCentre(document, "Tél : " + parametres.getTelephone(), petit);
            }
            document.add(separateur(petit));
            ajouterCentre(document, "AVOIR", gras);
            document.add(separateur(petit));

            document.add(new Paragraph("N° : " + avoir.getNumero(), normal));
            document.add(new Paragraph("Date : " + avoir.getDateCreation().format(DATE_FORMAT), normal));
            document.add(new Paragraph("Facture d'origine : " + avoir.getFacture().getNumero(), normal));
            document.add(new Paragraph("Émis par : " + avoir.getAuteur().getNom(), normal));
            document.add(separateur(petit));

            for (LigneAvoir ligne : avoir.getLignes()) {
                String desc = ligne.getQuantite() > 1
                        ? "%d x %s".formatted(ligne.getQuantite(), ligne.getArticleNom())
                        : ligne.getArticleNom();
                document.add(ligneMontant(desc, "-" + formaterFcfa(ligne.getMontant()), normal));
            }
            document.add(separateur(petit));

            document.add(ligneMontant("Total avoir", "-" + formaterFcfa(avoir.getMontant()), gras));
            document.add(separateur(petit));

            document.add(new Paragraph("Motif : " + libelleMotif(avoir), normal));
            document.add(new Paragraph("Remboursement : " + avoir.getModeRemboursement().name(), normal));
            document.add(new Paragraph("Établissement : " + avoir.getEtablissement().getNom(), normal));
            if (parametres.getMessageFin() != null && !parametres.getMessageFin().isBlank()) {
                document.add(separateur(petit));
                ajouterCentre(document, parametres.getMessageFin(), normal);
            }

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur lors de la génération de l'avoir : " + e.getMessage(), e);
        }
        return out.toByteArray();
    }

    private static String libelleMotif(Avoir avoir) {
        String base = avoir.getMotif().name();
        return avoir.getMotifDetail() != null && !avoir.getMotifDetail().isBlank()
                ? base + " — " + avoir.getMotifDetail()
                : base;
    }

    private static void ajouterCentre(Document document, String texte, Font font) throws DocumentException {
        Paragraph p = new Paragraph(texte, font);
        p.setAlignment(Element.ALIGN_CENTER);
        document.add(p);
    }

    private static Paragraph separateur(Font font) {
        Paragraph p = new Paragraph("- - - - - - - - - - - - - - - - - -", font);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingBefore(2f);
        p.setSpacingAfter(2f);
        return p;
    }

    private static Paragraph ligneMontant(String libelle, String montant, Font font) {
        int largeurCible = 32;
        int pointsSuite = Math.max(1, largeurCible - libelle.length() - montant.length());
        String ligne = libelle + " ".repeat(1) + ".".repeat(pointsSuite) + " " + montant;
        return new Paragraph(ligne, font);
    }

    private static String formaterFcfa(BigDecimal montant) {
        if (montant == null) {
            return "0";
        }
        return "%,.0f".formatted(montant).replace(',', ' ');
    }
}
