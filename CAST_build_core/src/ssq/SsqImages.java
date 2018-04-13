package ssq;

import java.awt.*;

import imageGroups.AudioVisual;


public class SsqImages extends AudioVisual {
	static final private int kMaxGroups = 3;
	
	static public Image sPooledSqr;
	static public Image sSqr[] = new Image[kMaxGroups];
	
	static final public int kGroupVarSquaredWidth = 19;
	static final public int kPooledVarSquaredWidth = 44;
	
	static final public int kAscent = 16;
	static final public int kDescent = 6;

	static private boolean loadedSsq = false;
	
	synchronized static public void loadSsq(Component theComponent) {
		if (loadedSsq)
			return;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		sPooledSqr = loadImage("anova/sPooledSqr.gif", tracker, sPooledSqr, theComponent);
		
		for (int i=0 ; i<kMaxGroups ; i++)
			sSqr[i] = loadImage("anova/sSqr" + (i+1) + ".gif", tracker, sSqr[i], theComponent);
		
		waitForLoad(tracker);
		loadedSsq = true;
	}
}