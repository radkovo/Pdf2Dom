/**
 * CSSBoxTree.java
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
 * Created on 27.9.2011, 16:39:00 by burgetr
 */
package org.fit.cssbox.pdf;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFactory;
import cz.vutbr.web.css.TermNumeric.Unit;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.fit.cssbox.layout.BlockBox;
import org.fit.cssbox.layout.BlockReplacedBox;
import org.fit.cssbox.layout.BrowserConfig;
import org.fit.cssbox.layout.ReplacedImage;
import org.fit.cssbox.layout.TextBox;
import org.fit.cssbox.layout.Viewport;
import org.fit.cssbox.layout.VisualContext;
import org.fit.pdfdom.BoxStyle;
import org.fit.pdfdom.PDFDomTree;
import org.fit.pdfdom.PathSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * This class implements direct creation of a CSSBox tree from a PDF file. It creates a tree if boxes compatible
 * with the original CSSBox {@link org.fit.cssbox.layout.BoxFactory} result for HTML documents. The resulting tree contains the boxes
 * together with their styles. Its further processing (layout and positioning) is the same as for the tree of boxes
 * obtained from the HTML documents.
 * 
 * @author burgetr
 */
public class CSSBoxTree extends PDFDomTree
{
    private static Logger log = LoggerFactory.getLogger(PDFDomTree.class);

    /** Scale factor for unknown fonts - it is used to prevent overlaping the boxes when an inappropriate font is used */
    protected float unknownFontScale = 0.95f;
    
    /** Length units used in the output */
    protected Unit unit = Unit.pt;
    
    /** Root graphics context */
	protected Graphics2D g;
	/** Root visual context */
	protected VisualContext ctx;
	/** Preferred dimensions of the result */
	protected Dimension dim;
	/** Base URL used for eventual references in the input file */
	protected URL baseurl;
	
	/** The resulting viewport */
	protected Viewport viewport;
	/** HTML box */
	protected BlockBox html;
	/** BODY box */
	protected BlockBox body;
	/** The box representing the page that is currently being created */
    protected BlockBox pagebox;

    /** Used CSSBox configuration */
    protected BrowserConfig config;
    
    /** Internal counter for assigning the node IDs */
    protected int next_order;
    
    /**
     * Creates a new instance bound to certain graphic context.
     * @param g The graphic context used for displaying the rendered result. It is used for obtaining the font metrics.
     * @param ctx The initial CSSBox visual context used for the viewport.
     * @param dim The initial dimensions of the viewport. The resulting dimensions may be updated according to the page contents.
     * @param baseurl Base url used for loading referenced objects.
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public CSSBoxTree(Graphics2D g, VisualContext ctx, Dimension dim, URL baseurl) throws IOException, ParserConfigurationException
    {
        super();
    	this.g = g;
    	this.ctx = ctx;
    	this.dim = dim;
    	this.baseurl = baseurl;
    	init();
    }
    
    /**
     * Internal initialization.
     */
    private void init()
    {
        next_order = 0;
    }

    public BrowserConfig getConfig()
    {
        return config;
    }

    public void setConfig(BrowserConfig config)
    {
        this.config = config;
    }

    /**
     * Obtains the resulting viewport that represents the root node of the resulting box tree.
     * @return the viewport
     */
    public Viewport getViewport()
    {
        return viewport;
    }

    /**
     * Obtains the first unused ID of the box. This can be used for obtaining the box count.
     * @return the first unused box ID
     */
    public int getLastId()
    {
        return next_order;
    }

    @Override
    protected void createDocument() throws ParserConfigurationException
    {
    	super.createDocument();
    	//create viewport with the initial dimension
        Element vp = createAnonymousElement(getDocument(), "Xdiv", "block");
        Element root = getDocument().getDocumentElement();
        viewport = new Viewport(vp, g, ctx, null, root, dim.width, dim.height);
        viewport.setConfig(config);
        //create the root boxes
        html = createBlock(viewport, root, false);
        html.setStyle(createBlockStyle());
        viewport.addSubBox(html);
        body = createBlock(html, (Element) root.getElementsByTagName("body").item(0), false);
        body.setStyle(createBodyStyle());
        html.addSubBox(body);
    }

