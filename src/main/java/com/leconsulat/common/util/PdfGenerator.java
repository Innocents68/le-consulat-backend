package com.leconsulat.common.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Minimal helper around OpenPDF used to produce real, downloadable PDF documents
 * (invoices, cash reports, reporting exports) — API_CONTRACT.md §3, §6, §7.
 */
public final class PdfGenerator {

    private PdfGenerator() {
    }

    public static byte[] simpleDocument(String title, List<String[]> lines, String[] tableHeaders, List<String[]> tableRows) {
        return simpleDocument("LE CONSULAT", title, lines, tableHeaders, tableRows);
    }

    /** §6.10.1 (RG-103) : le nom de la structure vient des Paramètres, jamais codé en dur. */
    public static byte[] simpleDocument(String nomMagasin, String title, List<String[]> lines, String[] tableHeaders, List<String[]> tableRows) {
        Document document = new Document(PageSize.A4, 36, 36, 54, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(114, 28, 36));
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);

            Paragraph header = new Paragraph(nomMagasin != null ? nomMagasin.toUpperCase() : "LE CONSULAT", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph subtitle = new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13));
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(16f);
            document.add(subtitle);

            if (lines != null) {
                for (String[] line : lines) {
                    Paragraph p = new Paragraph(line[0] + (line.length > 1 ? " : " + line[1] : ""), normalFont);
                    document.add(p);
                }
                document.add(Chunk.NEWLINE);
            }

            if (tableHeaders != null && tableHeaders.length > 0) {
                PdfPTable table = new PdfPTable(tableHeaders.length);
                table.setWidthPercentage(100);
                for (String h : tableHeaders) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                    cell.setBackgroundColor(new Color(114, 28, 36));
                    cell.setPadding(6f);
                    table.addCell(cell);
                }
                if (tableRows != null) {
                    for (String[] row : tableRows) {
                        for (String value : row) {
                            PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value, cellFont));
                            cell.setPadding(5f);
                            table.addCell(cell);
                        }
                    }
                }
                document.add(table);
            }

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur lors de la génération du PDF : " + e.getMessage(), e);
        }
        return out.toByteArray();
    }
}
