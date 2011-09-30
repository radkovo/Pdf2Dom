/**
 * RectPath.java
 * (c) Radek Burget, 2011
 *
 * PdfDOM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * PdfDOM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public License
 * along with CSSBox. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Created on 23.9.2011, 12:44:27 by burgetr
 */
package org.fit.pdfdom;

/**
 * A rectangle element of a graphics path.
 * @author burgetr
 */
public class RectPath
{
    private float x, y, width, height;

    public RectPath(float x, float y, float width, float height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * @return the x
     */
    public float getX()
    {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(float x)
    {
        this.x = x;
    }

    /**
     * @return the y
     */
    public float getY()
    {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(float y)
    {
        this.y = y;
    }

    /**
     * @return the width
     */
    public float getWidth()
    {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(float width)
    {
        this.width = width;
    }

    /**
     * @return the height
     */
    public float getHeight()
    {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(float height)
    {
        this.height = height;
    }
    
    
    
}
