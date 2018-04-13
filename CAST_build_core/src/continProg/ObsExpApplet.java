package continProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;
import imageUtils.*;

import contin.*;


public class ObsExpApplet extends XApplet {
	static final public String DATA_NAME_PARAM = "dataName";

	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String X_VAR_NAME_PARAM = "xVarName";
	
	static final private String Y_LABELS_PARAM = "yLabels";
	static final private String X_LABELS_PARAM = "xLabels";
	
	static final private String X_MARGIN_PARAM = "xMargin";
	static final private String Y_CONDIT_PARAM = "yCondit";
	
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final private String MAX_EXPECTED_PARAM = "maxExpected";
	static final private String MAX_DIFF_PARAM = "maxDiff";
	static final protected String MAX_CHI2_PARAM = "maxChi2";
	
	static final private String CHI2_TYPE_PARAM = "chi2Type";
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	private String[] dataName;
	private String[] xMarginString;
	private String[] yConditString;
	
	private RandomMultinomial generator;
	
	private int[] sampleSize;
	protected XButton sampleButton;
	private XChoice sampleSizeChoice;
	private int currentSampleSizeIndex = 0;
	
	private XChoice dataSetChoice;
	private int currentDataSetIndex = 0;
	
	protected ObsExpTableView oeView;
	
	public void setupApplet() {
		checkDataSets();
		
		data = readData();
		
		setLayout(new BorderLayout(10, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new ProportionLayout(0.55, 0, ProportionLayout.VERTICAL,
																							ProportionLayout.TOTAL));
			leftPanel.add(ProportionLayout.TOP, dataPanel(data));
			leftPanel.add(ProportionLayout.BOTTOM, diffPanel(data));
		
		add("West", leftPanel);
			
		summaryData = createSummaryData(data);
		summaryData.takeSample();
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new ProportionLayout(0.55, 0, ProportionLayout.VERTICAL,
																							ProportionLayout.TOTAL));
			rightPanel.add(ProportionLayout.TOP, controlPanel(data));
			rightPanel.add(ProportionLayout.BOTTOM, chi2Panel(data));
		
		add("Center", rightPanel);
	}
	
	protected void checkDataSets() {
		int nDataSets = 1;
		while (getParameter(DATA_NAME_PARAM + (nDataSets + 1)) != null)
			nDataSets ++;
		
		dataName = new String[nDataSets];
		xMarginString = new String[nDataSets];
		yConditString = new String[nDataSets];
		
		dataName[0] = getParameter(DATA_NAME_PARAM);
		xMarginString[0] = getParameter(X_MARGIN_PARAM);
		yConditString[0] = getParameter(Y_CONDIT_PARAM);
		
		for (int i=1 ; i<nDataSets ; i++) {
			dataName[i] = getParameter(DATA_NAME_PARAM + (i + 1));
			xMarginString[i] = getParameter(X_MARGIN_PARAM + (i + 1));
			yConditString[i] = getParameter(Y_CONDIT_PARAM + (i + 1));
		}
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		CatDistnVariable xModelVar = new CatDistnVariable(getParameter(X_VAR_NAME_PARAM));
		xModelVar.readLabels(getParameter(X_LABELS_PARAM));
		xModelVar.setParams(xMarginString[0]);
		data.addVariable("xModel", xModelVar);
		
		ContinResponseVariable yModelVar = new ContinResponseVariable(
														getParameter(Y_VAR_NAME_PARAM), data, "xModel");
		yModelVar.readLabels(getParameter(Y_LABELS_PARAM));
		yModelVar.setProbs(yConditString[0], ContinResponseVariable.CONDITIONAL);
		data.addVariable("yModel", yModelVar);
		
		CatVariable xVar = new CatVariable(getParameter(X_VAR_NAME_PARAM));
		xVar.readLabels(getParameter(X_LABELS_PARAM));
		data.addVariable("x", xVar);
		
		StringTokenizer st = new StringTokenizer(getParameter(SAMPLE_SIZE_PARAM));
		sampleSize = new int[st.countTokens()];
		for (int i=0 ; i<sampleSize.length ; i++) {
			String nextSize = st.nextToken();
			if (nextSize.indexOf("*") == 0) {
				nextSize = nextSize.substring(1);
				currentSampleSizeIndex = i;
			}
			sampleSize[i] = Integer.parseInt(nextSize);
		}
		
		double p[] = getGeneratorProbs(xModelVar, yModelVar);
		
		generator = new RandomMultinomial(sampleSize[currentSampleSizeIndex], p);
		BiCatSampleVariable yVar = new BiCatSampleVariable(getParameter(Y_VAR_NAME_PARAM),
															generator, Variable.USES_REPEATS, data, "x");
		yVar.readLabels(getParameter(Y_LABELS_PARAM));
		data.addVariable("y", yVar);
		
		return data;
	}
	
