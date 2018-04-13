package test;

import java.awt.*;

import imageGroups.AudioVisual;


public class TestImages extends AudioVisual {
	static public Image mu, estSD2, t;
	static final public int kParamWidth = 83;
	static final public int kParam2Width = 118;
	static final public int kParamAscent = 30;
	static final public int kParamDescent = 10;
	static final public int kParamHeight = kParamAscent + kParamDescent;		//		=40
	
	static public boolean loadedTest = false;
	
	synchronized static public void loadTest(Component theComponent) {
		if (loadedTest)
			return;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		mu = loadImage("xEquals/mu.png", tracker, mu, theComponent);
		estSD2 = loadImage("xEquals/estSD2.png", tracker, estSD2, theComponent);
		t = loadImage("xEquals/t.png", tracker, t, theComponent);
		
		waitForLoad(tracker);
		loadedTest = true;
	}
}