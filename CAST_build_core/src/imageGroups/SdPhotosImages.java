package imageGroups;

import java.awt.*;
import java.util.*;

import imageGroups.AudioVisual;


public class SdPhotosImages extends AudioVisual {
	static final public int kWidth = 500;
	static final public int kHeight = 230;
	
	static private HashMap thePhotos = new HashMap();
	
	synchronized static public Image getPhoto(String imageDirAndFile, Component theComponent) {
		Image thePhoto = (Image)thePhotos.get(imageDirAndFile);
		if (thePhoto != null)
			return thePhoto;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
//		thePhoto = Toolkit.getDefaultToolkit().getImage("javaImages/" + imageDirAndFile);
//		tracker.addImage(thePhoto, 0);
		
		thePhoto = loadImage(imageDirAndFile, tracker, null, theComponent);
		thePhotos.put(imageDirAndFile, thePhoto);
		
		waitForLoad(tracker);
		
//		System.out.println("got photo for : " + imageDirAndFile);
//		System.out.println("width = " + thePhoto.getWidth(theComponent) + ", height = " + thePhoto.getHeight(theComponent));
		return thePhoto;
	}
}