package org.fit.pdfdom;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;

public class TestUtils
{
    public static Document parseWithPdfDomTree(String resource)
            throws IOException, ParserConfigurationException, TransformerException
    {
        InputStream is = TestUtils.class.getResourceAsStream(resource);
        Document doc = parseWithPdfDomTree(is);
        is.close();
        return doc;
    }

    public static Document parseWithPdfDomTree(InputStream is)
            throws IOException, ParserConfigurationException, TransformerException
    {
        PDDocument pdf = PDDocument.load(is);
        PDFDomTree parser = new PDFDomTree();

        Writer output = new StringWriter();
        parser.writeText(pdf, output);
        pdf.close();
        String htmlOutput = output.toString();
//        File debugOutFile = new File(file.getName().replace(".pdf", ".html"));
//        FileUtils.write(debugOutFile, htmlOutput);

        return Jsoup.parse(htmlOutput);
    }

    private static String areaAssertMessage = "Target object is not in %s area of containing rectangle";
    private static double delta = 10.0;

    public static void assertInBottomArea(Rectangle2D.Double target, Rectangle2D.Double container)
    {
        String message = String.format(areaAssertMessage, "bottom");

        Assert.assertThat(message, target.y, greaterThan(container.getHeight() / 2));
        Assert.assertThat(message, target.y, lessThan(container.getHeight() + delta));
    }

    public static void assertInTopArea(Rectangle2D.Double target, Rectangle2D.Double container)
    {
        String message = String.format(areaAssertMessage, "top");

        Assert.assertThat(message, target.y, lessThan(container.getHeight() / 2));
        Assert.assertThat(message, target.y, greaterThan(-delta));
    }

    public static void assertInLeftArea(Rectangle2D.Double target, Rectangle2D.Double container)
    {
        String message = String.format(areaAssertMessage, "left");

        Assert.assertThat(message, target.x, lessThan(container.getWidth() / 2));
        Assert.assertThat(message, target.x, greaterThan(-delta));
    }

    public static void assertInRightArea(Rectangle2D.Double target, Rectangle2D.Double container)
    {
        String message = String.format(areaAssertMessage, "right");

        Assert.assertThat(message, target.x, greaterThan(container.getWidth() / 2));
        Assert.assertThat(message, target.x, lessThan(container.getWidth() + delta));
    }

    public static void assertNotOutsideOf(Rectangle2D.Double target, Rectangle2D.Double container)
    {
        String message = "Target rectangle is outside of container rectangle.";

        Assert.assertThat(message, target.x + target.width, lessThan(container.width + delta));
        Assert.assertThat(message, target.y + target.height, lessThan(container.height + delta));
    }

    public static void assertInBottomRightCorner(Rectangle2D.Double target, Rectangle2D.Double container)
    {
        assertNotOutsideOf(target, container);
        assertInRightArea(target, container);
        assertInBottomArea(target, container);
    }

    public static void assertInBottomLeftCorner(Rectangle2D.Double target, Rectangle2D.Double container)
    {
        assertNotOutsideOf(target, container);
        assertInLeftArea(target, container);
        assertInBottomArea(target, container);
    }

    public static void assertInTopLeftCorner(Rectangle2D.Double target, Rectangle2D.Double container)
    {
        assertNotOutsideOf(target, container);
        assertInLeftArea(target, container);
        assertInTopArea(target, container);
    }

    public static void assertInTopRightCorner(Rectangle2D.Double target, Rectangle2D.Double container)
    {
        assertNotOutsideOf(target, container);
        assertInTopArea(target, container);
        assertInRightArea(target, container);
    }
}
