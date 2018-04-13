package randomisationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import distn.*;
import coreGraphics.*;
import random.RandomMultinomial;
import coreSummaries.*;

import cat.*;


public class SuccessSimApplet extends CoreSimApplet {
	static final private String PROPN_DECIMALS_PARAM = "propnDecimals";
	static final private String NORMAL_PARAM_DECIMALS_PARAM = "normalDecimals";
	static final private String THEORY_TYPE_PARAM = "theoryType";
	
	static final private String N_SUCCESS_PARAM = "nSuccess";
	
	static final private String PROB_LABEL_PARAM = "probLabel";
	static final private String N_LABEL_PARAM = "nLabel";
	
	private boolean useNormalApprox = true;
	private XChoice summaryDisplayChoice;
	private int currentSummaryDisplay = 0;
	
	private NumValue pSuccess;
	private int actualSuccess;
	
	protected void addPanels(XPanel samplePanel, XPanel summaryPanel) {
		setLayout(new BorderLayout(0, 30));
		add("North", samplePanel);
		
		add("Center", summaryPanel);
	}
	
	protected void readParameters() {
		super.readParameters();
		
		StringTokenizer st = new StringTokenizer(getParameter(SAMPLE_INFO_PARAM));
		sampleSize = Integer.parseInt(st.nextToken());
		pSuccess = new NumValue(st.nextToken());
		
		actualSuccess = Integer.parseInt(getParameter(N_SUCCESS_PARAM));
		
		String theoryTypeString = getParameter(THEORY_TYPE_PARAM);
		useNormalApprox = (theoryTypeString != null) && theoryTypeString.equals("normal");
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		double p[] = new double[2];
		p[0] = pSuccess.toDouble();
		p[1] = 1.0 - p[0];
		RandomMultinomial generator = new RandomMultinomial(sampleSize, p);
		
		CatSampleVariable sv = new CatSampleVariable(getParameter(CAT_NAME_PARAM), generator);
		sv.readLabels(getParameter(CAT_LABELS_PARAM));
		data.addVariable("y", sv);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
			CountVariable count = new CountVariable(translate("No of Success"), "y");
		summaryData.addVariable("stat", count);
		
		if (showTheory) {
			if (useNormalApprox) {
				NormalDistnVariable theory = new NormalDistnVariable(translate("Theory"));
				theory.setMean(pSuccess.toDouble() * sampleSize);
				theory.setSD(Math.sqrt(sampleSize * pSuccess.toDouble() * (1.0 - pSuccess.toDouble())));
				
				summaryData.addVariable("theory", theory);
			}
			else {
				BinomialDistnVariable theory = new BinomialDistnVariable(translate("Theory"));
				theory.setProb(pSuccess.toDouble());
				theory.setCount(sampleSize);
				
				summaryData.addVariable("theory", theory);
			}
		}
		
		return summaryData;
	}
	
	protected XPanel modelInfoPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			String pLabel = getParameter(PROB_LABEL_PARAM);
			XLabel probText = new XLabel(pLabel + " = " + pSuccess.toString(), XLabel.CENTER, this);
			probText.setFont(getStandardBoldFont());
			probText.setForeground(kDarkRed);
		thePanel.add(probText);
		
			String nLabel = getParameter(N_LABEL_PARAM);
			XLabel sampleSizeText = new XLabel(nLabel + " " + sampleSize, XLabel.CENTER, this);
			sampleSizeText.setFont(getStandardBoldFont());
			sampleSizeText.setForeground(kDarkRed);
		
		thePanel.add(sampleSizeText);
		
