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

import org.fit.pdfdom.resource.EmbedAsBase64Handler;
import org.fit.pdfdom.resource.HtmlResourceHandler;
import org.fit.pdfdom.resource.IgnoreResourceHandler;
import org.fit.pdfdom.resource.SaveResourceToDirHandler;

import java.io.File;

public class PDFDomTreeConfig
{
    private HtmlResourceHandler imageHandler;
    private HtmlResourceHandler fontHandler;

    public static PDFDomTreeConfig createDefaultConfig() {
        PDFDomTreeConfig config = new PDFDomTreeConfig();
        config.setFontHandler(embedAsBase64());
        config.setImageHandler(embedAsBase64());

        return config;
    }

    public static HtmlResourceHandler embedAsBase64() {
        return new EmbedAsBase64Handler();
    }

    public static HtmlResourceHandler saveToDirectory(File directory) {
        return new SaveResourceToDirHandler(directory);
    }

    public static HtmlResourceHandler ignoreResource() {
        return new IgnoreResourceHandler();
    }

    private PDFDomTreeConfig() {
    }

    public HtmlResourceHandler getImageHandler()
    {
        return imageHandler;
    }

    public void setImageHandler(HtmlResourceHandler imageHandler)
    {
        this.imageHandler = imageHandler;
    }

    public HtmlResourceHandler getFontHandler()
    {
        return fontHandler;
    }

    public void setFontHandler(HtmlResourceHandler fontHandler)
    {
        this.fontHandler = fontHandler;
    }
}
