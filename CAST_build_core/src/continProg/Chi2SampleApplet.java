package continProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;

import contin.*;


public class Chi2SampleApplet extends ObsExpApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	
	private String kChiSquaredName;
	
	XCheckbox accumulateCheck;
	
	public void setupApplet() {
		kChiSquaredName = translate("Chi-squared");
		super.setupApplet();
	}
	
	protected SummaryDataSet createSummaryData(DataSet data) {
		SummaryDataSet summaryData = super.createSummaryData(data);
		
			NumValue maxChi2 = new NumValue(getParameter(MAX_CHI2_PARAM));
			Chi2Variable chi2 = new Chi2Variable(null, oeView, maxChi2.decimals);
		summaryData.addVariable("chi2", chi2);
		
		return summaryData;
	}
	
	protected XPanel summaryPanel(DataSet data, String variableKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			theHorizAxis.setAxisName(kChiSquaredName);
		thePanel.add("Bottom", theHorizAxis);
		
			StackedDotPlotView theView = new StackedDotPlotView(data, this, theHorizAxis);
			theView.lockBackground(Color.white);
			theView.setActiveNumVariable(variableKey);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected XPanel chi2Panel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 4));
		
		thePanel.add("North", super.chi2Panel(data));
		
		thePanel.add("Center", summaryPanel(summaryData, "chi2"));
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 8));
		
		thePanel.add(sampleSizePanel());
		
		addMarginChoice(thePanel);
		
		thePanel.add(takeSampleButton(true));
		
			accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		thePanel.add(accumulateCheck);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == accumulateCheck) {
			summaryData.setAccumulate(accumulateCheck.getState());
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