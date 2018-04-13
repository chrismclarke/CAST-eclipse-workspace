package exper2Prog;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import models.*;
import glmAnova.*;

import ssq.*;
import exper2.*;


public class NestedFactorApplet extends XApplet {
	static final private String BLOCK_NAME_PARAM = "blockName";
	static final private String BLOCK_VALUES_PARAM = "blockValues";
	static final private String BLOCK_LABELS_PARAM = "blockLabels";
	static final private String FACTOR_NAME_PARAM = "factorName";
	static final private String FACTOR_VALUES_PARAM = "factorValues";
	static final private String FACTOR_LABELS_PARAM = "factorLabels";
	static final private String Y_NAME_PARAM = "yName";
	static final private String Y_MODEL_PARAM = "yModel";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	
	static final private String PARAM_DECIMALS_PARAM = "paramDecimals";
	static final private String COMPONENT_NAMES_PARAM = "componentNames";
	static final private String MAX_SUMMARIES_PARAM = "maxSummaries";
	
	static final private String[] kXGeneratorKeys = {"block", "factor"};
	static final private String[] kFactorKeys = {"factor"};
	static final private String[] kBlockKeys = {"block"};
	
	static final private String[] kComponentKeys = {"totalComp", "factorComp", "blockComp", "residComp"};
	
	static final private Color kTableBackgroundColor = new Color(0xDAE4FF);
	static final private Color kComponentColors[] = {new Color(0x009900), Color.red, Color.blue, new Color(0x660033)};
	
	private double meanParam;
	private double errorSD;
	private double[] blockParam;
	private double[] factorParam;
	
	private DataSet data;
	private AnovaSummaryData summaryData;
	
	private NumValue maxSsq, maxMss, maxF;
	
	private NestedFactorView theView;
	
	private XNoValueSlider blockSlider, factorSlider;
	private XCheckbox shadeCheck, zeroResidCheck;
	
	public void setupApplet() {
		data = readData();
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		centerErrorsInBlocks(data);
		
		setLayout(new BorderLayout(0, 10));
			
		add("North", controlPanel());
			
		add("Center", displayPanel(data));
			
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
			
			bottomPanel.add(anovaPanel(data));
			bottomPanel.add(checkPanel());
			
		add("South", bottomPanel);
		
		updateParameters();
	}
	
	private void centerErrorsInBlocks(DataSet data) {
		NumVariable errorVar = (NumVariable)data.getVariable("error");
		CatVariable blockVar = (CatVariable)data.getVariable("block");
		int n = errorVar.noOfValues();
		int nBlocks = blockVar.noOfCategories();
		for (int blockIndex=0 ; blockIndex<nBlocks ; blockIndex++) {
			double sum = 0;
			int noInBlock = 0;
			Value block = blockVar.getLabel(blockIndex);
			for (int i=0 ; i<n ; i++)
				if (blockVar.valueAt(i) == block) {
					noInBlock ++;
					sum += errorVar.doubleValueAt(i);
				}
			double mean = sum / noInBlock;
			for (int i=0 ; i<n ; i++)
				if (blockVar.valueAt(i) == block) {
					NumValue val = (NumValue)errorVar.valueAt(i);
					val.setValue(val.toDouble() - mean);
				}
		}
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
			CatVariable blockVar = new CatVariable(getParameter(BLOCK_NAME_PARAM));
			blockVar.readLabels(getParameter(BLOCK_LABELS_PARAM));
			blockVar.readValues(getParameter(BLOCK_VALUES_PARAM));
			int nBlocks = blockVar.noOfCategories();
		data.addVariable("block", blockVar);
		
			CatVariable factorVar = new CatVariable(getParameter(FACTOR_NAME_PARAM));
			factorVar.readLabels(getParameter(FACTOR_LABELS_PARAM));
			factorVar.readValues(getParameter(FACTOR_VALUES_PARAM));
			int nLevels = factorVar.noOfCategories();
		data.addVariable("factor", factorVar);
		
			int n = blockVar.noOfValues();
			RandomNormal errorGen = new RandomNormal(n, 0, 1, 4);
			NumSampleVariable errorVar = new NumSampleVariable(translate("Error"), errorGen, 9);
//			errorVar.generateNextSample();
//			centerInBlocks(errorVar, blockVar);
		data.addVariable("error", errorVar);
		
			MultipleRegnModel model = new MultipleRegnModel("Model", data, kXGeneratorKeys,
																															getParameter(Y_MODEL_PARAM));
		data.addVariable("model", model);
			
			meanParam = model.getParameter(0).toDouble();
			errorSD = model.evaluateSD().toDouble();
		
			blockParam = new double[nBlocks - 1];
			for (int i=1  ; i<nBlocks ; i++)
				blockParam[i - 1] = model.getParameter(i).toDouble();
		
			factorParam = new double[nLevels - 1];
			for (int i=1  ; i<nLevels ; i++)
				factorParam[i - 1] = model.getParameter(nBlocks + i - 1).toDouble();
		
			ResponseVariable yVar = new ResponseVariable(getParameter(Y_NAME_PARAM), data,
																								kXGeneratorKeys, "error", "model", 9);
		data.addVariable("y", yVar);
		
			int decimals = Integer.parseInt(getParameter(PARAM_DECIMALS_PARAM));
		
			MultipleRegnModel lsFactor = new MultipleRegnModel("LS_factor", data, kFactorKeys);
			lsFactor.setParameterDecimals(decimals);
		data.addVariable("lsFactor", lsFactor);
			
			MultipleRegnModel lsBlock = new MultipleRegnModel("LS_block", data, kBlockKeys);
			lsBlock.setParameterDecimals(decimals);
		data.addVariable("lsBlock", lsBlock);
		
		data.addVariable(kComponentKeys[0], new BasicComponentVariable("Total", data,
														kFactorKeys, "y", "lsFactor", BasicComponentVariable.TOTAL, 9));
		
		data.addVariable(kComponentKeys[1], new BasicComponentVariable("Factor", data,
														kFactorKeys, "y", "lsFactor", BasicComponentVariable.EXPLAINED, 9));
		
		data.addVariable(kComponentKeys[3], new BasicComponentVariable(translate("Residual"), data,
														kBlockKeys, "y", "lsBlock", BasicComponentVariable.RESIDUAL, 9));
		
			FittedValueVariable fitFactor = new FittedValueVariable("Fit_factor", data, kFactorKeys,
																																"lsFactor", 9);
		data.addVariable("fitFactor", fitFactor);
		
			FittedValueVariable fitBlock = new FittedValueVariable("Fit_block", data, kBlockKeys,
																																"lsBlock", 9);
		data.addVariable("fitBlock", fitBlock);
		
		data.addVariable(kComponentKeys[2], new SeqComponentVariable("Blocks within factor", data,
																													"fitBlock", "fitFactor", 9));
		
		return data;
	}
	
