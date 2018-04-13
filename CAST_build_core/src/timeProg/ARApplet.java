package timeProg;

import java.awt.*;

import dataView.*;
import time.*;
import utils.*;
import random.RandomNormal;


class AutoCorrelSlider extends XSlider {
	public AutoCorrelSlider(XApplet applet) {
		super("-1.0", "1.0", applet.translate("Autocorrelation") + " = ", -100, 100, 0, applet);
	}
	
	protected Value translateValue(int val) {
		return new NumValue(val * 0.01, 2);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return translateValue(getMinValue()).stringWidth(g);
	}
}

public class ARApplet extends BasicTimeApplet {
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String AR_VAR_NAME_PARAM = "arVarName";
	
	private RandomNormal generator;
	
	private ARVariable theARVariable;
	private XSlider correlSlider;
	private XButton sampleButton;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		synchronized (data) {
			String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
			generator = new RandomNormal(randomInfo);
			double vals[] = generator.generate();
			
			data.addNumVariable("y", getParameter(VAR_NAME_PARAM), vals);
		}
		theARVariable = new ARVariable(getParameter(AR_VAR_NAME_PARAM), data, "y");
		theARVariable.setExtraDecimals(2);
		data.addVariable("ar", theARVariable);
		return data;
	}
	
	protected String getCrossKey() {
		return "ar";
	}
	
	protected String[] getLineKeys() {
		String keys[] = {"ar"};
		return keys;
	}
	
	protected boolean showDataValue() {
		return false;
	}
	
	protected boolean showSmoothedValue() {
		return false;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 10, 0, 0);
		thePanel.setLayout(new BorderLayout(10, 3));
		
		correlSlider = new AutoCorrelSlider(this);
		thePanel.add("Center", correlSlider);
		
		XPanel samplePanel = new XPanel();
		samplePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																					VerticalLayout.VERT_CENTER, 0));
		sampleButton = new XButton(translate("New sample"), this);
		samplePanel.add(sampleButton);
		thePanel.add("East", samplePanel);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == correlSlider) {
			int value = correlSlider.getValue();
			theARVariable.setSerialCorr(((double)value) * 0.01);
			return true;
		}
		
		if (target == sampleButton) {
			NumVariable variable = (NumVariable)getData().getVariable("y");
			double vals[] = generator.generate();
			variable.setValues(vals);
			getData().variableChanged("y");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean  action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}