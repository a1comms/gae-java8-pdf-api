package com.iamacarpet.pdf.api;

import java.io.IOException;
import java.util.logging.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;

import com.itextpdf.text.pdf.FdfReader;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfBoolean;

@WebServlet(
    name = "PDFMergeFromGCS",
    urlPatterns = {"/pdf/api/v1/pdf-merge-from-gcs"}
)
public class PDFMergeFromGCS extends HttpServlet {
	
  /**
  * This is where backoff parameters are configured. Here it is aggressively retrying with
  * backoff, up to 10 times but taking no more that 15 seconds total to do so.
  */
  private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
	      .initialRetryDelayMillis(10)
	      .retryMaxAttempts(10)
	      .totalRetryPeriodMillis(15000)
	      .build());

  /**Used below to determine the size of chucks to read in. Should be > 1kb and < 10MB */
  private static final int BUFFER_SIZE = 2 * 1024 * 1024;
	  
  private static final Logger log = Logger.getLogger(PDFMergeFromGCS.class.getName());

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
      
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");

    response.getWriter().print("<form action=\"/pdf/api/v1/pdf-merge-from-gcs\" method=\"POST\" enctype=\"application/x-www-form-urlencoded\"><textarea name=\"fdf\"></textarea><input type=\"text\" name=\"pdf_filename\" /><input type=\"submit\" value=\"Submit\" />\r\n");

  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
	  byte[] fdfData = java.util.Base64.getDecoder().decode(request.getParameter("fdf"));
	  
	  GcsFilename fileName = new GcsFilename(AppIdentityServiceFactory.getAppIdentityService().getDefaultGcsBucketName(), "pdf/" + request.getParameter("pdf_filename"));
	  GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(fileName, 0, 2048);
	  InputStream input_p = Channels.newInputStream(readChannel);
			  
	  FdfReader fdf_reader_p = new FdfReader(fdfData);
	  PdfReader input_reader_p = new PdfReader(input_p);
	  java.io.ByteArrayOutputStream ofs_p = new java.io.ByteArrayOutputStream();
	  PdfStamper writer_p = null;
	  try {
		  writer_p = new PdfStamper(input_reader_p, ofs_p);
	  } catch (DocumentException e) {
		  // TODO Auto-generated catch block
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
		  // TODO Auto-generated catch block
		  log.severe(e.toString());
		  return;
	  }

	  response.setContentType("application/pdf");
	  response.setCharacterEncoding("UTF-8");
	  ofs_p.flush();
	  response.getOutputStream().write(ofs_p.toByteArray());  
  }
}