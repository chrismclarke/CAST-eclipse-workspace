package corrProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.RandomNormal;
import coreVariables.*;
import coreGraphics.*;

//import scatter.*;
//import scatterProg.*;


class CorrelationSlider extends XSlider {
	public CorrelationSlider(XApplet applet) {
		super("-1", "+1", applet.translate("Correlation") + " = ", 0, 200, 100, applet);
	}
	
	protected Value translateValue(int val) {
		return new NumValue(getCorrelation(val), 2);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return translateValue(getMinValue()).stringWidth(g);
	}
	
	protected double getCorrelation() {
		return getCorrelation(getValue());
	}
	
	protected double getCorrelation(int val) {
		return (val - 100) * 0.01;
	}
}

public class CorrelationApplet extends ScatterApplet {
	static final private String X_RANDOM_NORMAL_PARAM = "random";
	static final private String Y_DISTN_PARAM = "yDistn";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	
	private RandomNormal generator;
	
	private CorrelatedVariable yVariable;
	private NumVariable xVariable, zVariable;
	private CorrelationSlider rSlider;
	private XChoice sampleSizeChoice;
	private XButton takeSampleButton;
	
	int sampleSize[] = new int[3];
	
	public void setupApplet() {
		StringTokenizer theValues = new StringTokenizer(getParameter(SAMPLE_SIZE_PARAM));
		for (int i=0 ; i<3 ; i++)
			sampleSize[i] = Integer.parseInt(theValues.nextToken());
		super.setupApplet();
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		String randomInfo = getParameter(X_RANDOM_NORMAL_PARAM);
		generator = new RandomNormal(randomInfo);
		generator.setSampleSize(sampleSize[0]);
		
		xVariable = new NumVariable(getParameter(X_VAR_NAME_PARAM));
		xVariable.setValues(generator.generate());
		data.addVariable("x", xVariable);
		
		zVariable = new NumVariable("");
		zVariable.setValues(generator.generate());
		data.addVariable("z", zVariable);
		
		yVariable = new CorrelatedVariable(getParameter(Y_VAR_NAME_PARAM), data, "x", "z",
																						getParameter(Y_DISTN_PARAM));
		data.addVariable("y", yVariable);
		
		return data;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		rSlider = new CorrelationSlider(this);
		thePanel.add("North", rSlider);
		
		XPanel samplePanel = new XPanel();
		samplePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		takeSampleButton = new XButton(translate("Sample"), this);
		samplePanel.add(takeSampleButton);
		
		sampleSizeChoice = new XChoice(this);
		for (int i=0 ; i<3 ; i++)
			sampleSizeChoice.addItem(String.valueOf(sampleSize[i]) + " " + translate("values"));
		sampleSizeChoice.select(0);
		samplePanel.add(sampleSizeChoice);
		thePanel.add("Center", samplePanel);
		
		return thePanel;
	}
	
	private void generateNewSample() {
		synchronized (data) {
			xVariable.setValues(generator.generate());
			zVariable.setValues(generator.generate());
			
			data.variableChanged("x");
			data.variableChanged("z");
		}
	}
	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			generateNewSample();
			return true;
		}
		else if (target == sampleSizeChoice) {
			int sampSizeIndex = sampleSizeChoice.getSelectedIndex();
			generator.setSampleSize(sampleSize[sampSizeIndex]);
			generateNewSample();
			return true;
		}
		else if (target == rSlider) {
			yVariable.setCorrelation(rSlider.getCorrelation());
			data.variableChanged("y");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}