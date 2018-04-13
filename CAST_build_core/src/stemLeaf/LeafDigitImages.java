package stemLeaf;

import java.awt.*;

import imageGroups.AudioVisual;


public class LeafDigitImages extends AudioVisual {
	static final public int BLACK_DIGITS = 0;
	static final public int BLUE_DIGITS = 1;
	static final public int RED_DIGITS = 2;
	static final public int GREY_DIGITS = 3;
	
	static final public Image digit[][] = {new Image[10], new Image[10], new Image[10], new Image[10]};
	static public Image minusBlue;
	static public Image minusGrey;
	
	static final public int kDigitWidth = 8;
	static final public int kDigitHeight = 12;
	static final public int kHorizSpace = 2;
	static final public int kVertSpace = 3;
	
	synchronized static public void loadDigits(Component theComponent) {
		if (minusBlue != null)
			return;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		for (int i=0 ; i<10 ; i++) {
			digit[BLACK_DIGITS][i] = loadImage("leafDigits/digit" + i + ".gif", tracker, digit[BLACK_DIGITS][i], theComponent);
			digit[BLUE_DIGITS][i] = loadImage("leafDigits/digitBlue" + i + ".gif", tracker, digit[BLUE_DIGITS][i], theComponent);
			digit[RED_DIGITS][i] = loadImage("leafDigits/digitRed" + i + ".gif", tracker, digit[RED_DIGITS][i], theComponent);
			digit[GREY_DIGITS][i] = loadImage("leafDigits/digitGrey" + i + ".gif", tracker, digit[GREY_DIGITS][i], theComponent);
		}
		minusBlue = loadImage("leafDigits/minusBlue.gif", tracker, minusBlue, theComponent);
		minusGrey = loadImage("leafDigits/minusGrey.gif", tracker, minusGrey, theComponent);
		
		waitForLoad(tracker);
	}
	
	static public int getWidth() {
		return LeafDigitImages.kDigitWidth + LeafDigitImages.kHorizSpace;
	}
}