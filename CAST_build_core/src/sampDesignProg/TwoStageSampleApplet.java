package sampDesignProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;
import distn.*;
import coreSummaries.*;

import sampDesign.*;
import time.*;


//class PrimaryUnitSlider extends XSlider {
class PrimaryUnitSlider extends XValueAdjuster {
	private int[] primaryUnits;
	
	PrimaryUnitSlider(int[] primaryUnits, int startPrimaryIndex, XApplet applet) {
//		super(String.valueOf(primaryUnits[0]), String.valueOf(primaryUnits[primaryUnits.length - 1]),
//																							"Primary units sampled:  ", 0, primaryUnits.length - 1,
//																							startPrimaryIndex, applet);
		super("", 0, primaryUnits.length - 1, startPrimaryIndex, applet);
		this.primaryUnits = primaryUnits;
		setFont(applet.getStandardBoldFont());
	}
	
	protected Value translateValue(int val) {
		return new NumValue(primaryUnits[val], 0);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return translateValue(primaryUnits.length - 1).stringWidth(g);
	}
	
	public int getPrimaryUnits() {
		return primaryUnits[getValue()];
	}
}



public class TwoStageSampleApplet extends XApplet {
	static final private String RANDOM_NORMAL_PARAM = "randomNormal";
	static final private String DECIMALS_PARAM = "decimals";
	static final private String MEAN_PARAM = "mean";
	static final private String CLUSTER_CORR_PARAM = "clusterCorr";
	static final private String CLUSTER_SAMPLE_PARAM = "clustersPerSample";
	static final private String PRIMARY_UNIT_PARAM = "primaryUnits";
	static final private String RANDOM_SEED_PARAM = "sampleSeed";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final private String STAGE_COST_PARAM = "stageCost";
	static final private String UNITS_PARAM = "units";
	
	static final private int kMaxCorrSteps = 200;
	
	static final private Color kCostBackground = new Color(0xDDDDEE);
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	private XButton sampleButton, resetButton;
	private PrimaryUnitSlider primaryUnitSlider;
	private XNoValueSlider clusterSlider;
	private XChoice samplingSchemeChoice;
	private int currentSamplingScheme = Sample2StageVariable.TWO_STAGE;
	
	protected int decimals;
	private double startCorr;
	private int primaryUnits[];
	private int primaryStartIndex;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		
		setLayout(new BorderLayout());
		
		add("Center", displayPanel(data, summaryData));
		
