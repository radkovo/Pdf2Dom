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

import org.fit.pdfdom.resource.SaveResourceToDirHandler;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.fit.pdfdom.TestUtils.getOutputEnabled;

public class TestImages
{
    private static final String EXTRACT_DIR = "image-extract-dir";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void givenPdfWithImages_whenConvertedWithSaveToDirHandler_thenFirstImageSavedToDir() throws Exception
    {
        PDFDomTreeConfig config = PDFDomTreeConfig.createDefaultConfig();
        config.setImageHandler(new SaveResourceToDirHandler(getExtractDir()));

        TestUtils.parseWithPdfDomTree("images.pdf", config);
        File tempFontFile = new File(getFullExtractPath() + "Untitled.png");

        Assert.assertTrue(tempFontFile.exists());
    }

    @Test
    public void givenPdfWithImages_whenConvertedWithSaveToDirHandler_thenSecondImageHasDifferentFileName()
            throws Exception
    {
        PDFDomTreeConfig config = PDFDomTreeConfig.createDefaultConfig();
        config.setImageHandler(new SaveResourceToDirHandler(getExtractDir()));

        TestUtils.parseWithPdfDomTree("images.pdf", config);
        File tempFontFile = new File(getFullExtractPath() + "Untitled1.png");

        Assert.assertTrue(tempFontFile.exists());
    }

    @Test
    public void givenPdfWithImagesSameFileNamesCreated_whenConvertedWithSaveToDirHandler_thenDoesNotGetStuckInInfiniteLoop() throws Exception
    {
        PDFDomTreeConfig config = PDFDomTreeConfig.createDefaultConfig();
        config.setImageHandler(new SaveResourceToDirHandler(getExtractDir()));

        TestUtils.parseWithPdfDomTree("/HorariosMadrid_Segovia.pdf", config);
        File tempFontFile = new File(getFullExtractPath() + "PDF Document.png");
        File tempFontFile2 = new File(getFullExtractPath() + "PDF Document1.png");

        Assert.assertTrue(tempFontFile.exists());
        Assert.assertTrue(tempFontFile2.exists());
    }

    private File getExtractDir() throws IOException
    {
        return getOutputEnabled() ? new File(EXTRACT_DIR) : folder.newFolder(EXTRACT_DIR);
    }

    private String getFullExtractPath() throws IOException
    {
        return getOutputEnabled() ? EXTRACT_DIR + "/" : folder.getRoot().getPath() + "/" + EXTRACT_DIR + "/";
    }
}
