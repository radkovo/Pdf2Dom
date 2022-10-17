Pdf2Dom
=======

[![Build Status](https://travis-ci.org/radkovo/Pdf2Dom.png)](https://travis-ci.org/radkovo/Pdf2Dom)

Pdf2Dom is a PDF parser that converts the documents to a HTML DOM representation. The obtained DOM tree may be then
serialized to a HTML file or further processed. The inline CSS definitions contained in the resulting document are
used for making the HTML page as similar as possible to the PDF input. A command-line utility for converting the PDF
documents to HTML is included in the distribution package. Pdf2Dom may be also used as an independent Java library
with a standard DOM interface for your DOM-based applications. 

Pdf2Dom is based on the Apache PDFBox™ library.

See the project page for more information and downloads:
http://cssbox.sourceforge.net/pdf2dom

See also the [Pdf2Dom-lite](https://github.com/radkovo/Pdf2Dom-lite) fork that provides a lightweight version 
of Pdf2Dom with no font decoding support but significantly reduced dependencies.
