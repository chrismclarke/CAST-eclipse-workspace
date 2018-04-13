package testProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.*;
import distn.*;
import qnUtils.*;
import sampling.*;
import coreGraphics.*;
import coreSummaries.*;
import imageGroups.*;
import imageUtils.*;
import test.*;


public class TAgainstNormalApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String SUMMARY_DECIMALS_PARAM = "summaryDecimals";
	static final private String FORMULA_GIF_PARAM = "formulaGif";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	
	static final private String kZAxisScale = "-10 10 -10 5";
	
	static final private Color kPinkColor = new Color(0xFFCCCC);
	static final private Color kDarkPinkColor = new Color(0xBB8888);
	static final private Color kLightBlueColor = new Color(0xCCCCFF);
	
	private DataSet data;
	private SummaryDataSet summaryData;
	private RandomNormal generator;
	
	private RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck;
	private XChoice sampleSizeChoice;
	private int currentSampleSize = 0;
	
//	private HypothesisTest test;
//	private int testTail;
	
	private NumValue normalMean, normalSD;
	
	private int zDecimals;
	private int sampleSize[];
	
	public void setupApplet() {
		MeanSDImages.loadMeanSD(this);
		
		getData();
		getSummaryData(data);
		
		setLayout(new ProportionLayout(0.5, 25, ProportionLayout.HORIZONTAL,
																								ProportionLayout.TOTAL));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new BorderLayout(0, 3));
			
			leftPanel.add("Center", dataPlotPanel(data));
			
			leftPanel.add("South", controlPanel(data, summaryData));
		
		add(ProportionLayout.LEFT, leftPanel);
		
			XPanel rightPanel = new InsetPanel(0, 30);
			rightPanel.setLayout(new BorderLayout(0, 20));
			
			rightPanel.add("North", calcPanel(data, summaryData));
			
			rightPanel.add("Center", tDistnPanel(summaryData));
		
		add(ProportionLayout.RIGHT, rightPanel);
	}
	
	private void getData() {
		data = new DataSet();
		
			String generatorString = getParameter(RANDOM_NORMAL_PARAM);
				StringTokenizer st = new StringTokenizer(generatorString);
				@SuppressWarnings("unused")
				int count = Integer.parseInt(st.nextToken());
				normalMean = new NumValue(st.nextToken());
				normalSD = new NumValue(st.nextToken());
			generator = new RandomNormal(generatorString);
		
		data.addVariable("y", new NumSampleVariable(getParameter(VAR_NAME_PARAM), generator, 10));
		
			NormalDistnVariable normalDistn = new NormalDistnVariable("normal");
			normalDistn.setMean(normalMean.toDouble());
			normalDistn.setSD(normalSD.toDouble());
			
		data.addVariable("normal", normalDistn);
	}
	
	private void getSummaryData(DataSet data) {
		summaryData = new SummaryDataSet(data, "y");
		
			HypothesisTest test = new UnivarHypothesisTest(data, "y", normalMean,
																HypothesisTest.HA_NOT_EQUAL, HypothesisTest.MEAN, this);
		
			StringTokenizer st = new StringTokenizer(getParameter(SUMMARY_DECIMALS_PARAM));
			zDecimals = Integer.parseInt(st.nextToken());
		
			StatisticValueVariable zValue = new StatisticValueVariable(translate("t statistic"), test, zDecimals);
		summaryData.addVariable("z", zValue);

			NormalDistnVariable zDistn = new NormalDistnVariable("z distn");
		summaryData.addVariable("zDistn", zDistn);
		
		summaryData.takeSample();
	}
	
	private XPanel dataPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			CoreVariable v = data.getVariable("y");
			theHorizAxis.setAxisName(v.name);
		
		thePanel.add("Bottom", theHorizAxis);
		
			JitterPlusNormalView dataView = new JitterPlusNormalView(data, this, theHorizAxis, "normal", 1.0);
			dataView.setDensityColor(kLightBlueColor);
			dataView.lockBackground(Color.white);
			dataView.setDistnLabel(new LabelValue(translate("Population")), Color.blue);
			
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	private XPanel tDistnPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			theHorizAxis.readNumLabels(kZAxisScale);
			CoreVariable v = summaryData.getVariable("z");
			theHorizAxis.setAxisName(v.name);
		thePanel.add("Bottom", theHorizAxis);
		
			StackedPlusNormalView zView = new StackedPlusNormalView(summaryData, this, theHorizAxis, "zDistn");
			zView.lockBackground(Color.white);
			zView.setDensityColor(kPinkColor);
			zView.setDistnLabel(new LabelValue(translate("Standard normal distn")), kDarkPinkColor);
		
		thePanel.add("Center", zView);
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 25));
		
			XPanel statisticPanel = new XPanel();
			statisticPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			
			statisticPanel.add(new SummaryView(data, this, "y", null, SummaryView.MEAN, zDecimals, SummaryView.SAMPLE));

			statisticPanel.add(new SummaryView(data, this, "y", null, SummaryView.SD, zDecimals, SummaryView.SAMPLE));
				
		thePanel.add("North", statisticPanel);
		
		thePanel.add("Center", samplingPanel(summaryData));
		
		return thePanel;
	}
	
	private XPanel samplingPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
			XPanel sampleSizePanel = new XPanel();
			sampleSizePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
			XLabel sampSizeLabel = new XLabel(translate("Sample size") + ":", XLabel.LEFT, this);
			sampSizeLabel.setFont(getStandardBoldFont());
			sampleSizePanel.add(sampSizeLabel);
				
				sampleSizeChoice = new XChoice(this);
				readSampleSizes();
				for (int i=0 ; i<sampleSize.length ; i++)
					sampleSizeChoice.addItem(String.valueOf(sampleSize[i]));
			
			sampleSizePanel.add(sampleSizeChoice);
			
		thePanel.add("West", sampleSizePanel);
			
			XPanel samplePanel = new XPanel();
			samplePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 7));
			
				takeSampleButton = new RepeatingButton(translate("Take sample"), this);
			samplePanel.add(takeSampleButton);
			
				accumulateCheck = new XCheckbox(translate("Accumulate"), this);
			samplePanel.add(accumulateCheck);
		
		thePanel.add("Center", samplePanel);
		
		return thePanel;
	}
	
	private void readSampleSizes() {
		StringTokenizer st = new StringTokenizer(getParameter(SAMPLE_SIZE_PARAM));
		int nSizes = 0;
		while (st.hasMoreTokens()) {
			nSizes++;
			st.nextToken();
		}
		sampleSize = new int[nSizes];
		st = new StringTokenizer(getParameter(SAMPLE_SIZE_PARAM));
		for (int i=0 ; i<nSizes ; i++)
			sampleSize[i] = Integer.parseInt(st.nextToken());
	}
	
	private XPanel calcPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 2));
		
			String formulaGif = getParameter(FORMULA_GIF_PARAM) + ".gif";
			OneValueImageView zCalculator = new OneValueImageView(summaryData, "z", this, formulaGif, 25, new NumValue(-9.9, zDecimals));
			
		thePanel.add(zCalculator);
		
		return thePanel;
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
			if (currentSampleSize != newChoice) {
				currentSampleSize = newChoice;
				summaryData.changeSampleSize(sampleSize[newChoice]);
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