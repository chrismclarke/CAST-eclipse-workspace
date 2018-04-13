package samplingProg;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import sampling.*;

public class NormalStandardisedApplet extends NormalParamApplet {
	static final protected String AXIS_STEPS_PARAM = "axisSteps";
	
	protected StandardisingAxis theHorizAxis;
	
	protected XPanel probPanel(DataSet data) {
		XPanel thePanel = super.probPanel(data);
		
		NormalDistnVariable y = (NormalDistnVariable)data.getVariable(distnKey);
		double startMean = y.getMean().toDouble();
		double startSD = y.getSD().toDouble();
		y.setMean(0.0);
		y.setSD(1.0);
		y.setMinSelection(Double.NEGATIVE_INFINITY);
		y.setMaxSelection(Double.NEGATIVE_INFINITY);
		theHorizAxis.setInitialMinMax(startMean - 3.0 * startSD, startMean + 3.0 * startSD);
		
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		theHorizAxis = new StandardisingAxis(this);
		String labelInfo = getParameter(AXIS_STEPS_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
		theProbAxis = new VertAxis(this);
		labelInfo = getParameter(PROB_AXIS_PARAM);
		theProbAxis.readNumLabels(labelInfo);
		thePanel.add("Left", theProbAxis);
		
		HistoAndNormalView theView = new HistoAndNormalView(data, this, theHorizAxis, theProbAxis, distnKey,
																		null, 0.0, 0.0, HistoAndNormalView.SHOW_MEANSD,	HistoAndNormalView.FILL_DENSITY);
		theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected void setAxisFromSlider() {
		double mean = meanSlider.getParameter().toDouble();
		double sd = sdSlider.getParameter().toDouble();
		theHorizAxis.setMinMax(mean - 3.0 * sd, mean + 3.0 * sd);
	}

	
	private boolean localAction(Object target) {
		if (target == meanSlider || target == sdSlider) {
			setAxisFromSlider();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}