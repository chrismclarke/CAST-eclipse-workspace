package inferenceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.RandomNormal;
import distn.*;
import coreSummaries.*;
import imageGroups.*;
import imageUtils.*;

import inference.*;


public class AdjustKIntervalApplet extends XApplet {
	static final private String DATA_INFO_PARAM = "horizAxis";
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final private String MEAN_SD_DECIMALS_PARAM = "meanSDDecimals";
	static final private String CI_DECIMALS_PARAM = "ciDecimals";
	static final protected String K_LIMITS_PARAM = "kLimits";
	static final protected String MAX_MEAN_SD_PARAM = "maxMeanSD";
	
	static final private Color kDarkRed = new Color(0x990000);
	static final private Color kAdjusterBackground = new Color(0xFFF3CE);
	
	static final private Color kNormalColor = new Color(0xDDDDFF);
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	private ParameterSlider kSlider;
	private RepeatingButton takeSampleButton;
	private XChoice sampleSizeChoice;
	
	private NumValue kMin, kStart, kMax;
	
	private int sampleSize[];
	private int currentSampleSizeIndex;
	
	private SampleAndMeanView dataView;
	
	public void setupApplet() {
		readSampleSizes();
		data = getData();
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		setLayout(new ProportionLayout(0.5, 20, ProportionLayout.HORIZONTAL,
																									ProportionLayout.TOTAL));
		
		add(ProportionLayout.LEFT, dataPanel(data, summaryData));
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new ProportionLayout(0.6, 0, ProportionLayout.VERTICAL,
																									ProportionLayout.TOTAL));
				rightPanel.add(ProportionLayout.TOP, adjustKPanel(summaryData));
				rightPanel.add(ProportionLayout.BOTTOM, ciPanel(summaryData));
		
		add(ProportionLayout.RIGHT, rightPanel);
	}
	
	private void readSampleSizes() {
		StringTokenizer st = new StringTokenizer(getParameter(SAMPLE_SIZE_PARAM));
		sampleSize = new int[st.countTokens()];
		sampleSizeChoice = new XChoice(this);
		for (int i=0 ; i<sampleSize.length ; i++) {
			String nextSize = st.nextToken();
			if (nextSize.indexOf("*") == 0) {
				nextSize = nextSize.substring(1);
				currentSampleSizeIndex = i;
			}
			sampleSizeChoice.addItem(nextSize);
			sampleSize[i] = Integer.parseInt(nextSize);
		}
		sampleSizeChoice.select(currentSampleSizeIndex);
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		RandomNormal generator = new RandomNormal(getParameter(RANDOM_NORMAL_PARAM));
		generator.setSampleSize(sampleSize[currentSampleSizeIndex]);
		NumVariable y = new NumSampleVariable(getParameter(VAR_NAME_PARAM), generator, 10);
		data.addVariable("y", y);
		
			NormalDistnVariable dataDistn = new NormalDistnVariable("data model");
				StringTokenizer st = new StringTokenizer(getParameter(RANDOM_NORMAL_PARAM));
				st.nextToken();		//	noOfValues
				NumValue modelMean = new NumValue(st.nextToken());
				NumValue modelSD = new NumValue(st.nextToken());
			dataDistn.setMean(modelMean.toDouble());
			dataDistn.setSD(modelSD.toDouble());
		data.addVariable("model", dataDistn);
		
		return data;
	}
	
	private SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
			int meanSdDecimals = Integer.parseInt(getParameter(MEAN_SD_DECIMALS_PARAM));
		summaryData.addVariable("mean", new MeanVariable(translate("mean"), "y", meanSdDecimals));
		summaryData.addVariable("sd", new SDVariable(translate("st devn"), "y", meanSdDecimals));
		
			StringTokenizer st = new StringTokenizer(getParameter(K_LIMITS_PARAM));
			kMin = new NumValue(st.nextToken());
			kMax = new NumValue(st.nextToken());
			kStart = new NumValue(st.nextToken());
			int ciDecimals = Integer.parseInt(getParameter(CI_DECIMALS_PARAM));
		MeanCIVariable ci = new MeanCIVariable("95% confidence interval", kStart.toDouble(),
															sampleSize[currentSampleSizeIndex] - 1, "y", ciDecimals);
		
		summaryData.addVariable("ci", ci);
		
		return summaryData;
	}
	
	private XPanel dataPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 20));
		
		thePanel.add("Center", dotPlotPanel(data, summaryData));
		thePanel.add("North", samplingPanel());
		thePanel.add("South", meanSDPanel(summaryData));
		
		return thePanel;
	}
	
	private HorizAxis getAxis(DataSet data, String variableKey, String axisInfoParam) {
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(axisInfoParam);
		theHorizAxis.readNumLabels(labelInfo);
		Variable v = (Variable)data.getVariable(variableKey);
		theHorizAxis.setAxisName(v.name);
		return theHorizAxis;
	}
	
	private XPanel dotPlotPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = getAxis(data, "y", DATA_INFO_PARAM);
		thePanel.add("Bottom", theHorizAxis);
		
			dataView = new SampleAndMeanView(data, this, theHorizAxis, "model", 0, SampleAndMeanView.NO_MEAN_VALUE);
			dataView.lockBackground(Color.white);
			dataView.setDensityColor(kNormalColor);
			dataView.setConfidenceInterval(summaryData, "ci");
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	private XPanel samplingPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 7));
		
				XPanel sampleSizePanel = new XPanel();
				sampleSizePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					XLabel nLabel = new XLabel(translate("Sample size") + " =", XLabel.RIGHT, this);
					nLabel.setFont(getStandardBoldFont());
				sampleSizePanel.add(nLabel);
				sampleSizePanel.add(sampleSizeChoice);
		thePanel.add(sampleSizePanel);
		
			takeSampleButton = new RepeatingButton(translate("Another sample"), this);
			
		thePanel.add(takeSampleButton);
		
		return thePanel;
	}
	
	private XPanel meanSDPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
