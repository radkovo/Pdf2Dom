/**
 *
 */
package org.fit.pdfdom;

import java.io.IOException;
import java.util.HashMap;

import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.mabb.fontverter.FVFont;
import org.mabb.fontverter.FontVerter;
import org.mabb.fontverter.pdf.PdfFontExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A table for storing entries about the embedded fonts and their usage.
 *
 * @author burgetr
 */
public class FontTable extends HashMap<String, FontTable.Entry>
{
    Logger log = LoggerFactory.getLogger(FontTable.class);
    private static final long serialVersionUID = 1L;
    private static int nextNameIndex = 1;

    public void addEntry(String fontName, PDFontDescriptor descriptor)
    {
        FontTable.Entry entry = get(fontName);
        if (entry == null)
        {
            String usedName = nextUsedName();
            FontTable.Entry newEntry = new FontTable.Entry(fontName, usedName, descriptor);

            if(newEntry.isEntryValid())
                put(fontName, newEntry);
        }
    }

    public void addEntry(String fontName, PDFont font)
    {
        FontTable.Entry entry = get(fontName);
        if (entry == null)
        {
            String usedName = nextUsedName();
            FontTable.Entry newEntry = new FontTable.Entry(fontName, usedName, font);

            if(newEntry.isEntryValid())
                put(fontName, newEntry);
        }
    }

    public String getUsedName(String fontName)
    {
        FontTable.Entry entry = get(fontName);
        if (entry == null)
            return null;
        else
            return entry.usedName;
    }

    protected String nextUsedName()
    {
        return "F" + (nextNameIndex++);
    }

    public class Entry
    {
        public String fontName;
        public String usedName;
        public PDFontDescriptor descriptor;

        private PDFont baseFont;
        private byte[] cachedFontData;
        private String mimeType = "x-font-truetype";
        private String fileEnding;

        public Entry(String fontName, String usedName, PDFontDescriptor descriptor)
        {
            this.fontName = fontName;
            this.usedName = usedName;
            this.descriptor = descriptor;
        }

        public Entry(String fontName, String usedName, PDFont font)
        {
            this.fontName = fontName;
            this.usedName = usedName;
            this.descriptor = font.getFontDescriptor();
            this.baseFont = font;
        }

        public String getDataURL() throws IOException
        {
            char[] cdata = new char[0];
            if (getFontData() != null)
                cdata = Base64Coder.encode(getFontData());

            return String.format("data:%s;base64,%s", mimeType, new String(cdata));
        }

        public byte[] getFontData() throws IOException
        {
            if (cachedFontData != null)
                return cachedFontData;

            if (descriptor.getFontFile2() != null && baseFont instanceof PDType0Font)
                cachedFontData = loadType0TtfDescendantFont();
            else if (descriptor.getFontFile2() != null)
                cachedFontData = loadTrueTypeFont(descriptor.getFontFile2());
            else if (descriptor.getFontFile() != null)
                cachedFontData = loadType1Font(descriptor.getFontFile());
            else if (descriptor.getFontFile3() != null)
                // FontFile3 docs say any font type besides TTF/OTF or Type 1..
                cachedFontData = loadOtherTypeFont(descriptor.getFontFile3());

            return cachedFontData;
        }

        public boolean isEntryValid() {
            byte[] fontData = new byte[0];
            try
            {
                fontData = getFontData();
            } catch (IOException e)
            {
                log.warn("Error loading font '{}' Message: {} {}", fontName, e.getMessage(), e.getClass());
            }

            return fontData != null && fontData.length != 0;
        }

        private byte[] loadTrueTypeFont(PDStream fontFile) throws IOException
        {
            // could convert to WOFF though for optimal html output instead.
            mimeType = "application/x-font-truetype";
            fileEnding = "otf";

            byte[] fontData = fontFile.toByteArray();

            FVFont font = FontVerter.readFont(fontData);
            byte[] fvFontData = tryNormalizeFVFont(font);
            if (fvFontData.length != 0)
                fontData = fvFontData;

            return fontData;
        }

        private byte[] loadType0TtfDescendantFont() throws IOException
        {
            mimeType = "application/x-font-truetype";
            fileEnding = "otf";
            try
            {
                FVFont font = PdfFontExtractor.convertType0FontToOpenType((PDType0Font) baseFont);
                byte[] fontData = tryNormalizeFVFont(font);

                if (fontData.length != 0)
                    return fontData;
            } catch (Exception ex)
            {
                ex.printStackTrace();
                log.warn("Error loading type 0 with ttf descendant font '{}' Message: {} {}",
                        fontName, ex.getMessage(), ex.getClass());

            }

            return descriptor.getFontFile2().toByteArray();
        }

        private byte[] loadType1Font(PDStream fontFile) throws IOException
        {
            log.warn("Type 1 fonts are not supported by Pdf2Dom.");
            return new byte[0];
        }

        private byte[] loadOtherTypeFont(PDStream fontFile) throws IOException
        {
            // Likley Bare CFF which needs to be converted to a font supported by browsers, can be
            // other font types which are not yet supported.
            try
            {
                FVFont font = FontVerter.convertFont(fontFile.toByteArray(), FontVerter.FontFormat.WOFF1);
                mimeType = "application/x-font-woff";
                fileEnding = font.getProperties().getFileEnding();

                return font.getData();
            } catch (Exception ex) {
                log.error("Issue converting Bare CFF font or the font type is not supportedby Pdf2Dom, " +
                        "Font: {} Exception: {} {}", fontName, ex.getMessage(), ex.getClass());

                // don't barf completley for font conversion issue, html will still be useable without.
                return new byte[0];
            }
        }

        private byte[] tryNormalizeFVFont(FVFont font)
        {
            try
            {
                // browser validation can fail for many TTF fonts from pdfs
                if (!font.doesPassStrictValidation())
                {
                    font.normalize();
                    return font.getData();
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }

            return new byte[0];
        }

        @Override
        public int hashCode()
        {
            return fontName.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Entry other = (Entry) obj;
            if (!getOuterType().equals(other.getOuterType())) return false;
            if (fontName == null)
            {
                if (other.fontName != null) return false;
            }
            else if (!fontName.equals(other.fontName)) return false;
            return true;
        }

        public String getFileEnding()
        {
            return fileEnding;
        }

        private FontTable getOuterType()
        {
            return FontTable.this;
        }

    }

}
