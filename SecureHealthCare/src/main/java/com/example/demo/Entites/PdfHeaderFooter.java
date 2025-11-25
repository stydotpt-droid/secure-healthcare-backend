package com.example.demo.Entites;



import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class PdfHeaderFooter extends PdfPageEventHelper {

    private String hospitalName;
    private String address;

    private Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

    public PdfHeaderFooter(String hospitalName, String address) {
        this.hospitalName = hospitalName;
        this.address = address;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfPTable header = new PdfPTable(1);
        PdfPTable footer = new PdfPTable(1);

        header.setTotalWidth(527);
		header.setLockedWidth(true);
		header.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		header.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
		header.addCell(new Phrase(hospitalName, headerFont));
		header.writeSelectedRows(0, -1, 34, 830, writer.getDirectContent());

		footer.setTotalWidth(527);
		footer.setLockedWidth(true);
		footer.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		footer.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
		footer.addCell(new Phrase("Address: " + address, footerFont));
		footer.writeSelectedRows(0, -1, 34, 30, writer.getDirectContent());
    }
}

