/**
 * HtmlDivLine.java
 *
 * Created on 13. 10. 2022, 11:33:54 by burgetr
 */
package org.fit.pdfdom;

/**
 * Maps input line to an HTML div rectangle, since HTML does not support standard lines.
 */
public class HtmlDivLine
{
    private final float x1;
    private final float y1;
    private final float x2;
    private final float y2;
    private final float width;
    private final float height;
    private final float lineWidth;
    //horizontal or vertical lines are treated separately (no rotations used)
    private final boolean horizontal;
    private final boolean vertical;

    public HtmlDivLine(float x1, float y1, float x2, float y2, float lineWidth)
    {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.lineWidth = lineWidth;
        this.width = Math.abs(x2 - x1);
        this.height = Math.abs(y2 - y1);
        this.horizontal = (height < 0.5f);
        this.vertical = (width < 0.5f);
    }

    public float getHeight()
    {
        return vertical ? height : 0;
    }

    public float getWidth()
    {
        if (vertical)
            return 0;
        else if (horizontal)
            return width;
        else
            return distanceFormula(x1, y1, x2, y2);
    }

    public float getLeft()
    {
        if (horizontal || vertical)
            return Math.min(x1, x2);
        else
            return Math.abs((x2 + x1) / 2) - getWidth() / 2;
    }

    public float getTop()
    {
        if (horizontal || vertical)
            return Math.min(y1, y2);
        else
            // after rotation top left will be center of line so find the midpoint and correct for the line to border transform
            return Math.abs((y2 + y1) / 2) - (getLineStrokeWidth() + getHeight()) / 2;
    }

    public double getAngleDegrees()
    {
        if (horizontal || vertical)
            return 0;
        else
            return Math.toDegrees(Math.atan((y2 - y1) / (x2 - x1)));
    }

    public float getLineStrokeWidth()
    {
        float lw = lineWidth;
        if (lw < 0.5f)
            lw = 0.5f;
        return lw;
    }

    public boolean isVertical()
    {
        return vertical;
    }

    public String getBorderSide()
    {
        return vertical ? "border-right" : "border-bottom";
    }
    
    private float distanceFormula(float x1, float y1, float x2, float y2)
    {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }
}
