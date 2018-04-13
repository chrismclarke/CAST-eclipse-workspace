package randomStat;

import java.awt.*;

import imageGroups.AudioVisual;


public class FitEstImages extends AudioVisual {
	static public Image n, estMean, estSD, estPropn, estPropnSD, fitMean, fitSD, fitPropn,
																successMean, successSD;
	static final public int kParamWidth = 83;
	static final public int kParam2Width = 118;
	static final public int kParamAscent = 30;
	static final public int kParamDescent = 10;
	static final public int kParamHeight = kParamAscent + kParamDescent;		//		=40
	static final public int kSuccessMeanWidth = 83;
	static final public int kSuccessSDWidth = 130;
	
	static final public int kSuccessAscent = 18;
	static final public int kSuccessDescent = 9;
	static final public int kSuccessHeight = kSuccessAscent + kSuccessDescent;		//		=27
	
	static public boolean loadedFitEst = false;
	
	synchronized static public void loadFitEst(Component theComponent) {
		if (loadedFitEst)
			return;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		estMean = loadImage("xEquals/estMean.png", tracker, estMean, theComponent);
		estSD = loadImage("xEquals/estSD.png", tracker, estSD, theComponent);
		fitMean = loadImage("xEquals/fitMean.png", tracker, fitMean, theComponent);
		fitSD = loadImage("xEquals/fitSD.png", tracker, fitSD, theComponent);
		estPropn = loadImage("xEquals/estPropn.png", tracker, estPropn, theComponent);
		estPropnSD = loadImage("xEquals/estPropnSD.png", tracker, estPropnSD, theComponent);
		fitPropn = loadImage("xEquals/fitPropn.png", tracker, fitPropn, theComponent);
		n = loadImage("xEquals/n.png", tracker, n, theComponent);
		successMean = loadImage("xEquals/successMean.png", tracker, successMean, theComponent);
		successSD = loadImage("xEquals/successSD.png", tracker, successSD, theComponent);
		
		waitForLoad(tracker);
		loadedFitEst = true;
	}
}