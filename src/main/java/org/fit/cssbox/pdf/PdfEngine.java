/**
 * PdfEngine.java
 * (c) Radek Burget, 2019
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
 * Created on 16.11.2019, 19:39:00 by burgetr
 */
package org.fit.cssbox.pdf;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.css.FontTable;
import org.fit.cssbox.layout.BrowserConfig;
import org.fit.cssbox.layout.Dimension;
import org.fit.cssbox.layout.GraphicsEngine;
import org.fit.cssbox.layout.GraphicsVisualContext;
import org.fit.cssbox.layout.Rectangle;
import org.fit.cssbox.layout.VisualContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * A browser engine that extends GraphicsEngine with the possibility of rendering the PDF tree.
 * 
 * @author burgetr
 */
public class PdfEngine extends GraphicsEngine
{
    private static Logger log = LoggerFactory.getLogger(PdfEngine.class);

    protected PDDocument pdfdocument;
    protected int startPage;
    protected int endPage;
    protected boolean pdfLoaded = false;
    
    protected CSSBoxTree boxtree;

    
    public PdfEngine(Element root, DOMAnalyzer decoder, Dimension dim, URL baseurl)
    {
        super(root, decoder, dim, baseurl); //DOM mode (no PDF)
    }

    /**
     * Creates an engine for a PDF file and computes the layout.
     * @param document the PDF document
     * @param startPage starting page
     * @param endPage ending page
     * @param decoder DOM analyzer
     * @param dim preferred canvas dimension
     * @param baseurl base URL for loading referenced data
     */
    public PdfEngine(PDDocument document, int startPage, int endPage, DOMAnalyzer decoder, Dimension dim, URL baseurl)
    {
        super(null, decoder, dim, baseurl);
        this.pdfdocument = document;
        this.startPage = startPage;
        this.endPage = endPage;
        createPdfLayout(dim);
        pdfLoaded = true;
    }
    
    /**
     * Creates an engine for a PDF file and computes the layout.
     * @param document the PDF document
     * @param startPage starting page
     * @param endPage ending page
     * @param decoder DOM analyzer
     * @param dim preferred canvas dimension
     * @param baseurl base URL for loading referenced data
     */
    public PdfEngine(PDDocument document, DOMAnalyzer decoder, Dimension dim, URL baseurl)
    {
        super(null, decoder, dim, baseurl);
        this.pdfdocument = document;
        this.startPage = 0;
        this.endPage = Integer.MAX_VALUE;
        createPdfLayout(dim);
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
    public void createLayout(Dimension dim, Rectangle visibleRect)
    {
        if (pdfdocument != null) //PDF mode: create the layout only when the DOM tree is already finished
        {
            if (pdfLoaded)
                createPdfLayout(dim);
        }
        else //DOM mode
        {
            if (getRootElement() != null)
                super.createLayout(dim, visibleRect);
        }
    }
    
    @Override
    protected VisualContext createVisualContext(BrowserConfig config, FontTable fontTable)
    {
        GraphicsVisualContext ctx = new GraphicsVisualContext(getImageGraphics(), null, config, fontTable);
        return ctx;
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
                initOutputMedia(dim.width, dim.height);
                
                log.info("Creating PDF boxes");
                GraphicsVisualContext ctx = (GraphicsVisualContext) createVisualContext(getConfig(), null);
                
                boxtree = new CSSBoxTree(ctx, dim, getBaseUrl());
                boxtree.setConfig(getConfig());
                boxtree.processDocument(pdfdocument, startPage, endPage);
                setViewport(boxtree.getViewport());
                setRootElement(boxtree.getDocument().getDocumentElement());
                log.info("We have " + boxtree.getLastId() + " boxes");
                getViewport().initSubtree();
                
                log.info("Layout for "+dim.width+"px");
                getViewport().doLayout(dim.width, true, true);
                log.info("Resulting size: " + getViewport().getWidth() + "x" + getViewport().getHeight() + " (" + getViewport() + ")");
        
                log.info("Updating viewport size");
                getViewport().updateBounds(dim);
                log.info("Resulting size: " + getViewport().getWidth() + "x" + getViewport().getHeight() + " (" + getViewport() + ")");
                
                if (getViewport().getWidth() > dim.width || getViewport().getHeight() > dim.height)
                {
                    initOutputMedia(Math.max(getViewport().getWidth(), dim.width), Math.max(getViewport().getHeight(), dim.height));
                }
                
                log.trace("Positioning for " + getViewport().getWidth() + "x" + getViewport().getHeight() + "px");
                getViewport().absolutePositions();
                
                log.trace("Drawing");
                renderViewport(getViewport());
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (getRootElement() != null) //processing a DOM tree
        {
            super.createLayout(dim);
        }
    }
 
}
