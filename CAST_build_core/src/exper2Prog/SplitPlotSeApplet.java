package exper2Prog;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import models.*;
import valueList.*;

import exper2.*;


public class SplitPlotSeApplet extends XApplet {
	static final private String BLOCK_NAME_PARAM = "blockName";
	static final private String BLOCK_VALUES_PARAM = "blockValues";
	static final private String BLOCK_LABELS_PARAM = "blockLabels";
	static final private String Y_NAME_PARAM = "yName";
	static final private String Y_MODEL_PARAM = "yModel";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	
	static final private String NO_OF_LEVELS_PARAM = "noOfLevels";
	static final private String MAX_SE_PARAM = "maxSe";
	
	static final private String[] kBlockKeys = {"block"};
	
	static final private Color kSeBackgroundColor = new Color(0xDAE4FF);
	static final private Color kTitleColor = new Color(0x660000);
	
	private double overallMean;
	private double errorSD, blockSD;
	private int nBlocks;
	private double blockMean[];
	
	private DataSet data;
	
	private SplitPlotView theView;
	
	private FixedValueView plotSeValueView, subplotSeValueView;
	
	private XNoValueSlider blockSlider, errorSlider;
	private XButton sampleButton;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 10));
			
		add("North", controlPanel());
		add("Center", displayPanel(data));
		add("South", sePanel(data));
		
		newExperiment(data);
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
			CatVariable blockVar = new CatVariable(getParameter(BLOCK_NAME_PARAM));
			blockVar.readLabels(getParameter(BLOCK_LABELS_PARAM));
			blockVar.readValues(getParameter(BLOCK_VALUES_PARAM));
			nBlocks = blockVar.noOfCategories();
		data.addVariable("block", blockVar);
		
			int n = blockVar.noOfValues();
			RandomNormal errorGen = new RandomNormal(n, 0, 1, 4);
			NumSampleVariable errorVar = new NumSampleVariable(translate("Error"), errorGen, 9);
		data.addVariable("error", errorVar);
		
			StringTokenizer st = new StringTokenizer(getParameter(Y_MODEL_PARAM));
			overallMean = Double.parseDouble(st.nextToken());
			blockSD = Double.parseDouble(st.nextToken());
			errorSD = Double.parseDouble(st.nextToken());
			MultipleRegnModel model = new MultipleRegnModel("Model", data, kBlockKeys);
			model.setParameterDecimals(9);
		data.addVariable("model", model);
		
			ResponseVariable yVar = new ResponseVariable(getParameter(Y_NAME_PARAM), data,
																								kBlockKeys, "error", "model", 9);
		data.addVariable("y", yVar);
			
			MultipleRegnModel lsBlock = new MultipleRegnModel("LS_block", data, kBlockKeys);
			lsBlock.setParameterDecimals(9);
		data.addVariable("lsBlock", lsBlock);
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			VertAxis yAxis = new VertAxis(this);
			yAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
		thePanel.add("Left", yAxis);
		
			HorizAxis blockAxis = new HorizAxis(this);
			CatVariable blockVar = (CatVariable)data.getVariable("block");
			blockAxis.setCatLabels(blockVar);
		thePanel.add("Bottom", blockAxis);
			
			theView = new SplitPlotView(data, this, yAxis, blockAxis, "y", "block", "lsBlock");
			theView.lockBackground(Color.white);
				
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private XPanel oneSePanel(boolean isBlockLevel, NumValue maxSe, String title) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			XLabel titleLabel = new XLabel(title, XLabel.CENTER, this);
			titleLabel.setFont(getBigBoldFont());
		thePanel.add(titleLabel);
		
			FixedValueView theView = new FixedValueView(translate("se") + " =", maxSe, 0.0, this);
			if (isBlockLevel)
				plotSeValueView = theView;
			else
				subplotSeValueView = theView;
		theView.setFont(getBigBoldFont());
		thePanel.add(theView);
		
		return thePanel;
	}
	
	private XPanel sePanel(DataSet data) {
		XPanel thePanel = new InsetPanel(20, 4);
		thePanel.setLayout(new BorderLayout(0, 6));
		
			XLabel topLabel = new XLabel(translate("SE for factor whose levels") + "...", XLabel.CENTER, this);
			topLabel.setFont(getBigBoldFont());
			topLabel.setForeground(kTitleColor);
			
		thePanel.add("North", topLabel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new ProportionLayout(0.5, 20));
				
				NumValue maxSe = new NumValue(getParameter(MAX_SE_PARAM));
			bottomPanel.add(ProportionLayout.LEFT, oneSePanel(true, maxSe, "... " + translate("vary between blocks")));
				
			bottomPanel.add(ProportionLayout.RIGHT, oneSePanel(false, maxSe, "... " + translate("vary within blocks")));
		
		thePanel.add("Center", bottomPanel);
		
		thePanel.lockBackground(kSeBackgroundColor);
		return thePanel;
	}
	
	
	protected XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
			XPanel sliderPanel = new XPanel();
			sliderPanel.setLayout(new ProportionLayout(0.5, 10));
			
				blockSlider = new XNoValueSlider(translate("low"), translate("high"), translate("Between-block variability"), 0, 100, 50, this);
				
			sliderPanel.add(ProportionLayout.LEFT, blockSlider);
			
				errorSlider = new XNoValueSlider(translate("low"), translate("high"), translate("Within-block variability"), 0, 100, 50, this);
				
			sliderPanel.add(ProportionLayout.RIGHT, errorSlider);
		
		thePanel.add("Center", sliderPanel);
		
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				StringTokenizer st = new StringTokenizer(translate("Another*Experiment"), "*");
				sampleButton = new XButton(st.nextToken() + "\n" + st.nextToken(), this);
			buttonPanel.add(sampleButton);
			
		thePanel.add("East", buttonPanel);
		
		return thePanel;
	}
	
	
	private void newExperiment(DataSet data) {
		NumSampleVariable errorVar = (NumSampleVariable)data.getVariable("error");
		errorVar.generateNextSample();
		
		RandomNormal blockGen = new RandomNormal(nBlocks, overallMean, blockSD, 4);
		blockMean = blockGen.generate();
		
		updateParameters();
	}
	
	private double averageEffect(double[] param) {
		double sum = 0.0;
		for (int i=0 ; i<param.length ; i++)
			sum += param[i];
		return sum / (param.length + 1);
	}
	
	private void updateParameters() {
		double blockP = blockSlider.getValue() / (double)blockSlider.getMaxValue();
		double oldBlockMean = averageEffect(blockMean);
		
		double newBlockMean[] = new double[nBlocks];
		for (int i=0 ; i<nBlocks ; i++)
			newBlockMean[i] = oldBlockMean * (1 - blockP) + blockMean[i] * blockP;
		
		MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
		model.setParameter(0, newBlockMean[0]);
		
		for (int i=1 ; i<nBlocks ; i++)
			model.setParameter(i, newBlockMean[i] - newBlockMean[0]);
		
		double errorP = errorSlider.getValue() / (double)errorSlider.getMaxValue();
		model.setSD(errorP * errorSD);
		
		MultipleRegnModel lsBlock = (MultipleRegnModel)data.getVariable("lsBlock");
		lsBlock.updateLSParams("y");
		
		data.variableChanged("y");
		
		CatVariable blockVar = (CatVariable)data.getVariable("block");
		int nUnitsPerBlock = blockVar.noOfValues() / nBlocks;
		StringTokenizer st = new StringTokenizer(getParameter(NO_OF_LEVELS_PARAM));
		int plotReps = nBlocks / Integer.parseInt(st.nextToken());
		int subReps = nUnitsPerBlock / Integer.parseInt(st.nextToken());
		double subDiffVar = 2.0 * (errorP * errorP * errorSD * errorSD) / (nBlocks * subReps);
		double plotDiffVar = subDiffVar + 2.0 * (blockP * blockP * blockSD * blockSD) / plotReps;
		
		plotSeValueView.setValue(Math.sqrt(plotDiffVar));
		subplotSeValueView.setValue(Math.sqrt(subDiffVar));
	}

	
	private boolean localAction(Object target) {
		if (target == blockSlider || target == errorSlider) {
			updateParameters();
			return true;
		}
		else if (target == sampleButton) {
			newExperiment(data);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}