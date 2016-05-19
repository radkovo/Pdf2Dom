package org.fit.pdfdom;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.awt.geom.Rectangle2D;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestPageRotation
{
    private static final String testPath = "/page-rotation/";

    @Test
    public void rotated_270Degrees_withTopLeftImage_GivesImageTranslatedToBottomLeft() throws Exception
    {
        Document html = TestUtils.parseWithPdfDomTree(testPath + "object-page-rotate-270.pdf");

        Rectangle2D.Double pageRect = findPageRect(html);
        Rectangle2D.Double imageRect = findStyleRect(html.select("img").first().attr("style"));

        TestUtils.assertInBottomArea(imageRect, pageRect);
        TestUtils.assertInLeftArea(imageRect, pageRect);
    }

    @Test
    public void rotated_180Degrees_withTopLeftImage_GivesImageTranslatedToBottomRight() throws Exception
    {
        Document html = TestUtils.parseWithPdfDomTree(testPath + "object-page-rotate-180.pdf");

        Rectangle2D.Double pageRect = findPageRect(html);
        Rectangle2D.Double imageRect = findStyleRect(html.select("img").first().attr("style"));

        TestUtils.assertInBottomArea(imageRect, pageRect);
        TestUtils.assertInRightArea(imageRect, pageRect);
    }

    @Test
    public void rotated_90Degrees_withTopLeftImage_GivesImageTranslatedToTopRight() throws Exception
    {
        Document html = TestUtils.parseWithPdfDomTree(testPath + "object-page-rotate-90.pdf");

        Rectangle2D.Double pageRect = findPageRect(html);
        Rectangle2D.Double imageRect = findStyleRect(html.select("img").first().attr("style"));

        TestUtils.assertInTopArea(imageRect, pageRect);
        TestUtils.assertInRightArea(imageRect, pageRect);
    }

    @Test
    public void rotated_0Degrees_withTopLeftImage_ImageStaysInTopLeft() throws Exception
    {
        Document html = TestUtils.parseWithPdfDomTree(testPath + "object-page-rotate-0.pdf");

        Rectangle2D.Double pageRect = findPageRect(html);
        Rectangle2D.Double imageRect = findStyleRect(html.select("img").first().attr("style"));

        TestUtils.assertInTopArea(imageRect, pageRect);
        TestUtils.assertInLeftArea(imageRect, pageRect);
    }

    @Test
    public void rotated_0Degrees_withBottomRightRectangle_RectangleStaysInBottomRight() throws Exception
    {
        Document html = TestUtils.parseWithPdfDomTree(testPath + "object-page-rotate-0.pdf");
        Rectangle2D.Double imageRect = findStyleRect(html.select("div.r").first().attr("style"));

        TestUtils.assertInBottomRightCorner(imageRect, findPageRect(html));
    }

    @Test
    public void rotated_90Degrees_withBottomRightRect_GivesRectInBottomLeft() throws Exception
    {
        Document html = TestUtils.parseWithPdfDomTree(testPath + "object-page-rotate-90.pdf");
        Rectangle2D.Double imageRect = findStyleRect(html.select("div.r").first().attr("style"));

        TestUtils.assertInBottomLeftCorner(imageRect, findPageRect(html));
    }

    @Test
    public void rotated_180Degrees_withBottomRightRect_GivesRectInTopLeft() throws Exception
    {
        Document html = TestUtils.parseWithPdfDomTree(testPath + "object-page-rotate-180.pdf");
        Rectangle2D.Double imageRect = findStyleRect(html.select("div.r").first().attr("style"));

        TestUtils.assertInTopLeftCorner(imageRect, findPageRect(html));
    }

    @Test
    public void rotated_270Degrees_withBottomRightRect_GivesRectInTopRight() throws Exception
    {
        Document html = TestUtils.parseWithPdfDomTree(testPath + "object-page-rotate-270.pdf");
        Rectangle2D.Double imageRect = findStyleRect(html.select("div.r").first().attr("style"));

        TestUtils.assertInTopRightCorner(imageRect, findPageRect(html));
    }

    private static Rectangle2D.Double findPageRect(Document html) {
        Element page = html.select("div.page").first();
        String style = page.attr("style");
        return findStyleRect(style);
    }

    private static Rectangle2D.Double findStyleRect(String style)
    {
        double width = findNumberProperty("width", style);
        double height = findNumberProperty("height", style);
        double top = findNumberProperty("top", style);
        double left = findNumberProperty("left", style);

        return new Rectangle2D.Double(left, top, width, height);
    }

    private static double findNumberProperty(String find, String style)
    {
        String regex = find + ":([^;]*)(pt|px);";
        Matcher matcher = Pattern.compile(regex).matcher(style);
        if (matcher.find())
            return Double.parseDouble(matcher.group(1));

        return 0.0;
    }
}
