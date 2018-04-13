package imageGroups;

import java.awt.*;

import imageGroups.AudioVisual;


public class ScalesImages extends AudioVisual {
	static public Image tray, post;
	static public Image equals, notEquals, less, lessEquals, greater, greaterEquals;
	static public Image mu, pi, beta1, delta, muDiff, diffMeans, diffProbs;
	static public Image h0, hA;
	
	static final public int kTrayWidth = 70;
	static final public int kTrayHeight = 69;
	static final public int kTrayCentreOffset = 3;
	static final public int kTrayContentsOffset = 55;
	static final public int kPostWidth = 62;
	static final public int kPostHeight = 131;
	static final public int kPostCentreOffset = 13;
	
	static final public int kSignWidth = 12;
	static final public int kParamWidth = 13;
	static final public int kHeight = 15;
	static final public int kBaselineFromTop = 11;
	
	static final public int kGenericWidth = 22;
	static final public int kGenericHeight = 18;
	static final public int kGenericBaselineFromTop = 14;
	
	
	static public boolean loadedScales = false;
	
	synchronized static public void loadScales(Component theComponent) {
		if (loadedScales)
			return;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		tray = loadImage("scales/tray.png", tracker, tray, theComponent);
		post = loadImage("scales/post.png", tracker, post, theComponent);
		
		equals = loadImage("scales/equals.png", tracker, equals, theComponent);
		notEquals = loadImage("scales/notEquals.png", tracker, notEquals, theComponent);
		less = loadImage("scales/less.png", tracker, less, theComponent);
		lessEquals = loadImage("scales/lessEquals.png", tracker, lessEquals, theComponent);
		greater = loadImage("scales/greater.png", tracker, greater, theComponent);
		greaterEquals = loadImage("scales/greaterEquals.png", tracker, greaterEquals, theComponent);
		
		mu = loadImage("scales/mu.png", tracker, mu, theComponent);
		pi = loadImage("scales/pi.png", tracker, pi, theComponent);
		beta1 = loadImage("scales/beta1.png", tracker, beta1, theComponent);
		delta = loadImage("scales/delta.png", tracker, delta, theComponent);
		muDiff = loadImage("scales/muDiff.png", tracker, muDiff, theComponent);
		diffMeans = loadImage("scales/diffMeans.png", tracker, diffMeans, theComponent);
		diffProbs = loadImage("scales/diffProbs.png", tracker, diffProbs, theComponent);
		
		h0 = loadImage("scales/h0.png", tracker, h0, theComponent);
		hA = loadImage("scales/hA.png", tracker, hA, theComponent);
		
		waitForLoad(tracker);
		loadedScales = true;
	}
}