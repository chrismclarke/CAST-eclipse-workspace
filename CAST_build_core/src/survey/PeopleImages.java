package survey;

import java.awt.*;

import dataView.*;
import images.*;
import imageGroups.*;


public class PeopleImages extends AudioVisual {
	public int personWidth;
	public int personHeight;
	
	public int noOfOptions[];
//	private String optionName[];
	
	public Image pict[][][][];			//		index1 = gender
																	//		index2 = top, mid, bottom
																	//		index3 = alternative
																	//		index4 = standard or dim
	
	public PeopleImages(String[] optionName, int[] noOfOptions, int personWidth,
																					int personHeight, XApplet applet) {
//		this.optionName = optionName;
		this.noOfOptions = noOfOptions;
		this.personWidth = personWidth;
		this.personHeight = personHeight;
		
		MediaTracker tracker = new MediaTracker(applet);
		
		pict = new Image[optionName.length][][][];
		
		for (int i=0 ; i<optionName.length ; i++) {
			pict[i] = new Image[noOfOptions.length][][];
			
			for (int j=0 ; j<noOfOptions.length ; j++) {
				String coreNameJ = optionName[i] + j;
				pict[i][j] = loadImages(coreNameJ, noOfOptions[j], tracker, applet);
			}
		}
				
		waitForLoad(tracker);
	}
	
	private Image[][] loadImages(String coreName, int noOfAlternatives, MediaTracker tracker,
																																		XApplet applet) {
		Image[][] result = new Image[noOfAlternatives][];
		for (int i=0 ; i<noOfAlternatives ; i++) {
			result[i] = new Image[2];
			String fileName = "sampling2/" + coreName + i + ".gif";
			result[i][0] = CoreImageReader.getImage(fileName);
			tracker.addImage(result[i][0], 0);
			fileName = "sampling2/" + coreName + i + "dim.gif";
			result[i][1] = CoreImageReader.getImage(fileName);
			tracker.addImage(result[i][1], 0);
		}
		return result;
	}
}