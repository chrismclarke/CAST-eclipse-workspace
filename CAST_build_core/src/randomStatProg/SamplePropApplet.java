package randomStatProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import distn.*;
import random.RandomMultinomial;
import coreGraphics.*;
import coreSummaries.*;

import sampling.*;
import randomStat.*;


public class SamplePropApplet extends XApplet {
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	
	static final private String CAT_PROB_PARAM = "catProb";
	static final private String PROPN_NAME_PARAM = "propnName";
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String PROB_AXIS_PARAM = "probAxis";
	static final private String COUNT_HORIZ_AXIS_PARAM = "countHorizAxis";
	static final private String COUNT_VERT_AXIS_PARAM = "countVertAxis";
	static final private String SHOW_BINOMIAL_PARAM = "showBinomial";
	static final private String PROPORTION_TEXT_PARAM = "proportionText";
	static final private String COUNT_TEXT_PARAM = "countText";
	
	static final private int THEORY_NO = 0;
	static final private int THEORY_YES = 1;
	static final private int THEORY_CHECK = 2;
	
	static final private int kPropnDecimals = 3;
	
	static final private Color kPopnColor = new Color(0x990000);
	static final private Color kSampColor = new Color(0x0000BB);
	
	private DataSet data;
	private SummaryDataSet summaryData;
	private int noOfValues;
	
	private double modelProbs[];
	private MultiHorizAxis propnCountAxis;
	private MultiVertAxis countAxis;
	private StackedDiscBinomialView summaryView;
	
	private RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck;
	
	private XChoice sampleSizeChoice;
	private int sampleSize[];
	private int currentSizeIndex = 0;
	
	private XChoice propnCountChoice;
	private int propnCountIndex = 0;
	
	private XCheckbox theoryCheck;
	
	private int theoryDisplay = THEORY_NO;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		generateInitialSample(summaryData);
		
