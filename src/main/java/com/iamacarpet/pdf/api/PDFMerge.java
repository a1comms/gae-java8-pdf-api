package com.iamacarpet.pdf.api;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.itextpdf.text.pdf.FdfReader;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfBoolean;

@WebServlet(
    name = "PDFMerge",
    urlPatterns = {"/pdf/api/v1/pdf-merge"}
)
public class PDFMerge extends HttpServlet {

  private static final Logger log = Logger.getLogger(PDFMergeFromGCS.class.getName());
	
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
      
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");

    response.getWriter().print("<form action=\"/pdf/api/v1/pdf-merge\" method=\"POST\" enctype=\"application/x-www-form-urlencoded\"><textarea name=\"fdf\"></textarea><textarea name=\"pdf\"></textarea><input type=\"submit\" value=\"Submit\" />\r\n");

  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
	  byte[] fdfData = java.util.Base64.getDecoder().decode(request.getParameter("fdf"));
	  byte[] pdfData = java.util.Base64.getDecoder().decode(request.getParameter("pdf"));
			  
	  FdfReader fdf_reader_p = new FdfReader(fdfData);
	  PdfReader input_reader_p = new PdfReader(pdfData);
	  java.io.ByteArrayOutputStream ofs_p = new java.io.ByteArrayOutputStream();
	  PdfStamper writer_p = null;
	  try {
		  writer_p = new PdfStamper(input_reader_p, ofs_p);
	  } catch (DocumentException e) {
		  log.severe(e.toString());
		  return;
	  }
	  
	  if( input_reader_p.getAcroForm() != null ) {
		  AcroFields fields_p= writer_p.getAcroFields();
		  try {
			fields_p.setFields( fdf_reader_p );
		  } catch (DocumentException e) {
			  log.severe(e.toString());
			  return;
		  }
	  }
	  
	  writer_p.setFormFlattening( true );
	  
	  input_reader_p.removeUnusedObjects();

	  // done; write output
	  try {
		  writer_p.close();
	  } catch (DocumentException e) {
		  log.severe(e.toString());
		  return;
	  }

	  response.setContentType("application/pdf");
	  response.setCharacterEncoding("UTF-8");
	  ofs_p.flush();
	  response.getOutputStream().write(ofs_p.toByteArray());  
  }
}