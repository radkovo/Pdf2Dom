/**
 * PDFToHTML.java
 * (c) Radek Burget, 2011
 *
 * Pdf2Dom is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * Pdf2Dom is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public License
 * along with CSSBox. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Created on 19.9.2011, 13:34:54 by burgetr
 */
package org.fit.pdfdom;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * @author burgetr
 *
 */
public class PDFToHTML
{

    public static void main(String[] args)
    {
        
        if (args.length < 1)
        {
            System.out.println("Usage: PDFToHTML <infile> [<outfile>]");
            System.exit(1);
        }
        
        String infile = args[0];
        String outfile;
        if (args.length > 1)
            outfile = args[1];
        else
        {
            String base = args[0];
            if (base.toLowerCase().endsWith(".pdf"))
                base = base.substring(0, base.length() - 4);
            outfile = base + ".html";
        }
        
        PDDocument document = null;
        try
        {
            //document = PDDocument.load("test/CIC2011_Program.pdf");
            //document = PDDocument.load("test/RuleML-2010-Programme.pdf");
            //document = PDDocument.load("test/hassan.pdf");
            document = PDDocument.load(infile);
            if (document.isEncrypted())
            {
                try
                {
                    document.decrypt("");
                }
                catch (CryptographyException e)
                {
                    System.err.println("Error: Cryptography error:" + e.getMessage());
                    System.exit(1);
                }
            }
            
            PDFDomTree parser = new PDFDomTree();
            //parser.setDisableImageData(true);
            Writer output = new PrintWriter(outfile, "utf-8");
            parser.writeText(document, output);
            output.close();
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            //e.printStackTrace();
        }
        finally
        {
            if( document != null )
            {
                try
                {
                    document.close();
                } catch (IOException e) { 
                    System.err.println("Error: " + e.getMessage());
                    //e.printStackTrace();
                }
            }
        }
        
    }

}
