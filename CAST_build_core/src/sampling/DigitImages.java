package sampling;

import java.awt.*;

import imageGroups.AudioVisual;


public class DigitImages extends AudioVisual {
	static public Image digitImage[] = new Image[10];
	static public Image zeroPointImage, zeroPointLecturerImage;
	
	static final public int kDigitWidth = 44;
	static final public int kDigitHeight = 63;
	
	static final public int kZeroPointWidth = 60;
	static final public int kZeroPointHeight = 67;
	
	synchronized static public void loadDigits(Component theComponent) {
		if (digitImage[0] != null)
			return;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		for (int i=0 ; i<10 ; i++)
			digitImage[i] = loadImage("digits/digit" + i + ".gif", tracker, digitImage[i], theComponent);
		zeroPointImage = loadImage("digits/digit0Point.gif", tracker, zeroPointImage, theComponent);
		zeroPointLecturerImage = loadImage("digits/digit0PointLight.gif", tracker, zeroPointLecturerImage, theComponent);
		
		waitForLoad(tracker);
	}
}