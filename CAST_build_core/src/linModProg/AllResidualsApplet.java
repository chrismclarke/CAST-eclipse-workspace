package linModProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.*;
import models.*;
import formula.*;

import linMod.*;


public class AllResidualsApplet extends XApplet {
	static final protected String ERROR_SD_AXIS_PARAM = "errorAxis";
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	private AllResidualsView allResidsView;
	
	private RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck;
	private XChoice dotOrBoxChoice;
	private int currentDotBoxChoice = 0;
	
	private String residKeys[];
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		setLayout(new BorderLayout(20, 10));
		
		add("Center", displayPanel(data, summaryData));
		add("South", controlPanel(data, summaryData));
	}
	
	private DataSet getData() {
		SimpleRegnDataSet data = new SimpleRegnDataSet(this);
		data.addVariable("resid", new BasicComponentVariable(translate("Residual"), data, "x", "y",
																				"ls", BasicComponentVariable.RESIDUAL, 9));
		return data;
	}
	
	private SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new AnovaSummaryData(sourceData, "error");
		
		NumVariable xVar = (NumVariable)sourceData.getVariable("x");
		int n = xVar.noOfValues();
		residKeys = new String[n];
		for (int i=0 ; i<n ; i++) {
			residKeys[i] = "resid" + i;
			summaryData.addVariable(residKeys[i], new OneResidualVariable(translate("Residuals"), "resid", i));
		}
		
		return summaryData;
	}
	
	private XPanel displayPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																							ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.LEFT, dataPanel(data));
		thePanel.add(ProportionLayout.RIGHT, residPanel(data, summaryData));
		return thePanel;
	}
	
	private XPanel dataPanel(DataSet data) {
		SimpleRegnDataSet regnData = (SimpleRegnDataSet)data;
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		XPanel plotPanel = new XPanel();
		plotPanel.setLayout(new AxisLayout());
		
			HorizAxis xAxis = new HorizAxis(this);
			xAxis.readNumLabels(regnData.getXAxisInfo());
			xAxis.setAxisName(regnData.getXVarName());
			plotPanel.add("Bottom", xAxis);
			
			VertAxis yAxis = new VertAxis(this);
			yAxis.readNumLabels(regnData.getYAxisInfo());
			plotPanel.add("Left", yAxis);
			
			SampleLineView dataView = new SampleLineView(data, this, xAxis, yAxis, "x", "y", "model");
			dataView.setShowData(true);
			dataView.setShowResiduals(true);
			dataView.lockBackground(Color.white);
			
			plotPanel.add("Center", dataView);
		
		thePanel.add("Center", plotPanel);
			
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
			XLabel yVariateName = new XLabel(regnData.getYVarName(), XLabel.LEFT, this);
			yVariateName.setFont(yAxis.getFont());
			topPanel.add(yVariateName);
			
		thePanel.add("North", topPanel);
		
		return thePanel;
	}
	
	private XPanel residPanel(DataSet data, SummaryDataSet summaryData) {
		SimpleRegnDataSet regnData = (SimpleRegnDataSet)data;
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		XPanel plotPanel = new XPanel();
		plotPanel.setLayout(new AxisLayout());
		
			HorizAxis xAxis = new HorizAxis(this);
			xAxis.readNumLabels(regnData.getXAxisInfo());
			xAxis.setAxisName(regnData.getXVarName());
			plotPanel.add("Bottom", xAxis);
			
			VertAxis residAxis = new VertAxis(this);
			residAxis.readNumLabels(getParameter(ERROR_SD_AXIS_PARAM));
			plotPanel.add("Left", residAxis);
			
			allResidsView = new AllResidualsView(summaryData, this, xAxis, residAxis, residKeys, data, "x");
			allResidsView.setJitter(0.6);
			allResidsView.lockBackground(Color.white);
			
			plotPanel.add("Center", allResidsView);
		
		thePanel.add("Center", plotPanel);
			
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
			XLabel yVariateName = new XLabel(translate("Residuals"), XLabel.LEFT, this);
			yVariateName.setFont(residAxis.getFont());
			topPanel.add(yVariateName);
			
		thePanel.add("North", topPanel);
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																							ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.LEFT, samplingControlPanel(summaryData));
		thePanel.add(ProportionLayout.RIGHT, displayControlPanel());
		return thePanel;
	}
	
	private XPanel samplingControlPanel(DataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																										VerticalLayout.VERT_CENTER, 5));
	
			takeSampleButton = new RepeatingButton(translate("Take sample"), this);
		thePanel.add(takeSampleButton);
		
			accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		thePanel.add(accumulateCheck);
		
			ValueCountView theCount = new ValueCountView(summaryData, this);
			theCount.setLabel(translate("No of samples") + " =");
		thePanel.add(theCount);
			
		return thePanel;
	}
	
	private XPanel displayControlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																										VerticalLayout.VERT_CENTER, 2));
		
			XLabel displayLabel = new XLabel(translate("Display of residuals") + ":", XLabel.LEFT, this);
			displayLabel.setFont(getStandardBoldFont());
		thePanel.add(displayLabel);
		
			dotOrBoxChoice = new XChoice(this);
			dotOrBoxChoice.addItem(translate("Dot plots"));
			dotOrBoxChoice.addItem(translate("Box plots"));
			dotOrBoxChoice.addItem(MText.expandText("#plusMinus# 2 ") + translate("sd"));
			dotOrBoxChoice.disable();
		thePanel.add(dotOrBoxChoice);
			
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			summaryData.takeSample();
			if (!accumulateCheck.getState())
				allResidsView.newRandomJittering();
			int n = ((NumVariable)summaryData.getVariable("resid0")).noOfValues();
			if (n > 10)
				dotOrBoxChoice.enable();
			return true;
		}
		else if (target == accumulateCheck) {
			if (!accumulateCheck.getState()) {
				if (currentDotBoxChoice > 0) {
					dotOrBoxChoice.select(0);
					currentDotBoxChoice = 0;
					allResidsView.setDisplayType(AllResidualsView.DOT_PLOTS);
				}
				dotOrBoxChoice.disable();
			}
			summaryData.setAccumulate(accumulateCheck.getState());
			return true;
		}
		else if (target == dotOrBoxChoice) {
			int newChoice = dotOrBoxChoice.getSelectedIndex();
			if (newChoice != currentDotBoxChoice) {
				currentDotBoxChoice = newChoice;
				allResidsView.setDisplayType(newChoice == 0 ? AllResidualsView.DOT_PLOTS
																		: newChoice == 1 ? AllResidualsView.BOX_PLOTS
																										: AllResidualsView.TWO_SD);
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