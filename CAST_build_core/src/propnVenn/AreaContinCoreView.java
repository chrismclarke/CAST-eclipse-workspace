package propnVenn;

import java.awt.*;

import dataView.*;
import axis.*;


abstract public class AreaContinCoreView extends DataView {
	static final public boolean Y_MARGIN = true;
	static final public boolean X_MARGIN = false;
	
	static final public boolean CAN_SELECT = true;
	static final public boolean CANNOT_SELECT = false;
	
	static final protected int kFinalFrame = 40;
	static final private int kFramesPerSec = 10;
	
	protected String xKey, yKey;
	protected VertAxis yAxis;
	protected HorizAxis xAxis;
	
	protected boolean marginForY;
	protected boolean canSelect;
	
	protected int selectedX = -1;
	protected int selectedY = -1;
	
	protected PickMarginPanel theChoice;
	
	private boolean initialised = false;
	
	public AreaContinCoreView(DataSet theData, XApplet applet, VertAxis yAxis, HorizAxis xAxis, String yKey,
						String xKey, boolean canSelect, boolean yMargin) {
		super(theData, applet, new Insets(8, 8, 8, 8));
		
		this.xKey = xKey;
		this.yKey = yKey;
		this.yAxis = yAxis;
		this.xAxis = xAxis;
		this.canSelect = canSelect;
		marginForY = yMargin;
		
		setInitialFrame(kFinalFrame);
	}
	
	public int getSelectedX() {
		return selectedX;
	}
	
	public int getSelectedY() {
		return selectedY;
	}
	
	public void setJointProbChoice(PickMarginPanel theChoice) {
		this.theChoice = theChoice;
	}
	
	public void animateChange(boolean newMargin) {
		marginForY = newMargin;
		animateFrames(0, kFinalFrame, kFramesPerSec, null);
	}
	
	protected boolean initialise() {
		if (initialised)
			return false;
		else {
			initialised = true;
			return true;
		}
	}
	
	protected double getFramePropn() {
		double framePropn = getCurrentFrame() / (double)kFinalFrame;
		if (marginForY)
			framePropn = 1.0 - framePropn;
		return framePropn;
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (!canSelect)
			initialised = false;
		repaint();
	}
}
