package linModProg;

import java.awt.*;
import java.util.*;

import utils.*;
import dataView.*;
import models.*;
import formula.*;

import linMod.*;


public class SampleLineDragApplet extends SampleLineApplet {
	static final private String SLOPE_LIMITS_PARAM = "slopeLimits";
	static final private String FIXED_VALUE_PARAM = "fixedValue";
	
	private ParameterSlider slopeSlider;
	
	private String minSlopeString, maxSlopeString;
	private int noOfSlopeSteps;
	private double startSlope;
	
	private double fixedX, fixedY;
	
	public void setupApplet() {
		StringTokenizer theParams = new StringTokenizer(getParameter(SLOPE_LIMITS_PARAM));
		minSlopeString = theParams.nextToken();
		maxSlopeString = theParams.nextToken();
		noOfSlopeSteps = Integer.parseInt(theParams.nextToken());
		startSlope = Double.parseDouble(theParams.nextToken());
		
		RegnImages.loadRegn(this);
		
		super.setupApplet();
	}
	
	protected DataSet readData() {
		data = super.readData();
		
		LinearModel yDistn = (LinearModel)data.getVariable("model");
		
		String fixedString = getParameter(FIXED_VALUE_PARAM);
		if (fixedString == null) {
			fixedX = 0.0;
			fixedY = yDistn.getIntercept().toDouble();
		}
		else {
			StringTokenizer st = new StringTokenizer(fixedString);
			fixedX = Double.parseDouble(st.nextToken());
			fixedY = Double.parseDouble(st.nextToken());
		}
		
		return data;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
		
			String beta1 = MText.expandText("#beta##sub1#");
			slopeSlider = new ParameterSlider(new NumValue(minSlopeString), new NumValue(maxSlopeString),
										new NumValue(startSlope), noOfSlopeSteps, beta1, this);
		thePanel.add(slopeSlider);
		
		thePanel.add(super.controlPanel(data));
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == slopeSlider) {
			double newSlope = slopeSlider.getParameter().toDouble();
			LinearModel theModel = (LinearModel)data.getVariable("model");
			theModel.setSlope(newSlope);
			theModel.setIntercept(fixedY - fixedX * newSlope);
			data.variableChanged("model");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}