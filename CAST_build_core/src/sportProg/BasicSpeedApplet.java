package sportProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import time.*;
import sport.*;

abstract public class BasicSpeedApplet extends XApplet {
	static final public String DATA_NAME_PARAM = "dataName";

	static final private String AXIS_INFO_PARAM = "vertAxis";
	static final private String TIME_INFO_PARAM = "timeAxis";
	
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final private String DATA_DECIMALS_PARAM = "dataDecimals";
	static final private String EXPECTED_NAME_PARAM = "expectedName";
	
	private MinimumRandom generator;
	
	private RepeatingButton takeSampleButton;
	private XCheckbox algorithmCheck;
	private XCheckbox accumulateCheck;
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	private TimeView theView;
	
	public void setupApplet() {
		data = createData();
		summaryData = createSummaryData(data);
		if (summaryData != null)
			summaryData.takeSample();
		
		setLayout(new BorderLayout());
		add("North", valuePanel(data, summaryData));
		add("Center", displayPanel(data, summaryData));
		add("South", controlPanel(data));
	}
	
	abstract protected DataSet createData();
	abstract protected SummaryDataSet createSummaryData(DataSet sourceData);
	abstract protected XPanel displayPanel(DataSet data, SummaryDataSet summaryData);
	abstract protected XPanel controlPanel(DataSet data);
	abstract protected XPanel valuePanel(DataSet data, SummaryDataSet summaryData);
	abstract protected TimeView getTimeView(DataSet theData, String dataName,
																TimeAxis timeAxis, VertAxis numAxis);
	
	protected int[] readSampleSizes() {
		StringTokenizer st = new StringTokenizer(getParameter(SAMPLE_SIZE_PARAM));
		int firstCount = Integer.parseInt(st.nextToken());
//		double firstTrans = Math.log(firstCount);
		double firstTrans = Math.sqrt(firstCount);
		int lastCount = Integer.parseInt(st.nextToken());
//		double lastTrans = Math.log(lastCount);
		double lastTrans = Math.sqrt(lastCount);
		int noOfTimes = Integer.parseInt(st.nextToken());
		
		int sampleSize[] = new int[noOfTimes];
		for (int i=0 ; i<noOfTimes ; i++)
//			sampleSize[i] = (int)Math.round(Math.exp(firstTrans + i * (lastTrans - firstTrans) / (noOfTimes - 1)));
			sampleSize[i] = (int)Math.round(Math.pow(firstTrans + i * (lastTrans - firstTrans) / (noOfTimes - 1), 2));
		
		while (st.hasMoreTokens())
			sampleSize[Integer.parseInt(st.nextToken())] = 0;
		return sampleSize;
	}
	
	protected NormalDistnVariable createPopulationVariable(DataSet data, String popnKey) {
		NormalDistnVariable popnVar = new NormalDistnVariable(translate("Population"));
		data.addVariable(popnKey, popnVar);
		return popnVar;
	}
	
	protected void createSimulationVariable(DataSet data, int[] sampleSize,
									String popnDistnKey, String probKey, String dataKey) {
		generator = new MinimumRandom(sampleSize);
		
		NumSampleVariable pVar = new NumSampleVariable(translate("prob"), generator, 10);
		data.addVariable(probKey, pVar);
		
		int decimals = Integer.parseInt(getParameter(DATA_DECIMALS_PARAM));
		InvNormalQVariable timeVar = new InvNormalQVariable(getParameter(VAR_NAME_PARAM), pVar,
																				data, popnDistnKey, decimals);
		data.addVariable(dataKey, timeVar);
	}
	
	protected void createExpectedVariable(DataSet data, int[] sampleSize,
													String popnDistnKey, String expectedKey) {
		int noOfTimes = sampleSize.length;
		double eProbs[] = new double[noOfTimes];
		for (int i=0 ; i<noOfTimes ; i++)
			eProbs[i] = 1.0 / (sampleSize[i] + 1.0);
		
		NumVariable eProbVar = new NumVariable("expected");
		eProbVar.setValues(eProbs);
		data.addVariable("ep", eProbVar);
		
		int decimals = Integer.parseInt(getParameter(DATA_DECIMALS_PARAM));
		InvNormalQVariable eTimeVar = new InvNormalQVariable(getParameter(EXPECTED_NAME_PARAM),
																		eProbVar, data, popnDistnKey, decimals);
		data.addVariable(expectedKey, eTimeVar);
	}
	
	protected void createRealDataVariable(DataSet data, String dataKey) {
		data.addNumVariable(dataKey, getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		String labelVarName = getParameter(LABEL_NAME_PARAM);
		if (labelVarName != null)
			data.addLabelVariable("label", labelVarName, getParameter(LABELS_PARAM));
	}
	
	
	protected XPanel timeSeriesPanel(DataSet data, String dataKey, String expectedKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		VertAxis theVertAxis = new VertAxis(this);
		theVertAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
		thePanel.add("Left", theVertAxis);
		
		TimeAxis theHorizAxis = horizAxis(data);
		thePanel.add("Bottom", theHorizAxis);
		
		theView = getTimeView(data, getParameter(DATA_NAME_PARAM), theHorizAxis, theVertAxis);
		theView.setActiveNumVariable(dataKey);
		
		theView.setSmoothedVariable(dataKey);
		if (expectedKey != null)
			theView.addSmoothedVariable(expectedKey);
		
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	private TimeAxis horizAxis(DataSet data) {
//		String timeParam = getParameter(TIME_INFO_PARAM);
		IndexTimeAxis theHorizAxis = new IndexTimeAxis(this, data.getNumVariable().noOfValues());
		theHorizAxis.setTimeScale(getParameter(TIME_INFO_PARAM));
		theHorizAxis.setAxisName("Year");
		return theHorizAxis;
	}
	
	protected XCheckbox createAlgorithmCheck() {
		algorithmCheck = new XCheckbox(translate("Fast algorithm"), this);
		return algorithmCheck;
	}
	
	protected RepeatingButton createSampleButton() {
		takeSampleButton = new RepeatingButton(translate("Do simulation"), this);
		return takeSampleButton;
	}
	
	protected XCheckbox createAccumulateCheck() {
		accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		return accumulateCheck;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else if (evt.target == takeSampleButton) {
			if (!algorithmCheck.getState()) {
				takeSampleButton.disable();
				takeSampleButton.update(takeSampleButton.getGraphics());
							//	needs update() rather than repaint() since no repaint occurs during long simulation
			}
			summaryData.takeSample();
			takeSampleButton.enable();
			data.variableChanged("y");
			return true;
		}
		else if (evt.target == accumulateCheck) {
			summaryData.setAccumulate(accumulateCheck.getState());
			return true;
		}
		else if (evt.target == algorithmCheck) {
			generator.setAlgorithm(algorithmCheck.getState() ? MinimumRandom.FAST_ALGORITHM
																				: MinimumRandom.SIMPLE_ALGORITHM);
			return true;
		}
		return false;
	}
}