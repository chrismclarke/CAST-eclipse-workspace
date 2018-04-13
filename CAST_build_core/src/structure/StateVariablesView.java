package structure;

import java.awt.*;

import dataView.*;
import valueList.*;
import images.*;


public class StateVariablesView extends DataView {
	
	static final private int kPictWidth = 100;
	static final private int kPictHeight = 40;
	static final private int kVertGap = 3;
	static final private int kMaxWait = 30000;		//		30 seconds
	
	static final private int kValueXOffset = 48;
	static final private int kValueYOffset = 34;
	static final private int kHiliteOffset = 42;
	
	static final private Color kVarHiliteColor = new Color(0x66FFCC);		//		pale green
	
	private String[] variableKey;
	private Image[] variablePict;
	
//	private Image bodyPict;
	private ScrollValueList theList;
	
	private int selectedVarIndex = -1;
	
	public StateVariablesView(DataSet theData, XApplet applet, ScrollValueList theList,
																			String[] variableKey, String variableStringPrefix) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		this.theList = theList;
		this.variableKey = variableKey;
		variablePict = new Image[variableKey.length];
		
		MediaTracker tracker = new MediaTracker(applet);
		for (int i=0 ; i<variablePict.length ; i++) {
			variablePict[i] = CoreImageReader.getImage(variableStringPrefix + variableKey[i] + ".gif");
			tracker.addImage(variablePict[i], 0);
		}
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
		setForeground(Color.red);
//		lockBackground(Color.white);
		
		setFont(applet.getStandardBoldFont());
	}
	
	private void drawValue(Graphics g, String key, int index, Image img,
																								boolean selected, int left, int top) {
		g.setColor(Color.white);
		g.fillRect(left, top, kPictWidth, kPictHeight);
		
		if (selected) {
			g.setColor(kVarHiliteColor);
			g.fillRect(left + kHiliteOffset, top, kPictWidth - kHiliteOffset, kPictHeight);
		}
		
		g.drawImage(img, left, top, this);
		
		if (selected) {
			g.drawRect(left, top - 1, kPictWidth, kPictHeight + 1);
			g.drawRect(left + 1, top, kPictWidth, kPictHeight - 1);
		}
		
		g.setColor(getForeground());
		
		if (index >= 0) {
			Value val = ((Variable)getVariable(key)).valueAt(index);
			val.drawRight(g, left + kValueXOffset, top + kValueYOffset);
		}
	}
	
	public void paintView(Graphics g) {
		int minHeight = kPictHeight * variablePict.length + kVertGap * (variablePict.length - 1);
		int top = (getSize().height - minHeight) / 2;
		int left = (getSize().width - kPictWidth) / 2;
		
		int selIndex = getSelection().findSingleSetFlag();
		for (int i=0 ; i<variablePict.length ; i++) {
			drawValue(g, variableKey[i], selIndex, variablePict[i], (selectedVarIndex == i),
																																							left, top);
			top += (kPictHeight + kVertGap);
		}
		
//		top = (getSize().height - minHeight) / 2 + kPictHeight + kVertGap - 2;
//		g.setColor(Color.lightGray);
//		for (int i=1 ; i<variablePict.length ; i++) {
//			g.drawLine(0, top, getSize().width, top);
//			top += (kPictHeight + kVertGap);
//		}
	}
	

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		int height = kPictHeight * variablePict.length + kVertGap * (variablePict.length - 1);
		return new Dimension(kPictWidth + 10, height);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		int minHeight = kPictHeight * variablePict.length + kVertGap * (variablePict.length - 1);
		int top = (getSize().height - minHeight) / 2;
		int left = (getSize().width - kPictWidth) / 2;
		int varIndex = (y - top) / (kPictHeight + kVertGap);
		int varOffset = (y - top) % (kPictHeight + kVertGap);
		
		if (x < left || x > (left + kPictWidth) || varOffset > kPictHeight || varOffset < 0
																				|| varIndex < 0 || varIndex >= variablePict.length)
			return null;
		else
			return new IndexPosInfo(varIndex);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		doDrag(null, startInfo);
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		int hitVarIndex = (toPos == null) ? -1 : ((IndexPosInfo)toPos).itemIndex;
		selectedVarIndex = hitVarIndex;
		repaint();
		theList.setSelectedCols(hitVarIndex, -1);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
	}
}
	