		add("East", controlPanel(data, summaryData));
	}
	
	protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel rightPanel = new XPanel();
		rightPanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL, ProportionLayout.REMAINDER));
			
			XPanel rightTopPanel = new XPanel();
			rightTopPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
			rightTopPanel.add(samplingSliderPanel());
			rightTopPanel.add(samplingPanel(false));
		
		rightPanel.add(ProportionLayout.TOP, rightTopPanel);
		
			XPanel rightBottomPanel = new XPanel();
			rightBottomPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 15));
			rightBottomPanel.add(costPanel());
		
		rightPanel.add(ProportionLayout.BOTTOM, rightBottomPanel);
		return rightPanel;
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		readSliderParams();
		
			decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
			String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
			RandomNormal generator = new RandomNormal(randomInfo);
			double mean = Double.parseDouble(getParameter(MEAN_PARAM));
			
			ClusterSampleVariable popn = new ClusterSampleVariable(getParameter(VAR_NAME_PARAM),
																					generator, mean, 1.0 - startCorr, decimals);
			popn.setClustersPerSample(Integer.parseInt(getParameter(CLUSTER_SAMPLE_PARAM)));
			popn.generateNextSample();
		data.addVariable("popn", popn);
		
			long samplingSeed = Long.parseLong(getParameter(RANDOM_SEED_PARAM));
			int nPrimary = primaryUnits[primaryStartIndex];
			int nSecondary = Integer.parseInt(getParameter(SAMPLE_SIZE_PARAM)) / nPrimary;
			Sample2StageVariable ySamp = new Sample2StageVariable(getParameter(VAR_NAME_PARAM),
																	data, "popn", samplingSeed, nPrimary, nSecondary);
			
		data.addVariable("y", ySamp);
		
		return data;
	}
	
	private void readSliderParams() {
		startCorr = Double.parseDouble(getParameter(CLUSTER_CORR_PARAM));
	
		StringTokenizer st = new StringTokenizer(getParameter(PRIMARY_UNIT_PARAM));
		int n = 0;
		while (st.hasMoreTokens()) {
			st.nextToken();
			n++;
		}
		st = new StringTokenizer(getParameter(PRIMARY_UNIT_PARAM));
		primaryUnits = new int[n];
		primaryStartIndex = 0;
		for (int i=0 ; i<n ; i++) {
			String nextString = st.nextToken();
			if (nextString.charAt(0) == '*') {
				primaryStartIndex = i;
				nextString = nextString.substring(1);
			}
			primaryUnits[i] = Integer.parseInt(nextString);
		}
	}
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		SummaryDataSet summaryData = new SummaryDataSet(data, "y");
		
		summaryData.addVariable("mean", new MeanVariable(translate("Mean"), "y", decimals));
		
			NormalDistnVariable normal = new NormalDistnVariable("Normal");
			
		summaryData.addVariable("normal", normal);
			setUpNormal(data, summaryData, true);
		
		summaryData.setAccumulate(true);
		return summaryData;
	}
	
	
	private void setUpNormal(DataSet data, SummaryDataSet summaryData, boolean changedPopn) {
		Sample2StageVariable ySamp = (Sample2StageVariable)data.getVariable("y");
		if (changedPopn)
			ySamp.calcTwoStageInfo();
		double mean = ySamp.getMean();
		double sd = ySamp.getSD();
		
		NormalDistnVariable normal = (NormalDistnVariable)summaryData.getVariable("normal");
		normal.setMean(mean);
		normal.setSD(sd);
	}

//---------------------------------------------------------------------

	
	protected XPanel displayPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("Center", new TwoStageSelectionView(data, this, "y"));
		
		return thePanel;
	}

	
	protected XPanel samplingPanel(boolean allowReset) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
					
			sampleButton = new XButton(translate("Take sample"), this);
		thePanel.add(sampleButton);
		
		if (allowReset) {
			resetButton = new XButton(translate("Reset"), this);
			thePanel.add(resetButton);
		}
		
		return thePanel;
	}

	
	protected XPanel samplingSliderPanel() {
		XPanel thePanel = new XPanel();
			
		if (primaryUnits.length > 1) {
			thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, 3));
			
				XLabel unitLabel = new XLabel(translate("Primary units sampled"), XLabel.LEFT, this);
				unitLabel.setFont(getStandardBoldFont());
			thePanel.add(unitLabel);
			
				primaryUnitSlider = new PrimaryUnitSlider(primaryUnits, primaryStartIndex, this);
			thePanel.add(primaryUnitSlider);
		}
		else
			thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
	}
	
	protected String correlationSliderName() {
		return translate("Variation within primary units") + ":";
	}

	
	protected XPanel correlationSliderPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
