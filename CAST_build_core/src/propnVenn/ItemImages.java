package propnVenn;

import java.awt.*;

import imageGroups.AudioVisual;


public class ItemImages extends AudioVisual {
	static public Image apples[] = new Image[4];
	
	static final public int kItemWidth = 20;
	static final public int kItemHeight = 18;
	
	synchronized static public void loadApples(Component theComponent) {
		if (apples[0] != null)
			return;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		apples[0] = loadImage("contin/redAppleOK.png", tracker, apples[0], theComponent);
		apples[1] = loadImage("contin/redAppleBad.png", tracker, apples[1], theComponent);
		apples[2] = loadImage("contin/greenAppleOK.png", tracker, apples[2], theComponent);
		apples[3] = loadImage("contin/greenAppleBad.png", tracker, apples[3], theComponent);
		
		waitForLoad(tracker);
	}
}