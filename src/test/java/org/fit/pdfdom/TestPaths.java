/*
 *  Copyright (c) Matthew Abboud 2016
 *
 *  Pdf2Dom is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Pdf2Dom is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with CSSBox. If not, see <http://www.gnu.org/licenses/>.
 */

package org.fit.pdfdom;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;
import org.mabb.gfxassert.geom.ShapeSubset;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import static org.mabb.gfxassert.GfxAssertMatchers.containsColor;
import static org.mabb.gfxassert.geom.ShapeSubset.*;

public class TestPaths
{
    @Test
    public void givenPdfFilledNonRectPath_whenConverted_thenImageCreatedForPath() throws Exception
    {
        Document html = TestUtils.parseWithPdfDomTree("/shapes/arrow-path.pdf");
        Element image = html.select("img").first();

        Assert.assertNotNull(image);
    }

    @Test
    public void givenPdfFilledNonRectPath_whenConverted_thenImageFilledWithCorrectColor() throws Exception
    {
        Document html = TestUtils.parseWithPdfDomTree("/shapes/arrow-path.pdf");
        Element htmlImage = html.select("img").first();

        String base64Data = htmlImage.attr("src").replace("data:image/png;base64,", "");
        byte[] imageData = Base64.decodeBase64(base64Data);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));

        Color fillColor = new Color(217, 217, 217);
        Assert.assertThat(image, containsColor(fillColor).in(bottomArea()));
        Assert.assertThat(image, containsColor(fillColor).in(topArea()));
    }
}
