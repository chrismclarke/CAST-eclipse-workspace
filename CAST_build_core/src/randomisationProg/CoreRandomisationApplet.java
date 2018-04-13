package randomisationProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.*;

import randomisation.*;


abstract public class CoreRandomisationApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "vertAxis";
	static final private String SUM_AXIS_INFO_PARAM = "summaryAxis";
	
	static final protected int ACTUAL = 0;
	static final protected int RANDOMISED = 1;
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	protected RandomisationInterface actualDataView, randDataView;
	
	private RepeatingButton sampleButton;
	private XCheckbox animateCheck, accumulateCheck;
	private XLabel randomisationLabel;
	
	private boolean startedRandomisation = false;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		
		setLayout(new ProportionLayout(0.75, 20, ProportionLayout.VERTICAL,
																															ProportionLayout.TOTAL));
			
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(10, 5));
			
			topPanel.add("Center", displayPanel(data));
			topPanel.add("East", controlPanel(data));
		
		add(ProportionLayout.TOP, topPanel);
		add(ProportionLayout.BOTTOM, summaryPanel(summaryData));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
			
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		summaryData = new SummaryDataSet(data, "randRand");
		return summaryData;
	}
	
	private XPanel summaryPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new AxisLayout());
			
				HorizAxis numAxis = new HorizAxis(this);
				String labelInfo = getParameter(SUM_AXIS_INFO_PARAM);
				numAxis.readNumLabels(labelInfo);
				CoreVariable v = summaryData.getVariable("stat");
				numAxis.setAxisName(v.name);
			dataPanel.add("Bottom", numAxis);
			
				double maxAbsDiff = getMaxAbsDiff();
				DataView diffView = new RandomDiffDotView(summaryData, this, numAxis, maxAbsDiff);
				diffView.lockBackground(Color.white);
			dataPanel.add("Center", diffView);
		
		thePanel.add("Center", dataPanel);
		
			XPanel propnPanel = new XPanel();
			propnPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																													VerticalLayout.VERT_CENTER, 2));
				XLabel propnLabel = new XLabel(translate("Propn more extreme"), XLabel.LEFT, this);
				propnLabel.setFont(getStandardBoldFont());
			propnPanel.add(propnLabel);
			
				ExtremePropnView propnValue = new ExtremePropnView(summaryData, this, "stat", maxAbsDiff);
				propnValue.setFont(getBigBoldFont());
				propnValue.setHighlight(true);
				propnValue.setLabel("");
			propnPanel.add(propnValue);
		
		thePanel.add("East", propnPanel);
		
		return thePanel;
	}
	
	abstract protected double getMaxAbsDiff();
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(5, 0));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new ProportionLayout(getActualSimPropn(), 5));
				XLabel actualLabel = new XLabel(translate("Actual Data"), XLabel.CENTER, this);
				actualLabel.setFont(getBigBoldFont());
			
			topPanel.add(ProportionLayout.LEFT, actualLabel);
				
				randomisationLabel = new XLabel(translate("Actual Data"), XLabel.CENTER, this);
				randomisationLabel.setFont(getBigBoldFont());
			
			topPanel.add(ProportionLayout.RIGHT, randomisationLabel);
		
		thePanel.add("North", topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new ProportionLayout(getActualSimPropn(), 0));
			bottomPanel.add(ProportionLayout.LEFT, dataPlotPanel(data, ACTUAL));
			bottomPanel.add(ProportionLayout.RIGHT, dataPlotPanel(data, RANDOMISED));
		
		thePanel.add("Center", bottomPanel);
		
		return thePanel;
	}
	
	abstract protected double getActualSimPropn();
	
	
	protected XPanel dataPlotPanel(DataSet data, int actualOrRandomised) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new AxisLayout());
			
				VertAxis numAxis = new VertAxis(this);
				String labelInfo = getParameter(AXIS_INFO_PARAM);
				numAxis.readNumLabels(labelInfo);
				CoreVariable v = data.getVariable("y");
				numAxis.setAxisName(v.name);
			dataPanel.add("Left", numAxis);
			
			RandomisationInterface tempView = createAndAddView(dataPanel, data,
																											actualOrRandomised, numAxis);
			
				if (actualDataView == null)
					actualDataView = tempView;
				else
					randDataView = tempView;
		
		thePanel.add("Center", dataPanel);
		
		thePanel.add("South", createStatisticPanel(data, (DataView)tempView, actualOrRandomised));
		
		return thePanel;
	}
	
	abstract protected RandomisationInterface createAndAddView(XPanel targetPanel,
																			DataSet data, int actualOrRandomised, VertAxis numAxis);
	
	abstract protected XPanel createStatisticPanel(DataSet data, DataView dataView,
																																		int actualOrRandomised);
	
	abstract protected boolean randomiseNotSimulate();
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
		
		if (randDataView != null) {
			animateCheck = new XCheckbox(translate("Animate"), this);
			animateCheck.setState(true);
			thePanel.add(animateCheck);
		}
		
			sampleButton = new RepeatingButton(randomiseNotSimulate() ? translate("Randomise") : translate("Simulate"), this);
		thePanel.add(sampleButton);
		
			accumulateCheck = new XCheckbox(translate("Accumulate"), this);
			accumulateCheck.disable();
		thePanel.add(accumulateCheck);
		
			ValueCountView theCount = new ValueCountView(summaryData, this);
		thePanel.add(theCount);
		
		return thePanel;
	}
	
	public void frameChanged(DataView theView) {
		if (theView.getCurrentFrame() == RandomisationInterface.kEndFrame) {
			sampleButton.enable();
		}
	}

	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			if (!startedRandomisation) {
				startedRandomisation = true;
				randomisationLabel.setText(randomiseNotSimulate() ? translate("Randomised Data")
																															: translate("Simulated Data"));
				accumulateCheck.enable();
			}
			if (animateCheck != null && animateCheck.getState()) {
				sampleButton.disable();
				randDataView.fixOldInfo();
				summaryData.takeSample();
				randDataView.doAnimation();
			}
			else {
				if (randDataView != null)
					((DataView)randDataView).setFrame(TwoGroupDotView.kEndFrame);
				summaryData.takeSample();
			}
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