	private AnovaSummaryData getSummaryData(DataSet data) {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SUMMARIES_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxMss = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		return new AnovaSummaryData(data, "error", kComponentKeys, maxSsq.decimals, 3);
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			VertAxis yAxis = new VertAxis(this);
			yAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
		thePanel.add("Left", yAxis);
		
			NestedFactorAxis blockAxis = new NestedFactorAxis(this);
			CatVariable blockVar = (CatVariable)data.getVariable("block");
			CatVariable factorVar = (CatVariable)data.getVariable("factor");
			blockAxis.setCatLabels(blockVar, factorVar, kComponentColors[1]);
			blockAxis.setForeground(kComponentColors[2]);
		thePanel.add("Bottom", blockAxis);
			
			theView = new NestedFactorView(data, this, yAxis, blockAxis, "y", "block",
																										"factor", "lsBlock", "lsFactor", kComponentColors);
				theView.lockBackground(Color.white);
				
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private XPanel anovaPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel tablePanel = new InsetPanel(20, 5);
			tablePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				StringTokenizer st = new StringTokenizer(getParameter(COMPONENT_NAMES_PARAM), "#");
				String componentName[] = new String[st.countTokens()];
				for (int i=0 ; i<componentName.length ; i++)
					componentName[i] = st.nextToken();
			
				AnovaTableView tableView = new AnovaTableView(summaryData, this,
											kComponentKeys, maxSsq, maxMss, maxF, AnovaTableView.SSQ_AND_MSSQ);
				tableView.setComponentNames(componentName);
				tableView.setComponentColors(kComponentColors);
			
			tablePanel.add(tableView);
		
			tablePanel.lockBackground(kTableBackgroundColor);
		thePanel.add(tablePanel);
		
		return thePanel;
	}
	
	
	protected XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
			XPanel sliderPanel = new XPanel();
			sliderPanel.setLayout(new ProportionLayout(0.5, 10));
			
				factorSlider = new XNoValueSlider(translate("Low"), translate("High"),
																						translate("Variability between factor levels"), 0, 100, 50, this);
				
			sliderPanel.add(ProportionLayout.LEFT, factorSlider);
			
				blockSlider = new XNoValueSlider(translate("Low"), translate("High"),
																						translate("Variability between blocks"), 0, 100, 50, this);
				
			sliderPanel.add(ProportionLayout.RIGHT, blockSlider);
		
		thePanel.add(sliderPanel);
		
			zeroResidCheck = new XCheckbox(translate("No variation within blocks"), this);
		thePanel.add(zeroResidCheck);
		
		return thePanel;
	}
	
	private XPanel checkPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			shadeCheck = new XCheckbox(translate("Shade components"), this);
		thePanel.add(shadeCheck);
		
		return thePanel;
	}
	
	
	private double averageEffect(double[] param) {
		double sum = 0.0;
		for (int i=0 ; i<param.length ; i++)
			sum += param[i];
		return sum / (param.length + 1);
	}
	
	private void updateParameters() {
		double blockP = blockSlider.getValue() / (double)blockSlider.getMaxValue();
		double factorP = factorSlider.getValue() / (double)factorSlider.getMaxValue();
		
		double oldBlockMean = averageEffect(blockParam);
		double oldFactorMean = averageEffect(factorParam);
		
		double newBlockMean = oldBlockMean * blockP;
		double newFactorMean = oldFactorMean * factorP;
		
		MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
		model.setParameter(0, meanParam + (oldBlockMean - newBlockMean) + (oldFactorMean - newFactorMean));
		
		for (int i=0 ; i<blockParam.length ; i++)
			model.setParameter(i + 1, blockParam[i] * blockP);
		
		for (int i=0 ; i<factorParam.length ; i++)
			model.setParameter(blockParam.length + i + 1, factorParam[i] * factorP);
		
		model.setSD(zeroResidCheck.getState() ? 0.0 : errorSD);
		
		MultipleRegnModel lsFactor = (MultipleRegnModel)data.getVariable("lsFactor");
		lsFactor.updateLSParams("y");
		
		MultipleRegnModel lsBlock = (MultipleRegnModel)data.getVariable("lsBlock");
		lsBlock.updateLSParams("y");
		
		data.variableChanged("y");
		
		summaryData.setSingleSummaryFromData();
	}

	
	private boolean localAction(Object target) {
		if (target == blockSlider || target == factorSlider) {
			updateParameters();
			return true;
		}
		else if (target == shadeCheck) {
			theView.setShadeComponents(shadeCheck.getState());
			theView.repaint();
			return true;
		}
		else if (target == zeroResidCheck) {
			updateParameters();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}