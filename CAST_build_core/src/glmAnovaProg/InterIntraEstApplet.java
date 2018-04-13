package glmAnovaProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import valueList.*;
import formula.*;

import multiRegn.*;
import glmAnova.*;


public class InterIntraEstApplet extends XApplet {
	static final private String MAX_PARAM_PARAM = "maxParam";
	static final private String SHOW_SE_PARAM = "showSe";
	static final private String ALLOW_COMBINE_PARAM = "allowCombine";
	
	static final private Color kEqnColor = new Color(0x990000);
	static final private Color kSeBackground = new Color(0xFFEEEE);
	
	private IncompleteBlockDataSet data;
	private double overallMean;
	private double[] combinedCoeff;
	private boolean showSe, allowCombine;
	
	private XPanel p1LabelPanel, p2LabelPanel, equalsLabelPanel, combinedPanel;
	private CardLayout p1LabelLayout, p2LabelLayout, equalsLabelLayout, combinedLayout;
	
	private XCheckbox combineCheck;
	private XChoice displayChoice;
	private int currentDisplay = 0;
	
	private ValueView intraCoeffView[], interCoeffView[], combinedCoeffView[];
	
	public void setupApplet() {
		showSe = getParameter(SHOW_SE_PARAM).equals("true");
		allowCombine = getParameter(ALLOW_COMBINE_PARAM).equals("true");
		data = readData();
		
		setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 0));
			
		add(estimatesPanel(data));
		
		add(controlPanel());
	}
	
	private IncompleteBlockDataSet readData() {
		IncompleteBlockDataSet data = new IncompleteBlockDataSet(this);
		
		if (showSe) {
			data.addVariable("resid", new BasicComponentVariable(translate("Residual"), data,
																				IncompleteBlockDataSet.kIntraKeys, "y", "intraLS",
																				BasicComponentVariable.RESIDUAL, 9));
		
			data.addVariable("blockAdjust", new SeqComponentVariable("BlockAfterFactor", data,
																													"intraFit", "factorFit", 9));
		}
		
		NumVariable yVar = (NumVariable)data.getVariable("y");
		ValueEnumeration ye = yVar.values();
		int n = 0;
		double sy = 0.0;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			if (!Double.isNaN(y)) {
				n ++;
				sy += y;
			}
		}
		overallMean = sy / n;
		
		return data;
	}
	
	private XPanel hideShowPanel(CardLayout layout, XPanel component) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(layout);
		
		thePanel.add("Hide", new XPanel());
		thePanel.add("Show", component);
		
		return thePanel;
	}
	
	private XPanel estimatesPanel(DataSet data) {
		CatVariable factorVar = (CatVariable)data.getVariable("factor");
		int nLevels = factorVar.noOfCategories();
		int nBlocks = ((CatVariable)data.getVariable("block")).noOfCategories();
		NumValue maxParam = new NumValue(getParameter(MAX_PARAM_PARAM));
		
		double varDiffIntra = 0.0, varDiffInter = 0.0, pIntra = 0.0, varDiffCombined = 0.0;
		if (showSe) {
			double errorVar = getErrorVar(data);
			double blockVar = getBlockVar(data, errorVar);
			
//			System.out.println("errorVar = " + errorVar + ", blockVar = " + blockVar);
			
			varDiffIntra = getVarDiffIntra(data, nBlocks, maxParam, errorVar);
			
			int blockSize = factorVar.noOfValues() / nBlocks;
			double effectiveErrorVar = blockVar * blockSize + errorVar;
			varDiffInter = getVarDiffInter(data, 1, maxParam, effectiveErrorVar);
			if (allowCombine) {
				pIntra = varDiffInter / (varDiffInter + varDiffIntra);
				varDiffCombined = pIntra * pIntra * varDiffIntra + (1 - pIntra) * (1 - pIntra) * varDiffInter;
			}
		}
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		if (allowCombine) {
			XLabel p1Label = new XLabel(new NumValue(pIntra, 3).toString() + " "
																				+ MText.translateUnicode("times") + " ", XLabel.LEFT, this);
			p1Label.setFont(getBigBoldFont());
			p1Label.setForeground(kEqnColor);
			
			p1LabelLayout = new CardLayout();
			p1LabelPanel = hideShowPanel(p1LabelLayout, p1Label);
			
			thePanel.add(p1LabelPanel);
		}
		
			intraCoeffView = getCoeffViews(data, "intraLS", nBlocks - 1, nLevels, maxParam);
		thePanel.add(coeffSetPanel(translate("Intra-block"), translate("estimates"), intraCoeffView, varDiffIntra, maxParam, showSe));
		
		if (allowCombine) {
			XLabel p2Label = new XLabel(" + " + new NumValue(1 - pIntra, 3).toString() + " "
																				+ MText.translateUnicode("times") + " ", XLabel.LEFT, this);
			p2Label.setFont(getBigBoldFont());
			p2Label.setForeground(kEqnColor);
			
			p2LabelLayout = new CardLayout();
			p2LabelPanel = hideShowPanel(p2LabelLayout, p2Label);
			
			thePanel.add(p2LabelPanel);
		}
		
			interCoeffView = getCoeffViews(data, "interLS", 0, nLevels, maxParam);
		thePanel.add(coeffSetPanel(translate("Inter-block"), translate("estimates"), interCoeffView, varDiffInter, maxParam, showSe));
		
		if (allowCombine) {
				XLabel equalsLabel = new XLabel(" = ", XLabel.LEFT, this);
				equalsLabel.setFont(getBigBoldFont());
				equalsLabel.setForeground(kEqnColor);
				
				equalsLabelLayout = new CardLayout();
				equalsLabelPanel = hideShowPanel(equalsLabelLayout, equalsLabel);
			thePanel.add(equalsLabelPanel);
			
				combinedCoeffView = getCombinedViews(data, pIntra, nBlocks, nLevels, maxParam);
				
				combinedLayout = new CardLayout();
				combinedPanel = hideShowPanel(combinedLayout, coeffSetPanel(translate("Combined"),
													translate("estimates"), combinedCoeffView, varDiffCombined, maxParam, showSe));
			thePanel.add(combinedPanel);
		}
		
		return thePanel;
	}
	
	private ValueView[] getCoeffViews(DataSet data, String modelKey, int startCoeff, int nLevels,
																														NumValue maxParam) {
		ValueView[] theViews = new ValueView[nLevels];
		
		CatVariable factorVar = (CatVariable)data.getVariable("factor");
		theViews[0] = new FixedValueView(factorVar.getLabel(0).toString(), maxParam, 0, this);
		
		for (int i=1 ; i<nLevels ; i++)
			theViews[i] = new CoefficientView(data, this, modelKey, maxParam,
																					startCoeff + i, factorVar.getLabel(i).toString(), null);
		
		return theViews;
	}
	
	private ValueView[] getCombinedViews(DataSet data, double pIntra, int nBlocks, int nLevels,
																														NumValue maxParam) {
		ValueView[] theViews = new ValueView[nLevels];
		
		CatVariable factorVar = (CatVariable)data.getVariable("factor");
		theViews[0] = new FixedValueView(factorVar.getLabel(0).toString(), maxParam, 0, this);
		
		MultipleRegnModel intraModel = (MultipleRegnModel)data.getVariable("intraLS");
		MultipleRegnModel interModel = (MultipleRegnModel)data.getVariable("interLS");
		
		combinedCoeff = new double[nLevels];
		
		for (int i=1 ; i<nLevels ; i++) {
			double interCoeff = interModel.getParameter(i).toDouble();
			double intraCoeff = intraModel.getParameter(nBlocks + i - 1).toDouble();
			combinedCoeff[i] = pIntra * intraCoeff + (1 - pIntra) * interCoeff;
			theViews[i] = new FixedValueView(factorVar.getLabel(i).toString(), maxParam, combinedCoeff[i], this);
		}
		
		return theViews;
	}
	
	private XPanel coeffSetPanel(String heading1, String heading2, ValueView[] coeffView,
																											double varDiff, NumValue maxParam, boolean showSe) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, 8));
		
			XPanel headingPanel = new XPanel();
			headingPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, -4));
			
				XLabel heading1Label = new XLabel(heading1, XLabel.CENTER, this);
				heading1Label.setFont(getBigBoldFont());
			headingPanel.add(heading1Label);
			
				XLabel heading2Label = new XLabel(heading2, XLabel.CENTER, this);
				heading2Label.setFont(getBigBoldFont());
			headingPanel.add(heading2Label);
			
		thePanel.add(headingPanel);
		
			XPanel coeffPanel = new XPanel();
			coeffPanel.setLayout(new VerticalLayout(VerticalLayout.RIGHT, VerticalLayout.VERT_TOP, 3));
			
			for (int i=0 ; i<coeffView.length ; i++) {
				coeffView[i].setAlignRight();
				coeffPanel.add(coeffView[i]);
			}
		thePanel.add(coeffPanel);
		
		if (showSe)
			thePanel.add(sePanel(varDiff, maxParam));
		
		return thePanel;
	}
	
	private XPanel sePanel(double varDiff, NumValue maxParam) {
		XPanel thePanel = new InsetPanel(8, 3);
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, 3));
		
				XLabel headingLabel = new XLabel(translate("se(diff)"), XLabel.CENTER, this);
				headingLabel.setFont(getBigBoldFont());
			innerPanel.add(headingLabel);
			
				FixedValueView seView = new FixedValueView(null, maxParam, Math.sqrt(varDiff), this);
				seView.setAlignCenter();
			innerPanel.add(seView);
		
		thePanel.lockBackground(kSeBackground);
		thePanel.add("Center", innerPanel);
		return thePanel;
	}
	
	private double getErrorVar(DataSet data) {
		CoreComponentVariable residComp = (CoreComponentVariable)data.getVariable("resid");
		double ssq = residComp.getSsq();
		int df = residComp.getDF();
		return ssq / df;
	}
	
	private double getBlockVar(DataSet data, double errorVar) {
		CoreComponentVariable blockComp = (CoreComponentVariable)data.getVariable("blockAdjust");
		double ssq = blockComp.getSsq();
		int df = blockComp.getDF();
		double msq = ssq / df;
		
		CatVariable blockVar = (CatVariable)data.getVariable("block");
		int nBlocks = blockVar.noOfCategories();
		CatVariable factorVar = (CatVariable)data.getVariable("factor");
		int nLevels = factorVar.noOfCategories();
		int nReps = factorVar.noOfValues() / nLevels;
		
		return Math.max(0.0, (msq - errorVar) * (nBlocks - 1) / nLevels / (nReps - 1));
	}
	
	private double getVarDiffIntra(DataSet data, int index, NumValue maxParam, double errorVar) {
		MultipleRegnModel lsIntra = (MultipleRegnModel)data.getVariable("intraLS");
		double coeffVar[] = lsIntra.getCoeffVariances("y", true, errorVar);
		return coeffVar[(index + 1) * (index + 2) / 2 - 1];
	}
	
	private double getVarDiffInter(DataSet data, int index, NumValue maxParam, double effectiveVar) {
		MultipleRegnModel lsFactor = (MultipleRegnModel)data.getVariable("interLS");
		double coeffVar[] = lsFactor.getCoeffVariances("y", true, effectiveVar);
		return coeffVar[(index + 1) * (index + 2) / 2 - 1];
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			displayChoice = new XChoice(translate("Display"), XChoice.HORIZONTAL, this);
			displayChoice.addItem(translate("Estimated coefficients"));
			displayChoice.addItem(translate("Estimated means"));
		thePanel.add(displayChoice);
		
		if (allowCombine) {
			combineCheck = new XCheckbox(translate("Combine estimates"), this);
			thePanel.add(combineCheck);
		}
		
		return thePanel;
	}
	
	
	private void setCoeffOffsets(ValueView[] coeffView, boolean useOffset, String modelKey,
																												int startCoeff, int nLevels, int nBlocks) {
		MultipleRegnModel model = (MultipleRegnModel)data.getVariable(modelKey);
		double sumCoeff = 0.0;
		for (int i=1 ; i<nLevels ; i++)
			sumCoeff += model.getParameter(startCoeff + i - 1).toDouble();
		
		double offset = overallMean - sumCoeff / nLevels;
		
		for (int i=0 ; i<coeffView.length ; i++)
			if (coeffView[i] instanceof FixedValueView)
				((FixedValueView)coeffView[i]).setValue(useOffset ? offset : 0.0);
			else {
				((CoefficientView)coeffView[i]).setCoeffOffset(useOffset ? offset : Double.NaN);
				coeffView[i].repaint();
			}
	}
	
	
	private void setCombinedOffsets(ValueView[] coeffView, boolean useOffset, int nLevels) {
		double sumCoeff = 0.0;
		for (int i=1 ; i<nLevels ; i++)
			sumCoeff += combinedCoeff[i];
		
		double offset = overallMean - sumCoeff / nLevels;
		for (int i=0 ; i<coeffView.length ; i++)
			((FixedValueView)coeffView[i]).setValue(combinedCoeff[i] + (useOffset ? offset : 0.0));
	}
	
	private boolean localAction(Object target) {
		if (target == displayChoice) {
			int newChoice = displayChoice.getSelectedIndex();
			if (newChoice != currentDisplay) {
				currentDisplay = newChoice;
				int nLevels = ((CatVariable)data.getVariable("factor")).noOfCategories();
				int nBlocks = ((CatVariable)data.getVariable("block")).noOfCategories();
				setCoeffOffsets(interCoeffView, newChoice == 1, "interLS", 1, nLevels, nBlocks);
				setCoeffOffsets(intraCoeffView, newChoice == 1, "intraLS", nBlocks, nLevels, nBlocks);
				if (allowCombine)
					setCombinedOffsets(combinedCoeffView, newChoice == 1, nLevels);
			}
			return true;
		}
		else if (target == combineCheck) {
			String showHide = combineCheck.getState() ? "Show" : "Hide";
			p1LabelLayout.show(p1LabelPanel, showHide);
			p2LabelLayout.show(p2LabelPanel, showHide);
			equalsLabelLayout.show(equalsLabelPanel, showHide);
			combinedLayout.show(combinedPanel, showHide);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}

}