package stdErrorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.RandomNormal;
import distn.*;
import coreGraphics.*;
import coreSummaries.*;
import coreVariables.*;

import stdError.*;


public class ErrorDistnApplet extends XApplet {
	static final private String DATA_INFO_PARAM = "dataAxis";
	static final private String MEAN_INFO_PARAM = "meanAxis";
	static final private String ERROR_INFO_PARAM = "errorAxis";
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String MEAN_NAME_PARAM = "meanName";
	static final protected String DECIMALS_PARAM = "decimals";
	
	static final protected Color kErrorCrossColor = new Color(0x000099);
	static final protected Color kMeanCrossColor = new Color(0x660000);
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	private RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck;
	private XCheckbox displayTheoryCheck;
	
	private int noOfValues;
	private NumValue modelMean, modelSD;
	
	protected DataPlusDistnInterface meanView, errorView;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		setLayout(new BorderLayout());
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new ProportionLayout(0.35, 10, ProportionLayout.VERTICAL,
																									ProportionLayout.TOTAL));
				dataPanel.add(ProportionLayout.TOP, dataPanel(data));
				dataPanel.add(ProportionLayout.BOTTOM, summaryPanel(summaryData));
		
		add("Center", dataPanel);
		
			XPanel controlPanel = new XPanel();
			controlPanel.setLayout(new ProportionLayout(0.35, 10, ProportionLayout.VERTICAL,
																									ProportionLayout.TOTAL));
				controlPanel.add(ProportionLayout.TOP, sampleControlPanel(summaryData));
				controlPanel.add(ProportionLayout.BOTTOM, displayControlPanel(summaryData));
				
		add("East", controlPanel);
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		RandomNormal generator = new RandomNormal(getParameter(RANDOM_NORMAL_PARAM));
		NumVariable y = new NumSampleVariable(getParameter(VAR_NAME_PARAM), generator, 10);
		data.addVariable("y", y);
		
		NormalDistnVariable dataDistn = new NormalDistnVariable("data model");
		StringTokenizer st = new StringTokenizer(getParameter(RANDOM_NORMAL_PARAM));
		noOfValues = Integer.parseInt(st.nextToken());
		modelMean = new NumValue(st.nextToken());
		modelSD = new NumValue(st.nextToken());
		dataDistn.setParams(modelMean.toString() + " " + modelSD.toString());
		data.addVariable("model", dataDistn);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
			int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
			MeanVariable mean = new MeanVariable(getParameter(MEAN_NAME_PARAM), "y", decimals);
		
		summaryData.addVariable("mean", mean);
		
			ScaledVariable error = new ScaledVariable(translate("Error"), mean, "mean", -modelMean.toDouble(),
																																				1.0, decimals);
		
		summaryData.addVariable("error", error);
		
			NormalDistnVariable meanDistn = new NormalDistnVariable("mean distn");
			NumValue meanSD = new NumValue(modelSD.toDouble() / Math.sqrt(noOfValues), modelSD.decimals + 6);
			meanDistn.setParams(modelMean.toString() + " " + meanSD.toString());
		summaryData.addVariable("meanDistn", meanDistn);

			NormalDistnVariable errorDistn = new NormalDistnVariable("error distn");
			errorDistn.setParams("0.0 " + meanSD.toString());
		summaryData.addVariable("errorDistn", errorDistn);
		
		return summaryData;
	}
	
	private HorizAxis getAxis(DataSet data, String variableKey, String axisInfoParam) {
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(axisInfoParam);
		theHorizAxis.readNumLabels(labelInfo);
		Variable v = (Variable)data.getVariable(variableKey);
		theHorizAxis.setAxisName(v.name);
		return theHorizAxis;
	}
	
	protected XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = getAxis(data, "y", DATA_INFO_PARAM);
		thePanel.add("Bottom", theHorizAxis);
		
			ErrorDistnView dataView = new ErrorDistnView(data, this, theHorizAxis, "model",
																														1.0, ErrorDistnView.SAMPLE_AND_MEAN);
			dataView.lockBackground(Color.white);
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	protected XPanel summaryPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.55, 10, ProportionLayout.VERTICAL));
		
		thePanel.add(ProportionLayout.TOP, summaryDotPanel(summaryData,
																			MEAN_INFO_PARAM, ErrorDistnView.SUMMARY_MEANS));
		thePanel.add(ProportionLayout.BOTTOM, summaryDotPanel(summaryData,
																			ERROR_INFO_PARAM, ErrorDistnView.SUMMARY_ERRORS));
		
		return thePanel;
	}
	
	protected XPanel summaryDotPanel(SummaryDataSet summaryData, String axisInfoParam,
																																					int displayType) {
		String variableKey = (displayType == ErrorDistnView.SUMMARY_MEANS) ? "mean" : "error";
		String theoryKey = (displayType == ErrorDistnView.SUMMARY_MEANS) ? "meanDistn" : "errorDistn";
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			Color mainColor = (displayType == ErrorDistnView.SUMMARY_ERRORS) ? kErrorCrossColor
																															: kMeanCrossColor;
		
			HorizAxis theHorizAxis = getAxis(summaryData, variableKey, axisInfoParam);
			theHorizAxis.setForeground(mainColor);
		thePanel.add("Bottom", theHorizAxis);
		
			ErrorDistnView dataView = new ErrorDistnView(summaryData, this, theHorizAxis, theoryKey, 1.0, displayType);
			dataView.setActiveNumVariable(variableKey);
			dataView.lockBackground(Color.white);
			dataView.setForeground(mainColor);
		thePanel.add("Center", dataView);
			
			if (displayType == ErrorDistnView.SUMMARY_MEANS)
				meanView = dataView;
			else
				errorView = dataView;
		
		return thePanel;
	}
	
	protected XPanel sampleControlPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
			takeSampleButton = new RepeatingButton(translate("Take sample"), this);
			
		thePanel.add(takeSampleButton);
		
			accumulateCheck = new XCheckbox(translate("Accumulate"), this);
			
		thePanel.add(accumulateCheck);
		
		return thePanel;
	}
	
	protected XPanel displayControlPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
			displayTheoryCheck = new XCheckbox("Show Theory", this);
			
		thePanel.add(displayTheoryCheck);
		
		return thePanel;
	}
	
	private void doTakeSample() {
		summaryData.takeSample();
	}
	
	protected void doShowTheory(boolean showNotHide) {
		int displayType = showNotHide ? DataPlusDistnInterface.CONTIN_DISTN
																										: DataPlusDistnInterface.NO_DISTN;
		meanView.setShowDensity(displayType);
		errorView.setShowDensity(displayType);
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
		else if (target == displayTheoryCheck) {
			doShowTheory(displayTheoryCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}