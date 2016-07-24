/**
 * PDFToHTML.java
 * (c) Radek Burget, 2011
 *
 * Pdf2Dom is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * Pdf2Dom is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public License
 * along with CSSBox. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Created on 19.9.2011, 13:34:54 by burgetr
 */
package org.fit.pdfdom;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.fit.pdfdom.resource.HtmlResourceHandler;
import org.fit.pdfdom.resource.IgnoreResourceHandler;
import org.fit.pdfdom.resource.SaveResourceToDirHandler;

/**
 * @author burgetr
 *
 */
public class PDFToHTML
{

    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            System.out.println("Usage: PDFToHTML <infile> [<outfile>] [<options>]");
            System.out.println("Options: ");
            System.out.println("-fm=[mode] Font handler mode. [mode] = EMBED_BASE64, SAVE_TO_DIR, IGNORE");
            System.out.println("-fdir=[path] Directory to extract fonts to. [path] = font extract directory ie dir/my-font-dir");
            System.out.println();
            System.out.println("-im=[mode] Image handler mode. [mode] = EMBED_BASE64, SAVE_TO_DIR, IGNORE");
            System.out.println("-idir=[path] Directory to extract images to. [path] = image extract directory ie dir/my-image-dir");

            System.exit(1);
        }
        
        String infile = args[0];
        String outfile;
        if (args.length > 1 && !args[1].startsWith("-"))
            outfile = args[1];
        else
        {
            String base = args[0];
            if (base.toLowerCase().endsWith(".pdf"))
                base = base.substring(0, base.length() - 4);
            outfile = base + ".html";
        }

        PDFDomTreeConfig config = parseOptions(args);

        PDDocument document = null;
        try
        {
            document = PDDocument.load(new File(infile));
            PDFDomTree parser = new PDFDomTree(config);
            //parser.setDisableImageData(true);
            Writer output = new PrintWriter(outfile, "utf-8");
            parser.writeText(document, output);
            output.close();
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            if( document != null )
            {
                try
                {
                    document.close();
                } catch (IOException e) { 
                    System.err.println("Error: " + e.getMessage());
                    //e.printStackTrace();
                }
            }
        }
    }

    private static PDFDomTreeConfig parseOptions(String[] args)
    {
        PDFDomTreeConfig config = PDFDomTreeConfig.createDefaultConfig();

        List<CommandLineFlag> flags = parseFlags(args);
        for (CommandLineFlag flagOn : flags)
        {
            if (flagOn.flagName.equals("fm"))
            {
                HtmlResourceHandler handler = createResourceHandlerFor(flagOn.value);
                config.setFontHandler(handler);
            } else if (flagOn.flagName.equals("fdir"))
                config.setFontHandler(new SaveResourceToDirHandler(new File(flagOn.value)));

            else if (flagOn.flagName.equals("im"))
            {
                HtmlResourceHandler handler = createResourceHandlerFor(flagOn.value);
                config.setImageHandler(handler);
            } else if (flagOn.flagName.equals("idir"))
                config.setImageHandler(new SaveResourceToDirHandler(new File(flagOn.value)));
        }

        return config;
    }

    private static HtmlResourceHandler createResourceHandlerFor(String value)
    {
        HtmlResourceHandler handler = PDFDomTreeConfig.embedAsBase64();
        if (value.equalsIgnoreCase("EMBED_BASE64"))
            handler = PDFDomTreeConfig.embedAsBase64();
        else if (value.equalsIgnoreCase("SAVE_TO_DIR"))
            handler = new SaveResourceToDirHandler();
        else if (value.equalsIgnoreCase("IGNORE"))
            handler = new IgnoreResourceHandler();

        return handler;
    }

    private static List<CommandLineFlag> parseFlags(String[] args)
    {
        List<CommandLineFlag> flags = new ArrayList<CommandLineFlag>();
        for (String argOn : args)
        {
            if (argOn.startsWith("-"))
                flags.add(CommandLineFlag.parse(argOn));
        }
        return flags;
    }

    private static class CommandLineFlag
    {
        public String flagName;
        public String value = "";

        public static CommandLineFlag parse(String argOn)
        {
            CommandLineFlag flag = new CommandLineFlag();
            String[] flagSplit = argOn.split("=");
            flag.flagName = flagSplit[0].replace("-", "");
            if (flagSplit.length > 1)
                flag.value = flagSplit[1].replace("=", "");

            return flag;
        }
    }
}
