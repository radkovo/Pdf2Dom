package org.fit.pdfdom;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

import static org.fit.pdfdom.TestUtils.getOutputEnabled;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import static org.hamcrest.core.AnyOf.anyOf;

public class TestPDFDomTree
{
    private static final String testPath = "/";

    @Test
    public void neitherRenderingModeText_outputTextIsInvisible() throws Exception
    {
        Document html = TestUtils.parseWithPdfDomTree(testPath + "text-rendering-mode-neither.pdf");
        Element text = html.select("div[class=p]").first();

        Assert.assertThat("Text element should be invisible.",
                text.attr("style"), containsString("color:" + BoxStyle.transparentColor));
    }

    @Test
    public void fillRenderingModeText_outputIsFilledWithNoOutline() throws Exception
    {
        String expectedTextFillColor1 = "color:#8000fe;"; //this differs between different platforms (why?)
        String expectedTextFillColor2 = "color:#8000ff;";

        Document html = TestUtils.parseWithPdfDomTree(testPath + "text-rendering-mode-fill.pdf");
        Element text = html.select("div[class=p]").first();

        Assert.assertThat(text.attr("style"), anyOf(containsString(expectedTextFillColor1), containsString(expectedTextFillColor2)));
        Assert.assertThat(text.attr("style"), not(containsString("webkit-text-stroke")));
    }

    @Test
    public void strokeRenderingModeText_outputTextIsOutlinedAndNotFilled() throws Exception
    {
        Document html = TestUtils.parseWithPdfDomTree(testPath + "text-rendering-mode-stroke.pdf");
        Element text = html.select("div[class=p]").first();

        Assert.assertThat("Text element should not have fill color.",
                text.attr("style"), containsString("color:" + BoxStyle.transparentColor));

        Assert.assertThat("Text element is missing stroke color.",
                text.attr("style"), containsString("webkit-text-stroke: #ff00ff"));
    }

    @Test
    public void strokeAndFillRenderingModeText() throws Exception
    {
        Document html = TestUtils.parseWithPdfDomTree(testPath + "text-rendering-mode-stroke-and-fill.pdf");
        Element text = html.select("div[class=p]").first();

        Assert.assertThat("Text element is missing fill color.",
                text.attr("style"), Matchers.either(containsString("color:#9af0e7")).or(containsString("color:#9af0e6"))); //allow some rounding while transforming from HSV

        Assert.assertThat("Text element is missing stroke color.",
                text.attr("style"), containsString("webkit-text-stroke: #ff00ff"));
    }

    @Test
    public void givenMultiPagePdf_renderOnlyFirstPage_outputHtmlOnlyHasFirstPage() throws Exception
    {
        Document htmlDoc = convertWithPageRange(testPath + "3-page-document.pdf", 0, 1);
        String htmlText = htmlDoc.html();

        Assert.assertThat(htmlText, containsString("#1"));

        Assert.assertThat(htmlText, not(containsString("#2")));
        Assert.assertThat(htmlText, not(containsString("#3")));
    }

    public static Document convertWithPageRange(String resource, int start, int end) throws Exception
    {
        InputStream is = TestUtils.class.getResourceAsStream(resource);
        Document doc = parseWithPdfDomTree(is, start, end);
        is.close();

        if (getOutputEnabled()) {
            File debugOutFile = new File(resource.replace(".pdf", ".html").replaceAll(".*/", ""));
            FileUtils.write(debugOutFile, doc.outerHtml());
        }
        return doc;
    }

    public static Document parseWithPdfDomTree(InputStream is, int start, int end) throws Exception
    {
        PDDocument pdf = PDDocument.load(is);
        PDFDomTree parser = new PDFDomTree();
        parser.setStartPage(start);
        parser.setEndPage(end);

        Writer output = new StringWriter();
        parser.writeText(pdf, output);
        pdf.close();
        String htmlOutput = output.toString();

        return Jsoup.parse(htmlOutput);
    }
}