    //===========================================================================================
    
    @Override
    protected void startNewPage()
    {
        super.startNewPage();
        pagebox = createBlock(body, curpage, false);
        pagebox.setStyle(createPageStyle());
        body.addSubBox(pagebox);
    }

    @Override
    protected void renderText(String data, float width)
    {
        //DOM element
        Element el = createTextElement(data, width);
        curpage.appendChild(el);
        //Block box
        BlockBox block = createBlock(pagebox, el, false);
        block.setStyle(createTextStyle(curstyle, width));
        pagebox.addSubBox(block);
        //Text box
        TextBox text = createTextBox(block, (Text) el.getFirstChild());
        block.addSubBox(text);
    }

    @Override
    protected void renderPath(List<PathSegment> path, boolean stroke, boolean fill)
    {
        float[] rect = toRectangle(path);
        if (rect != null)
        {
            //DOM element
            Element el = createRectangleElement(rect[0], rect[1], rect[2]-rect[0], rect[3]-rect[1], stroke, fill);
            curpage.appendChild(el);
            //Block box
            BlockBox block = createBlock(pagebox, el, false);
            block.setStyle(createRectangleStyle(rect[0], rect[1], rect[2]-rect[0], rect[3]-rect[1], stroke, fill));
            pagebox.addSubBox(block);
        }
        else if (stroke)
        {
            for (PathSegment segm : path)
            {
                if (segm.getX1() == segm.getX2() || segm.getY1() == segm.getY2())
                {
                    //DOM element
                    Element el = createLineElement(segm.getX1(), segm.getY1(), segm.getX2(), segm.getY2());
                    curpage.appendChild(el);
                    //Block box
                    BlockBox block = createBlock(pagebox, el, false);
                    block.setStyle(createLineStyle(segm.getX1(), segm.getY1(), segm.getX2(), segm.getY2()));
                    pagebox.addSubBox(block);
                }
                else
                    log.warn("Skipped non-orthogonal line segment");
            }
        }
    }
    
    /*protected void renderRectangle(float x, float y, float width, float height, boolean stroke, boolean fill)
    {
        //DOM element
        Element el = createRectangleElement(x, y, width, height, stroke, fill);
        curpage.appendChild(el);
        //Block box
        BlockBox block = createBlock(pagebox, el, false);
        block.setStyle(createRectangleStyle(x, y, width, height, stroke, fill));
        pagebox.addSubBox(block);
    }*/

    @Override
    protected void renderImage(float x, float y, float width, float height, String mimetype, byte[] data)
    {
        //DOM element
        Element el = createImageElement(x, y, width, height, mimetype, data);
        curpage.appendChild(el);
        //Image box
        BlockBox block = createBlock(pagebox, el, true);
        block.setStyle(createRectangleStyle(x, y, width, height, false, false));
        pagebox.addSubBox(block);
    }
    
    //===========================================================================================
    
    /**
     * Creates a new DOM element that represents an anonymous box in a document.
     * @param doc the document
     * @param name the anonymous element name (generally arbitrary)
     * @param display the display style value for the block
     * @return the new element
     */
    protected Element createAnonymousElement(Document doc, String name, String display)
    {
        Element div = doc.createElement(name);
        div.setAttribute("class", "Xanonymous");
        div.setAttribute("style", "display:" + display);
        return div;
    }
    
    /**
     * Creates a new block box from the given element with the given parent. No style is assigned to the resulting box. 
     * @param parent The parent box in the tree of boxes.
     * @param n The element that this box belongs to.
     * @param replaced When set to <code>true</code>, a replaced block box will be created. Otherwise, a normal non-replaced block will be created.
     * @return The new block box.
     */
    protected BlockBox createBlock(BlockBox parent, Element n, boolean replaced)
    {
        BlockBox root;
        if (replaced)
        {
            BlockReplacedBox rbox = new BlockReplacedBox((Element) n, (Graphics2D) parent.getGraphics().create(), parent.getVisualContext().create());
            rbox.setViewport(viewport);
            rbox.setContentObj(new ReplacedImage(rbox, rbox.getVisualContext(), baseurl, n.getAttribute("src")));
            root = rbox;
        }
        else
        {
            root = new BlockBox((Element) n, (Graphics2D) parent.getGraphics().create(), parent.getVisualContext().create());
            root.setViewport(viewport);
        }
        root.setBase(baseurl);
        root.setParent(parent);
        root.setContainingBlock(parent);
        root.setClipBlock(viewport);
        root.setOrder(next_order++);
        return root;
    }
    
