package com.leconsulat.vente.util;

import com.leconsulat.parametres.entity.FormatTicket;
import com.leconsulat.parametres.entity.ParametresGeneraux;
import com.leconsulat.vente.entity.Commande;
import com.leconsulat.vente.entity.Facture;
import com.leconsulat.vente.entity.LigneCommande;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Ticket de caisse au gabarit du cahier des charges (§9.1) — page étroite façon imprimante
 * thermique, volontairement séparé de {@link com.leconsulat.common.util.PdfGenerator} qui
 * produit des rapports A4. L'en-tête et le pied de page viennent des Paramètres (§6.10.1, RG-103
 * — jamais codés en dur, Lot 6a).
 */
public final class TicketGenerator {

    private static final float LARGEUR_58MM = 164f; // ~58mm en points (1mm ≈ 2.83pt)
    private static final float LARGEUR_80MM = 227f;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private TicketGenerator() {
    }

    public static byte[] genererPdf(Facture facture, ParametresGeneraux parametres) {
        Commande commande = facture.getCommande();
        boolean duplicata = facture.getNombreImpressions() > 0;
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

            int copies = Math.max(1, parametres.getNombreCopies());
            for (int i = 0; i < copies; i++) {
                if (i > 0) {
                    document.newPage();
                }
                imprimerEnTete(document, facture.getEtablissement().getNom(), parametres, titre, normal, petit);
                document.add(separateur(petit));

                if (duplicata) {
                    ajouterCentre(document, "*** DUPLICATA ***", gras);
                }
                ajouterCentre(document, "REÇU DE CAISSE", gras);
                document.add(separateur(petit));

                document.add(new Paragraph("N° : " + facture.getNumero(), normal));
                document.add(new Paragraph("Date : " + facture.getDateEmission().format(DATE_FORMAT), normal));
                if (commande.getTable() != null) {
                    document.add(new Paragraph("Table : " + commande.getTable().getNumero(), normal));
                }
                if (commande.getClientNom() != null && !commande.getClientNom().isBlank()) {
                    document.add(new Paragraph("Client : " + commande.getClientNom(), normal));
                }
                document.add(new Paragraph("Caissier : " + commande.getCaissier().getNom(), normal));
                document.add(separateur(petit));

                for (LigneCommande ligne : commande.getLignes()) {
                    String desc = ligne.getQuantite() > 1
                            ? "%d x %s".formatted(ligne.getQuantite(), ligne.getArticleNom())
                            : ligne.getArticleNom();
                    document.add(ligneMontant(desc, formaterFcfa(ligne.getMontant()), normal));
                }
                document.add(separateur(petit));

                document.add(ligneMontant("Total brut", formaterFcfa(facture.getMontantBrut()), normal));
                if (facture.getRemise() != null && facture.getRemise().signum() > 0) {
                    document.add(ligneMontant("Remise", "-" + formaterFcfa(facture.getRemise()), normal));
                }
                document.add(ligneMontant("Total net", formaterFcfa(facture.getMontantNet()), gras));
                if (facture.getMontantRecu() != null) {
                    document.add(ligneMontant(facture.getMode().name(), formaterFcfa(facture.getMontantRecu()), normal));
                    document.add(ligneMontant("Monnaie rendue", formaterFcfa(facture.getMonnaieRendue()), normal));
                }
                document.add(separateur(petit));

                document.add(new Paragraph("Type de paiement : " + facture.getMode().name(), normal));
                document.add(new Paragraph("Établissement : " + facture.getEtablissement().getNom(), normal));
                document.add(separateur(petit));

                ajouterCentre(document, parametres.getMessageFin() != null && !parametres.getMessageFin().isBlank()
                        ? parametres.getMessageFin() : "Merci de votre visite !", normal);
            }

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur lors de la génération du ticket : " + e.getMessage(), e);
        }
        return out.toByteArray();
    }

    /** RG-103 : nom de l'établissement toujours affiché (identifie précisément la vente),
     * adresse/téléphone/e-mail de la structure ajoutés seulement s'ils sont renseignés. */
    private static void imprimerEnTete(Document document, String nomEtablissement, ParametresGeneraux parametres,
                                        Font titre, Font normal, Font petit) throws DocumentException {
        ajouterCentre(document, nomEtablissement.toUpperCase(), titre);
        if (parametres.getAdresse() != null && !parametres.getAdresse().isBlank()) {
            ajouterCentre(document, parametres.getAdresse(), petit);
        }
        if (parametres.getTelephone() != null && !parametres.getTelephone().isBlank()) {
            ajouterCentre(document, "Tél : " + parametres.getTelephone(), petit);
        }
        if (parametres.getEmail() != null && !parametres.getEmail().isBlank()) {
            ajouterCentre(document, parametres.getEmail(), petit);
        }
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
        // Pas de tableau ici (page trop étroite pour PdfPTable + bordures) : on simule
        // l'alignement à droite avec des points de suite, lisible sur 80mm comme sur 58mm.
        int largeurCible = 32;
        int pointsSuite = Math.max(1, largeurCible - libelle.length() - montant.length());
        String ligne = libelle + " ".repeat(1) + ".".repeat(pointsSuite) + " " + montant;
        return new Paragraph(ligne, font);
    }

    private static String formaterFcfa(java.math.BigDecimal montant) {
        if (montant == null) {
            return "0";
        }
        return "%,.0f".formatted(montant).replace(',', ' ');
    }
}
