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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class SaveResourceToDirHandler implements HtmlResourceHandler
{
    public static final String DEFAULT_RESOURCE_DIR = "resources/";

    private final File directory;
    private List<String> writtenFileNames = new LinkedList<String>();

    public SaveResourceToDirHandler()
    {
        directory = null;
    }

    public SaveResourceToDirHandler(File directory)
    {
        this.directory = directory;
    }

    public String handleResource(HtmlResource resource) throws IOException
    {
        String dir = DEFAULT_RESOURCE_DIR;
        if (directory != null)
            dir = directory.getPath() + "/";

        String fileName = findNextUnusedFileName(resource.getName());
        String resourcePath = dir + fileName + "." + resource.getFileEnding();

        File file = new File(resourcePath);
        FileUtils.writeByteArrayToFile(file, resource.getData());

        writtenFileNames.add(fileName);

        return resourcePath;
    }

    private String findNextUnusedFileName(String fileName)
    {
        int i = 1;
        String usedName = fileName;
        while (writtenFileNames.contains(usedName)) {
            usedName = fileName + i;
            i++;
        }

        return usedName;
    }
}
