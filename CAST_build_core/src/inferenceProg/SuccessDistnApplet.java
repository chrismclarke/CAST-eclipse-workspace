package inferenceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.*;
import distn.*;
import coreGraphics.*;
import random.RandomMultinomial;
import coreSummaries.*;
import formula.*;
import imageGroups.*;

import cat.*;
import inference.*;


public class SuccessDistnApplet extends XApplet {
	static final private String PROB_PARAM = "probLimits";
	static final private String N_SAMPLE_SIZE_PARAM = "nSampleSize";
	static final private String SAMPLE_SIZE_PARAM = "sampleInfo";
	static final protected String DECIMALS_PARAM = "decimals";
	
//	static final private String CAT_PROB_PARAM = "catProb";
//	static final private String SAMPLING_PARAM = "sampling";
//	static final private String SUMMARY_AXIS_INFO_PARAM = "summaryAxis";
	static final protected String PROPN_NAME_PARAM = "propnName";
	static final protected String PROB_AXIS_PARAM = "probAxis";
	
	static final protected String PROB_LABEL_PARAM = "probLabel";
	static final protected String N_LABEL_PARAM = "nLabel";
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	private RandomMultinomial generator;
	private ParameterSlider probSlider;
	
	private XChoice sampleSizeChoice;
	private int currentSampleIndex = 0;
	private XChoice summaryDisplayChoice;
	private int currentSummaryDisplay = 0;
	private XChoice fittedDistnChoice;
	private int currentFittedDistn = 0;
	
	private int sampleSize[];
	private String axisInfo[];
	private int targetCount[];
	private String meanLimits[];
	private String sdLimits[];
	
	private RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck;
	
	private ParameterSlider meanSlider, sdSlider;
	private XButton bestFitButton, bestModelButton; 
	
	private MultiHorizAxis summaryAxis;
	private DiscretePlusNormView summaryView;
	
	private NormalDistnVariable normalApproxDistn;
	
	XPanel paramPanel;
	private CardLayout paramPanelLayout;
	
	private ProportionBelowView simulationPropn;
	private ProportionView modelPropn;
	
	public void setupApplet() {
		MeanSDImages.loadMeanSD(this);
		
		createSamplingInfo();
		
		data = getData();
		summaryData = getSummaryData(data);
		
		summaryData.takeSample();
		
		setLayout(new BorderLayout(0, 30));
		add("North", samplePanel(data, summaryData));
		add("Center", summaryPanel(data, summaryData, targetCount[currentSampleIndex]));
	}
	
	private void createSamplingInfo() {
		StringTokenizer st = new StringTokenizer(getParameter(PROB_PARAM));

		String s = st.nextToken();
		NumValue minValue = new NumValue(s);
		if (st.hasMoreTokens())
			s = st.nextToken();
		NumValue maxValue = new NumValue(s);
		if (st.hasMoreTokens())
			s = st.nextToken();
		NumValue startValue = new NumValue(s);
		
		probSlider = new ParameterSlider(minValue, maxValue, startValue, getParameter(PROB_LABEL_PARAM),
																										this);
		
		int noOfSampleSizes = Integer.parseInt(getParameter(N_SAMPLE_SIZE_PARAM));
		sampleSize = new int[noOfSampleSizes];
		axisInfo = new String[noOfSampleSizes];
		targetCount = new int[noOfSampleSizes];
		meanLimits = new String[noOfSampleSizes];
		sdLimits = new String[noOfSampleSizes];
		for (int i=0 ; i<noOfSampleSizes ; i++) {
			String sampleSizeString = getParameter(SAMPLE_SIZE_PARAM + i);
			st = new StringTokenizer(sampleSizeString, "/");
			String nString = st.nextToken();
			if (nString.charAt(0) == '*') {
				currentSampleIndex = i;
				nString = nString.substring(1);
			}
			sampleSize[i] = Integer.parseInt(nString);
			axisInfo[i] = st.nextToken();
			targetCount[i] = Integer.parseInt(st.nextToken());
			meanLimits[i] = st.nextToken();
			sdLimits[i] = st.nextToken();
		}
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		double p[] = new double[2];
		p[0] = probSlider.getParameter().toDouble();
		p[1] = 1.0 - p[0];
		generator = new RandomMultinomial(sampleSize[currentSampleIndex], p);
		
		CatSampleVariable sv = new CatSampleVariable(getParameter(CAT_NAME_PARAM), generator);
		sv.readLabels(getParameter(CAT_LABELS_PARAM));
		data.addVariable("y", sv);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
		CountVariable propn = new CountVariable(getParameter(PROPN_NAME_PARAM), "y");
		
		summaryData.addVariable("count", propn);
		
		normalApproxDistn = new NormalDistnVariable("normalApprox");
		summaryData.addVariable("normal", normalApproxDistn);
		
		return summaryData;
	}
	
