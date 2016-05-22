/**
 * 
 */
package org.fit.pdfdom;

import java.io.IOException;
import java.util.HashMap;

import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;

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
        
        public Entry(String fontName, String usedName, PDFontDescriptor descriptor)
        {
            this.fontName = fontName;
            this.usedName = usedName;
            this.descriptor = descriptor;
        }
        
        public String getDataURL() throws IOException
        {
            final char[] cdata = Base64Coder.encode(getFontData());
            return "data:application/x-font-truetype;base64," + new String(cdata);
        }
        
        public byte[] getFontData() throws IOException
        {
            return descriptor.getFontFile2().toByteArray();
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
