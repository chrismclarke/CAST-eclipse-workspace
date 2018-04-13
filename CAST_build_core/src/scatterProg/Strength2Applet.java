package scatterProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import coreGraphics.*;
import coreVariables.*;

import scatter.*;


class TransitionSlider extends XSlider {
	static final private int kSliderSteps = 200;
	
	public TransitionSlider(String minString, String maxString, String labelString,
													double initialPropn, XApplet applet) {
		super(minString, maxString, labelString, 0, kSliderSteps,
							(int)Math.round(initialPropn * kSliderSteps), applet);
	}
	
	protected Value translateValue(int val) {
		return null;
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return 0;
	}
	
	protected double getProportion() {
		return getProportion(getValue());
	}
	
	protected double getProportion(int val) {
		return ((double)val) / kSliderSteps;
	}
	
	protected void setProportion(double p) {
		setValue((int)Math.round(p * kSliderSteps));
	}
}


public class Strength2Applet extends ScatterApplet {
	static final private String SLICE_WIDTH_PARAM = "sliceWidth";
	
	private CorrelatedVariable y2Variable;
	
	private double rawCorr;
	
	private TransitionSlider strengthSlider;
	private XButton realDataButton;
	
	protected DataSet readData() {
		DataSet data = readCoreData();
		
		NumVariable yVariable = (NumVariable)data.getVariable("y");
		
		y2Variable = new CorrelatedVariable(getParameter(Y_VAR_NAME_PARAM), data, "x", "y",
																					yVariable.getMaxDecimals());
		data.addVariable("y2", y2Variable);
		
		rawCorr = y2Variable.getRawCorrelation();

		return data;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		double selectRange = Double.parseDouble(getParameter(SLICE_WIDTH_PARAM));
		SliceScatterView theView = new SliceScatterView(data, this, theHorizAxis, theVertAxis,
																																							"x", "y2", selectRange);
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new BorderLayout(25, 2));
		
			String text[] = getSliderText();
			strengthSlider = new TransitionSlider(text[0], text[1], text[2], rawCorr * rawCorr, this);
		controlPanel.add("Center", strengthSlider);
		
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				realDataButton = new XButton("Actual Data", this);
			buttonPanel.add(realDataButton);
			
		controlPanel.add("East", buttonPanel);
		
		return controlPanel;
	}
	
	protected String[] getSliderText() {
		String text[] = {"Weak", "Strong", "Strength of relationship"};
		return text;
	}

	
	private boolean localAction(Object target) {
		if (target == realDataButton) {
			strengthSlider.setProportion(rawCorr * rawCorr);
//										fires event to change correlation
//			y2Variable.setCorrelation(rawCorr);
//			data.variableChanged("y2");
			return true;
		}
		else if (target == strengthSlider) {
			double newCorr = Math.sqrt(strengthSlider.getProportion());
			if (rawCorr < 0.0)
				newCorr = - newCorr;
			y2Variable.setCorrelation(newCorr);
			data.variableChanged("y2");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}