package sampling;

import java.awt.*;

import imageGroups.AudioVisual;


public class TreatmentImages extends AudioVisual {
	static final private int kMaxTreatments = 3;
	
	static public Image treatImage[] = new Image[kMaxTreatments];
	
	static final public int kWidth = 27;
	static final public int kHeight = 27;
	
	synchronized static public void loadTreatments(Component theComponent) {
		if (treatImage[0] != null)
			return;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		for (int i=0 ; i<kMaxTreatments ; i++)
			treatImage[i] = loadImage("treatments/letter" + (i+1) + ".png", tracker, treatImage[i],
																																					theComponent);
		
		waitForLoad(tracker);
	}
}