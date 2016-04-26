/**
 * BoxStyle.java
 * (c) Radek Burget, 2011
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
 * Created on 16.9.2011, 22:29:26 by radek
 */
package org.fit.pdfdom;

/**
 * This class represents a style of a text box.
 * @author radek
 */
public class BoxStyle
{
	public static final String defaultColor = "#000000";
	public static final String defaultFontWeight = "normal";
	public static final String defaultFontStyle = "normal";
	public static final String defaultPosition = "absolute";
	public static final String transparentColor = "rgba(0,0,0,0)";

	private String units;
	
	//font
	private String fontFamily;
	private float fontSize;
	private String fontWeight;
	private String fontStyle;
    private float lineHeight;
	private float wordSpacing;
	private	float letterSpacing;
	private String color;
	private String strokeColor;
	//position
	private String position;
	private float left;
	private float top;
	
	/**
	 * Creates a new style using the specified units for lengths.
	 * @param units Units used for lengths (e.g. 'pt')
	 */
	public BoxStyle(String units)
	{
		this.units = new String(units);
		fontFamily = null;
		fontSize = 0;
		fontWeight = null;
		fontStyle = null;
		lineHeight = 0;
		wordSpacing = 0;
		letterSpacing = 0;
		color = null;
		position = null;
		left = 0;
		top = 0;
	}
	
	public BoxStyle(BoxStyle src)
	{
		this.units = new String(src.units);
		fontFamily = src.fontFamily == null ? null : new String(src.fontFamily);
		fontSize = src.fontSize;
		fontWeight = src.fontWeight == null ? null : new String(src.fontWeight);
		fontStyle = src.fontStyle == null ? null : new String(src.fontStyle);
		lineHeight = src.lineHeight;
		wordSpacing = src.wordSpacing;
		letterSpacing = src.letterSpacing;
		color = src.color == null ? null : new String(src.color);
		position = src.position == null ? null : new String(src.position);
		left = src.left;
		top = src.top;
		strokeColor = src.strokeColor;
	}
	
	public String toString()
	{
		StringBuilder ret = new StringBuilder();
		if (position != null && !position.equals(defaultPosition))
			appendString(ret, "position", position);
		appendLength(ret, "top", top);
		appendLength(ret, "left", left);
		appendLength(ret, "line-height", lineHeight);
		if (fontFamily != null)
			appendString(ret, "font-family", fontFamily);
		if (fontSize != 0)
			appendLength(ret, "font-size", fontSize);
		if (fontWeight != null && !defaultFontWeight.equals(fontWeight))
			appendString(ret, "font-weight", fontWeight);
		if (fontStyle != null && !defaultFontStyle.equals(fontStyle))
			appendString(ret, "font-style", fontStyle);
		if (wordSpacing != 0)
			appendLength(ret, "word-spacing", wordSpacing);
		if (letterSpacing != 0)
			appendLength(ret, "letter-spacing", letterSpacing);
		if (color != null && !defaultColor.equals(color))
			appendString(ret, "color", color);
		if (strokeColor != null && !strokeColor.equals(transparentColor))
			ret.append(createTextStrokeCss(strokeColor));

		return ret.toString();
	}
	
	private void appendString(StringBuilder s, String propertyName, String value)
	{
		s.append(propertyName);
		s.append(':');
		s.append(value);
		s.append(';');
	}
	
	private void appendLength(StringBuilder s, String propertyName, float value)
	{
		s.append(propertyName);
		s.append(':');
		s.append(formatLength(value));
		s.append(';');
	}
	
	public String formatLength(float length)
	{
		//return String.format(Locale.US, "%1.1f%s", length, units); //nice but slow
		return (float) length + units;
	}

	private String createTextStrokeCss(String color)
	{
		// text shadow fall back for non webkit, gets disabled in default style sheet
		// since can't use @media in inline styles
		String strokeCss = "-webkit-text-stroke: %color% 1px ;" +
				"text-shadow:" +
				"-1px -1px 0 %color%, " +
				"1px -1px 0 %color%," +
				"-1px 1px 0 %color%, " +
				"1px 1px 0 %color%;";

		return strokeCss.replaceAll("%color%", color);
	}
	
	//================================================================

	/**
	 * @return the units
	 */
	public String getUnits()
	{
		return units;
	}

	/**
	 * @param units the units to set
	 */
	public void setUnits(String units)
	{
		this.units = units;
	}

	/**
	 * @return the fontFamily
	 */
	public String getFontFamily()
	{
		return fontFamily;
	}

