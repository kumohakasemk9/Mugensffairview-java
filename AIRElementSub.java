/*
Copyright (C) 2023 kumohakase (license: https://creativecommons.org/licenses/by-nc/4.0/)
Animation element class
Please consider supporting me using kofi.com https://ko-fi.com/kumohakase
*/

public class AIRElementSub {
	public int GroupNo;
	public int ImageNo;
	public int X;
	public int Y;
	public int DispTime;
	
	/**
	 * Create object from string (Decode)
	 * Format: "g,i, x,y, d"
	 * Where g: GroupNo, i: ImageNo, x,y :Sprite offset coordinate
	 * d: displaytime (1/60s unit)
	 * @param i Animation element stirng
	 * @throws IllegalArgumentException when parameter is too short or
	 * bad format
	 */
	public AIRElementSub(String i) throws IllegalArgumentException {
		int p[] = new int[5];
		String s[] = i.replace(" ", "").split(",");
		if(s.length < 5) {
			throw new IllegalArgumentException("Parameter missing");
		}
		try {
			for(int a = 0; a < 5; a++) {
				p[a] = Integer.parseInt(s[a]);
			}
		} catch(NumberFormatException ex) {
			throw new IllegalArgumentException("Parameter parse failed");
		}
		GroupNo = p[0];
		ImageNo = p[1];
		X = p[2];
		Y = p[3];
		DispTime = p[4];
	}
	
	public String toString() {
		return String.format("AnimationElement Group%d Image%d (%dx%d) Time%d",
				GroupNo, ImageNo, X, Y, DispTime);
	}
}
