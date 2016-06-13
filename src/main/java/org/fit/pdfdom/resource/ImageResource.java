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

package org.fit.pdfdom.resource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageResource extends HtmlResource
{
    private final BufferedImage image;

    public ImageResource(String name, BufferedImage image)
    {
        super(name);

        this.image = image;
    }

    public byte[] getData() throws IOException
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", buffer);

        return buffer.toByteArray();
    }

    public String getFileEnding()
    {
        return "png";
    }

    public String getMimeType()
    {
        return "image/png";
    }
}
