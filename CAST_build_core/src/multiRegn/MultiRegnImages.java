package multiRegn;

import java.awt.*;

import imageGroups.AudioVisual;


public class MultiRegnImages extends AudioVisual {
	
	static public Image explained, bracketTop, bracketCenter, bracketBottom;
	
	static final public int kBracketWidth = 9;
	static final public int kBracketEndHt = 6;
	static final public int kBracketCenterHt = 7;

	static final public int kExplainedWidth = 14;
	static final public int kExplainedHeight = 60;

	static private boolean loadedMultiRegn = false;
	
	synchronized static public void loadMultiRegn(Component theComponent) {
		if (loadedMultiRegn)
			return;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		explained = loadImage("anova/explained.gif", tracker, explained, theComponent);
		
		bracketTop = loadImage("anova/bracketTop.gif", tracker, bracketTop, theComponent);
		bracketCenter = loadImage("anova/bracketCenter.gif", tracker, bracketCenter, theComponent);
		bracketBottom = loadImage("anova/bracketBottom.gif", tracker, bracketBottom, theComponent);
		
		waitForLoad(tracker);
		loadedMultiRegn = true;
	}
}