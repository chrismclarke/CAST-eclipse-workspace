package varianceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.RandomNormal;
import valueList.*;
import distn.*;
import coreGraphics.*;
import coreVariables.*;
import formula.*;

import variance.*;


public class SampleVarianceApplet extends XApplet {
	static final protected String AXIS_INFO_PARAM = "horizAxis";
	static final protected String SUMMARY_AXIS_INFO_PARAM = "summaryAxis";
	static final protected String MEAN_SD_PARAM = "meanSD";
	static final protected String DATA_DECIMALS_PARAM = "dataDecimals";
	static final protected String VARIANCE_NAME_PARAM = "varianceName";
	static final protected String SUMMARY_DECIMALS_PARAM = "summaryDecimals";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final private String VARIANCE_LIMITS_PARAM = "varianceLimits";
	
	static final private Color kLightGreyColor = new Color(0x999999);
//	static final private int kSigma2Ascent = 16;
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	protected JitterPlusNormalView dataView;
	protected StackedPlusNormalView summaryView;
	
	protected RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck;
	
	private XChoice sampleSizeChoice;
	private int sampleSize[];
	private int currentSizeIndex = 0;
	
	private ParameterSlider varianceSlider;
	
	protected int noOfValues;
	
	public void setupApplet() {
		sampleSizeChoice = getSampleSizeChoice();
		
		data = getData();
		summaryData = getSummaryData(data);
		generateInitialSample(summaryData);
		
		setLayout(new BorderLayout());
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL,
																									ProportionLayout.TOTAL));
			dataPanel.add(ProportionLayout.TOP, dataPanel(data, "y", "model", AXIS_INFO_PARAM));
			dataPanel.add(ProportionLayout.BOTTOM, dataPanel(summaryData, "variance", "theory",
																											SUMMARY_AXIS_INFO_PARAM));
		
		add("Center", dataPanel);
		add("East", controlPanel(data, summaryData, "variance"));
	}
	
	protected void generateInitialSample(SummaryDataSet summaryData) {
		setSummaryTheoryParameters(summaryData, "theory");
		summaryData.takeSample();
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			RandomNormal generator = new RandomNormal(noOfValues, 0.0, 1.0, 3.0);
			int decimals = Integer.parseInt(getParameter(DATA_DECIMALS_PARAM));
			NumVariable error = new NumSampleVariable("error", generator, decimals);
		data.addVariable("error", error);
		
		ScaledVariable y = new ScaledVariable(getParameter(VAR_NAME_PARAM), error, "error",
																														getParameter(MEAN_SD_PARAM));
		data.addVariable("y", y);
		
			NormalDistnVariable dataDistn = new NormalDistnVariable("data model");
			dataDistn.setParams(getParameter(MEAN_SD_PARAM));
		data.addVariable("model", dataDistn);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "error");
		
		int decimals = Integer.parseInt(getParameter(SUMMARY_DECIMALS_PARAM));
		VarianceVariable variance = new VarianceVariable(getParameter(VARIANCE_NAME_PARAM),
																																					"y", decimals);
		
		summaryData.addVariable("variance", variance);
		
		GammaDistnVariable varianceDistn = new GammaDistnVariable(translate("Chi-squared"));
		summaryData.addVariable("theory", varianceDistn);
		
		return summaryData;
	}
	
	protected void setSummaryTheoryParameters(SummaryDataSet summaryData, String theoryKey) {
		GammaDistnVariable varianceDistn = (GammaDistnVariable)summaryData.getVariable(theoryKey);
		
		int df = noOfValues - 1;
		ScaledVariable yVar = (ScaledVariable)data.getVariable("y");
		double scaling = yVar.getParam(1) * yVar.getParam(1) / (noOfValues - 1);
		
		varianceDistn.setShape(df * 0.5);
		varianceDistn.setScale(2.0 * scaling);
	}
	
	private void setNormalViewLabels(DataSet data, String modelKey) {
		NormalDistnVariable normVar = (NormalDistnVariable)data.getVariable(modelKey);
		dataView.setDistnLabel(new LabelValue(translate("Normal") + "(" + normVar.getMean().toString()
													+ ", " + normVar.getSD().toString() + ")"), kLightGreyColor);
	}
	
	protected XPanel dataPanel(DataSet data, String variableKey, String modelKey, String axisParam) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			theHorizAxis.readNumLabels(getParameter(axisParam));
			Variable v = (Variable)data.getVariable(variableKey);
			theHorizAxis.setAxisName(v.name);
		thePanel.add("Bottom", theHorizAxis);
		
			DataView tempView;
			if (data instanceof SummaryDataSet) {
				tempView = summaryView = new ScaledChi2View(data, this, theHorizAxis, modelKey, 2);
				setSummaryTheoryParameters((SummaryDataSet)data, modelKey);
			}
			else {
				tempView = dataView = new JitterPlusNormalView(data, this, theHorizAxis, modelKey, 1.0);
//				tempView = dataView = new StackedPlusNormalView(data, this, theHorizAxis, modelKey);
				setNormalViewLabels(data, modelKey);
			}
			tempView.setActiveNumVariable(variableKey);
			tempView.lockBackground(Color.white);
		
		thePanel.add("Center", tempView);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData, String summaryKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 30));
		
		thePanel.add(topControlPanel(data));
		thePanel.add(middleControlPanel(data, summaryData, summaryKey));
		thePanel.add(bottomControlPanel(data, summaryData));
		
		return thePanel;
	}
	
	private XChoice getSampleSizeChoice() {
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
		
		XChoice choice = new XChoice(this);
		for (int i=0 ; i<sampleSize.length; i++)
			choice.addItem("n = " + String.valueOf(sampleSize[i]));
		choice.select(currentSizeIndex);
		return choice;
	}
	
	private XPanel topControlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0,0));
			StringTokenizer st = new StringTokenizer(getParameter(VARIANCE_LIMITS_PARAM));
			String minVariance = st.nextToken();
			String maxVariance = st.nextToken();
			int noOfSteps = Integer.parseInt(st.nextToken());
			NormalDistnVariable normalDist = (NormalDistnVariable)data.getVariable("model");
			double startSD = normalDist.getSD().toDouble();
			double startVariance = startSD * startSD;
			
			String sigma2 = MText.expandText("#sigma##sup2#");
			varianceSlider = new ParameterSlider(new NumValue(minVariance), new NumValue(maxVariance),
										new NumValue(startVariance), noOfSteps, sigma2, this);
			varianceSlider.setForeground(Color.blue);
		thePanel.add("Center", varianceSlider);
		return thePanel;
	}
	
	protected XPanel middleControlPanel(DataSet data, SummaryDataSet summaryData, String summaryKey) {
		XPanel topPanel = new XPanel();
		topPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
		topPanel.add(sampleSizeChoice);
		
			OneValueView varView = new OneValueView(summaryData, summaryKey, this);
			varView.setFont(getBigFont());
		
		topPanel.add(varView);
		return topPanel;
	}
	
	protected XPanel bottomControlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel bottomPanel = new XPanel();
		bottomPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
			takeSampleButton = new RepeatingButton(translate("Take sample"), this);
		bottomPanel.add(takeSampleButton);
		
			accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		bottomPanel.add(accumulateCheck);
		
			ValueCountView theCount = new ValueCountView(summaryData, this);
			theCount.setLabel(translate("No of samples") + " = ");
		bottomPanel.add(theCount);
		
		return bottomPanel;
	}
	
	protected void doTakeSample() {
		summaryData.takeSample();
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			doTakeSample();
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
				noOfValues = sampleSize[currentSizeIndex];
				setSummaryTheoryParameters(summaryData, "theory");
				summaryData.changeSampleSize(noOfValues);
			}
			return true;
		}
		else if (target == varianceSlider) {
			double newVariance = varianceSlider.getParameter().toDouble();
			double newSD = Math.sqrt(newVariance);
			NormalDistnVariable normalDist = (NormalDistnVariable)data.getVariable("model");
			normalDist.setSD(newSD);
			setNormalViewLabels(data, "model");
			
			ScaledVariable yVar = (ScaledVariable)data.getVariable("y");
			yVar.setParam(1, newSD);
			
			data.variableChanged("y");
			
			setSummaryTheoryParameters(summaryData, "theory");
			summaryData.setSingleSummaryFromData();
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}