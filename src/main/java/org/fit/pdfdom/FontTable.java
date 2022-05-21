/**
 *
 */
package org.fit.pdfdom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.fit.pdfdom.resource.HtmlResource;
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
public class FontTable
{
    private static Logger log = LoggerFactory.getLogger(FontTable.class);
    private static Pattern fontFamilyRegex = Pattern.compile("([^+^-]*)[+-]([^+]*)");

    private List<Entry> entries = new ArrayList<Entry>();

    public void addEntry(PDFont font)
    {
        FontTable.Entry entry = get(font);

        if (entry == null)
        {
            String fontName = font.getName();
            String family = findFontFamily(fontName);

            String usedName = nextUsedName(family);
            FontTable.Entry newEntry = new FontTable.Entry(font.getName(), usedName, font);

            if (newEntry.isEntryValid())
                add(newEntry);
        }
    }

    public Entry get(PDFont find)
    {
        for (Entry entryOn : entries)
        {
            if (entryOn.equalToPDFont(find))
                return entryOn;
        }

        return null;
    }

    public List<Entry> getEntries()
    {
        return new ArrayList<Entry>(entries);
    }

    public String getUsedName(PDFont font)
    {
        FontTable.Entry entry = get(font);
        if (entry == null)
            return null;
        else
            return entry.usedName;
    }

    protected String nextUsedName(String fontName)
    {
        int i = 1;
        String usedName = fontName;
        while (isNameUsed(usedName))
        {
            usedName = fontName + i;
            i++;
        }

        return usedName;
    }

    protected boolean isNameUsed(String name)
    {
        for (Entry entryOn : entries)
        {
            if (entryOn.usedName.equals(name))
                return true;
        }

        return false;
    }

    protected void add(Entry entry) {
        entries.add(entry);
    }

    private String findFontFamily(String fontName)
    {
        // pdf font family name isn't always populated so have to find ourselves from full name
        String familyName = fontName;

        Matcher familyMatcher = fontFamilyRegex.matcher(fontName);
        if (familyMatcher.find())
            // currently tacking on weight/style too since we don't generate html for it yet
            // and it's helpful for debugugging
            familyName = familyMatcher.group(1) + " " + familyMatcher.group(2);

        // browsers will barf if + in family name
        return familyName.replaceAll("[+]"," ");
    }

    public class Entry extends HtmlResource
    {
        public String fontName;
        public String usedName;
        public PDFontDescriptor descriptor;

        private PDFont baseFont;
        private byte[] cachedFontData;
        private String mimeType = "x-font-truetype";
        private String fileEnding;

        public Entry(String fontName, String usedName, PDFont font)
        {
            super(fontName);

            this.fontName = fontName;
            this.usedName = usedName;
            this.descriptor = font.getFontDescriptor();
            this.baseFont = font;
        }

        public byte[] getData() throws IOException
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
                fontData = getData();
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
            byte[] fvFontData = null;
            try {
                FVFont font = FontVerter.readFont(fontData);
                fvFontData = tryNormalizeFVFont(font);
            } catch (IOException e) {
                log.warn("Unsupported FontFile found. Normalisation will be skipped.");
            }

            if (fvFontData != null && fvFontData.length != 0)
                fontData = fvFontData;

            return fontData;
        }

        private byte[] loadType0TtfDescendantFont() throws IOException
        {
            mimeType = "application/x-font-truetype";
            fileEnding = "ttf";
            try
            {
                FVFont font = PdfFontExtractor.convertType0FontToOpenType((PDType0Font) baseFont);
                byte[] fontData = tryNormalizeFVFont(font);

                if (fontData.length != 0)
                    return fontData;
            } catch (Exception ex)
            {
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
                if (!font.isValid())
                    font.normalize();

                return font.getData();
            } catch (Exception ex)
            {
                log.warn("Error normalizing font '{}' Message: {} {}",
                        fontName, ex.getMessage(), ex.getClass());
            }

            return new byte[0];
        }

        public boolean equalToPDFont(PDFont compare) {
            // Appears you can have two different fonts with the same actual font name since text position font
            // references go off a seperate dict lookup name. PDFBox doesn't include the lookup name with the
            // PDFont, so might have to submit a change there to be really sure fonts are indeed the same.
            return compare.getName().equals(baseFont.getName()) &&
                    compare.getType().equals(baseFont.getType()) &&
                    compare.getSubType().equals(baseFont.getSubType());
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

        public String getMimeType() {
            return mimeType;
        }
    }
}