	private XPanel samplePanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(40, 5));
		
		thePanel.add("Center", paramControlPanel(data));
		thePanel.add("East", sampleViewPanel(data));
		thePanel.add("South", samplingControlPanel(summaryData));
		
		return thePanel;
	}
	
	private XPanel sampleViewPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.6, 5));
		
		thePanel.add(ProportionLayout.LEFT, dataTablePanel(data));
		thePanel.add(ProportionLayout.RIGHT, dataPieView(data));
		
		return thePanel;
	}
	
	private XPanel samplingControlPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
		
		takeSampleButton = new RepeatingButton(translate("Take sample"), this);
		thePanel.add(takeSampleButton);
		
		accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		thePanel.add(accumulateCheck);
		
		ValueCountView theCount = new ValueCountView(summaryData, this);
		theCount.setLabel("");
		thePanel.add(theCount);
		
		return thePanel;
	}
	
	private XPanel paramControlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 3));
		
		if (probSlider.getMinValue() == probSlider.getMaxValue()) {
			XPanel pPanel = new XPanel();
			pPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
				String pLabel = getParameter(PROB_LABEL_PARAM);
				XLabel probText = new XLabel(pLabel + " = " + probSlider.getParameter().toString(), XLabel.LEFT, this);
				probText.setFont(getStandardBoldFont());
			pPanel.add(probText);
			
			thePanel.add(pPanel);
		}
		else
			thePanel.add(probSlider);
			
			XPanel nPanel = new XPanel();
			nPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			String nLabel = getParameter(N_LABEL_PARAM);
			if (sampleSize.length == 1) {
				XLabel sampleSizeText = new XLabel(nLabel + " " + sampleSize[0], XLabel.LEFT, this);
				sampleSizeText.setFont(getStandardBoldFont());
				nPanel.add(sampleSizeText);
			}
			else {
				XLabel label = new XLabel(nLabel, XLabel.LEFT, this);
				label.setFont(getStandardBoldFont());
				nPanel.add(label);
				
					sampleSizeChoice = new XChoice(this);
					for (int i=0 ; i<sampleSize.length ; i++)
						sampleSizeChoice.addItem(String.valueOf(sampleSize[i]));
					sampleSizeChoice.select(currentSampleIndex);
				
				nPanel.add(sampleSizeChoice);
			}
		
		thePanel.add(nPanel);
		
		return thePanel;
	}
	
	private DataView dataPieView(DataSet data) {
		return new PieView(data, this, "y", CatDataView.SELECT_ONE);
	}
	
	private XPanel dataTablePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
		CatVariable v = (CatVariable)data.getVariable("y");
		XLabel varName = new XLabel(v.name, XLabel.CENTER, this);
		varName.setFont(getStandardBoldFont());
		thePanel.add(varName);
		
		int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
		FreqTableView tableView = new FreqTableView(data, this, "y", CatDataView.SELECT_ONE, decimals);
