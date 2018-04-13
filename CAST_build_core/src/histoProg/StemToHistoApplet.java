package histoProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import imageUtils.*;


class PictureSlider extends XSlider {
	private Value[] pictureLabel;
	
	public PictureSlider(Value[] pictureLabel, XApplet applet) {
		super(null, null, " ", 0, pictureLabel.length - 1, 0, applet);
		this.pictureLabel = pictureLabel;
	}
	
	protected Value translateValue(int val) {
		return pictureLabel[val];
	}
	
	protected int getMaxValueWidth(Graphics g) {
		int width = 0;
		for (int i=0 ; i<pictureLabel.length ; i++)
			width = Math.max(width, pictureLabel[i].stringWidth(g));
		return width;
	}
}

public class StemToHistoApplet extends XApplet {
	static final private String PICTURE_PARAM = "picture";
	static final private String PICTURE_LABELS_PARAM = "pictureLabels";
	
	private PictureSlider pictureSlider;
	private ImageSwapCanvas thePicture;
	
	public void setupApplet() {
		StringTokenizer st = new StringTokenizer(getParameter(PICTURE_PARAM));
		String pictureFileCore = st.nextToken();
		int noOfPictures = Integer.parseInt(st.nextToken());
		
		String imageName[] = new String[noOfPictures];
		for (int i=0 ; i<noOfPictures ; i++)
			imageName[i] = pictureFileCore + (i+1) + ".gif";
		
		String picturelabels = getParameter(PICTURE_LABELS_PARAM);
		LabelEnumeration le = new LabelEnumeration(picturelabels);
		LabelValue pictureLabel[] = new LabelValue[noOfPictures];
		for (int i=0 ; i<noOfPictures ; i++) {
			String nextLabel = (String)le.nextElement();
			pictureLabel[i] = (nextLabel.equals("*")) ? pictureLabel[i-1] : new LabelValue(nextLabel);
		}
		
		setLayout(new BorderLayout(0, 10));
		
			thePicture = new ImageSwapCanvas(imageName, this);
			thePicture.showVersion(0);
			
		add("Center", thePicture);
		
			pictureSlider = new PictureSlider(pictureLabel, this);
			pictureSlider.setFont(getBigBoldFont());
		
		add("South", pictureSlider);
	}
	
	private boolean localAction(Object target) {
		if (target == pictureSlider) {
			int value = pictureSlider.getValue();
			thePicture.showVersion(value);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}