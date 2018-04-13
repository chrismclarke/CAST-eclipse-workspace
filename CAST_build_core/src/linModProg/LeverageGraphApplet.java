package linModProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.*;
import random.RandomNormal;
import models.*;

import linMod.*;


public class LeverageGraphApplet extends XApplet {
	static final private String REGN_MODEL_PARAM = "regnModel";
	static final protected String X_AXIS_PARAM = "horizAxis";
	static final protected String Y_AXIS_PARAM = "vertAxis";
	static final private String RANDOM_SEED = "randomSeed";
	static final protected String X_VAR_NAME_PARAM = "xVarName";
	static final protected String Y_VAR_NAME_PARAM = "yVarName";
	static final protected String X_VALUES_PARAM = "xValues";
	static final protected String FIT_VAR_AXIS_PARAM = "fitVarAxis";
	static final protected String FIT_SD_AXIS_PARAM = "fitSDAxis";
	
	static final protected int SAMPLE = 0;
	static final protected int CUMULATIVE = 1;
	
	static final private Color kDarkGreen = new Color(0x009900);
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	private LeverageView leverageView;
	
	private RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck;
	private XChoice sdVarChoice;
	private int currentSdVarChoice = 0;
	
	private VertAxis fitVarAxis, fitSDAxis;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		setLayout(new ProportionLayout(0.5, 5, ProportionLayout.VERTICAL));
		
		add(ProportionLayout.TOP, lsLinesPanel(data, summaryData));
		add(ProportionLayout.BOTTOM, bottomPanel(data, summaryData));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
		
		String seedString = getParameter(RANDOM_SEED);
		NumVariable xVar = (NumVariable)data.getVariable("x");
		int count = xVar.noOfValues();
		String randomParams = String.valueOf(count) + " 0.0 1.0 " + seedString + " 3.0";
		RandomNormal generator = new RandomNormal(randomParams);
		NumSampleVariable error = new NumSampleVariable("error", generator, 10);
		error.setSampleSize(count);
		data.addVariable("error", error);
		
		LinearModel yDistn = new LinearModel(getParameter(Y_VAR_NAME_PARAM), data, "x");
		yDistn.setParameters(getParameter(REGN_MODEL_PARAM));
		data.addVariable("model", yDistn);
		
		ResponseVariable yData = new ResponseVariable(getParameter(Y_VAR_NAME_PARAM),
																						data, "x", "error", "model", 10);
		data.addVariable("y", yData);
		
