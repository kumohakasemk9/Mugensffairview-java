/*
Copyright (C) 2023 kumohakase (license: https://creativecommons.org/licenses/by-nc/4.0/)
Animation descriptor class
Please consider supporting me using kofi.com https://ko-fi.com/kumohakase
*/

public class AIRElement {
	public int AnimationNumber;
	public int LoopstartNum;
	public AIRElementSub Elements[];
	
	/**
	 * Initialize object
	 * @param a Animation Number
	 * @param l Loopstart point
	 */
	public AIRElement(int a, int l) {
		AnimationNumber = a;
		LoopstartNum = l;
		Elements = new AIRElementSub[0];
	}
	
	/**
	 * Append animation element
	 * @param a data
	 */
	public void AppendAnimationElement(AIRElementSub a) {
		AIRElementSub t[] = new AIRElementSub[Elements.length + 1];
		System.arraycopy(Elements, 0, t, 0, Elements.length);
		t[t.length - 1] = a;
		Elements = t;
	}
	
	public String toString() {
		return String.format("Animation%d (%d images)", AnimationNumber,
				Elements.length);
	}
}
