/**
 * SubPath.java
 * (c) Radek Burget, 2015
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
 * Created on 9.9.2015, 15:27:27 by burgetr
 */
package org.fit.pdfdom;

/**
 * @author burgetr
 *
 */
public class PathSegment
{
    private float x1, y1, x2, y2;

    public PathSegment(float x1, float y1, float x2, float y2)
    {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public float getX1()
    {
        return x1;
    }

    public void setX1(float x1)
    {
        this.x1 = x1;
    }

    public float getY1()
    {
        return y1;
    }

    public void setY1(float y1)
    {
        this.y1 = y1;
    }

    public float getX2()
    {
        return x2;
    }

    public void setX2(float x2)
    {
        this.x2 = x2;
    }

    public float getY2()
    {
        return y2;
    }

    public void setY2(float y2)
    {
        this.y2 = y2;
    }
    
}
