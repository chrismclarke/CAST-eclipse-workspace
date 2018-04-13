package randomStat;

import java.awt.*;

import imageGroups.AudioVisual;


public class SuccessImages extends AudioVisual {
	static public Image muXEquals, sdXEquals;
	static final public int kParamWidth = 36;
	static final public int kParamAscent = 13;
	static final public int kParamDescent = 6;
	static final public int kParamHeight = kParamAscent + kParamDescent;		//	=19;
	
	static public boolean loadedSuccess = false;
	
	synchronized static public void loadSuccess(Component theComponent) {
		if (loadedSuccess)
			return;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		muXEquals = loadImage("xEquals/muXEquals.png", tracker, muXEquals, theComponent);
		sdXEquals = loadImage("xEquals/sdXEquals.png", tracker, sdXEquals, theComponent);
		
		waitForLoad(tracker);
		loadedSuccess = true;
	}
}