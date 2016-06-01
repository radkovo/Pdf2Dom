package org.fit.pdfdom;

import java.io.File;

public class PDFDomTreeConfig
{
    private FontExtractMode fontMode;
    private File fontExtractDirectory;

    public static PDFDomTreeConfig createDefaultConfig() {
        PDFDomTreeConfig config = new PDFDomTreeConfig();
        config.fontMode = FontExtractMode.EMBED_BASE64;

        return config;
    }

    private PDFDomTreeConfig() {
    }

    public FontExtractMode getFontMode()
    {
        return fontMode;
    }

    public void setFontMode(FontExtractMode fontMode)
    {
        this.fontMode = fontMode;
    }

    public File getFontExtractDirectory()
    {
        return fontExtractDirectory;
    }

    public void setFontExtractDirectory(File fontExtractDirectory)
    {
        this.fontExtractDirectory = fontExtractDirectory;
    }

    public enum FontExtractMode {
        EMBED_BASE64,
        SAVE_TO_DIR,
        IGNORE_FONTS
    }
}