//		tableView.lockBackground(Color.white);
		
		thePanel.add(tableView);
		
		return thePanel;
	}
	
	private XPanel summaryPanel(DataSet data, SummaryDataSet summaryData, int targetCount) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.6, 5));
		
		thePanel.add(ProportionLayout.LEFT, summaryDataPanel(data, summaryData, targetCount));
		thePanel.add(ProportionLayout.RIGHT, fittedModelPanel(data, summaryData));
		
		setTargetCount();
		
		return thePanel;
	}
	
	private XPanel fittedModelPanel(DataSet data, DataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				fittedDistnChoice = new XChoice(this);
				fittedDistnChoice.addItem("Simulation only");
				fittedDistnChoice.addItem("Simulation and normal");
			choicePanel.add(fittedDistnChoice);
		
		thePanel.add("North", choicePanel);
		
			paramPanel = new XPanel();
				paramPanelLayout = new CardLayout();
			paramPanel.setLayout(paramPanelLayout);
			paramPanel.add("None", new XPanel());
			paramPanel.add("Normal", normalParamPanel(summaryData));
			
		thePanel.add("Center", paramPanel);
		
		return thePanel;
	}
	
	private void adjustMeanSD() {
		NormalDistnVariable distn = (NormalDistnVariable)summaryData.getVariable("normal");
		distn.setMean(meanSlider.getParameter().toDouble());
		distn.setSD(sdSlider.getParameter().toDouble());
		normalApproxDistn.setMinSelection(Double.NEGATIVE_INFINITY);
		normalApproxDistn.setMaxSelection(targetCount[currentSampleIndex] + 0.5);
	}
	
	private ParameterSlider createSlider(String params, String paramName) {
		StringTokenizer st = new StringTokenizer(params);
		NumValue min = new NumValue(st.nextToken());
		NumValue max = new NumValue(st.nextToken());
		int nSteps = Integer.parseInt(st.nextToken());
		NumValue start = new NumValue(st.nextToken());
		return new ParameterSlider(min, max, start, nSteps, paramName, this);
	}
	
	private void changeSliderLimits(ParameterSlider slider, String params) {
		StringTokenizer st = new StringTokenizer(params);
		NumValue min = new NumValue(st.nextToken());
		NumValue max = new NumValue(st.nextToken());
		int nSteps = Integer.parseInt(st.nextToken());
		NumValue start = new NumValue(st.nextToken());
		slider.changeLimits(min, max, start, nSteps);
	}
	
	private XPanel normalParamPanel(DataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 3));
		
			String mu = MText.expandText("#mu#");
			meanSlider = createSlider(meanLimits[currentSampleIndex], mu);
			meanSlider.setForeground(Color.blue);
		thePanel.add(meanSlider);
			
			String sigma = MText.expandText("#sigma#");
			sdSlider = createSlider(sdLimits[currentSampleIndex], sigma);
			sdSlider.setForeground(Color.red);
		thePanel.add(sdSlider);
			
			adjustMeanSD();
			
			XPanel lowParamPanel = new XPanel();
			lowParamPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
			
				XPanel buttonPanel = new XPanel();
				buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
					bestFitButton = new XButton("Best (Sim)", this);
				
				buttonPanel.add(bestFitButton);
			
					bestModelButton = new XButton("Best (Model)", this);
				buttonPanel.add(bestModelButton);
			
			lowParamPanel.add(buttonPanel);
			
			modelPropn = new ProportionView(summaryData, "normal", this);
			lowParamPanel.add(modelPropn);
			
		thePanel.add(lowParamPanel);
		return thePanel;
	}
	
	private XPanel summaryDataPanel(DataSet data, DataSet summaryData, int targetCount) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("Center", dotPlotPanel(data, summaryData, targetCount));
		thePanel.add("South", summaryControlPanel(summaryData));
		
		return thePanel;
	}
	
