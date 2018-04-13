package pairBlockProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;
import formula.*;


import multiRegn.*;
import ssq.*;
import pairBlock.*;

public class BlockTreatComponentApplet extends XApplet {
	static final private String MAX_SUMMARIES_PARAM = "maxSummaries";
	static final private String COMPONENT_DECIMALS_PARAM = "componentDecimals";
	
	protected TwoTreatDataSet data;
	protected SummaryDataSet summaryData;
	
	protected BlockTreatComponentView componentPlot;
	
	protected NumValue maxSsq, maxMss, maxF, maxRSquared;
	protected ComponentEqnPanel ssqEquation;
	
	private XChoice componentChoice;
	private int currentComponent = 0;
	
	public void setupApplet() {
		data = readData();
		summaryData = getSummaryData(data);
		
		setLayout(new BorderLayout(0, 10));
		
		add("Center", dataPanel(data));
		
		add("South", controlPanel(data, summaryData));
	}
	
	private NumValue[] copyParams(MultipleRegnModel ls) {
		int nParam = ls.noOfParameters();
		NumValue paramCopy[] = new NumValue[nParam];
		for (int i=0 ; i<nParam ; i++)
			paramCopy[i] = new NumValue(ls.getParameter(i));
		return paramCopy;
	}
	
	protected TwoTreatDataSet readData() {
		TwoTreatDataSet data = new TwoTreatDataSet(this);
		
			MultipleRegnModel ls = (MultipleRegnModel)data.getVariable("ls");
		
			MultipleRegnModel lsX = new MultipleRegnModel("X only", data, MultiRegnDataSet.xKeys,
																																						copyParams(ls));
			lsX.updateLSParams("y", data.getXOnlyConstraints());
		data.addVariable("lsX", lsX);
		
			MultipleRegnModel lsZ = new MultipleRegnModel("Z only", data, MultiRegnDataSet.xKeys,
																																						copyParams(ls));
			lsZ.updateLSParams("y", data.getZOnlyConstraints());
		data.addVariable("lsZ", lsZ);
		
		int componentDecimals = Integer.parseInt(getParameter(COMPONENT_DECIMALS_PARAM));
		SeqXZComponentVariable.addComponentsToData(data, "x", "z", "y", "lsX", "lsZ", "ls",
																																componentDecimals);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SUMMARIES_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxMss = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		maxRSquared = new NumValue(st.nextToken());
		
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error",
							SeqXZComponentVariable.kZXComponentKey, maxSsq.decimals, maxRSquared.decimals);
		
		summaryData.setSingleSummaryFromData();
		return summaryData;
	}
	
	protected BlockTreatComponentView getComponentView(MultiRegnDataSet data, HorizAxis yAxis,
																											VertAxis treatAxis) {
		return new BlockTreatComponentView(data, this, yAxis, treatAxis, "y", "x", "z",
									BlockTreatComponentView.TREAT_COMPONENT_DISPLAY, BlockTreatComponentView.TREAT_MEAN);
	}
	
	protected XPanel dataPanel(MultiRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XLabel groupLabel = new XLabel(data.getVariable("x").name, XLabel.LEFT, this);
		thePanel.add("North", groupLabel);
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new AxisLayout());
			
				HorizAxis yAxis = new HorizAxis(this);
				String labelInfo = data.getYAxisInfo();
				yAxis.readNumLabels(labelInfo);
				yAxis.setAxisName(data.getYVarName());
			innerPanel.add("Bottom", yAxis);
			
				VertAxis treatAxis = new VertAxis(this);
				CatVariable treatVariable = (CatVariable)data.getVariable("x");
				treatAxis.setCatLabels(treatVariable);
			innerPanel.add("Left", treatAxis);
			
				componentPlot = getComponentView(data, yAxis, treatAxis);
				componentPlot.setCrossSize(DataView.LARGE_CROSS);
				componentPlot.lockBackground(Color.white);
			innerPanel.add("Center", componentPlot);
		
		thePanel.add("Center", innerPanel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(MultiRegnDataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 8));
			
			componentChoice = new XChoice(this);
			componentChoice.addItem("Treatment sum of squares");
			componentChoice.addItem("Block sum of squares");
		
		thePanel.add(componentChoice);
		
			AnovaImages.loadBlockImages(this);
		
			FormulaContext bigContext = new FormulaContext(Color.black, getBigFont(), this);
			ssqEquation = new ComponentEqnPanel(summaryData, SeqXZComponentVariable.kZXComponentKey, 
							maxSsq, AnovaImages.blockSsqs, SeqXZComponentVariable.kComponentColor,
							AnovaImages.kBlockSsqWidth, AnovaImages.kBlockSsqHeight, bigContext);
			ssqEquation.highlightComponent(2);
		thePanel.add(ssqEquation);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == componentChoice) {
			int newDisplay = componentChoice.getSelectedIndex();
			if (newDisplay != currentComponent) {
				currentComponent = newDisplay;
				
				int componentDisplay = (newDisplay == 0) ? BlockTreatComponentView.TREAT_COMPONENT_DISPLAY
													: BlockTreatComponentView.BLOCK_COMPONENT_DISPLAY;
				
				int meanDisplay = (newDisplay == 0) ? BlockTreatComponentView.TREAT_MEAN
													: BlockTreatComponentView.BLOCK_MEAN;
				componentPlot.changeComponentDisplay(componentDisplay, meanDisplay);
				
				ssqEquation.highlightComponent(newDisplay == 0 ? 2 : 1);
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