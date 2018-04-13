package linMod;

import java.awt.*;

import imageGroups.AudioVisual;


public class RatioImages extends AudioVisual {
	static public Image t, plusMinus;
	static final public int kWidth = 41;
	static final public int kAscent = 14;
	static final public int kDescent = 0;
	static final public int kHeight = kAscent + kDescent;
	
	static public boolean loadedRatio = false;
	
	synchronized static public void loadRatio(Component theComponent) {
		if (loadedRatio)
			return;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		t = loadImage("xEquals/t.png", tracker, t, theComponent);
		plusMinus = loadImage("xEquals/plusMinus.png", tracker, plusMinus, theComponent);
		
		waitForLoad(tracker);
		loadedRatio = true;
	}
}