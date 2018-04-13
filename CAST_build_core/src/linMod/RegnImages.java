package linMod;

import java.awt.*;

import imageGroups.AudioVisual;


public class RegnImages extends AudioVisual {
	static public Image yMean, ySD, x, blueX, yHat, slopeMeanHat, slopeSDHat, regnN, b1, beta1,
																			muY, muYHat, sigmaYHat, z;
	static final public int kYParamWidth = 31;
	static final public int kYParamAscent = 9;
	static final public int kYParamDescent = 9;
	static final public int kYParamHeight = kYParamAscent + kYParamDescent;		//		=18
	
	static final public int kYHatParamWidth = 25;
	static final public int kYHatParamAscent = 15;
	static final public int kYHatParamDescent = 5;
	static final public int kYHatParamHeight = kYHatParamAscent + kYHatParamDescent;		//	=20
	
	static final public int kSlopeMeanHatWidth = 37;
	static final public int kSlopeMeanHatAscent = 16;
	static final public int kSlopeMeanHatDescent = 9;
	static final public int kSlopeMeanHatHeight = kSlopeMeanHatAscent + kSlopeMeanHatDescent;		//	=20
	
	static final public int kXParamWidth = 8;
	static final public int kXParamHeight = 8;
	
	static final public int kMuYParamWidth = 31;
	static final public int kMuYParamAscent = 15;
	static final public int kMuYParamDescent = 9;
	static final public int kMuYParamHeight = kMuYParamAscent + kMuYParamDescent;		//		=24
	
	static public boolean loadedRegn = false;
	
	synchronized static public void loadRegn(Component theComponent) {
		if (loadedRegn)
			return;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		yMean = loadImage("xEquals/yMean.png", tracker, yMean, theComponent);
		ySD = loadImage("xEquals/ySD.png", tracker, ySD, theComponent);
		yHat = loadImage("xEquals/yHat.png", tracker, yHat, theComponent);
		x = loadImage("symbols/x.gif", tracker, x, theComponent);
		z = loadImage("symbols/z.gif", tracker, x, theComponent);
		blueX = loadImage("symbols/blueX.gif", tracker, blueX, theComponent);
		slopeMeanHat = loadImage("xEquals/slopeMeanHat.png", tracker, slopeMeanHat, theComponent);
		slopeSDHat = loadImage("xEquals/slopeSDHat.png", tracker, slopeSDHat, theComponent);
		regnN = loadImage("xEquals/regnN.png", tracker, regnN, theComponent);
		b1 = loadImage("xEquals/b1.png", tracker, b1, theComponent);
		beta1 = loadImage("xEquals/beta1.png", tracker, beta1, theComponent);
		muY = loadImage("xEquals/muY.png", tracker, muY, theComponent);
		muYHat = loadImage("xEquals/muYHat.png", tracker, muYHat, theComponent);
		sigmaYHat = loadImage("xEquals/sigmaYHat.png", tracker, sigmaYHat, theComponent);
		
		waitForLoad(tracker);
		loadedRegn = true;
	}
}