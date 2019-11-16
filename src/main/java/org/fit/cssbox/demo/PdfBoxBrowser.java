/**
 * PdfBoxBrowser.java
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
 * Created on 4.10.2011, 11:22:11 by burgetr
 */
package org.fit.cssbox.demo;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.tree.DefaultTreeModel;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.io.DOMSource;
import org.fit.cssbox.io.DefaultDOMSource;
import org.fit.cssbox.io.DefaultDocumentSource;
import org.fit.cssbox.io.DocumentSource;
import org.fit.cssbox.layout.BrowserCanvas;
import org.fit.cssbox.layout.Dimension;
import org.fit.cssbox.layout.GraphicsEngine;
import org.fit.cssbox.layout.Viewport;
import org.fit.cssbox.pdf.PdfEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import cz.vutbr.web.css.MediaSpec;

/**
 * This demo shows a simple browser of PDF files based on transforming the files to DOM and rendering by CSSBox.
 * It is based on the {@link BoxBrowser} demo from the CSSBox packages, only the document pre-processing part is changed.
 * 
 * @author burgetr
 */
public class PdfBoxBrowser extends org.fit.cssbox.demo.BoxBrowser
{
    private static Logger log = LoggerFactory.getLogger(PdfBoxBrowser.class);

    @Override
    public URL displayURL(String urlstring)
    {
        try {
            if (!urlstring.startsWith("http:") &&
                    !urlstring.startsWith("https:") &&
                    !urlstring.startsWith("ftp:") &&
                    !urlstring.startsWith("file:"))
                        urlstring = "http://" + urlstring;
                
            DocumentSource docSource = new DefaultDocumentSource(urlstring);
            urlText.setText(docSource.getURL().toString());
            
            GraphicsEngine engine;
            Document dom = null;
            InputStream is = docSource.getInputStream();
            if (docSource.getContentType().equals("application/pdf"))
            {
                log.info("Parsing PDF: " + docSource.getURL());
                PDDocument doc = loadPdf(is);
                engine = new PdfEngine(doc, null,
                        new Dimension(contentScroll.getSize().width, contentScroll.getSize().height),
                        docSource.getURL());
            }
            else
            {
                DOMSource parser = new DefaultDOMSource(docSource);
                Document doc = parser.parse();
                String encoding = parser.getCharset();
                
                MediaSpec media = new MediaSpec("screen");
                updateCurrentMedia(media);
                
                DOMAnalyzer da = new DOMAnalyzer(doc, docSource.getURL());
                if (encoding == null)
                    encoding = da.getCharacterEncoding();
                da.setDefaultEncoding(encoding);
                da.setMediaSpec(media);
                da.attributesToStyles();
                da.addStyleSheet(null, CSSNorm.stdStyleSheet(), DOMAnalyzer.Origin.AGENT);
                da.addStyleSheet(null, CSSNorm.userStyleSheet(), DOMAnalyzer.Origin.AGENT);
                da.addStyleSheet(null, CSSNorm.formsStyleSheet(), DOMAnalyzer.Origin.AGENT);
                da.getStyleSheets();
                
                engine = new GraphicsEngine(da.getRoot(), da, docSource.getURL());
                dom = doc;
            }
            is.close();
            
            contentCanvas = new BrowserCanvas(engine);
            ((BrowserCanvas) contentCanvas).setConfig(config);
            ((BrowserCanvas) contentCanvas).createLayout(contentScroll.getSize(), contentScroll.getVisibleRect());
            docSource.close();
            
            contentCanvas.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e)
                {
                    System.out.println("Click: " + e.getX() + ":" + e.getY());
                    canvasClick(e.getX(), e.getY());
                }
                public void mousePressed(MouseEvent e) { }
                public void mouseReleased(MouseEvent e) { }
                public void mouseEntered(MouseEvent e) { }
                public void mouseExited(MouseEvent e) { }
            });
            contentScroll.setViewportView(contentCanvas);

            //box tree
            Viewport viewport = engine.getViewport();
            root = createBoxTree(viewport);
            boxTree.setModel(new DefaultTreeModel(root));
            
            //dom tree
            if (dom == null) //DOM not initialized in PDF mode
                dom = ((PdfEngine) engine).getBoxTree().getDocument();
            domRoot = createDomTree(dom);
            domTree.setModel(new DefaultTreeModel(domRoot));
            
            //=============================================================================
            
            return docSource.getURL();
            
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
        
    }

    protected PDDocument loadPdf(InputStream is) throws IOException
    {
        PDDocument document = null;
        document = PDDocument.load(is);
        return document;
    }
    
    public static void main(String[] args)
    {
        browser = new PdfBoxBrowser();
        JFrame main = browser.getMainWindow();
        main.setSize(1200,600);
        main.setVisible(true);
    }

}