//		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 0));
			
			StringTokenizer st = new StringTokenizer(getParameter(MAX_MEAN_SD_PARAM));
		
			OneValueImageView meanView = new OneValueImageView(summaryData, "mean",
													this, "xEquals/sampMeanRed.png", MeanSDImages.kParamAscent, new NumValue(st.nextToken()));
			meanView.setHighlightSelection(false);
			meanView.setForeground(kDarkRed);
		thePanel.add(meanView);
		
			OneValueImageView sdView = new OneValueImageView(summaryData, "sd",
													this, "xEquals/sampSDBlue.png", MeanSDImages.kParamAscent, new NumValue(st.nextToken()));
			sdView.setHighlightSelection(false);
			sdView.setForeground(Color.blue);
		thePanel.add(sdView);
		
		return thePanel;
	}
	
	private XPanel ciPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			OneValueImageView ciView = new OneValueImageView(summaryData, "ci", this, "ci/meanCIEquals.png", 19);
		thePanel.add(ciView);
		
		return thePanel;
	}
	
	private XPanel adjustKPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new InsetPanel(12, 12);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 10));
			
			XPanel headingPanel = new XPanel();
			headingPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			headingPanel.add(new ImageCanvas("ci/meanCI.png", this));
		
		thePanel.add(headingPanel);
		
			kSlider = new ParameterSlider(kMin, kMax, kStart, translate("Constant") + ", k",  this);
			kSlider.setFont(getStandardBoldFont());
		thePanel.add(kSlider);
		
			XPanel levelPanel = new XPanel();
			levelPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				ConfidenceLevelView levelView = new ConfidenceLevelView(summaryData, "ci", this);
				levelView.setFont(getStandardBoldFont());
				levelView.setForeground(kDarkRed);
			levelPanel.add(levelView);
		
		thePanel.add(levelPanel);
		
		thePanel.lockBackground(kAdjusterBackground);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == sampleSizeChoice) {
			int newSampleSizeIndex = sampleSizeChoice.getSelectedIndex();
			if (newSampleSizeIndex != currentSampleSizeIndex) {
				currentSampleSizeIndex = newSampleSizeIndex;
				MeanCIVariable ci = (MeanCIVariable)summaryData.getVariable("ci");
				ci.setDF(sampleSize[currentSampleSizeIndex] - 1);
				summaryData.changeSampleSize(sampleSize[currentSampleSizeIndex]);
			}
			return true;
		}
		else if (target == kSlider) {
			NumValue kVal = kSlider.getParameter();
			MeanCIVariable ci = (MeanCIVariable)summaryData.getVariable("ci");
			ci.setT(kVal.toDouble());
			summaryData.valueChanged(0);
			dataView.repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}