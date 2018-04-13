package twoGroupProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import valueList.*;
import coreGraphics.*;
import models.*;

import twoGroup.*;


abstract public class CoreDiffApplet extends XApplet {
	static final protected String MAX_SUMMARY_PARAM = "maxSummary";
	static final protected String SUMMARY_AXIS_PARAM = "summaryAxis";
	
	static final private Color kPinkColor = new Color(0xFFCCCC);
	
	static final protected Color groupMainColor[] = {PersonPicture.kFemaleBorderColor, PersonPicture.kMaleBorderColor};
	static final protected Color groupShadeColor[] = {PersonPicture.kFemaleFillColor, PersonPicture.kMaleFillColor};
	
	protected CoreModelDataSet data;
	protected SummaryDataSet summaryData;
	
	private RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck;
	
	private JitterPlusNormalView summaryDotPlot;
	
	public void setupApplet() {
		data = readData();
		summaryData = getSummaryData(data);
		
		setLayout(new BorderLayout(0, 4));
		
		add("Center", displayPanel(data, summaryData));
		
		add("South", samplingControlPanel(summaryData));
	}
	
	abstract protected CoreModelDataSet readData();
	
	abstract protected SummaryDataSet getSummaryData(CoreModelDataSet sourceData);
	
	abstract protected void setTheoryParams(CoreModelDataSet data, SummaryDataSet summaryData);
	
	protected XPanel samplingControlPanel(DataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
		takeSampleButton = new RepeatingButton(translate("Take sample"), this);
		thePanel.add(takeSampleButton);
		
		accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		thePanel.add(accumulateCheck);
		
		ValueCountView theCount = new ValueCountView(summaryData, this);
		theCount.setLabel(translate("No of samples") + " =");
		thePanel.add(theCount);
		
		return thePanel;
	}
	
	abstract protected double getLeftProportion();
	
	protected XPanel displayPanel(CoreModelDataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(getLeftProportion(), 10));
		thePanel.add(ProportionLayout.LEFT, dataPanel(data));
		thePanel.add(ProportionLayout.RIGHT, summaryPanel(data, summaryData));
		return thePanel;
	}
	
	abstract protected XPanel dataPanel(CoreModelDataSet data);
	
	abstract protected XPanel summaryPanel(CoreModelDataSet data, SummaryDataSet summaryData);
	
	abstract protected String getSummaryYAxisInfo(CoreModelDataSet data);
	
	protected XPanel groupPlotPanel(CoreModelDataSet data, SummaryDataSet summaryData,
													String dataKey, String theoryKey, int group) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
			
			CatVariable xVar = (CatVariable)data.getVariable("x");
			
			HorizAxis theAxis = new HorizAxis(this);
			theAxis.readNumLabels(getSummaryYAxisInfo(data));
			theAxis.setAxisName(xVar.getLabel(group).toString());
			theAxis.setForeground(groupMainColor[group]);
		thePanel.add("Bottom", theAxis);
		
			JitterPlusNormalView theView = new JitterPlusNormalView(summaryData, this, theAxis, theoryKey, 1.0);
			theView.setActiveNumVariable(dataKey);
			theView.setShowDensity (DataPlusDistnInterface.CONTIN_DISTN);
			theView.setDensityColor(groupShadeColor[group]);
			theView.lockBackground(Color.white);
			theView.setViewBorder(new Insets(5, 0, 5, 0));
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected XPanel differencePlotPanel(SummaryDataSet summaryData, String dataKey,
																									String theoryKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theAxis = new HorizAxis(this);
			theAxis.readNumLabels(getParameter(SUMMARY_AXIS_PARAM));
			theAxis.setAxisName(summaryData.getVariable(dataKey).name);
			theAxis.setForeground(Color.red);
		thePanel.add("Bottom", theAxis);
		
			summaryDotPlot = new JitterPlusNormalView(summaryData, this, theAxis, theoryKey, 1.0);
			summaryDotPlot.setActiveNumVariable(dataKey);
			summaryDotPlot.setShowDensity (DataPlusDistnInterface.NO_DISTN);
			summaryDotPlot.setDensityColor(kPinkColor);
			summaryDotPlot.lockBackground(Color.white);
			summaryDotPlot.setViewBorder(new Insets(5, 0, 5, 0));
		thePanel.add("Center", summaryDotPlot);
		
		return thePanel;
	}
	
	
	public void setShowTheory(boolean showDistn) {
		summaryDotPlot.setShowDensity (showDistn ? DataPlusDistnInterface.CONTIN_DISTN
																		: DataPlusDistnInterface.NO_DISTN);
	}
	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			summaryData.takeSample();
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