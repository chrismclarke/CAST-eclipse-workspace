package imageGroups;

import java.awt.*;


public class MeanSDImages extends AudioVisual {
	static public Image popnMean, popnSD, popnProp, popnMean2, popnSD2, popnProp2,
									sampMean, sampSD, sampProp, sampN, x, sampSDBlue, sampMeanDiff;
	static final public int kParamWidth = 26;
	static final public int kParamAscent = 12;
	static final public int kParamDescent = 5;
	static final public int kParamHeight = kParamAscent + kParamDescent;		//		=17
	
	static public boolean loadedMeanSD = false;
	
	synchronized static public void loadMeanSD(Component theComponent) {
		if (loadedMeanSD)
			return;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		popnMean = loadImage("xEquals/popnMean.png", tracker, popnMean, theComponent);
		popnSD = loadImage("xEquals/popnSD.png", tracker, popnSD, theComponent);
		popnProp = loadImage("xEquals/popnProp.png", tracker, popnProp, theComponent);
		popnMean2 = loadImage("xEquals/popnMean2.png", tracker, popnMean2, theComponent);
		popnSD2 = loadImage("xEquals/popnSD2.png", tracker, popnSD2, theComponent);
		popnProp2 = loadImage("xEquals/popnProp2.png", tracker, popnProp2, theComponent);
		sampMean = loadImage("xEquals/sampMean.png", tracker, sampMean, theComponent);
		sampSD = loadImage("xEquals/sampSD.png", tracker, sampSD, theComponent);
		sampProp = loadImage("xEquals/sampProp.png", tracker, sampProp, theComponent);
		sampN = loadImage("xEquals/sampN.png", tracker, sampN, theComponent);
		x = loadImage("xEquals/x.png", tracker, x, theComponent);
		sampSDBlue = loadImage("xEquals/sampSDBlue.png", tracker, sampSDBlue, theComponent);
		sampMeanDiff = loadImage("xEquals/sampMeanDiff.png", tracker, sampMeanDiff, theComponent);
		
		waitForLoad(tracker);
		loadedMeanSD = true;
	}
}