//			clusterSlider = new ParameterSlider(minCorr, maxCorr, startCorr,
//																									"Correlation within primary unit", this);
			clusterSlider = new XNoValueSlider(translate("High"), translate("Low"), correlationSliderName(), 0,
															kMaxCorrSteps, (int)Math.round(startCorr * startCorr * kMaxCorrSteps), this);
		
		thePanel.add("Center", clusterSlider);
		return thePanel;
	}


	protected XPanel costPanel() {
		String costString = getParameter(STAGE_COST_PARAM);
		if (costString !=  null)  {
			XPanel thePanel = new InsetPanel(10, 3);
			thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, 3));
				
				XLabel costLabel = new XLabel(translate("Cost"), XLabel.LEFT, this);
				costLabel.setFont(getStandardBoldFont());
			thePanel.add(costLabel);
				
				StringTokenizer st = new StringTokenizer(costString);
				String primaryString = st.nextToken();
				NumValue primaryCost = new NumValue(primaryString);
				String secondaryString = st.nextToken();
				NumValue secondaryCost = new NumValue(secondaryString);
				NumValue maxTotalCost = new NumValue(st.nextToken());
				String unitsString = getParameter(UNITS_PARAM);
			
				XLabel primaryCostLabel = new XLabel(translate("Primary unit") + ": " + primaryString
																				+ " " + unitsString, XLabel.LEFT, this);
				primaryCostLabel.setForeground(Color.blue);
				
			thePanel.add(primaryCostLabel);
			
				XLabel secondaryCostLabel = new XLabel(translate("Secondary unit") + ": " + secondaryString
																				+ " " + unitsString, XLabel.LEFT, this);
				secondaryCostLabel.setForeground(Color.blue);
				
			thePanel.add(secondaryCostLabel);
				
				TwoStageCostView costValue = new TwoStageCostView(data, this, "y", primaryCost,
																																		secondaryCost, maxTotalCost);
				costValue.setFont(getStandardBoldFont());
				costValue.setUnitsString(unitsString);
			thePanel.add(costValue);
			
			thePanel.lockBackground(kCostBackground);
			
			return thePanel;
		}
		else {
			XPanel thePanel = new XPanel();
			thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			return thePanel;
		}
	}

	
	protected XPanel samplingTypePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
		thePanel.add(new XLabel(translate("Sampling scheme") + ":", XLabel.LEFT, this));
		
			samplingSchemeChoice = new XChoice(this);
			samplingSchemeChoice.addItem(translate("Cluster sample"));
			samplingSchemeChoice.addItem(translate("Simple random sample"));
		thePanel.add(samplingSchemeChoice);
		
		return thePanel;
	}

//----------------------------------------------------------------
	
	private void clearSample() {
		summaryData.clearData();
		summaryData.variableChanged("mean");
		
		Sample2StageVariable y = (Sample2StageVariable)data.getVariable("y");
		y.clearSample();
		data.variableChanged("y");
	}
	
	protected void doTakeSample() {
		summaryData.takeSample();
	}
	
	protected void setClusterCorr() {
		double corr = clusterSlider.getValue() / (double)kMaxCorrSteps;
//		corr = Math.sqrt(corr);
		ClusterSampleVariable popn = (ClusterSampleVariable)data.getVariable("popn");
		popn.setProportion(1.0 - corr);
		data.variableChanged("popn");
		
		setUpNormal(data, summaryData, true);
		clearSample();
	}
	
	protected void changeSamplingScheme() {
		Sample2StageVariable y = (Sample2StageVariable)data.getVariable("y");
		y.setSamplingScheme(currentSamplingScheme);
		
		setUpNormal(data, summaryData, false);
		clearSample();
	}
	
	protected void setPrimaryUnits() {
		Sample2StageVariable y = (Sample2StageVariable)data.getVariable("y");
		int primaryUnits = primaryUnitSlider.getPrimaryUnits();
		int secondaryUnits = y.getSampleSize() / primaryUnits;
		
		y.setSampleSizes(primaryUnits, secondaryUnits);
		
		setUpNormal(data, summaryData, false);
		clearSample();
	}

	
	private boolean localAction(Object target) {
		if (target == resetButton) {
			clearSample();
			return true;
		}
		else if (target == sampleButton) {
			doTakeSample();
			return true;
		}
		else if (target == clusterSlider) {
			setClusterCorr();
			return true;
		}
		else if (target == primaryUnitSlider) {
			setPrimaryUnits();
			return true;
		}
		else if (target == samplingSchemeChoice) {
			if (currentSamplingScheme != samplingSchemeChoice.getSelectedIndex()) {
				currentSamplingScheme = samplingSchemeChoice.getSelectedIndex();
				changeSamplingScheme();
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