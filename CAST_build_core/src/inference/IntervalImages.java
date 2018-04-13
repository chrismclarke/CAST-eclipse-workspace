package inference;

import java.awt.*;

import imageGroups.AudioVisual;


public class IntervalImages extends AudioVisual {
	static public Image meanPlusMinus, timesSDMean, nu, tNu;
	static final public int kMeanPlusMinusWidth = 33;
	static final public int kTimesSDMeanWidth = 40;
	static final public int kParamDescent = 12;
	static final public int kParamAscent = 20;
	static final public int kParamHeight = kParamAscent + kParamDescent;  //  = 32
	
	static final public int kNuHeight = 18;
	static final public int kNuWidth = 27;
	
	static public boolean loadedInterval = false;
	
	synchronized static public void loadIntervalImages(Component theComponent) {
		if (loadedInterval)
			return;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		meanPlusMinus = loadImage("symbols/meanPlusMinus.gif", tracker, meanPlusMinus, theComponent);
		timesSDMean = loadImage("symbols/timesSDMean.gif", tracker, timesSDMean, theComponent);
		
		nu = loadImage("xEquals/nu.png", tracker, nu, theComponent);
		tNu = loadImage("xEquals/tNu.png", tracker, tNu, theComponent);
		
		waitForLoad(tracker);
		loadedInterval = true;
	}
}