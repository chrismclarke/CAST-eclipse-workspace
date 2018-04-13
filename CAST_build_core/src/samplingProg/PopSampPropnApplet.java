package samplingProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import sampling.*;


public class PopSampPropnApplet extends PopSampStatsApplet {
	static final private String SLIDER_PARAM = "slider";
	
	private ValueSlider limitSlider;
	private PropnDotPlotView otherView = null;
	
	protected XPanel dotPlotPanel(DataSet data, String freqKey, boolean popNotSamp) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
		PropnDotPlotView theView = new PropnDotPlotView(data, this, theHorizAxis, otherView, freqKey, popNotSamp, limitSlider);
		if (otherView != null)
			otherView.setLinkedView(theView);
		otherView = theView;
		
		theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected XPanel summaryPanel(DataSet data, String freqKey, boolean popNotSamp) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 5));
		
		thePanel.add(new XLabel(popNotSamp ? translate("Probability") : translate("Proportion"), XLabel.LEFT, this));
		thePanel.add(new SummaryView(data, this, "y", freqKey, SummaryView.PROPN, summaryDecimals, popNotSamp));
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new BorderLayout(12, 0));
		
		StringTokenizer st = new StringTokenizer(getParameter(SLIDER_PARAM));
		double min = Double.parseDouble(st.nextToken());
		double max = Double.parseDouble(st.nextToken());
		int noOfSteps = Integer.parseInt(st.nextToken());
		int decimals = Integer.parseInt(st.nextToken());
		limitSlider = new ValueSlider(min, max, noOfSteps, decimals, this);
		controlPanel.add("Center", limitSlider);
		
		controlPanel.add("East", super.controlPanel(data));
		
		return controlPanel;
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			((FreqVariable)data.getVariable("freq")).sample(sampleSize, FreqVariable.WITHOUT_REPLACEMENT);
			data.valueChanged(0);		//		variableChanged() clears all selected flags
			data.setSelection("y", Double.NEGATIVE_INFINITY, limitSlider.getSliderValue());
			return true;
		}
		else if (target == limitSlider) {
			boolean change = data.setSelection("y", Double.NEGATIVE_INFINITY,
																						limitSlider.getSliderValue());
			if (!change)
			data.valueChanged(0);		//		to get display to redraw with moved yellow band
				
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}