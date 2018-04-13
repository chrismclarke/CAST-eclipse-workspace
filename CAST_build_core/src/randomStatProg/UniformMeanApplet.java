package randomStatProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;

import distribution.*;
import randomStat.*;


public class UniformMeanApplet extends XApplet {
	static final private String BASE_OUTCOMES_PARAM = "baseOutcomes";
	static final private String MAX_SAMPLE_SIZE_PARAM = "maxSampleSize";
	static final private String MEAN_AXIS_PARAM = "meanAxis";
	
	private DataSet data;
	
	private ParameterSlider nSlider;
	
	private UniformSumAxis totalAxis;
	
	public void setupApplet() {
		int baseOutcomes = Integer.parseInt(getParameter(BASE_OUTCOMES_PARAM));
		int maxSampleSize = Integer.parseInt(getParameter(MAX_SAMPLE_SIZE_PARAM));
		
		data = new DataSet();
		UniformSumVariable y = new UniformSumVariable("x", baseOutcomes, maxSampleSize);
		data.addVariable("distn", y);
		data.setSelection("distn", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		
		setLayout(new BorderLayout(0, 10));
		
		add("South", sliderPanel(maxSampleSize));
		
		add("Center", barchartPanel(data, baseOutcomes, this));
	}
	
	private XPanel barchartPanel(DataSet data, int baseOutcomes, XApplet applet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis meanAxis = new HorizAxis(applet);
		meanAxis.readNumLabels(getParameter(MEAN_AXIS_PARAM));
		meanAxis.setAxisName(translate("Mean"));
		thePanel.add("Bottom", meanAxis);
		
		totalAxis = new UniformSumAxis(baseOutcomes, applet);
		totalAxis.setAxisName(translate("Total"));
		thePanel.add("Bottom", totalAxis);
		
		DiscreteProbView barChart = new DiscreteProbView(data, applet, "distn", null, totalAxis, DiscreteProbView.NO_DRAG);
		barChart.lockBackground(Color.white);
		thePanel.add("Center", barChart);
		
		return thePanel;
	}
	
	private XPanel sliderPanel(int maxN) {
		XPanel thePanel = new InsetPanel(40, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
		nSlider = new ParameterSlider(new NumValue(1, 0), new NumValue(maxN, 0), new NumValue(1, 0),
																																	translate("Number of dice"), this);
		thePanel.add("Center", nSlider);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == nSlider) {
			UniformSumVariable y = (UniformSumVariable)data.getVariable("distn");
			int newN = (int)Math.round(nSlider.getParameter().toDouble());
			y.setSampleSize(newN);
			totalAxis.setCountLabels(newN);
			data.variableChanged("distn");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}