	protected SummaryDataSet createSummaryData(DataSet data) {
		return new SummaryDataSet(data, "y");
	}
	
	private double[] getGeneratorProbs(CatDistnVariable xModelVar, ContinResponseVariable yModelVar) {
		int nXCats = xModelVar.noOfCategories();
		int nYCats = yModelVar.noOfCategories();
		double p[] = new double[nXCats * nYCats];
		double[][] yConditXProb = yModelVar.getConditionalProbs();
		double[] xMarginalProb = xModelVar.getProbs();
		for (int i=0 ; i< nXCats ; i++)
			for (int j=0 ; j<nYCats ; j++)
				p[i * nYCats + j] = xMarginalProb[i] * yConditXProb[i][j];
		return p;
	}
	
	protected XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
		
		NumValue maxExpected = new NumValue(getParameter(MAX_EXPECTED_PARAM));
		
		oeView = new ObsExpTableView(data, this, "y", "x", maxExpected);
		thePanel.add(oeView);
		
		return thePanel;
	}
	
	protected XPanel diffPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
		
		thePanel.add(new ImageCanvas("chi2/diff.png", this));
		
		NumValue maxDiff = new NumValue(getParameter(MAX_DIFF_PARAM));
		thePanel.add(new ObsMinusExpView(data, this, oeView, maxDiff));
		
		return thePanel;
	}
	
	protected XPanel chi2Panel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
		
			String chi2TypeString = getParameter(CHI2_TYPE_PARAM);
			int chi2Type = ((chi2TypeString != null) && !chi2TypeString.equals("correct"))
									? Chi2ValueView.NOT_OVER_EXPECTED : Chi2ValueView.OVER_EXPECTED;
		
			NumValue maxChi2 = new NumValue(getParameter(MAX_CHI2_PARAM));
		thePanel.add(new Chi2ValueView(data, this, oeView, chi2Type, maxChi2));
		
		return thePanel;
	}
	
	protected XPanel sampleSizePanel() {
		XPanel sizePanel = new XPanel();
		sizePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
			XLabel sampleLabel = new XLabel(translate("Sample size") + ":", XLabel.RIGHT, this);
			sampleLabel.setForeground(Color.red);
			sampleLabel.setFont(getStandardBoldFont());
		sizePanel.add(sampleLabel);
			
			sampleSizeChoice = new XChoice(this);
			for (int i=0 ; i<sampleSize.length ; i++)
				sampleSizeChoice.addItem(String.valueOf(sampleSize[i]));
			sampleSizeChoice.select(currentSampleSizeIndex);
		sizePanel.add(sampleSizeChoice);
		return sizePanel;
	}
	
	protected XButton takeSampleButton(boolean repeating) {
		sampleButton = repeating ? new RepeatingButton(translate("Take sample"), this)
											: new XButton(translate("Take sample"), this);
		return sampleButton;
	}
	
	protected void addMarginChoice(XPanel thePanel) {
		if (dataName.length > 1) {
			XPanel dataSetPanel = new XPanel();
			dataSetPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
				XLabel modelLabel = new XLabel(translate("Model") + ":", XLabel.RIGHT, this);
				modelLabel.setForeground(Color.red);
				modelLabel.setFont(getStandardBoldFont());
			dataSetPanel.add(modelLabel);
				
				dataSetChoice = new XChoice(this);
				for (int i=0 ; i<dataName.length ; i++)
					dataSetChoice.addItem(dataName[i]);
				dataSetChoice.select(currentDataSetIndex);
			dataSetPanel.add(dataSetChoice);
			
			thePanel.add(dataSetPanel);
		}
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 8));
		
		thePanel.add(sampleSizePanel());
		
		addMarginChoice(thePanel);
		
		thePanel.add(takeSampleButton(false));
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == sampleSizeChoice) {
			if (sampleSizeChoice.getSelectedIndex() != currentSampleSizeIndex) {
				currentSampleSizeIndex = sampleSizeChoice.getSelectedIndex();
				summaryData.changeSampleSize(sampleSize[currentSampleSizeIndex]);
			}
			return true;
		}
		else if (target == dataSetChoice) {
			if (dataSetChoice.getSelectedIndex() != currentDataSetIndex) {
				currentDataSetIndex = dataSetChoice.getSelectedIndex();
				
				CatDistnVariable xModelVar = (CatDistnVariable)data.getVariable("xModel");
				ContinResponseVariable yModelVar = (ContinResponseVariable)data.getVariable("yModel");
				
				xModelVar.setParams(xMarginString[currentDataSetIndex]);
				yModelVar.setProbs(yConditString[currentDataSetIndex], ContinResponseVariable.CONDITIONAL);
				
				double p[] = getGeneratorProbs(xModelVar, yModelVar);
				generator.setProbs(p);
				
				summaryData.clearData();
				summaryData.takeSample();
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