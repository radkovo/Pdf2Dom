
package org.fit.pdfdom;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDCIDFont;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.util.TextPosition;

public class TextMetrics
{
    private float x, baseline, width, height, pointSize, descent, ascent, fontSize;
    private PDFont font;

    public TextMetrics(TextPosition tp)
    {
        x = tp.getXDirAdj();
        baseline = tp.getYDirAdj();
        font = tp.getFont();
        width = tp.getWidth();
        height = tp.getHeight();
        pointSize = tp.getFontSizeInPt();
        fontSize = tp.getYScale();
        ascent = getAscent();
        descent = getDescent();
    }

    public void append(TextPosition tp)
    {
        width += tp.getX() - (x + width) + tp.getWidth();
        height = Math.max(height, tp.getHeight());
        ascent = Math.max(ascent, getAscent(tp.getFont(), tp.getYScale()));
        descent = Math.min(descent, getDescent(tp.getFont(), tp.getYScale()));
    }
    
    public float getX()
    {
        return x;
    }

    public float getTop()
    {
        if (ascent != 0)
            return baseline - ascent;
        else
            return baseline - getBoundingBoxAscent();
    }

    public float getBottom()
    {
        if (descent != 0)
            return baseline - descent;
        else
            return baseline - getBoundingBoxDescent();
    }

    public float getBaseline()
    {
        return baseline;
    }

    public float getAscent()
    {
        return getAscent(font, fontSize);
    }

    public float getDescent()
    {
        return getDescent(font, fontSize);
    }

    public float getBoundingBoxDescent()
    {
        return getBoundingBoxDescent(font, fontSize);
    }

    public float getBoundingBoxAscent()
    {
        return getBoundingBoxAscent(font, fontSize);
    }

    public static float getBoundingBoxDescent(PDFont font, float fontSize)
    {
        try
        {
            PDRectangle bBox = font.getFontBoundingBox();
            float boxDescent = bBox.getLowerLeftY();
            return (boxDescent / 1000) * fontSize;
        } catch (IOException e) {
        }
        return 0.0f;
    }

    public static float getBoundingBoxAscent(PDFont font, float fontSize)
    {
        try
        {
            PDRectangle bBox = font.getFontBoundingBox();
            float boxAscent = bBox.getUpperRightY();
            return (boxAscent / 1000) * fontSize;
        } catch (IOException e) {
        }
        return 0.0f;
    }

    private static float getAscent(PDFont font, float fontSize)
    {
        try
        {
            if (font instanceof PDSimpleFont)
            {
                PDSimpleFont simpleFont = (PDSimpleFont) font;
                return (simpleFont.getFontDescriptor().getAscent() / 1000) * fontSize;
            }
            else if (font instanceof PDCIDFont)
            {
                PDCIDFont cidFont = (PDCIDFont) font;
                return (cidFont.getFontDescriptor().getAscent() / 1000) * fontSize;
            }
        } catch (Exception e) {
        }
        return 0.0f;
    }

    private static float getDescent(PDFont font, float fontSize)
    {
        try
        {
            if (font instanceof PDSimpleFont)
            {
                PDSimpleFont simpleFont = (PDSimpleFont) font;
                return (-Math.abs(simpleFont.getFontDescriptor().getDescent()) / 1000) * fontSize;
            }
            else if (font instanceof PDCIDFont)
            {
                PDCIDFont cidFont = (PDCIDFont) font;
                return (-Math.abs(cidFont.getFontDescriptor().getDescent()) / 1000) * fontSize;
            }
        } catch (Exception e)
        {
        }
        return 0.0f;
    }

    public float getWidth()
    {
        return width;
    }

    public float getHeight()
    {
        return getBottom() - getTop();
    }

    public float getPointSize()
    {
        return pointSize;
    }


}