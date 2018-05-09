package com.iamacarpet.pdf.api;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.itextpdf.text.pdf.FdfWriter;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfBoolean;

@WebServlet(
    name = "PDFDumpFDF",
    urlPatterns = {"/pdf/api/v1/pdf-dump"}
)
public class PDFDumpFDF extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
      
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");

    response.getWriter().print("<form action=\"/pdf/api/v1/pdf-dump\" method=\"POST\" enctype=\"application/x-www-form-urlencoded\"><textarea name=\"pdf\"></textarea><input type=\"submit\" value=\"Submit\" />\r\n");

  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
	  byte[] pdfData = java.util.Base64.getDecoder().decode(request.getParameter("pdf"));
			  
	  PdfReader input_reader_p = new PdfReader(pdfData);
	  java.io.ByteArrayOutputStream ofs_p = new java.io.ByteArrayOutputStream();
	  FdfWriter writer_p = new FdfWriter(ofs_p);
	  
	  AcroFields fields_p= input_reader_p.getAcroFields();
	  fields_p.exportAsFdf(writer_p);
	  
	  writer_p.write();

	  response.setContentType("text/plain");
	  response.setCharacterEncoding("UTF-8");
	  ofs_p.flush();
	  response.getOutputStream().write(ofs_p.toByteArray());  
  }
}