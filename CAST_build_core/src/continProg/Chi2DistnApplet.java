package continProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import valueList.*;
import coreGraphics.*;

import contin.*;


public class Chi2DistnApplet extends ObsExpApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	
	private String kChiSquaredName;
	
	private XCheckbox accumulateCheck;
	protected DataPlusDistnInterface summaryView;
	
	public void setupApplet() {
		kChiSquaredName = translate("Chi-squared");
		
		checkDataSets();
		data = readData();
		summaryData = new SummaryDataSet(data, "y");
		
		setLayout(new BorderLayout(10, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL,
																					ProportionLayout.REMAINDER));
			leftPanel.add(ProportionLayout.TOP, dataPanel(data));
			leftPanel.add(ProportionLayout.BOTTOM, diffPanel(data));
			
		addSummaryVariables(data, summaryData);
		summaryData.takeSample();
			
		add("West", leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new BorderLayout(0, 30));
				
				XPanel topRightPanel = new XPanel();
				topRightPanel.setLayout(new BorderLayout(0, 10));
				topRightPanel.add("North", controlPanel(data));
				topRightPanel.add("Center", chi2Panel(data));
			
			rightPanel.add("North", topRightPanel);
			rightPanel.add("Center", summaryPanel(summaryData, "chi2", "theory"));
		
		add("Center", rightPanel);
	}
	
	private void addSummaryVariables(DataSet data, SummaryDataSet summaryData) {
		NumValue maxChi2 = new NumValue(getParameter(MAX_CHI2_PARAM));
		Chi2Variable chi2 = new Chi2Variable(null, oeView, maxChi2.decimals);
		summaryData.addVariable("chi2", chi2);
		
		Chi2DistnVariable chi2Distn = new Chi2DistnVariable("chi2 distn");
		chi2Distn.setParams("4");
		chi2Distn.setDF(oeView.getDF());
		summaryData.addVariable("theory", chi2Distn);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 8));
		
		thePanel.add(sampleSizePanel());
		
		thePanel.add(takeSampleButton(true));
		
			XPanel accumulatePanel = new XPanel();
			accumulatePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			
				accumulateCheck = new XCheckbox(translate("Accumulate"), this);
			accumulatePanel.add(accumulateCheck);
				ValueCountView theCount = new ValueCountView(summaryData, this);
				theCount.setLabel(translate("Samples") + ":");
			accumulatePanel.add(theCount);
			
		thePanel.add(accumulatePanel);
		
		return thePanel;
	}
	
	protected XPanel summaryPanel(DataSet data, String variableKey, String modelKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		theHorizAxis.setAxisName(kChiSquaredName);
		thePanel.add("Bottom", theHorizAxis);
		
		JitterPlusNormalView localView = getDataView(data, variableKey, modelKey, theHorizAxis,
																				DataPlusDistnInterface.NO_DISTN);
		summaryView = localView;
		thePanel.add("Center", localView);
		localView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	private JitterPlusNormalView getDataView(DataSet data, String variableKey,
										String modelKey, HorizAxis theHorizAxis, int densityDisplayType) {
		JitterPlusNormalView dataView = new JitterPlusNormalView(data, this, theHorizAxis, modelKey, 1.0);
		dataView.setActiveNumVariable(variableKey);
		dataView.setShowDensity(densityDisplayType);
		return dataView;
	}
	
	public void setTheoryShow(boolean theoryShow) {
		summaryView.setShowDensity(theoryShow ? DataPlusDistnInterface.CONTIN_DISTN : DataPlusDistnInterface.NO_DISTN);
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