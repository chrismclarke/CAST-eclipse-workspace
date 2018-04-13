package contin;

import java.awt.*;

import imageGroups.AudioVisual;


public class ContinImages extends AudioVisual {
	static public Image marginTrans, conditTrans, xMarginLabel, xConditLabel,
																						yMarginLabel, yConditLabel;
	static public Image jointProb, jointProbX, jointProbY;
	
	static final public int kMTransWidth = 225;
	static final public int kMTransHeight = 140;
	static final public int kCTransWidth = 246;
	static final public int kCTransHeight = 154;
	static final public int kXLabelWidth = 166;
	static final public int kXLabelHeight = 14;
	static final public int kYLabelWidth = 14;
	static final public int kYLabelHeight = 166;
	
	static final public int kJointHeight = 18;
	static final public int kJointWidth = 68;
	
	synchronized static public void loadMarginTrans(Component theComponent) {
		if (marginTrans != null)
			return;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		marginTrans = loadImage("contin/marginTrans.png", tracker, marginTrans, theComponent);
		
		waitForLoad(tracker);
	}
	
	synchronized static public void loadConditTrans(Component theComponent) {
		if (conditTrans != null)
			return;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		conditTrans = loadImage("contin/conditTrans.png", tracker, conditTrans, theComponent);
		
		waitForLoad(tracker);
	}
	
	synchronized static public void loadJointProbs(Component theComponent) {
		if (jointProb != null)
			return;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		jointProb = loadImage("contin/jointProb.png", tracker, jointProb, theComponent);
		jointProbX = loadImage("contin/jointProbX.png", tracker, jointProbX, theComponent);
		jointProbY = loadImage("contin/jointProbY.png", tracker, jointProbY, theComponent);
		
		waitForLoad(tracker);
	}
	
	synchronized static public void loadLabels(Component theComponent) {
		if (xMarginLabel != null && xConditLabel != null && yMarginLabel != null
																						&& yConditLabel != null)
			return;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		xMarginLabel = loadImage("contin/xMarginLabel.png", tracker, xMarginLabel, theComponent);
		xConditLabel = loadImage("contin/xConditLabel.png", tracker, xConditLabel, theComponent);
		yMarginLabel = loadImage("contin/yMarginLabel.png", tracker, yMarginLabel, theComponent);
		yConditLabel = loadImage("contin/yConditLabel.png", tracker, yConditLabel, theComponent);
		
		waitForLoad(tracker);
	}
}