/*
	private void setViewHighlight(int targetCount) {
		summaryView.setCrossHighlight(targetCount + 0.5, StackedDiscreteView.LOW_HIGHLIGHT);
		DistnVariable distnVar = (DistnVariable)summaryData.getVariable("normal");
		distnVar.setMinSelection(Double.NEGATIVE_INFINITY);
		distnVar.setMaxSelection(targetCount + 0.5);
	}
*/
	
	private XPanel dotPlotPanel(DataSet data, DataSet summaryData, int targetCount) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			summaryAxis = new MultiHorizAxis(this, sampleSize.length);
			summaryAxis.readNumLabels(axisInfo[0]);
			for (int i=1 ; i<axisInfo.length ; i++)
				summaryAxis.readExtraNumLabels(axisInfo[i]);
			summaryAxis.setStartAlternate(currentSampleIndex);
			summaryAxis.setChangeMinMax(true);
			CatSampleVariable sv = (CatSampleVariable)data.getVariable("y");
			summaryAxis.setAxisName("Number of '" + sv.getLabel(0).toString() + "'");
		
		thePanel.add("Bottom", summaryAxis);
		
			summaryView = new DiscretePlusNormView(summaryData, this, summaryAxis, "count", "normal");
			summaryView.setCrossHighlight(targetCount + 0.5, StackedDiscreteView.LOW_HIGHLIGHT);
			summaryView.setHighlightBackground(true);
			summaryView.lockBackground(Color.white);
		thePanel.add("Center", summaryView);
		
		return thePanel;
	}
	
	private void setTargetCount() {
		int target = targetCount[currentSampleIndex];
		simulationPropn.setTargetValue(target);
		simulationPropn.setLabel("Proportion with " + target + " or less =");
		simulationPropn.setLabel("Proportion with " + target + " or less =");
		simulationPropn.repaint();
		
		modelPropn.setLabel("Prob of " + target + " or less (normal) =");
		modelPropn.repaint();
	}
	
	private XPanel summaryControlPanel(DataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
			
			choicePanel.add(new XLabel("Show simulation results as...", XLabel.LEFT, this));
			
				summaryDisplayChoice = new XChoice(this);
				summaryDisplayChoice.addItem(translate("Crosses"));
				summaryDisplayChoice.addItem(translate("Histogram"));
				summaryDisplayChoice.addItem("No display");
			
			choicePanel.add(summaryDisplayChoice);
		
		thePanel.add(choicePanel);
		
			XPanel propnPanel = new XPanel();
			propnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
				simulationPropn = new ProportionBelowView(summaryData, "count", this);
			propnPanel.add(simulationPropn);
		
		thePanel.add(propnPanel);
		
		return thePanel;
	}
	
	private void clearSample() {
		CatSampleVariable sampleVar = (CatSampleVariable)data.getVariable("y");
		sampleVar.clearSample();
		data.variableChanged("y");
		
		summaryData.clearData();
		summaryData.variableChanged("count");
	}
	
	private void setBestSimParams(NormalDistnVariable distn, NumVariable simCounts) {
		double sy = 0.0;
		double syy = 0.0;
		int n = 0;
		ValueEnumeration e = simCounts.values();
		while (e.hasMoreValues()) {
			double nextVal = e.nextDouble();
			sy += nextVal;
			syy += nextVal * nextVal;
			n ++;
		}
		double mean = sy / n;
		double sd = Math.sqrt((syy - mean * sy) / (n - 1));
		distn.setMean(mean);
		distn.setSD(sd);
		
		if (meanSlider != null)
			meanSlider.setParameter(mean);
		if (sdSlider != null)
			sdSlider.setParameter(sd);
		adjustMeanSD();
	}
	
	private void setBestModelParams(NormalDistnVariable distn) {
		double p = probSlider.getParameter().toDouble();
		int n = sampleSize[currentSampleIndex];
		double mean = n * p;
		double sd = Math.sqrt(n * p * (1.0 - p));
		distn.setMean(mean);
		distn.setSD(sd);
		
		if (meanSlider != null)
			meanSlider.setParameter(mean);
		if (sdSlider != null)
			sdSlider.setParameter(sd);
		adjustMeanSD();
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
		else if (target == probSlider) {
			double newProb[] = new double[2];
			newProb[0] = probSlider.getParameter().toDouble();
			generator.setProbs(newProb);
			
			clearSample();
			return true;
		}
		else if (target == sampleSizeChoice) {
			int newChoice = sampleSizeChoice.getSelectedIndex();
			if (currentSampleIndex != newChoice) {
				currentSampleIndex = newChoice;
				generator.setSampleSize(sampleSize[currentSampleIndex]);
				
				summaryAxis.setAlternateLabels(currentSampleIndex);
				summaryAxis.repaint();
				
				clearSample();
				
				changeSliderLimits(meanSlider, meanLimits[currentSampleIndex]);
				changeSliderLimits(sdSlider, sdLimits[currentSampleIndex]);
				
				adjustMeanSD();
				summaryView.setCrossHighlight(targetCount[currentSampleIndex] + 0.5, StackedDiscreteView.LOW_HIGHLIGHT);
				setTargetCount();
				summaryData.variableChanged("normal");
			}
			return true;
		}
		else if (target == summaryDisplayChoice) {
			int newChoice = summaryDisplayChoice.getSelectedIndex();
			if (currentSummaryDisplay != newChoice) {
				currentSummaryDisplay = newChoice;
				int display = (currentSummaryDisplay == 0) ? StackedDiscreteView.CROSS_DISPLAY
									: (currentSummaryDisplay == 1) ? StackedDiscreteView.HISTO_DISPLAY
									: StackedDiscreteView.NO_DISPLAY;
				summaryView.setDisplayType(display);
				summaryView.repaint();
			}
			return true;
		}
		else if (target == fittedDistnChoice) {
			int newChoice = fittedDistnChoice.getSelectedIndex();
			if (currentFittedDistn != newChoice) {
				currentFittedDistn = newChoice;
				summaryView.setDrawTheory(currentFittedDistn == 1);
				summaryView.repaint();
				paramPanelLayout.show(paramPanel, newChoice == 0 ? "None" : "Normal");
			}
			return true;
		}
		else {
			NormalDistnVariable distn = (NormalDistnVariable)summaryData.getVariable("normal");
			if (target == meanSlider) {
				adjustMeanSD();
				summaryData.variableChanged("normal");
				return true;
			}
			else if (target == sdSlider) {
				adjustMeanSD();
				summaryData.variableChanged("normal");
				return true;
			}
			else if (target == bestFitButton) {
				NumVariable simCounts = (NumVariable)summaryData.getVariable("count");
				setBestSimParams(distn, simCounts);
				summaryData.variableChanged("normal");
				return true;
			}
			else if (target == bestModelButton) {
				setBestModelParams(distn);
				summaryData.variableChanged("normal");
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}