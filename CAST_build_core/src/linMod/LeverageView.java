package linMod;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class LeverageView extends DataView {
//	static public final String LEVERAGE_PLOT = "leveragePlot";
	
	private String lineKey;
	private HorizAxis xAxis;
	private VertAxis yAxis;
	
	public LeverageView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis, String lineKey) {
		super(theData, applet, new Insets(0, 5, 0, 5));
		this.lineKey = lineKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
	}
	
	public void paintView(Graphics g) {
		LinearModel model = (LinearModel)getVariable(lineKey);
		
		model.drawMean(g, this, xAxis, yAxis);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