		setLayout(new BorderLayout(0, 0));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.5, 20, ProportionLayout.VERTICAL,
																									ProportionLayout.TOTAL));
			mainPanel.add(ProportionLayout.TOP, dataPanel(data, "y", "model"));
			
				XPanel bottomPanel = new XPanel();
				bottomPanel.setLayout(new BorderLayout(0, 20));
				bottomPanel.add("North", controlPanel(data, summaryData, "propn"));
				bottomPanel.add("Center", summaryPanel(summaryData, "propn", "theory"));
			
			mainPanel.add(ProportionLayout.BOTTOM, bottomPanel);
		
		add("Center", mainPanel);
		
		if (theoryDisplay == THEORY_CHECK) {
			XPanel checkPanel = new InsetPanel(0, 10, 0, 0);
			checkPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
				theoryCheck = new XCheckbox("Show theory", this);
			checkPanel.add(theoryCheck);
			
			add("South", checkPanel);
		}
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		CatDistnVariable dataDistn = new CatDistnVariable("data model");
		dataDistn.readLabels(getParameter(CAT_LABELS_PARAM));
		dataDistn.setParams(getParameter(CAT_PROB_PARAM));
		modelProbs = dataDistn.getProbs();
		data.addVariable("model", dataDistn);
		
		RandomMultinomial generator = new RandomMultinomial(1, modelProbs);
		CatSampleVariable y = new CatSampleVariable(getParameter(CAT_NAME_PARAM), generator);
		y.readLabels(getParameter(CAT_LABELS_PARAM));
		data.addVariable("y", y);
		
		return data;
	}
	
	private SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
		PropnVariable propn = new PropnVariable(getParameter(PROPN_NAME_PARAM), "y", kPropnDecimals);
		
		summaryData.addVariable("propn", propn);
		
		BinomialDistnVariable propnDistn = new BinomialDistnVariable("propn distn");
		summaryData.addVariable("theory", propnDistn);
		
		String showBinomString = getParameter(SHOW_BINOMIAL_PARAM);
		if (showBinomString != null)
			theoryDisplay = showBinomString.equals("true") ? THEORY_YES
											: showBinomString.equals("check") ? THEORY_CHECK
											: THEORY_NO;
		
		return summaryData;
	}
	
	private void generateInitialSample(SummaryDataSet summaryData) {
		String sizeString = getParameter(SAMPLE_SIZE_PARAM);
		StringTokenizer st = new StringTokenizer(sizeString);
		int noOfSizes = st.countTokens();
		sampleSize = new int[noOfSizes];
		for (int i=0 ; i<noOfSizes ; i++) {
			String nextSize = st.nextToken();
			boolean isInitialSize = nextSize.startsWith("*");
			if (isInitialSize) {
				nextSize = nextSize.substring(1);
				currentSizeIndex = i;
			}
			sampleSize[i] = Integer.parseInt(nextSize);
		}
		noOfValues = sampleSize[currentSizeIndex];
		setTheoryParameters(summaryData, "theory");
		summaryData.changeSampleSize(sampleSize[currentSizeIndex]);
	}
	
	private void setTheoryParameters(SummaryDataSet summaryData, String theoryKey) {
		BinomialDistnVariable propnDistn = (BinomialDistnVariable)summaryData.getVariable(theoryKey);
		propnDistn.setParams(noOfValues + " " + modelProbs[0]);
	}
	
	private XPanel dataPanel(DataSet data, String variableKey, String modelKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10));
		
			String labelInfo = getParameter(PROB_AXIS_PARAM);
			
			VertAxis probAxis = new VertAxis(this);
			probAxis.readNumLabels(labelInfo);
		thePanel.add(ProportionLayout.LEFT, barChartPanel(data, modelKey, translate("Population"),
																					probAxis, probAxis, null, translate("Probability"), null, kPopnColor));
			probAxis = new VertAxis(this);
			probAxis.readNumLabels(labelInfo);
			
			countAxis = new MultiVertAxis(this, sampleSize.length);
			countAxis.readNumLabels(getParameter(COUNT_VERT_AXIS_PARAM + 0));
			for (int i=1 ; i<sampleSize.length ; i++)
					countAxis.readExtraNumLabels(getParameter(COUNT_VERT_AXIS_PARAM + i));
			countAxis.setStartAlternate(currentSizeIndex);
		
		thePanel.add(ProportionLayout.RIGHT, barChartPanel(data, variableKey, translate("Sample"),
																probAxis, countAxis, probAxis, translate("Count"), translate("Proportion"),
																kSampColor));
		
		return thePanel;
	}
	
	private XPanel barChartPanel(DataSet data, String variableKey, String titleString,
													VertAxis propnAxis, VertAxis leftAxis, VertAxis rightAxis, String leftString,
													String rightString, Color mainColor) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 4));
			
			XLabel title = new XLabel(titleString, XLabel.CENTER, this);
			title.setFont(getStandardBoldFont());
		thePanel.add("North", title);
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new BorderLayout(0, 0));
			
				XPanel labelPanel = new XPanel();
				labelPanel.setLayout(new BorderLayout(0, 0));
				
					XLabel leftLabel = new XLabel(leftString, XLabel.LEFT, this);
				labelPanel.add("West", leftLabel);
				
				XLabel rightLabel = null;
				if (rightString != null) {
					rightLabel = new XLabel(rightString, XLabel.LEFT, this);
					labelPanel.add("East", rightLabel);
				}
				labelPanel.add("Center", new XPanel());
			innerPanel.add("North", labelPanel);
		
				XPanel dataPanel = new XPanel();
				dataPanel.setLayout(new AxisLayout());
				
					HorizAxis horizAxis = new HorizAxis(this);
					CatVariableInterface cv = (CatVariableInterface)data.getVariable(variableKey);
					horizAxis.setCatLabels(cv);
				dataPanel.add("Bottom", horizAxis);
				
				dataPanel.add("Left", leftAxis);
				if (rightAxis != null)
					dataPanel.add("Right", rightAxis);
				
					PBarView dataView = new PBarView(data, this, variableKey, horizAxis, propnAxis);
					dataView.lockBackground(Color.white);
				dataPanel.add("Center", dataView);
		
			innerPanel.add("Center", dataPanel);
			
		thePanel.add("Center", innerPanel);
		
		if (mainColor != null) {
			title.setForeground(mainColor);
			leftLabel.setForeground(mainColor);
			if (rightLabel != null)
				rightLabel.setForeground(mainColor);
			horizAxis.setForeground(mainColor);
			leftAxis.setForeground(mainColor);
			if (rightAxis != null)
				rightAxis.setForeground(mainColor);
			dataView.setForeground(mainColor);
		}
		
		return thePanel;
	}
	
	private XPanel summaryPanel(DataSet data, String variableKey, String modelKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new AxisLayout());
			
				propnCountAxis = new MultiHorizAxis(this, sampleSize.length + 1);
				propnCountAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
				for (int i=0 ; i<sampleSize.length ; i++)
					propnCountAxis.readExtraNumLabels(getParameter(COUNT_HORIZ_AXIS_PARAM + i));
				if (theoryDisplay == THEORY_YES)
					propnCountAxis.setStartAlternate(currentSizeIndex + 1);
				
			innerPanel.add("Bottom", propnCountAxis);
			
				summaryView = new StackedDiscBinomialView(data, this, propnCountAxis, modelKey, null);
				summaryView.setShowDensity((theoryDisplay == THEORY_YES) ? DataPlusDistnInterface.DISCRETE_DISTN
																											: DataPlusDistnInterface.NO_DISTN);
				summaryView.lockBackground(Color.white);
			innerPanel.add("Center", summaryView);
		
		thePanel.add("Center", innerPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			
				propnCountChoice = new XChoice(this);
				propnCountChoice.addItem(getParameter(PROPORTION_TEXT_PARAM));
				propnCountChoice.addItem(getParameter(COUNT_TEXT_PARAM));
				if (theoryDisplay == THEORY_YES) {
					propnCountChoice.select(1);
					propnCountIndex = 1;
				}
			
			bottomPanel.add(propnCountChoice);
			
		thePanel.add("South", bottomPanel);
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data, SummaryDataSet summaryData, String summaryKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			XPanel sampleSizePanel = new XPanel();
				sampleSizePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
//				sampleSizePanel.add(new XLabel("Sample size:", XLabel.LEFT, this));
				sampleSizeChoice = new XChoice("n =", XChoice.HORIZONTAL, this);
				for (int i=0 ; i<sampleSize.length; i++)
					sampleSizeChoice.addItem(String.valueOf(sampleSize[i]));
				sampleSizeChoice.select(currentSizeIndex);
			
			sampleSizePanel.add(sampleSizeChoice);
		thePanel.add(sampleSizePanel);
		
			takeSampleButton = new RepeatingButton(translate("Another sample"), this);
		thePanel.add(takeSampleButton);
		
			accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		thePanel.add(accumulateCheck);
		
		return thePanel;
	}
	
	private void changeSampleSize(int newChoice) {
		noOfValues = sampleSize[currentSizeIndex];
		setTheoryParameters(summaryData, "theory");
		summaryData.changeSampleSize(noOfValues);
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
		else if (target == sampleSizeChoice) {
			int newChoice = sampleSizeChoice.getSelectedIndex();
			if (newChoice != currentSizeIndex) {
				currentSizeIndex = newChoice;
				changeSampleSize(newChoice);
				if (propnCountIndex == 1) {
					propnCountAxis.setAlternateLabels(currentSizeIndex + 1);
					propnCountAxis.repaint();
				}
				countAxis.setAlternateLabels(currentSizeIndex);
				countAxis.repaint();
			}
			return true;
		}
		else if (target == propnCountChoice) {
			int newChoice = propnCountChoice.getSelectedIndex();
			if (newChoice != propnCountIndex) {
				propnCountIndex = newChoice;
				propnCountAxis.setAlternateLabels(newChoice == 0 ? 0 : currentSizeIndex + 1);
				propnCountAxis.repaint();
			}
			return true;
		}
		else if (target == theoryCheck) {
			summaryView.setDimData(theoryCheck.getState());
			summaryView.setShowDensity(theoryCheck.getState() ? DataPlusDistnInterface.DISCRETE_DISTN
																											: DataPlusDistnInterface.NO_DISTN);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}