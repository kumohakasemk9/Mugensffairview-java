/*
Copyright (C) 2023 kumohakase (license: https://creativecommons.org/licenses/by-nc/4.0/)
Sprite element class
Please consider supporting me using kofi.com https://ko-fi.com/kumohakase
*/

import java.awt.image.*;
import java.awt.*;

public class SFFElement {
	public int X;
	public int Y;
	public int GroupNo;
	public int ImageNo;
	public boolean SharedPalette;
	public int LinkId;
	public byte ImgContent[];
	public Color Palette[];
	public int ImgWidth;
	public int ImgHeight;
	/**
	 * Initialize sff element, throws exception if there is bad parameter
	 * @param _x Axis x
	 * @param _y Axis y
	 * @param _g Group number
	 * @param _i Image number
	 * @param _s Shared palette flag
	 * @param _l Link id
	 */
	public SFFElement(int _x, int _y, int _g, int _i, boolean _s, int _l)
						throws IllegalArgumentException {
		if( !(-32768 <= _x && _x <= 32767 &&
				-32768 <= _y && _y <= 32767 &&
				0 <= _g && _g <= 65535 &&
				0 <= _i && _i <= 65535 &&
				0 <= _l && _l <= 65535) ) {
			throw new IllegalArgumentException("Parameter range overflow");
		}
		X = _x;
		Y = _y;
		GroupNo = _g;
		ImageNo = _i;
		SharedPalette = _s;
		LinkId = _l;
		ImgContent = null;
		Palette = null;
		ImgWidth = 0;
		ImgHeight = 0;
	}
	
	public String getPaletteModeStr() {
		return SharedPalette ? "Shared palette" : "Individual palette";
	}
	
	public String getLinkedImgStr() {
		if(ImgContent == null) {
			return String.format("Linked to %d", LinkId);
		} else {
			return "Actual";
		}
	}
	
	public String toString() {
		return String.format("SpriteData Group%d Image%d (%dx%d) Size: %dx%d " +
								"%s %s",
				GroupNo, ImageNo, X, Y, ImgWidth, ImgHeight, 
				getPaletteModeStr() , getLinkedImgStr() );
	}
}
