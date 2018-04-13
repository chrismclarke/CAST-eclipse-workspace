package linModProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.*;
import random.RandomNormal;
import models.*;

import regn.*;
import linMod.*;


public class SampleLSApplet extends XApplet {
	static final private String REGN_MODEL_PARAM = "regnModel";
	static final protected String X_AXIS_PARAM = "horizAxis";
	static final protected String Y_AXIS_PARAM = "vertAxis";
	static final private String RANDOM_SEED = "randomSeed";
	static final private String RANDOM_X_PARAM = "randomX";
	static final protected String MAX_PARAM = "maxParams";
	static final protected String X_VAR_NAME_PARAM = "xVarName";
	static final protected String Y_VAR_NAME_PARAM = "yVarName";
	static final protected String X_VALUES_PARAM = "xValues";
	static final private String X_DISTN_PARAM = "xDistn";
	
	static final protected int SAMPLE = 0;
	static final protected int CUMULATIVE = 1;
	
	static final protected int MODEL_EQN = 0;
	static final protected int LS_EQN = 1;
	
	protected NumValue intMax, slopeMax;
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
//	private SampleLineView dataView;
//	private LSLinesView summaryView;
	
	private RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck;
	
	protected ValueCountView sampleCount;		//		we need to keep variable so that
															//		SampleSlope2Applet can change background
	
	private RandomNormal xGenerator;
	
	public void setupApplet() {
		StringTokenizer paramLimits = new StringTokenizer(getParameter(MAX_PARAM));
		intMax = new NumValue(paramLimits.nextToken());
		slopeMax = new NumValue(paramLimits.nextToken());
		
		data = getData();
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		setLayout(new BorderLayout());
		
			XPanel viewPanel = new XPanel();
			viewPanel.setLayout(new BorderLayout());
			viewPanel.add("Center", displayPanel(data, summaryData));
			viewPanel.add("South", equationPanel(data));
		
		add("Center", viewPanel);
		
		
		add("South", samplingControlPanel(summaryData, 15, 0));
	}
	
	protected XPanel displayPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																							ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.LEFT, bivarDisplayPanel(data, SAMPLE));
		thePanel.add(ProportionLayout.RIGHT, bivarDisplayPanel(summaryData, CUMULATIVE));
		return thePanel;
	}
	
	protected XPanel equationPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																							ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.LEFT, singleEquationPanel(data, MODEL_EQN));
		thePanel.add(ProportionLayout.RIGHT, singleEquationPanel(data, LS_EQN));
		return thePanel;
	}
	
	protected int getValueCount(DataSet data) {
		NumVariable xVar = (NumVariable)data.getVariable("x");
		return xVar.noOfValues();
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		String xValueString = getParameter(X_VALUES_PARAM);
		String xNormalString = getParameter(RANDOM_X_PARAM);
		NumVariable xVar;
		if (xValueString == null && xNormalString == null)
			xVar = new NumClusterVariable(getParameter(X_VAR_NAME_PARAM),
																getParameter(X_DISTN_PARAM), data, "error");
		else {
			xVar = new NumVariable(getParameter(X_VAR_NAME_PARAM));
			if (xValueString == null) {
				xGenerator = new RandomNormal(xNormalString);
				double vals[] = xGenerator.generate();
				xVar.setValues(vals);
			}
			else
				xVar.readValues(xValueString);
		}
		data.addVariable("x", xVar);
		
		String seedString = getParameter(RANDOM_SEED);
		int count = getValueCount(data);
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
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "error");
		
		SlopeInterceptVariable slope = new SlopeInterceptVariable(translate("Slope"), "x", "y",
															slopeMax.decimals, SlopeInterceptVariable.SLOPE);
		SlopeInterceptVariable intercept = new SlopeInterceptVariable(translate("Intercept"), "x", "y",
															intMax.decimals, SlopeInterceptVariable.INTERCEPT);
		
		summaryData.addVariable("slope", slope);
		summaryData.addVariable("intercept", intercept);
		
		return summaryData;
	}
	
	protected XPanel samplingControlPanel(DataSet summaryData, int topInset, int bottomInset) {
		XPanel thePanel = new InsetPanel(0, topInset, 0, bottomInset);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
		takeSampleButton = new RepeatingButton(translate("Take sample"), this);
		thePanel.add(takeSampleButton);
		
		accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		thePanel.add(accumulateCheck);
		
		sampleCount = new ValueCountView(summaryData, this);
		sampleCount.setLabel(translate("No of samples") + " =");
		thePanel.add(sampleCount);
		
		return thePanel;
	}
	
	protected XPanel singleEquationPanel(DataSet data, int eqnType) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
		
		if (eqnType == LS_EQN) {
			LSEquationView lsLineEquation = new LSEquationView(data, this, "y", "x", intMax, slopeMax);
			lsLineEquation.setShowData(true);
			lsLineEquation.setForeground(Color.blue);
			thePanel.add(lsLineEquation);
			
		}
		else
			thePanel.add(new LinearEquationView(data, this, "model", null, null, intMax, intMax, slopeMax, slopeMax));
		return thePanel;
	}
	
	protected XPanel yNamePanel(DataSet data, VertAxis yAxis) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		XLabel yVariateName = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
		yVariateName.setFont(yAxis.getFont());
		thePanel.add(yVariateName);
		
		return thePanel;
	}
	
	protected XPanel bivarDisplayPanel(DataSet data, int viewType) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		XPanel plotPanel = new XPanel();
		plotPanel.setLayout(new AxisLayout());
		
			HorizAxis xAxis = new HorizAxis(this);
			xAxis.readNumLabels(getParameter(X_AXIS_PARAM));
			Variable x = (Variable)this.data.getVariable("x");		//		the parameter 'data' may be summaryData
			xAxis.setAxisName(x.name);
			plotPanel.add("Bottom", xAxis);
			
			VertAxis yAxis = new VertAxis(this);
			yAxis.readNumLabels(getParameter(Y_AXIS_PARAM));
			plotPanel.add("Left", yAxis);
			
			DataView dataView = (viewType == SAMPLE) ? getSampleView(data, xAxis, yAxis)
																: getSummaryView(data, xAxis, yAxis);
			dataView.lockBackground(Color.white);
			
			plotPanel.add("Center", dataView);
		
		thePanel.add("Center", plotPanel);
		thePanel.add("North", yNamePanel(data, yAxis));
		
		return thePanel;
	}
	
	protected DataView getSampleView(DataSet data, HorizAxis xAxis, VertAxis yAxis) {
		SampleLineView theView = new SampleLineView(data, this, xAxis, yAxis, "x", "y", "model");
		theView.setShowData(true);
		return theView;
	}
	
	protected DataView getSummaryView(DataSet data, HorizAxis xAxis, VertAxis yAxis) {
		return new LSLinesView((SummaryDataSet)data, this, xAxis, yAxis, "intercept", "slope");
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
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}