	/**
	 * @param fontFamily the fontFamily to set
	 */
	public void setFontFamily(String fontFamily)
	{
		this.fontFamily = fontFamily;
	}

	/**
	 * @return the fontSize
	 */
	public float getFontSize()
	{
		return fontSize;
	}

	/**
	 * @param fontSize the fontSize to set
	 */
	public void setFontSize(float fontSize)
	{
		this.fontSize = fontSize;
	}

	/**
	 * @return the fontWeight
	 */
	public String getFontWeight()
	{
		return fontWeight;
	}

	/**
	 * @param fontWeight the fontWeight to set
	 */
	public void setFontWeight(String fontWeight)
	{
		this.fontWeight = fontWeight;
	}

	/**
	 * @return the fontStyle
	 */
	public String getFontStyle()
	{
		return fontStyle;
	}

	/**
	 * @param fontStyle the fontStyle to set
	 */
	public void setFontStyle(String fontStyle)
	{
		this.fontStyle = fontStyle;
	}

	/**
     * @return the lineHeight
     */
    public float getLineHeight()
    {
        return lineHeight;
    }

    /**
     * @param lineHeight the lineHeight to set
     */
    public void setLineHeight(float lineHeight)
    {
        this.lineHeight = lineHeight;
    }

    /**
	 * @return the wordSpacing
	 */
	public float getWordSpacing()
	{
		return wordSpacing;
	}

	/**
	 * @param wordSpacing the wordSpacing to set
	 */
	public void setWordSpacing(float wordSpacing)
	{
		this.wordSpacing = wordSpacing;
	}

	/**
	 * @return the letterSpacing
	 */
	public float getLetterSpacing()
	{
		return letterSpacing;
	}

	/**
	 * @param letterSpacing the letterSpacing to set
	 */
	public void setLetterSpacing(float letterSpacing)
	{
		this.letterSpacing = letterSpacing;
	}

	/**
	 * @return the color
	 */
	public String getColor()
	{
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(String color)
	{
		this.color = color;
	}
	/**
	 * @return the strokeColor
	 */
	public String getStrokeColor()
	{
		return strokeColor;
	}

	/**
	 * @param strokeColor the strokeColor to set
	 */
	public void setStrokeColor(String strokeColor)
	{
		this.strokeColor = strokeColor;
	}

	/**
	 * @return the left
	 */
	public float getLeft()
	{
		return left;
	}

	/**
	 * @param left the left to set
	 */
	public void setLeft(float left)
	{
		this.left = left;
	}

	/**
	 * @return the top
	 */
	public float getTop()
	{
		return top;
	}

	/**
	 * @param top the top to set
	 */
	public void setTop(float top)
	{
		this.top = top;
	}

	//================================================================
	
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((color == null) ? 0 : color.hashCode());
        result = prime * result + ((strokeColor == null) ? 0 : strokeColor.hashCode());
        result = prime * result
                + ((fontFamily == null) ? 0 : fontFamily.hashCode());
        result = prime * result + Float.floatToIntBits(fontSize);
        result = prime * result
                + ((fontStyle == null) ? 0 : fontStyle.hashCode());
        result = prime * result
                + ((fontWeight == null) ? 0 : fontWeight.hashCode());
        result = prime * result + Float.floatToIntBits(letterSpacing);
        result = prime * result + Float.floatToIntBits(wordSpacing);
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BoxStyle other = (BoxStyle) obj;
        if (color == null)
        {
            if (other.color != null) return false;
        }
        else if (!color.equals(other.color)) return false;
		if (strokeColor == null)
        {
            if (other.strokeColor != null) return false;
        }
        else if (!strokeColor.equals(other.strokeColor)) return false;
        if (fontFamily == null)
        {
            if (other.fontFamily != null) return false;
        }
        else if (!fontFamily.equals(other.fontFamily)) return false;
        if (Float.floatToIntBits(fontSize) != Float
                .floatToIntBits(other.fontSize)) return false;
        if (fontStyle == null)
        {
            if (other.fontStyle != null) return false;
        }
        else if (!fontStyle.equals(other.fontStyle)) return false;
        if (fontWeight == null)
        {
            if (other.fontWeight != null) return false;
        }
        else if (!fontWeight.equals(other.fontWeight)) return false;
        if (Float.floatToIntBits(letterSpacing) != Float
                .floatToIntBits(other.letterSpacing)) return false;
        if (Float.floatToIntBits(wordSpacing) != Float
                .floatToIntBits(other.wordSpacing)) return false;
        return true;
    }


	
}
