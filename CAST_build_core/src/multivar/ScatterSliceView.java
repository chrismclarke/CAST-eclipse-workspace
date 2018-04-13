package multivar;

import java.awt.*;

import dataView.*;
import axis.*;


public class ScatterSliceView extends DataView {
//	static public final String SLICE_SCATTER_PLOT = "sliceScatterPlot";
	
	private HorizAxis xAxis;
	private VertAxis yAxis;
	private String xKey, yKey;
	
	private boolean showAllValues = false;
	
//	private static final int kMinHitDist = 9;
	
	public ScatterSliceView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis,
																												String xKey, String yKey) {
		super(theData, applet, new Insets(5, 5, 5, 5));
																//		5 pixels round for crosses to overlap into
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.xKey = xKey;
		this.yKey = yKey;
	}
	
	private Point getScreenPoint(NumValue xVal, NumValue yVal, Point thePoint) {
		if (Double.isNaN(xVal.toDouble()) || Double.isNaN(yVal.toDouble()))
			return null;
		try {
			int vertPos = yAxis.numValToPosition(yVal.toDouble());
			int horizPos = xAxis.numValToPosition(xVal.toDouble());
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	public void paintView(Graphics g) {
		NumVariable xVariable = (NumVariable)getVariable(xKey);
		ValueEnumeration xe = xVariable.values();
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		ValueEnumeration ye = yVariable.values();
		FlagEnumeration fe = getSelection().getEnumeration();
		Point p = null;
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			NumValue xVal = (NumValue)xe.nextValue();
			NumValue yVal = (NumValue)ye.nextValue();
			boolean nextSel = fe.nextFlag();
			if (showAllValues || nextSel) {
				p = getScreenPoint(xVal, yVal, p);
				if (p != null)
					drawCross(g, p);
			}
		}
	}
	
	public void setSlicing(boolean doSlicing) {
		showAllValues = !doSlicing;
		repaint();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