    /**
     * Creates a text box with the given parent and text node assigned.
     * @param contblock The parent node (and the containing block in the same time)
     * @param n The corresponding text node in the DOM tree.
     * @return The new text box.
     */
    protected TextBox createTextBox(BlockBox contblock, Text n)
    {
        TextBox text = new TextBox(n, (Graphics2D) contblock.getGraphics().create(), contblock.getVisualContext().create());
        text.setOrder(next_order++);
        text.setContainingBlock(contblock);
        text.setClipBlock(viewport);
        text.setViewport(viewport);
        text.setBase(baseurl);
        return text;
    }

    /**
     * Creates the style declaration for a text box based on the given {@link BoxStyle} structure.
     * @param style The source box style.
     * @return The element style definition.
     */
    protected NodeData createTextStyle(BoxStyle style, float width)
    {
        NodeData ret = CSSFactory.createNodeData();
        TermFactory tf = CSSFactory.getTermFactory();
        ret.push(createDeclaration("position", tf.createIdent("absolute")));
        ret.push(createDeclaration("overflow", tf.createIdent("hidden")));
        ret.push(createDeclaration("left", tf.createLength(style.getLeft(), unit)));
        ret.push(createDeclaration("top", tf.createLength(style.getTop(), unit)));
        ret.push(createDeclaration("line-height", tf.createLength(style.getLineHeight(), unit)));
		if (style.getFontFamily() != null)
			ret.push(createDeclaration("font-family", tf.createString(style.getFontFamily())));
		if (style.getFontSize() != 0)
		{
		    float size = (float) style.getFontSize();
		    if (style.getFontFamily() == null)
		        size = size * unknownFontScale;
			ret.push(createDeclaration("font-size", tf.createLength(size, unit)));
		}
		if (style.getFontWeight() != null)
			ret.push(createDeclaration("font-weight", tf.createIdent(style.getFontWeight())));
		if (style.getFontStyle() != null)
			ret.push(createDeclaration("font-style", tf.createIdent(style.getFontStyle())));
		if (style.getWordSpacing() != 0)
			ret.push(createDeclaration("word-spacing", tf.createLength((float) style.getWordSpacing(), unit)));
		if (style.getLetterSpacing() != 0)
			ret.push(createDeclaration("letter-spacing", tf.createLength((float) style.getLetterSpacing(), unit)));
		if (style.getColor() != null)
			ret.push(createDeclaration("color", tf.createColor(style.getColor())));
		
		ret.push(createDeclaration("width", tf.createLength(width, unit)));
		
        return ret;
    }
    
    /**
     * Creates an empty block style definition.
     * @return 
     */
    protected NodeData createBlockStyle()
    {
        NodeData ret = CSSFactory.createNodeData();
        TermFactory tf = CSSFactory.getTermFactory();
        ret.push(createDeclaration("display", tf.createIdent("block")));
        return ret;
    }
    
    /**
     * Creates a style definition used for the body element.
     * @return The body style definition.
     */
    protected NodeData createBodyStyle()
    {
        NodeData ret = createBlockStyle();
        TermFactory tf = CSSFactory.getTermFactory();
        ret.push(createDeclaration("background-color", tf.createColor(255, 255, 255)));
        return ret;
    }
    
