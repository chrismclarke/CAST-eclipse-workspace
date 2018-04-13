package designProg;

import java.awt.*;

import dataView.*;
import utils.*;
import imageUtils.*;

public class PictureSwapApplet extends XApplet {
	static final private String PICTURE_1_PARAM = "picture1";
	static final private String PICTURE_2_PARAM = "picture2";
	static final private String PICTURE_CHECK_PARAM = "pictureCheck";
	
	private XCheckbox pictureCheck;
	private ImageSwapCanvas thePicture;
	
	public void setupApplet() {
		setLayout(new BorderLayout(0, 10));
		
		String imageName[] = new String[2];
		imageName[0] = getParameter(PICTURE_1_PARAM) + ".png";
		imageName[1] = getParameter(PICTURE_2_PARAM) + ".png";
		
		thePicture = new ImageSwapCanvas(imageName, this);
		thePicture.showVersion(0);
		add("Center", thePicture);
		
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		pictureCheck = new XCheckbox(getParameter(PICTURE_CHECK_PARAM), this);
		controlPanel.add(pictureCheck);
		
		add("South", controlPanel);
	}
	
	private boolean localAction(Object target) {
		if (target == pictureCheck) {
			thePicture.showVersion( pictureCheck.getState() ? 1 : 0);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}