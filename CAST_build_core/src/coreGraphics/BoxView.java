package coreGraphics;

import java.awt.*;

import dataView.*;
import axis.*;


public class BoxView extends MarginalDataView implements BoxPlotConstants {
	
	protected static final int kMinDisplayWidth = 20;
	
//	static public final String BOX_PLOT = "boxPlot";
	
	protected boolean initialised = false;
	protected BoxInfo boxInfo;
	
	private boolean showOutliers = true;
	private Color fillColor = null;
	
	public BoxView(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		super(theData, applet, new Insets(5, 5, 5, 5), theAxis);
																//		5 pixels round for crosses to overlap into
	}
	
	public void setShowOutliers(boolean showOutliers) {
		this.showOutliers = showOutliers;
	}
	
	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}
	
	protected int getAxisPos(double x) {
		int horizPos = 0;
		try {
			horizPos = axis.numValToPosition(x);
		} catch (AxisException ex) {
			if (ex.axisProblem == AxisException.TOO_HIGH_ERROR)
				horizPos = axis.getAxisLength();
		}
		return horizPos;
	}
	
	protected NumValue[] getSortedValues(NumVariable variable) {
		return variable.getSortedData();
	}
	
	protected void initialiseBox(NumValue sortedVal[], BoxInfo boxInfo) {
		if (boxInfo == null)		//		this cannot happen if called from paintView()
			return;
		
		boxInfo.initialiseBox(sortedVal, showOutliers, axis);
	}
	
	public void reinitialiseAfterTransform() {
		initialiseBox(getSortedValues(getNumVariable()), boxInfo);
	}
	
	protected void drawBoxPlot(Graphics g, NumValue sortedVal[], BoxInfo boxInfo) {
		g.setColor(getForeground());
		
		boxInfo.drawBoxPlot(g, this, sortedVal, axis);
	}
	
	protected int getBoxBottom() {
		return (getDisplayWidth() - BoxInfo.kBoxHeight) / 2;
	}
	
	protected void initialise(NumVariable variable) {
		initialised = true;
		boxInfo = new BoxInfo();
		if (fillColor != null)
			boxInfo.setFillColor(fillColor);
		boxInfo.boxBottom = getBoxBottom();
		boxInfo.vertMidLine = boxInfo.boxBottom + boxInfo.getBoxHeight() / 2;
		NumValue sortedData[] = getSortedValues(variable);
//		for (int i=0 ; i<sortedData.length ; i++)
//			System.out.println(i + ": " + sortedData[i].toString());
		initialiseBox(sortedData, boxInfo);
	}
	
	public void paintView(Graphics g) {
		NumVariable variable = getNumVariable();
		if (!initialised)
			initialise(variable);
		
		drawBoxPlot(g, getSortedValues(variable), boxInfo);
	}

//-----------------------------------------------------------------------------------

	public int minDisplayWidth() {
		return kMinDisplayWidth;
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(getActiveNumKey())) {
			initialised = false;
			super.doChangeVariable(g, key);
		}
	}
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
}
	
