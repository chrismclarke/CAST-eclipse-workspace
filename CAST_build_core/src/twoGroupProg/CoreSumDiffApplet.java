package twoGroupProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import coreGraphics.*;


abstract public class CoreSumDiffApplet extends XApplet {
	static final protected Color kDataDensityColor = new Color(0xC0C0C0);	//	pale gray
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	private RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck;
	
	public void setupApplet() {
		startSetup();
		data = getData();
		summaryData = getSummaryData(data);
		generateInitialSample(summaryData);
		
		setLayout(new BorderLayout(10, 0));
		
		add("Center", dataPanel(data, summaryData, noOfSubPanels()));
		add("East", summaryPanel(data, summaryData, noOfSubPanels()));
		add("South", controlPanel());
	}
	
	abstract protected void startSetup();
	abstract protected DataSet getData();
	abstract protected SummaryDataSet getSummaryData(DataSet sourceData);
	abstract protected void setTheoryParameters(SummaryDataSet summaryData);
	abstract protected XPanel subDataPanel(DataSet data, SummaryDataSet summaryData, int index);
	abstract protected XPanel subSummaryPanel(DataSet data, SummaryDataSet summaryData, int index);
	abstract protected XPanel controlPanel();
	
	
	protected int noOfSubPanels() {
		return 3;
	}	
	
	
	private void generateInitialSample(SummaryDataSet summaryData) {
		setTheoryParameters(summaryData);
		summaryData.takeSample();
	}
	
	protected XPanel dataPanel(DataSet data, SummaryDataSet summaryData, int nSubPanels) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout((nSubPanels - 1.0) / nSubPanels, 0,
																																ProportionLayout.VERTICAL));
		thePanel.add(ProportionLayout.BOTTOM, subDataPanel(data, summaryData, nSubPanels - 1));
		
			if (nSubPanels == 2)
				thePanel.add(ProportionLayout.TOP, subDataPanel(data, summaryData, 0));
			else
				thePanel.add(ProportionLayout.TOP, dataPanel(data, summaryData, nSubPanels - 1));
		
		return thePanel;
	}
	
//	private XPanel dataPanel(DataSet data, SummaryDataSet summaryData) {
//		XPanel thePanel = new XPanel();
//		thePanel.setLayout(new ProportionLayout(0.333, 0, ProportionLayout.VERTICAL,
//																																	ProportionLayout.TOTAL));
//		thePanel.add(ProportionLayout.TOP, subDataPanel(data, summaryData, 0));
//		
//			XPanel bottomPanel = new XPanel();
//			bottomPanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL,
//																																	ProportionLayout.TOTAL));
//			
//			bottomPanel.add(ProportionLayout.TOP, subDataPanel(data, summaryData, 1));
//			bottomPanel.add(ProportionLayout.BOTTOM, subDataPanel(data, summaryData, 2));
//			
//		thePanel.add(ProportionLayout.BOTTOM, bottomPanel);
//		
//		return thePanel;
//	}
	
	protected XPanel oneDataPanel(DataSet data, String variableKey, String modelKey, 
													String axisInfo, Color cData, Color cDensity, String densityLabel) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			theHorizAxis.readNumLabels(axisInfo);
			Variable v = (Variable)data.getVariable(variableKey);
			theHorizAxis.setAxisName(v.name);
			theHorizAxis.setForeground(cData);
		thePanel.add("Bottom", theHorizAxis);
		
			StackedPlusNormalView dataView = new StackedPlusNormalView(data, this, theHorizAxis, modelKey);
			dataView.setActiveNumVariable(variableKey);
			dataView.lockBackground(Color.white);
			dataView.setForeground(cData);
			dataView.setDensityColor(cDensity);
			dataView.setViewBorder(new Insets(5, 0, 5, 0));
			if (densityLabel != null)
				dataView.setDistnLabel(new LabelValue(densityLabel), Color.gray);
		
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	protected XPanel oneDataPanel(DataSet data, String variableKey, String modelKey, 
																					String axisInfo, Color cData, Color cDensity) {
		return oneDataPanel(data, variableKey, modelKey, axisInfo, cData, cDensity, null);
	}
	
	private XPanel summaryPanel(DataSet data, SummaryDataSet summaryData, int nSubPanels) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout((nSubPanels - 1.0) / nSubPanels, 0,
																															ProportionLayout.VERTICAL));
		thePanel.add(ProportionLayout.BOTTOM, subSummaryPanel(data, summaryData, nSubPanels - 1));
		
			if (nSubPanels == 2)
				thePanel.add(ProportionLayout.TOP, subSummaryPanel(data, summaryData, 0));
			else
				thePanel.add(ProportionLayout.TOP, summaryPanel(data, summaryData, nSubPanels - 1));
		
		return thePanel;
	}
	
	protected XPanel samplingPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			
			XPanel panel1 = new XPanel();
			panel1.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				takeSampleButton = new RepeatingButton(translate("Take sample"), this);
			panel1.add(takeSampleButton);
			
		thePanel.add("West", panel1);
			
			XPanel panel2 = new XPanel();
			panel2.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				accumulateCheck = new XCheckbox(translate("Accumulate"), this);
			panel2.add(accumulateCheck);
			
		thePanel.add("Center", panel2);
		return thePanel;
	}
	
	private void doTakeSample() {
		summaryData.takeSample();
	}
	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			doTakeSample();
			return true;
		}
		else if (target == accumulateCheck) {
			summaryData.setAccumulate(accumulateCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}