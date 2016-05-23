/**
 *
 */
package org.fit.pdfdom;

import java.io.IOException;
import java.util.HashMap;

import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.fontverter.FontAdapter;
import org.fontverter.FontVerter;

/**
 * A table for storing entries about the embedded fonts and their usage.
 *
 * @author burgetr
 */
public class FontTable extends HashMap<String, FontTable.Entry>
{
    private static final long serialVersionUID = 1L;
    private static int nextNameIndex = 1;

    public void addEntry(String fontName, PDFontDescriptor descriptor)
    {
        FontTable.Entry entry = get(fontName);
        if (entry == null)
        {
            String usedName = nextUsedName();
            put(fontName, new FontTable.Entry(fontName, usedName, descriptor));
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

        private byte[] cachedFontData;
        private String mimeType = "x-font-truetype";

        public Entry(String fontName, String usedName, PDFontDescriptor descriptor)
        {
            this.fontName = fontName;
            this.usedName = usedName;
            this.descriptor = descriptor;
        }

        public String getDataURL() throws IOException
        {
            final char[] cdata = Base64Coder.encode(getFontData());
            return String.format("data:application/%s;base64,%s", mimeType, new String(cdata));
        }

        public byte[] getFontData() throws IOException
        {
            if (cachedFontData != null)
                return cachedFontData;

            if (descriptor.getFontFile2() != null)
                cachedFontData = loadTrueTypeFont(descriptor.getFontFile2());
            else if (descriptor.getFontFile() != null)
                cachedFontData = loadType1Font(descriptor.getFontFile());
            else if (descriptor.getFontFile3() != null)
                // FontFile3 docs say any font type besides TTF/OTF or Type 1..
                cachedFontData = loadOtherTypeFont(descriptor.getFontFile3());

            return cachedFontData;
        }

        private byte[] loadTrueTypeFont(PDStream fontFile) throws IOException
        {
            // true type can be used as is by browsers, could convert to WOFF though for optimal output.
            mimeType = "x-font-truetype";
            return fontFile.toByteArray();
        }

        private byte[] loadType1Font(PDStream fontFile) throws IOException
        {
            throw new IOException("Type 1 font's are not supported by Pdf2Dom");
        }

        private byte[] loadOtherTypeFont(PDStream fontFile) throws IOException
        {
            // Likley Bare CFF which needs to be converted to a font supported by browsers, but possibly other font types
            FontAdapter font = FontVerter.convertFont(fontFile.toByteArray(), FontVerter.FontFormat.WOFF1);
            mimeType = "x-font-woff";

            return font.getData();
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

        private FontTable getOuterType()
        {
            return FontTable.this;
        }

    }

}
