
package org.fit.pdfdom;

import java.io.IOException;

import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.text.TextPosition;

public class TextMetrics
{
    private float x, baseline, width, height, pointSize, descent, ascent, fontSize;
    private PDFont font;

    public TextMetrics(TextPosition tp)
    {
        x = tp.getX();
        baseline = tp.getY();
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
        final float descent = getDescent(font, fontSize);
        return descent > 0 ? -descent : descent; //positive descent is not allowed
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
            BoundingBox bBox = font.getBoundingBox();
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
            BoundingBox bBox = font.getBoundingBox();
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
            return (font.getFontDescriptor().getAscent() / 1000) * fontSize;
        } catch (Exception e) {
        }
        return 0.0f;
    }

    private static float getDescent(PDFont font, float fontSize)
    {
        try
        {
            return (font.getFontDescriptor().getDescent() / 1000) * fontSize;
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