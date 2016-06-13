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

import java.io.IOException;

public class EmbedAsBase64Handler implements HtmlResourceHandler
{
    public String handleResource(HtmlResource resource) throws IOException
    {
        char[] base64Data = new char[0];
        byte[] data = resource.getData();
        if (data != null)
            base64Data = Base64Coder.encode(data);

        return String.format("data:%s;base64,%s", resource.getMimeType(), new String(base64Data));
    }
}
