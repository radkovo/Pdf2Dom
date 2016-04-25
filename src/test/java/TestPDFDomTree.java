import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.fit.pdfdom.BoxStyle;
import org.fit.pdfdom.PDFDomTree;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

public class TestPDFDomTree
{
    private static final String testPath = "src/test/files/";

    @Test
    public void neitherRenderingModeText_outputTextIsInvisible() throws Exception
    {
        File testPdf = new File(testPath + "text-rendering-mode-neither.pdf");

        Document html = convertToHtml(testPdf);
        Element text = html.select("div[class=p]").first();

        Assert.assertThat("Text element should be invisible.",
                text.attr("style"), containsString("color:" + BoxStyle.transparentColor));
    }

    @Test
    public void fillRenderingModeText_outputIsFilledWithNoOutline() throws Exception
    {
        String expectedTextFillColor = "color:#8000fe;";
        File testPdf = new File(testPath + "text-rendering-mode-fill.pdf");

        Document html = convertToHtml(testPdf);
        Element text = html.select("div[class=p]").first();

        Assert.assertThat(text.attr("style"), containsString(expectedTextFillColor));
        Assert.assertThat(text.attr("style"), not(containsString("webkit-text-stroke")));
    }

    @Test
    public void strokeRenderingModeText_outputTextIsOutlinedAndNotFilled() throws Exception
    {
        File testPdf = new File(testPath + "text-rendering-mode-stroke.pdf");

        Document html = convertToHtml(testPdf);
        Element text = html.select("div[class=p]").first();

        Assert.assertThat("Text element should not have fill color.",
                text.attr("style"), containsString("color:" + BoxStyle.transparentColor));

        Assert.assertThat("Text element is missing stroke color.",
                text.attr("style"), containsString("webkit-text-stroke: #ff00ff"));
    }

    @Test
    public void strokeAndFillRenderingModeText() throws Exception
    {
        File testPdf = new File(testPath + "text-rendering-mode-stroke-and-fill.pdf");

        Document html = convertToHtml(testPdf);
        Element text = html.select("div[class=p]").first();

        Assert.assertThat("Text element is missing fill color.",
                text.attr("style"), containsString("color:#9af0e7"));

        Assert.assertThat("Text element is missing stroke color.",
                text.attr("style"), containsString("webkit-text-stroke: #ff00ff"));
    }

    public static Document convertToHtml(File file)
            throws IOException, ParserConfigurationException, TransformerException
    {
        PDDocument pdf = PDDocument.load(file);
        PDFDomTree parser = new PDFDomTree();

        Writer output = new StringWriter();
        parser.writeText(pdf, output);
        pdf.close();
        String htmlOutput = output.toString();
        // File debugOutFile = new File(file.getName().replace(".pdf", ".html"));
        // FileUtils.write(debugOutFile, htmlOutput);

        return Jsoup.parse(htmlOutput);
    }
}
