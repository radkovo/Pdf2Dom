package org.fit.pdfdom;

import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.File;

public class TestFonts
{
    @Test
    public void bareCff() throws Exception
    {
        Document html = TestUtils.parseWithPdfDomTree("/fonts/bare-cff.pdf");
    }
}
