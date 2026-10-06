package com.leconsulat.catalogue.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Code-barres linéaire (Code128 — encode n'importe quelle chaîne alphanumérique, contrairement à
 * l'EAN-13 limité à 12-13 chiffres) : couvre aussi bien un code interne auto-généré (produit sans
 * code d'origine, ex. plat maison) qu'un code déjà imprimé sur un produit acheté (recopié tel
 * quel, y compris s'il s'agit d'un EAN-13 du fabricant — Code128 le rend très bien aussi).
 */
public final class BarcodeGenerator {

    private BarcodeGenerator() {
    }

    public static byte[] genererPng(String contenu, int largeurPx, int hauteurPx) {
        try {
            BitMatrix matrix = new Code128Writer().encode(contenu, BarcodeFormat.CODE_128, largeurPx, hauteurPx);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la génération du code-barres : " + e.getMessage(), e);
        }
    }
}