		return thePanel;
	}
	
	protected XPanel sampleViewPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.6, 5));
		
		thePanel.add(ProportionLayout.LEFT, dataTablePanel(data));
		thePanel.add(ProportionLayout.RIGHT, dataPieView(data));
		
		return thePanel;
	}
	
	private DataView dataPieView(DataSet data) {
		return new PieView(data, this, "y", CatDataView.SELECT_ONE);
	}
	
	protected XPanel dataTablePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
		CatVariable v = (CatVariable)data.getVariable("y");
		XLabel varName = new XLabel(v.name, XLabel.CENTER, this);
		varName.setFont(getStandardBoldFont());
		thePanel.add(varName);
		
		int decimals = Integer.parseInt(getParameter(PROPN_DECIMALS_PARAM));
		FreqTableView tableView = new FreqTableView(data, this, "y", CatDataView.SELECT_ONE, decimals);
		
		thePanel.add(tableView);
		
		return thePanel;
	}
	
	protected String summaryName(DataSet data) {
		CatSampleVariable sv = (CatSampleVariable)data.getVariable("y");
		return translate("Number of") + " '" + sv.getLabel(0).toString() + "'";
	}
	
	protected DataView getSummaryView(DataSet summaryData, HorizAxis summaryAxis) {
		StackedDiscreteView theView = null;
		if (!showTheory)
			theView = new StackedDiscreteView(summaryData, this, summaryAxis, "stat");
		else if (useNormalApprox) {
			theView = new DiscretePlusNormView(summaryData, this, summaryAxis, "stat", "theory");
			((DiscretePlusNormView)theView).setInverseColors();
		}
		else
			theView = new DiscretePlusBinomView(summaryData, this, summaryAxis, "stat", "theory");
		
		if (tailType == LOW_TAIL)
			theView.setCrossHighlight(actualSuccess + 0.5, StackedDiscreteView.LOW_HIGHLIGHT);
		else
			theView.setCrossHighlight(actualSuccess - 0.5, StackedDiscreteView.HIGH_HIGHLIGHT);
		
		theView.setHighlightBackground(true);
		return theView;
	}
	
	protected String getMenuTheoryString() {
		return useNormalApprox ? translate("Normal approximation") : translate("Binomial distribution");
	}
	
	protected String extremePropnString() {
		return translate("Propn with") + " " + actualSuccess + " " + (tailType == LOW_TAIL ? translate("or fewer") : translate("or more"));
	}
	
	protected double lowCutOff() {
		return (tailType == LOW_TAIL) ? (actualSuccess + 0.5) : Double.NEGATIVE_INFINITY;
	}
	
	protected double highCutOff() {
		return (tailType == HIGH_TAIL) ? (actualSuccess - 0.5) : Double.POSITIVE_INFINITY;
	}
	
	protected XPanel simulationResultsPanel(DataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
			
			choicePanel.add(new XLabel(translate("Show results as") + "...", XLabel.LEFT, this));
			
				summaryDisplayChoice = new XChoice(this);
				summaryDisplayChoice.addItem(translate("Crosses"));
				summaryDisplayChoice.addItem(translate("Bar chart"));
			
			choicePanel.add(summaryDisplayChoice);
		
		thePanel.add(choicePanel);
		
		thePanel.add(super.simulationResultsPanel(summaryData));
		
		return thePanel;
	}
	
	protected XPanel theoryParamPanel(DataSet summaryData) {
		if (useNormalApprox)
			return normalParamPanel(summaryData);
		else
			return binomialParamPanel(summaryData);
	}
	
	protected String extremeProbString() {
		return translate("Prob of") + " " + actualSuccess + " " + (tailType == LOW_TAIL ? translate("or fewer") : translate("or more"));
	}
	
	private XPanel binomialParamPanel(DataSet summaryData) {
		XPanel titlePanel = new XPanel();
		titlePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
		titlePanel.add(new XLabel(translate("Theoretical distribution") + ":", XLabel.LEFT, this));
		titlePanel.add(new XLabel(translate("Binomial") + " (" + sampleSize + ", " + pSuccess.toString() + ")", XLabel.LEFT, this));
		
		return titlePanel;
	}
	
	private XPanel normalParamPanel(DataSet summaryData) {
		XPanel titlePanel = new XPanel();
		titlePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
		titlePanel.add(new XLabel(translate("Approximate distribution") + ":", XLabel.LEFT, this));
		
			int decimals = Integer.parseInt(getParameter(NORMAL_PARAM_DECIMALS_PARAM));
			NumValue mean = new NumValue(sampleSize * pSuccess.toDouble(), decimals);
			NumValue sd = new NumValue(Math.sqrt(mean.toDouble() * (1 - pSuccess.toDouble())), decimals);
		
		titlePanel.add(new XLabel(translate("Normal") + " (" + mean.toString() + ", " + sd.toString() + ")", XLabel.LEFT, this));
		
		return titlePanel;
	}
	
	protected void adjustSummaryDisplay() {
		((StackedDiscreteView)summaryView).setDrawTheory(currentResultsDisplay == 1);
		int simDisplay = (summaryDisplayChoice.getSelectedIndex() == 0)
										? StackedDiscreteView.CROSS_DISPLAY : StackedDiscreteView.BAR_DISPLAY;
		((StackedDiscreteView)summaryView).setDisplayType(currentResultsDisplay == 0
																					? simDisplay : StackedDiscreteView.NO_DISPLAY);
		summaryView.repaint();
	}

	
	private boolean localAction(Object target) {
		if (target == summaryDisplayChoice) {
			int newChoice = summaryDisplayChoice.getSelectedIndex();
			if (currentSummaryDisplay != newChoice) {
				currentSummaryDisplay = newChoice;
				int display = (currentSummaryDisplay == 0) ? StackedDiscreteView.CROSS_DISPLAY
									: (currentSummaryDisplay == 1) ? StackedDiscreteView.BAR_DISPLAY
									: StackedDiscreteView.NO_DISPLAY;
				((StackedDiscreteView)summaryView).setDisplayType(display);
				summaryView.repaint();
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}