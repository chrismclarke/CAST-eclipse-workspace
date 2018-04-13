package regnProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import models.*;
import coreGraphics.*;

import regnView.*;
import regn.*;


public class DragLineApplet extends ScatterApplet {
	static final protected String INTERCEPT_PARAM = "interceptLimits";
	static final protected String SLOPE_PARAM = "slopeLimits";
	
	protected NumValue intMin, intMax, intStart, slopeMin, slopeMax, slopeStart;
	
	public void setupApplet() {
		StringTokenizer paramLimits = new StringTokenizer(getParameter(INTERCEPT_PARAM));
		intMin = new NumValue(paramLimits.nextToken());
		intMax = new NumValue(paramLimits.nextToken());
		intStart = new NumValue(paramLimits.nextToken());
		
		paramLimits = new StringTokenizer(getParameter(SLOPE_PARAM));
		slopeMin = new NumValue(paramLimits.nextToken());
		slopeMax = new NumValue(paramLimits.nextToken());
		slopeStart = new NumValue(paramLimits.nextToken());
		
		super.setupApplet();
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		LinearModel modelVariable = new LinearModel("model", data, "x", intStart, slopeStart,
																																			new NumValue(0.0, 0));
		data.addVariable("model", modelVariable);
		
		return data;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel equationPanel = new XPanel();
		equationPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		equationPanel.add(new LinearEquationView(data, this, "model", getParameter(Y_VAR_NAME_PARAM),
																getParameter(X_VAR_NAME_PARAM), intMin, intMax, slopeMin, slopeMax));
		
		return equationPanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		DragLineView theView = new DragLineView(data, this, theHorizAxis, theVertAxis, "x", "y", "model");
		NumVariable yVar = (NumVariable)data.getVariable("y");
		if (yVar.noOfValues() > 100)
			theView.setCrossSize(DataView.SMALL_CROSS);
		return theView;
	}
}