package continProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;

import contin.*;


public class SampleContinApplet extends XApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String X_VAR_NAME_PARAM = "xVarName";
	
	static final private String Y_LABELS_PARAM = "yLabels";
	static final private String X_LABELS_PARAM = "xLabels";
	
	static final private String X_MARGIN_PARAM = "xMargin";
	static final private String Y_CONDIT_PARAM = "yCondit";
	
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final private String MAX_COUNT_PARAM = "maxCount";
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	private RandomMultinomial generator;
	
	private int[] sampleSize;
	private XButton sampleButton;
	private XChoice sampleSizeChoice;
	private int currentSampleSizeIndex = 0;
	
	public void setupApplet() {
		data = readData();
		summaryData = new SummaryDataSet(data, "y");
		summaryData.takeSample();
		
		setLayout(new BorderLayout(0, 30));
		add("North", modelPanel(data));
		add("Center", dataPanel(data));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
		CatDistnVariable xModelVar = new CatDistnVariable(getParameter(X_VAR_NAME_PARAM));
		xModelVar.readLabels(getParameter(X_LABELS_PARAM));
		xModelVar.setParams(getParameter(X_MARGIN_PARAM));
		data.addVariable("xModel", xModelVar);
		
		ContinResponseVariable yModelVar = new ContinResponseVariable(
														getParameter(Y_VAR_NAME_PARAM), data, "xModel");
		yModelVar.readLabels(getParameter(Y_LABELS_PARAM));
		yModelVar.setProbs(getParameter(Y_CONDIT_PARAM), ContinResponseVariable.CONDITIONAL);
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
		
		int nXCats = xModelVar.noOfCategories();
		int nYCats = yModelVar.noOfCategories();
		double p[] = new double[nXCats * nYCats];
		double[][] yConditXProb = yModelVar.getConditionalProbs();
		double[] xMarginalProb = xModelVar.getProbs();
		for (int i=0 ; i< nXCats ; i++)
			for (int j=0 ; j<nYCats ; j++)
				p[i * nYCats + j] = xMarginalProb[i] * yConditXProb[i][j];
		
		generator = new RandomMultinomial(sampleSize[currentSampleSizeIndex], p);
		BiCatSampleVariable yVar = new BiCatSampleVariable(getParameter(Y_VAR_NAME_PARAM),
															generator, Variable.USES_REPEATS, data, "x");
		yVar.readLabels(getParameter(Y_LABELS_PARAM));
		data.addVariable("y", yVar);
		
		return data;
	}
	
	private XPanel modelPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
		JointView modelView = new JointView(data, this, "yModel", "xModel", 3);
		modelView.setForeground(Color.blue);
		thePanel.add(modelView);
		
		thePanel.add(controlPanel(data));
		
		return thePanel;
	}
	
	private XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
		
		int maxCount = Integer.parseInt(getParameter(MAX_COUNT_PARAM));
		
		int leftDigits = 0;
		while (maxCount > 9) {
			leftDigits ++;
			maxCount /= 10;
		}
		
		thePanel.add(new DataJointView(data, this, "y", "x", -leftDigits));
		
		thePanel.add(new DataJointView(data, this, "y", "x", 3));
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 8));
		
		XLabel sampleLabel = new XLabel(translate("Sample size") + ":", XLabel.RIGHT, this);
		sampleLabel.setForeground(Color.red);
		sampleLabel.setFont(getStandardBoldFont());
		thePanel.add(sampleLabel);
		
		sampleSizeChoice = new XChoice(this);
		for (int i=0 ; i<sampleSize.length ; i++)
			sampleSizeChoice.addItem(String.valueOf(sampleSize[i]));
		sampleSizeChoice.select(currentSampleSizeIndex);
		thePanel.add(sampleSizeChoice);
		
		sampleButton = new XButton(translate("Take sample"), this);
		thePanel.add(sampleButton);
		
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
				generator.setSampleSize(sampleSize[currentSampleSizeIndex]);
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