package imageGroups;

import java.awt.*;

import images.*;


public class AudioVisual {
//	static public AudioClip beep;
	
	static final private int kMaxWait = 30000;		//		30 seconds
	
	static protected Image loadImage(String imageName, MediaTracker tracker, Image existingImage,
																								Component theComponent) {
		if (existingImage == null) {
/*
			String fileName = "javaImages/" + imageName;
			Image newImage = null;
			if (theComponent instanceof Applet) {
				Applet theApplet = ((Applet)theComponent);
//				URL imageURL = theApplet.getClass().getResource("/" + fileName);
//				newImage = theApplet.getImage(imageURL);
				newImage = theApplet.getImage(theApplet.getCodeBase(), fileName);
			}
			else
				newImage = Toolkit.getDefaultToolkit().getImage(fileName);
*/
			Image newImage = CoreImageReader.getImage(imageName);
			tracker.addImage(newImage, 0);
			return newImage;
		}
		else
			return existingImage;
	}
	
	static protected void waitForLoad(MediaTracker tracker) {
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
	}
	
/*
	synchronized static public void loadBeep(Applet theApplet) {
		if (beep == null)
			beep = theApplet.getAudioClip(theApplet.getCodeBase(), "javaImages/beep.au");
	}
*/
}