    /**
     * Creates a style definition used for pages.
     * @return The page style definition.
     */
    protected NodeData createPageStyle()
    {
        NodeData ret = createBlockStyle();
        TermFactory tf = CSSFactory.getTermFactory();
        ret.push(createDeclaration("position", tf.createIdent("relative")));
		ret.push(createDeclaration("border-width", tf.createLength(1f, Unit.px)));
		ret.push(createDeclaration("border-style", tf.createIdent("solid")));
		ret.push(createDeclaration("border-color", tf.createColor(0, 0, 255)));
		ret.push(createDeclaration("margin", tf.createLength(0.5f, Unit.em)));
		
        PDRectangle layout = getCurrentMediaBox();
        if (layout != null)
        {
            float w = layout.getWidth();
            float h = layout.getHeight();
            final int rot = pdpage.findRotation();
            if (rot == 90 || rot == 270)
            {
                float x = w; w = h; h = x;
            }
            
            ret.push(createDeclaration("width", tf.createLength(w, unit)));
            ret.push(createDeclaration("height", tf.createLength(h, unit)));
        }
        else
            log.warn("No media box found");
        
        return ret;
    }
    
    /**
     * Creates the style definition used for a rectangle element based on the given properties of the rectangle
     * @param x The X coordinate of the rectangle.
     * @param y The Y coordinate of the rectangle.
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     * @param stroke Should there be a stroke around?
     * @param fill Should the rectangle be filled?
     * @return The resulting element style definition.
     */
    protected NodeData createRectangleStyle(float x, float y, float width, float height, boolean stroke, boolean fill)
    {
        NodeData ret = CSSFactory.createNodeData();
        TermFactory tf = CSSFactory.getTermFactory();
        ret.push(createDeclaration("position", tf.createIdent("absolute")));
        ret.push(createDeclaration("left", tf.createLength(x, unit)));
        ret.push(createDeclaration("top", tf.createLength(y, unit)));
        ret.push(createDeclaration("width", tf.createLength(width, unit)));
        ret.push(createDeclaration("height", tf.createLength(height, unit)));
        
        if (stroke)
        {
            lineWidth = transformLength((float) getGraphicsState().getLineWidth());
            if (lineWidth == 0)
                ret.push(createDeclaration("border-width", tf.createLength(1f, Unit.px)));
            else
                ret.push(createDeclaration("border-width", tf.createLength(lineWidth, unit)));
            ret.push(createDeclaration("border-style", tf.createIdent("solid")));
            String color = (strokingColor == null) ? "#000000" : strokingColor;
            ret.push(createDeclaration("border-color", tf.createColor(color)));
        }
        
        if (fill)
        {
            String color = style.getColor();
            if (color != null)
                ret.push(createDeclaration("background-color", tf.createColor(color)));
        }

        return ret;
    }
    
    protected NodeData createLineStyle(float x1, float y1, float x2, float y2)
    {
        float x = Math.min(x1, x2);
        float y = Math.min(y1, y2);
        float width = Math.abs(x2 - x1);
        float height = Math.abs(y2 - y1);

        String bside;
        lineWidth = transformLength((float) getGraphicsState().getLineWidth());
        if (width == 0)
        {
            y += lineWidth / 2;
            height -= lineWidth;
            bside = "border-left";
        }
        else
        {
            x += lineWidth / 2;
            width -= lineWidth / 2;
            bside = "border-top";
        }
        
        NodeData ret = CSSFactory.createNodeData();
        TermFactory tf = CSSFactory.getTermFactory();
        ret.push(createDeclaration("position", tf.createIdent("absolute")));
        ret.push(createDeclaration("left", tf.createLength(x, unit)));
        ret.push(createDeclaration("top", tf.createLength(y, unit)));
        ret.push(createDeclaration("width", tf.createLength(width, unit)));
        ret.push(createDeclaration("height", tf.createLength(height, unit)));
        
        if (lineWidth == 0)
            ret.push(createDeclaration(bside + "-width", tf.createLength(1f, Unit.px)));
        else
            ret.push(createDeclaration(bside + "-width", tf.createLength(lineWidth, unit)));
        ret.push(createDeclaration(bside + "-style", tf.createIdent("solid")));
        String color = (strokingColor == null) ? "#000000" : strokingColor;
        ret.push(createDeclaration(bside + "-color", tf.createColor(color)));

        return ret;
    }
    
    /**
     * Creates a single property declaration.
     * @param property Property name.
     * @param term Property value.
     * @return The resulting declaration.
     */
    protected Declaration createDeclaration(String property, Term<?> term)
    {
        Declaration d = CSSFactory.getRuleFactory().createDeclaration();
        d.unlock();
        d.setProperty(property);
        d.add(term);
        return d;
    }
    
}
