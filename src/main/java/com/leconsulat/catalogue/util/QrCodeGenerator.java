package com.leconsulat.catalogue.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Recommandations et corrections.md §5/7 : un QR code unique par produit. Le contenu encode
 * directement l'identifiant du produit ({@code LECONSULAT:PRODUIT:<id>}) — pas de colonne dédiée
 * en base, pas d'endpoint de résolution : le frontend décode le texte et rappelle simplement
 * {@code GET /produits/{id}}, qui applique déjà le contrôle de périmètre habituel.
 */
public final class QrCodeGenerator {

    private static final String PREFIXE = "LECONSULAT:PRODUIT:";
    private static final Pattern ID_PATTERN = Pattern.compile("(\\d+)\\s*$");

    private QrCodeGenerator() {
    }

    public static String contenuPourProduit(Long produitId) {
        return PREFIXE + produitId;
    }

    /** Tolérant au contenu scanné : accepte le préfixe exact ou, si l'utilisateur a scanné le QR
     * avec une autre application, le premier nombre trouvé dans le texte. */
    public static Long extraireIdProduit(String contenuScanne) {
        if (contenuScanne == null) {
            return null;
        }
        Matcher m = ID_PATTERN.matcher(contenuScanne.trim());
        return m.find() ? Long.valueOf(m.group(1)) : null;
    }

    public static byte[] genererPng(String contenu, int taillePx) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = new QRCodeWriter().encode(contenu, BarcodeFormat.QR_CODE, taillePx, taillePx, hints);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Erreur lors de la génération du QR code : " + e.getMessage(), e);
        }
    }
}
