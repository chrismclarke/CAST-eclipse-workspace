package randomStatProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.*;
import valueList.*;
import distn.*;
import coreGraphics.*;
import coreSummaries.*;


public class MeanDistnApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String RANDOM_PARAM = "random";
	static final private String DATA_DECIMALS_PARAM = "dataDecimals";
	static final private String MEAN_NAME_PARAM = "meanName";
	static final private String SUMMARY_DECIMALS_PARAM = "summaryDecimals";
	static final private String MEAN_SD_DECIMALS_PARAM = "meanSdDecimals";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final private String POPN_DISTN_PARAM = "popnDistn";
	
	static final protected Color kPopnColor = new Color(0x990000);
	static final private Color kSampColor = new Color(0x0000BB);
	static final protected Color kSummaryColor = Color.black;
	
	static final private Color kNormalColor = new Color(0xE2BBE6);
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	private NumValue modelMean, modelSD, modelShape, modelScale;
	private int noOfValues;
	
	private RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck;
	
	private ParameterSlider sampleSizeSlider;
	private XChoice sampleSizeChoice;
	
	protected int sampleSize[];
	private int currentSizeIndex = 0;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		generateInitialSample(summaryData);
		
		setLayout(new ProportionLayout(0.7, 12, ProportionLayout.VERTICAL,
																												ProportionLayout.TOTAL));
			XPanel topTwoPanel = new XPanel();
			topTwoPanel.setLayout(new ProportionLayout(0.5, 12, ProportionLayout.VERTICAL,
																												ProportionLayout.TOTAL));
				XPanel topPanel = new XPanel();
				topPanel.setLayout(new BorderLayout(10, 0));
				topPanel.add("West", titlePanel(translate("Population"), kPopnColor));
				topPanel.add("South", takeSamplePanel());
				topPanel.add("Center", populationPanel(data, "model", kPopnColor));
		
			topTwoPanel.add(ProportionLayout.TOP, topPanel);
			
				XPanel middlePanel = new XPanel();
				middlePanel.setLayout(new BorderLayout(10, 0));
				middlePanel.add("West", titlePanel(translate("Sample"), kSampColor));
				middlePanel.add("South", controlPanel(summaryData, "mean"));
				middlePanel.add("Center", samplePanel(data, "y", kSampColor));
		
			topTwoPanel.add(ProportionLayout.BOTTOM, middlePanel);
		
		add(ProportionLayout.TOP, topTwoPanel);
			
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(10, 0));
			bottomPanel.add("West", titlePanel(translate("Summary"), kSummaryColor));
			bottomPanel.add("Center", meanPanel(summaryData, "mean", "theory", kSummaryColor));
	
		add(ProportionLayout.BOTTOM, bottomPanel);
	}
	
	protected void generateInitialSample(SummaryDataSet summaryData) {
		String sizeString = getParameter(SAMPLE_SIZE_PARAM);
		if (sizeString != null) {
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
		else {
			setTheoryParameters(summaryData, "theory");
			summaryData.takeSample();
		}
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		String distnString = getParameter(POPN_DISTN_PARAM);
		boolean isNormal = (distnString == null) || distnString.equals("normal");
		
			RandomContinuous generator;
			if (isNormal)
				generator = new RandomNormal(getParameter(RANDOM_PARAM));
			else
				generator = new RandomGamma(getParameter(RANDOM_PARAM));
			int decimals = Integer.parseInt(getParameter(DATA_DECIMALS_PARAM));
			NumVariable y = new NumSampleVariable(getParameter(VAR_NAME_PARAM), generator, decimals);
		data.addVariable("y", y);
		
			StringTokenizer st = new StringTokenizer(getParameter(RANDOM_PARAM));
			noOfValues = Integer.parseInt(st.nextToken());
			ContinDistnVariable popnDistn;
			
			if (isNormal) {
				NormalDistnVariable dataDistn = new NormalDistnVariable(getParameter(VAR_NAME_PARAM));
				modelMean = new NumValue(st.nextToken());
				modelSD = new NumValue(st.nextToken());
				dataDistn.setParams(modelMean.toString() + " " + modelSD.toString());
				popnDistn = dataDistn;
			}
			else {
				GammaDistnVariable dataDistn = new GammaDistnVariable(getParameter(VAR_NAME_PARAM));
				modelShape = new NumValue(st.nextToken());
				modelScale = new NumValue(st.nextToken());
				dataDistn.setParams(modelShape.toString() + " " + modelScale.toString());
				popnDistn = dataDistn;
			}
		data.addVariable("model", popnDistn);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
		int decimals = Integer.parseInt(getParameter(SUMMARY_DECIMALS_PARAM));
		MeanVariable mean = new MeanVariable(getParameter(MEAN_NAME_PARAM), "y", decimals);
		
		summaryData.addVariable("mean", mean);
		
		boolean isNormal = data.getVariable("model") instanceof NormalDistnVariable;
		ContinDistnVariable meanDistn;
		if (isNormal)
			meanDistn = new NormalDistnVariable(getParameter(MEAN_NAME_PARAM));
		else
			meanDistn = new GammaDistnVariable(getParameter(MEAN_NAME_PARAM));
		summaryData.addVariable("theory", meanDistn);
		
		return summaryData;
	}
	
	protected void setTheoryParameters(SummaryDataSet summaryData, String theoryKey) {
		CoreVariable distnVar = summaryData.getVariable(theoryKey);
		boolean isNormal = distnVar instanceof NormalDistnVariable;
		if (isNormal) {
			NormalDistnVariable meanDistn = (NormalDistnVariable)distnVar;
			double sd = modelSD.toDouble() / Math.sqrt(noOfValues);
			String sdDecimalsString = getParameter(MEAN_SD_DECIMALS_PARAM);
			int sdDecimals = (sdDecimalsString == null) ? modelSD.decimals + 6
																				: Integer.parseInt(sdDecimalsString);
			NumValue meanSD = new NumValue(sd, sdDecimals);
			meanDistn.setParams(modelMean.toString() + " " + meanSD.toString());
		}
		else {
			GammaDistnVariable meanDistn = (GammaDistnVariable)distnVar;
			double scale = modelScale.toDouble() / noOfValues;
			double shape = modelShape.toDouble() * noOfValues;
			meanDistn.setScale(scale);
			meanDistn.setShape(shape);
			String sdDecimalsString = getParameter(MEAN_SD_DECIMALS_PARAM);
			int sdDecimals = (sdDecimalsString == null) ? modelScale.decimals + 6
																				: Integer.parseInt(sdDecimalsString);
			meanDistn.setMeanSdDecimals(0, sdDecimals);
		}
		summaryData.variableChanged(theoryKey);
	}
	
	protected XPanel titlePanel(String title, Color c) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		if (title != null) {
			XLabel titleLabel = new XLabel(title, XLabel.LEFT, this);
			titleLabel.setFont(getStandardBoldFont());
			titleLabel.setForeground(c);
			thePanel.add(titleLabel);
		}
		return thePanel;
	}
	
	protected HorizAxis getAxis(DataSet data, String variableKey) {
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		CoreVariable v = data.getVariable(variableKey);
		theHorizAxis.setAxisName(v.name);
		return theHorizAxis;
	}
	
	protected XPanel populationPanel(DataSet data, String modelKey, Color c) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = getAxis(data, modelKey);
			horizAxis.setForeground(c);
		thePanel.add("Bottom", horizAxis);
		
			SimpleDistnView view = new SimpleDistnView(data, this, horizAxis, modelKey);
			view.setDensityScaling(0.9);
			view.setDensityColor(kNormalColor);
			view.lockBackground(Color.white);
			view.setForeground(c);
		thePanel.add("Center", view);
		
		return thePanel;
	}
	
	private XPanel samplePanel(DataSet data, String dataKey, Color c) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = getAxis(data, dataKey);
			horizAxis.setForeground(c);
		thePanel.add("Bottom", horizAxis);
		
			StackedDotPlotView view = new StackedDotPlotView(data, this, horizAxis);
			view.setActiveNumVariable(dataKey);
			view.lockBackground(Color.white);
			view.setForeground(c);
		thePanel.add("Center", view);
		
		return thePanel;
	}
	
	private XPanel meanPanel(SummaryDataSet summaryData, String variableKey, String modelKey,
																																							Color c) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = getAxis(summaryData, variableKey);
			horizAxis.setForeground(c);
		thePanel.add("Bottom", horizAxis);
		
			StackedPlusNormalView view = new StackedPlusNormalView(summaryData, this, horizAxis, modelKey);
			view.setActiveNumVariable(variableKey);
			view.setShowDensity(DataPlusDistnInterface.NO_DISTN);
			view.lockBackground(Color.white);
			view.setForeground(c);
		thePanel.add("Center", view);
		
		return thePanel;
	}
	
	protected XPanel sampleSizeChoicePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			XLabel sizeLabel = new XLabel(translate("Sample size") + ":", XLabel.LEFT, this);
			sizeLabel.setFont(getStandardBoldFont());
		thePanel.add(sizeLabel);
		
			sampleSizeChoice = new XChoice(this);
			for (int i=0 ; i<sampleSize.length; i++)
				sampleSizeChoice.addItem(String.valueOf(sampleSize[i]));
			sampleSizeChoice.select(currentSizeIndex);
			
		thePanel.add(sampleSizeChoice);
		return thePanel;
	}
	
	protected XPanel sampleSizeSliderPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			NumValue minSize = new NumValue(sampleSize[0], 0);
			NumValue maxSize = new NumValue(sampleSize[sampleSize.length - 1], 0);
			NumValue startSize = new NumValue(sampleSize[currentSizeIndex], 0);
			sampleSizeSlider = new ParameterSlider(minSize, maxSize, startSize, translate("Sample size"), this);
		thePanel.add("Center", sampleSizeSlider);
		return thePanel;
	}
	
	protected XPanel takeSamplePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 0));
		
			ArrowCanvas arrow = new ArrowCanvas(12, 4, 7, 8, ArrowCanvas.DOWN);
			arrow.setForeground(kPopnColor);
		thePanel.add(arrow);
		
			takeSampleButton = new RepeatingButton(translate("Take sample"), this);
		thePanel.add(takeSampleButton);
		
		if (sampleSize != null)
			thePanel.add(sampleSizeChoicePanel());
		
			arrow = new ArrowCanvas(12, 4, 7, 8, ArrowCanvas.DOWN);
			arrow.setForeground(kPopnColor);
		thePanel.add(arrow);
		
		return thePanel;
	}
	
	private XPanel controlPanel(SummaryDataSet summaryData, String summaryKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			OneValueView meanView = new OneValueView(summaryData, summaryKey, this);
			meanView.setLabel(translate("Mean") + " =");
			meanView.setForeground(kSampColor);
		thePanel.add(meanView);
			accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		thePanel.add(accumulateCheck);
		
		return thePanel;
	}
	
	protected void doTakeSample() {
		summaryData.takeSample();
	}
	
	protected void changeSampleSize(int newSize) {
		noOfValues = newSize;
		setTheoryParameters(summaryData, "theory");
		summaryData.changeSampleSize(noOfValues);
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
				changeSampleSize(sampleSize[newChoice]);
			}
			return true;
		}
		else if (target == sampleSizeSlider) {
			int newSize = (int)Math.round(sampleSizeSlider.getParameter().toDouble());
			changeSampleSize(newSize);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}