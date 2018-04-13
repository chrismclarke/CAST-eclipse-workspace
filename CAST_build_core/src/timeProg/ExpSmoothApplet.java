package timeProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import time.*;


public class ExpSmoothApplet extends BasicTimeApplet {
	static final private String SMOOTH_VAR_NAME_PARAM = "smoothName";
	
	protected ExpSmoothVariable smoothVariable;
	private ExpSmoothSlider aSlider;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		smoothVariable = new ExpSmoothVariable(getParameter(SMOOTH_VAR_NAME_PARAM), data, "y");
		smoothVariable.setExtraDecimals(2);
		data.addVariable("smooth", smoothVariable);
		
		return data;
	}
	
	protected String getCrossKey() {
		return "y";
	}
	
	protected String[] getLineKeys() {
		String keys[] = {"smooth"};
		return keys;
	}
	
	protected boolean showDataValue() {
		return true;
	}
	
	protected boolean showSmoothedValue() {
		return true;
	}
	
	protected ExpSmoothSlider createExpSmoothSlider() {
		aSlider = new ExpSmoothSlider(1.0, this);
		return aSlider;
	}
	
	protected TimeView createTimeView(TimeAxis theHorizAxis, VertAxis theVertAxis) {
		TimeView theView = new ExpTimeView(getData(), this, theHorizAxis, theVertAxis);
		theView.setSourceShading(true);
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(80, 0);
		thePanel.setLayout(new BorderLayout(10, 3));
//		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 3));
		
		thePanel.add("Center", createExpSmoothSlider());
//		thePanel.add("East", createShadingCheck());
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == aSlider) {
			smoothVariable.setSmoothConst(aSlider.getExpSmoothConst());
															//		which calls data.variableChanged()
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean  action(Event  evt, Object  what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}