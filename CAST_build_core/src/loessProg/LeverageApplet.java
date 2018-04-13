package loessProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;
import coreGraphics.*;

import loess.*;


public class LeverageApplet extends ScatterApplet {
	static final protected String OFFSET_PARAM = "offset";
	
	private LSLeverageView theView;
	private ParameterSlider offsetSlider;
	
	private NumValue minValue, maxValue, startValue;
	
	public void setupApplet() {
		StringTokenizer st = new StringTokenizer(getParameter(OFFSET_PARAM));
		minValue = new NumValue(st.nextToken());
		maxValue = new NumValue(st.nextToken());
		startValue = new NumValue(st.nextToken());
		
		super.setupApplet();
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		LinearModel modelVariable = new LinearModel("model", data, "x");
		modelVariable.setLSParams("y", 0, 0, 0);
		data.addVariable("model", modelVariable);
		
		return data;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.25, 0));
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new ProportionLayout(0.667, 0));
				
				offsetSlider = new ParameterSlider(minValue, maxValue, startValue,
																					"Perturbation of Y-value", this);
			rightPanel.add(ProportionLayout.LEFT, offsetSlider);
		thePanel.add(ProportionLayout.RIGHT, rightPanel);
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		theView = new LSLeverageView(data, this, theHorizAxis,
											theVertAxis, "x", "y", "model", startValue.toDouble());
		return theView;
	}
	
	private boolean localAction(Object target) {
		if (target == offsetSlider) {
			double newOffset = offsetSlider.getParameter().toDouble();
			theView.setOffset(newOffset);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}