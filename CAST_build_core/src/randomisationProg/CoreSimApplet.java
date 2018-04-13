package randomisationProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.*;

import randomisation.*;


abstract public class CoreSimApplet extends XApplet {
	static final protected String SAMPLE_INFO_PARAM = "sampleInfo";
	static final protected String SUMMARY_AXIS_INFO_PARAM = "summaryAxis";
	
	static final private String THEORY_AND_SIM_PARAM = "showTheory";
	static final private String TAIL_PARAM = "tail";
	
	static final protected int LOW_TAIL = 0;
	static final protected int HIGH_TAIL = 1;
	static final protected int TWO_TAIL = 2;
	
	static final protected Color kDarkRed = new Color(0x990000);
	
	protected int sampleSize;
	protected int tailType;
	protected boolean showTheory;
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	protected DataView summaryView;
	
	private XChoice resultsChoice;
	protected int currentResultsDisplay = 0;
	
	protected XPanel resultsPanel;
	protected CardLayout resultsPanelLayout;
	
	private RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck;
	
	public void setupApplet() {
		readParameters();
		
		data = getData();
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		DistnVariable theoryVar = (DistnVariable)summaryData.getVariable("theory");
		if (theoryVar != null) {
			theoryVar.setMinSelection(lowCutOff());
			theoryVar.setMaxSelection(highCutOff());
		}
		
		addPanels(samplePanel(data, summaryData), summaryPanel(data, summaryData));
	}
	
	abstract protected void addPanels(XPanel samplePanel, XPanel summaryPanel);
	
	protected void readParameters() {
		tailType = LOW_TAIL;
		String tailParam = getParameter(TAIL_PARAM);
		if (tailParam != null) {
			if (tailParam.equals("high"))
				tailType = HIGH_TAIL;
			else if (tailParam.equals("both"))
				tailType = TWO_TAIL;
		}
			
		String theorySimString = getParameter(THEORY_AND_SIM_PARAM);
		showTheory = (theorySimString == null) || theorySimString.equals("true");
	}
	
	abstract protected DataSet getData();
	
	abstract protected SummaryDataSet getSummaryData(DataSet sourceData);
	
	private XPanel samplePanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
			
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new BorderLayout(0, 10));
			leftPanel.add("North", modelInfoPanel(data));
			leftPanel.add("Center", sampleViewPanel(data));
		
		thePanel.add("Center", leftPanel);
		
		thePanel.add("East", samplingControlPanel(summaryData));
		
		return thePanel;
	}
	
	abstract protected XPanel modelInfoPanel(DataSet data);
	
	abstract protected XPanel sampleViewPanel(DataSet data);
	
	private XPanel samplingControlPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 12));
		
		takeSampleButton = new RepeatingButton(translate("Simulate"), this);
		thePanel.add(takeSampleButton);
		
		accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		thePanel.add(accumulateCheck);
		
		ValueCountView theCount = new ValueCountView(summaryData, this);
		theCount.setLabel("");
		thePanel.add(theCount);
		
		return thePanel;
	}
	
	private XPanel summaryPanel(DataSet data, DataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("Center", summaryDistnPanel(data, summaryData));
		thePanel.add("East", summaryControlPanel(summaryData));
		
		return thePanel;
	}
	
	abstract protected String summaryName(DataSet data);
	abstract protected DataView getSummaryView(DataSet summaryData, HorizAxis summaryAxis);
	
	protected HorizAxis getSummaryAxis(DataSet data) {
		HorizAxis summaryAxis = new HorizAxis(this);
		summaryAxis.readNumLabels(getParameter(SUMMARY_AXIS_INFO_PARAM));
		summaryAxis.setAxisName(summaryName(data));
		return summaryAxis;
	}
	
	protected XPanel summaryDistnPanel(DataSet data, DataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis summaryAxis = getSummaryAxis(data);
		thePanel.add("Bottom", summaryAxis);
		
			summaryView = getSummaryView(summaryData, summaryAxis);
			summaryView.lockBackground(Color.white);
		thePanel.add("Center", summaryView);
		
		return thePanel;
	}
	
	abstract protected String getMenuTheoryString();
	
	private XPanel summaryControlPanel(DataSet summaryData) {
		if (showTheory) {
			XPanel thePanel = new XPanel();
			thePanel.setLayout(new BorderLayout(0, 5));
			
				XPanel topPanel = new XPanel();
				topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				
					resultsChoice = new XChoice(this);
					resultsChoice.addItem(translate("Simulation"));
					resultsChoice.addItem(getMenuTheoryString());
				topPanel.add(resultsChoice);
				
			thePanel.add("North", topPanel);
			
				resultsPanel = new XPanel();
					resultsPanelLayout = new CardLayout();
				resultsPanel.setLayout(resultsPanelLayout);
				resultsPanel.add("Simulation", simulationResultsPanel(summaryData));
				resultsPanel.add("Theory", theoryPanel(summaryData));
			
			thePanel.add("Center", resultsPanel);
			
			return thePanel;
		}
		else
			return simulationResultsPanel(summaryData);
	}
	
	abstract protected String extremePropnString();
	abstract protected String extremeProbString();
	abstract protected double lowCutOff();
	abstract protected double highCutOff();
	
	protected XPanel simulationResultsPanel(DataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
			XLabel sizeLabel = new XLabel(extremePropnString(), XLabel.LEFT, this);
			sizeLabel.setForeground(kDarkRed);
			sizeLabel.setFont(getStandardBoldFont());
		thePanel.add(sizeLabel);
		
			ExtremePropnView simulationPropn = new ExtremePropnView(summaryData, this, "stat", lowCutOff(), highCutOff());
			simulationPropn.setForeground(kDarkRed);
			simulationPropn.setFont(getBigBoldFont());
			simulationPropn.setHighlight(true);
			simulationPropn.setLabel("");
		thePanel.add(simulationPropn);
		
		return thePanel;
	}
	
	private XPanel theoryPanel(DataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 10));
		
			XPanel titlePanel = new XPanel();
			titlePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 2));
			
		thePanel.add(theoryParamPanel(summaryData));
		
			XPanel propnPanel = new XPanel();
			propnPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
			
				XLabel sizeLabel = new XLabel(extremeProbString(), XLabel.LEFT, this);
				sizeLabel.setForeground(kDarkRed);
				sizeLabel.setFont(getStandardBoldFont());
			propnPanel.add(sizeLabel);
			
				ProportionView theoryProb = new ProportionView(summaryData, "theory", this);
				theoryProb.setForeground(kDarkRed);
				theoryProb.setFont(getBigBoldFont());
				theoryProb.setLabel("");
				theoryProb.setHighlight(true);
				theoryProb.setInversePropn(true);
			propnPanel.add(theoryProb);
		
		thePanel.add(propnPanel);
		
		return thePanel;
	}
	
	abstract protected XPanel theoryParamPanel(DataSet summaryData);
	
	abstract protected void adjustSummaryDisplay();

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == accumulateCheck) {
			summaryData.setAccumulate(accumulateCheck.getState());
			return true;
		}
		else if (target == resultsChoice) {
			int newChoice = resultsChoice.getSelectedIndex();
			if (currentResultsDisplay != newChoice) {
				currentResultsDisplay = newChoice;
				adjustSummaryDisplay();
				resultsPanelLayout.show(resultsPanel, newChoice == 0 ? "Simulation" : "Theory");
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}