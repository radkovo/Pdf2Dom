/**
 * PdfBrowserCanvas.java
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
 * Created on 29.9.2011, 13:29:43 by burgetr
 */
package org.fit.cssbox.pdf;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.layout.BrowserCanvas;
import org.fit.cssbox.layout.VisualContext;
import org.fit.cssbox.render.GraphicsRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * A browser canvas that is able to render PDF documents in addition to the HTML documents.
 * 
 * @author burgetr
 */
public class PdfBrowserCanvas extends BrowserCanvas
{
    private static Logger log = LoggerFactory.getLogger(PdfBrowserCanvas.class);
    
    private static final long serialVersionUID = -8053836572208370866L;
    
    protected PDDocument pdfdocument;
    protected int startPage;
    protected int endPage;
    protected boolean pdfLoaded = false;
    
    protected CSSBoxTree boxtree;

    /**
     * Creates a canvas from a HTML DOM (for compatibility with BrowserCanvas) and computes the layout.
     * @param root DOM root node
     * @param decoder CSS decoder
     * @param dim preferred dimension
     * @param baseurl base URL for loading referenced data
     */
    public PdfBrowserCanvas(Element root, DOMAnalyzer decoder, Dimension dim, URL baseurl)
    {
        super(root, decoder, dim, baseurl); //DOM mode (no PDF)
    }

    /**
     * Creates a canvas from a PDF file and computes the layout.
     * @param document the PDF document
     * @param startPage starting page
     * @param endPage ending page
     * @param decoder DOM analyzer
     * @param dim preferred canvas dimension
     * @param baseurl base URL for loading referenced data
     */
    public PdfBrowserCanvas(PDDocument document, int startPage, int endPage, DOMAnalyzer decoder, Dimension dim, URL baseurl)
    {
        super(null, decoder, dim, baseurl);
        this.pdfdocument = document;
        this.startPage = startPage;
        this.endPage = endPage;
        createPdfLayout(dim);
        pdfLoaded = true;
    }

    /**
     * Creates a canvas from a PDF file and computes the layout.
     * @param document the PDF document
     * @param decoder DOM analyzer
     * @param dim preferred canvas dimension
     * @param baseurl base URL for loading referenced data
     */
    public PdfBrowserCanvas(PDDocument document, DOMAnalyzer decoder, Dimension dim, URL baseurl)
    {
        super(null, decoder, dim, baseurl);
        this.pdfdocument = document;
        this.startPage = 0;
        this.endPage = Integer.MAX_VALUE;
        createPdfLayout(dim);
        pdfLoaded = true;
    }
    
    /**
     * Creates a canvas from a PDF file <b>without computing the layout</b>. The layout may be computed
     * afterwards by calling the {@link #createLayout(Dimension)} method.
     * @param document the PDF document
     * @param startPage starting page
     * @param endPage ending page
     * @param decoder DOM analyzer
     * @param baseurl base URL for loading referenced data
     */
    public PdfBrowserCanvas(PDDocument document, int startPage, int endPage, DOMAnalyzer decoder, URL baseurl)
    {
        super(null, decoder, baseurl);
        this.pdfdocument = document;
        this.startPage = startPage;
        this.endPage = endPage;
        pdfLoaded = true;
    }

    /**
     * Obtains the created tree of boxes.
     * @return the box tree.
     */
    public CSSBoxTree getBoxTree()
    {
    	return boxtree;
    }
    
    //===========================================================================================

    @Override
    public void createLayout(Dimension dim)
    {
        if (pdfdocument != null) //PDF mode: create the layout only when the DOM tree is already finished
        {
            if (pdfLoaded)
                createPdfLayout(dim);
        }
        else //DOM mode
        {
        	if (root != null)
        		super.createLayout(dim);
        }
    }
    
    /**
     * Creates the box tree for the PDF file.
     * @param dim
     */
    public void createPdfLayout(Dimension dim)
    {
        if (pdfdocument != null) //processing a PDF document
        {
            try {
                if (createImage)
                    img = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
                Graphics2D ig = img.createGraphics();
                
                log.info("Creating PDF boxes");
                VisualContext ctx = new VisualContext(null, null);
                
                boxtree = new CSSBoxTree(ig, ctx, dim, baseurl);
                boxtree.setConfig(config);
                boxtree.processDocument(pdfdocument, startPage, endPage);
                viewport = boxtree.getViewport();
                root = boxtree.getDocument().getDocumentElement();
                log.info("We have " + boxtree.getLastId() + " boxes");
                viewport.initSubtree();
                
                log.info("Layout for "+dim.width+"px");
                viewport.doLayout(dim.width, true, true);
                log.info("Resulting size: " + viewport.getWidth() + "x" + viewport.getHeight() + " (" + viewport + ")");
        
                log.info("Updating viewport size");
                viewport.updateBounds(dim);
                log.info("Resulting size: " + viewport.getWidth() + "x" + viewport.getHeight() + " (" + viewport + ")");
                
                if (createImage && (viewport.getWidth() > dim.width || viewport.getHeight() > dim.height))
                {
                    img = new BufferedImage(Math.max(viewport.getWidth(), dim.width),
                                            Math.max(viewport.getHeight(), dim.height),
                                            BufferedImage.TYPE_INT_RGB);
                    ig = img.createGraphics();
                }
                
                log.info("Positioning for "+img.getWidth()+"x"+img.getHeight()+"px");
                viewport.absolutePositions();
                
                clearCanvas();
                viewport.draw(new GraphicsRenderer(ig));
                setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
                revalidate();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (root != null) //processing a DOM tree
        {
            super.createLayout(dim);
        }
    }
    
}
