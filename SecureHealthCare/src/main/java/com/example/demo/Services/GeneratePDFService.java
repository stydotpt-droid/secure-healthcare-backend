package com.example.demo.Services;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.Entites.Hospital;
import com.example.demo.Entites.PdfHeaderFooter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

@Service
public class GeneratePDFService {

    public byte[] createPdf(String fullName, String doctorName, String hospitalName, Hospital hospital,
                            String reportType, Map<String, Object> parameters, String prescription) {
        try {
            Document document = new Document(PageSize.A4, 36, 36, 72, 72);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, out);

            // ✅ Use custom header/footer event
            PdfHeaderFooter event = new PdfHeaderFooter(hospitalName, hospital.getAddress());
            writer.setPageEvent(event);

            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Medical Report - " + reportType, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Patient and Doctor Info
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            Paragraph patientInfo = new Paragraph();
            patientInfo.add(new Phrase("Patient Name: ", labelFont));
            patientInfo.add(new Phrase(fullName + "\n", valueFont));
            patientInfo.add(new Phrase("Doctor Name: ", labelFont));
            patientInfo.add(new Phrase(doctorName + "\n", valueFont));
            patientInfo.add(new Phrase("Hospital: ", labelFont));
            patientInfo.add(new Phrase(hospitalName + "\n\n", valueFont));
            document.add(patientInfo);

            // Parameters Table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{1, 2});
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            PdfPCell header1 = new PdfPCell(new Phrase("Parameter"));
            header1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(header1);

            PdfPCell header2 = new PdfPCell(new Phrase("Value"));
            header2.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(header2);

            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                table.addCell(entry.getKey());
                table.addCell(String.valueOf(entry.getValue()));
            }

            document.add(table);

            // Prescription
            Paragraph presc = new Paragraph("Prescription:\n", labelFont);
            presc.add(new Phrase(prescription, valueFont));
            document.add(presc);

            document.close();
            writer.close();

            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
