package coreGraphics;

import java.awt.*;

import dataView.*;
import axis.*;


public class ScatterView extends DotPlotView {
//	static public final String SCATTER_PLOT = "scatterPlot";
	
	protected VertAxis yAxis;
	protected String xKey, yKey;
	
	public ScatterView(DataSet theData, XApplet applet,
										HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey) {
		super(theData, applet, xAxis, 0.0);
		setActiveNumVariable(xKey);
		this.xKey = xKey;
		this.yKey = yKey;
		this.yAxis = yAxis;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		NumValue yVal = (NumValue)(yVariable.valueAt(index));
		
		if (isBadValue(theVal) || isBadValue(yVal))
			return null;
		
		int vertPos = yAxis.numValToRawPosition(yVal.toDouble());
		int horizPos = axis.numValToRawPosition(theVal.toDouble());
		
		return translateToScreen(horizPos, vertPos, thePoint);
	}
	
	protected void checkJittering() {
	}
	
	public void changeVariables(String yKey, String xKey) {
		if (yKey == null && xKey == null)		//	 only occurs with UnknownRelnApplet
			this.yKey = null;
		else if (yKey != null)				//	it can be null if there is no change
			this.yKey = yKey;
		
		if (xKey != null && !xKey.equals(this.xKey)) {
			this.xKey = xKey;
			setActiveNumVariable(xKey);
		}
		repaint();
	}


//-----------------------------------------------------------------------------------
	
	protected void doTransformView(Graphics g, NumCatAxis theAxis) {
		if (theAxis == yAxis) {
			reinitialiseAfterTransform();
			repaint();
		}
		else
			super.doTransformView(g, theAxis);
	}
}
	
