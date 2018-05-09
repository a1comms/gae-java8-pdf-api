# PDF Conversion API for App Engine Standard Java8 Runtime

## This package requires a license from iText for commercial use.

This project provides an API that will merge data into a PDF form using a FDF file, then flatten it into a read-only PDF.

Deploy this project to App Engine using Eclipse.

### Endpoint /pdf/api/v1/pdf-merge
Accepts two POST variables, "pdf" and "fdf". "pdf" is a base64'd PDF file with form fields. "fdf" is a base64 encoded FDF file.

### Endpoint /pdf/api/v1/pdf-merge-from-gcs
Accepts two POST variables, "pdf_filename" and "fdf". "fdf" is a base64 encodeded FDF file. "pdf_filename" should be the name of a file that exists in the "pdf" folder of the default bucket.
