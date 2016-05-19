package org.fit.pdfdom;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

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
        String expectedTextFillColor = "color:#8000ff;";

        Document html = TestUtils.parseWithPdfDomTree(testPath + "text-rendering-mode-fill.pdf");
        Element text = html.select("div[class=p]").first();

        Assert.assertThat(text.attr("style"), containsString(expectedTextFillColor));
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
                text.attr("style"), containsString("color:#9af0e7"));

        Assert.assertThat("Text element is missing stroke color.",
                text.attr("style"), containsString("webkit-text-stroke: #ff00ff"));
    }

}