		return data;
	}
	
	private SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "error");
		
		SlopeInterceptVariable slope = new SlopeInterceptVariable("slope", "x", "y",
															9, SlopeInterceptVariable.SLOPE);
		SlopeInterceptVariable intercept = new SlopeInterceptVariable("intercept", "x", "y",
															9, SlopeInterceptVariable.INTERCEPT);
		
		summaryData.addVariable("slope", slope);
		summaryData.addVariable("intercept", intercept);
		
		summaryData.addVariable("leverage", leverageFunction(sourceData));
		
		return summaryData;
	}
	
	private QuadraticModel leverageFunction(DataSet data) {
		NumVariable xVar = (NumVariable)data.getVariable("x");
		ValueEnumeration xe = xVar.values();
		int n = 0;
		double sx = 0.0;
		double sxx = 0.0;
		while (xe.hasMoreValues()) {
			double x = xe.nextDouble();
			n ++;
			sx += x;
			sxx += x * x;
		}
		double xMean = sx / n;
		sxx -= sx * xMean;
		
		LinearModel model = (LinearModel)data.getVariable("model");
		double errorSD = model.evaluateSD().toDouble();
		double errorVar = errorSD * errorSD;
		
		NumValue b0 = new NumValue(errorVar * (1.0 / n + xMean * xMean / sxx), 10);
		NumValue b1 = new NumValue(- 2.0 * errorVar * xMean / sxx, 10);
		NumValue b2 = new NumValue(errorVar / sxx, 10);
		
		return new QuadraticModel("Leverage", data, null, b0, b1, b2, QuadraticModel.kZero);
	}
	
	private XPanel lsLinesPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																							ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.LEFT, bivarDisplayPanel(data, SAMPLE));
		thePanel.add(ProportionLayout.RIGHT, bivarDisplayPanel(summaryData, CUMULATIVE));
		return thePanel;
	}
	
	private XPanel bivarDisplayPanel(DataSet data, int viewType) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		XPanel plotPanel = new XPanel();
		plotPanel.setLayout(new AxisLayout());
		
			HorizAxis xAxis = new HorizAxis(this);
			xAxis.readNumLabels(getParameter(X_AXIS_PARAM));
			xAxis.setAxisName(getParameter(X_VAR_NAME_PARAM));
			plotPanel.add("Bottom", xAxis);
			
			VertAxis yAxis = new VertAxis(this);
			yAxis.readNumLabels(getParameter(Y_AXIS_PARAM));
			plotPanel.add("Left", yAxis);
			
			DataView dataView = (viewType == SAMPLE) ? getSampleView(data, xAxis, yAxis)
																: getSummaryView(data, xAxis, yAxis);
			dataView.lockBackground(Color.white);
			
			plotPanel.add("Center", dataView);
		
		thePanel.add("Center", plotPanel);
			
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
			XLabel yVariateName = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
			yVariateName.setFont(yAxis.getFont());
			topPanel.add(yVariateName);
			
		thePanel.add("North", topPanel);
		
		return thePanel;
	}
	
	private DataView getSampleView(DataSet data, HorizAxis xAxis, VertAxis yAxis) {
		SampleLineView theView = new SampleLineView(data, this, xAxis, yAxis, "x", "y", "model");
		theView.setShowData(true);
		return theView;
	}
	
	private DataView getSummaryView(DataSet data, HorizAxis xAxis, VertAxis yAxis) {
		return new LSLinesView((SummaryDataSet)data, this, xAxis, yAxis, "intercept", "slope");
	}
	
	private XPanel bottomPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(40, 0));
		thePanel.add("West", samplingControlPanel(summaryData));
		thePanel.add("Center", leverageDisplayPanel(summaryData));
		return thePanel;
	}
	
	private XPanel samplingControlPanel(DataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																											VerticalLayout.VERT_SPACED, 0));
																											
			XPanel samplePanel = new XPanel();
			samplePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																											VerticalLayout.VERT_CENTER, 5));
		
				takeSampleButton = new RepeatingButton(translate("Take sample"), this);
			samplePanel.add(takeSampleButton);
			
				accumulateCheck = new XCheckbox(translate("Accumulate"), this);
			samplePanel.add(accumulateCheck);
			
				ValueCountView theCount = new ValueCountView(summaryData, this);
				theCount.setLabel(translate("No of samples") + " =");
			samplePanel.add(theCount);
		
		thePanel.add(samplePanel);
		
			XPanel scalePanel = new XPanel();
			scalePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT,
																											VerticalLayout.VERT_CENTER, 2));
				XLabel sdVarLabel = new XLabel(translate("Scale for prediction spread") + ":", XLabel.LEFT, this);
				sdVarLabel.setFont(getStandardBoldFont());
			scalePanel.add(sdVarLabel);
			
				sdVarChoice = new XChoice(this);
				sdVarChoice.addItem(translate("St devn of prediction"));
				sdVarChoice.addItem(translate("Variance of prediction"));
			
			scalePanel.add(sdVarChoice);
			
		thePanel.add(scalePanel);
			
		return thePanel;
	}
	
	private XPanel leverageDisplayPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		XPanel plotPanel = new XPanel();
		plotPanel.setLayout(new AxisLayout());
		
			HorizAxis xAxis = new HorizAxis(this);
			xAxis.readNumLabels(getParameter(X_AXIS_PARAM));
			xAxis.setAxisName(getParameter(X_VAR_NAME_PARAM));
			plotPanel.add("Bottom", xAxis);
			
			fitVarAxis = new VertAxis(this);
			fitVarAxis.readNumLabels(getParameter(FIT_VAR_AXIS_PARAM));
			fitVarAxis.setPower(0.5);
			fitVarAxis.setForeground(kDarkGreen);
			plotPanel.add("Right", fitVarAxis);
			
			fitSDAxis = new VertAxis(this);
			fitSDAxis.readNumLabels(getParameter(FIT_SD_AXIS_PARAM));
			fitSDAxis.setForeground(Color.blue);
			plotPanel.add("Left", fitSDAxis);
			
			leverageView = new LeverageView(summaryData, this, xAxis, fitVarAxis, "leverage");
			leverageView.lockBackground(Color.white);
			
			plotPanel.add("Center", leverageView);
		
		thePanel.add("Center", plotPanel);
			
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(0, 0));
			
				XLabel sdLabel = new XLabel(translate("St devn"), XLabel.LEFT, this);
				sdLabel.setForeground(Color.blue);
			topPanel.add("Center", sdLabel);
				XLabel varLabel = new XLabel(translate("Variance"), XLabel.RIGHT, this);
				varLabel.setForeground(kDarkGreen);
			topPanel.add("East", varLabel);
			
		thePanel.add("North", topPanel);
		
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
		else if (target == sdVarChoice) {
			int newChoice = sdVarChoice.getSelectedIndex();
			if (newChoice != currentSdVarChoice) {
				currentSdVarChoice = newChoice;
				fitVarAxis.setPower((newChoice == 0) ? 0.5 : 1.0);
				fitVarAxis.repaint();
				fitSDAxis.setPower((newChoice == 0) ? 1.0 : 0.5);
				fitSDAxis.repaint();
				leverageView.repaint();
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