package linModProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import qnUtils.*;
import random.*;
import formula.*;

import linMod.*;



public class SlopeTestQnApplet extends CoreQuestionApplet {
	static final private String CORR_PARAM = "corr";
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String MAX_SLOPE_PARAM = "maxSlope";
																//		for displaying b1 and its st devn
	
//	static final private Color kGreenColor = new Color(0x006600);
	
	private DataSet data;
	
	private AxisGenerator axisGenerator;
	private SampleSizeGenerator sampleSizeGenerator;
	private ValueGenerator corrGenerator;
	
	private AxisChoice horizAxisInfo = new AxisChoice(null, 0, AxisChoice.HORIZONTAL);
	private AxisChoice vertAxisInfo = new AxisChoice(null, 0, AxisChoice.VERTICAL);
	
	private NumVariable xVariable, yVariable;
	
	private NumValue maxSlope;
	private NumValue tValue;
	
	private RandomBiNormal dataGenerator;
	
	private HypothesisTest test;
	
	public void setupApplet() {
		data = getData();
		
		axisGenerator = new AxisGenerator(this);
		sampleSizeGenerator = new SampleSizeGenerator(this);
		corrGenerator = new ValueGenerator(this, CORR_PARAM);
		
		test = new SlopeHypothesisTest(data, "slopeDistn", new NumValue(0.0),
																		HypothesisTest.HA_NOT_EQUAL, this);
		
		super.setupApplet();
	}
	
	protected boolean testNotProb() {
		return true;
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		dataGenerator = new RandomBiNormal(1, 0.0, 1.0, 0.0, 1.0, 0.0, 2.5);
		
		xVariable = new NumVariable(getParameter(X_VAR_NAME_PARAM));
		data.addVariable("x", xVariable);
		yVariable = new NumVariable(getParameter(Y_VAR_NAME_PARAM));
		data.addVariable("y", yVariable);
		
		maxSlope = new NumValue(getParameter(MAX_SLOPE_PARAM));
		SlopeDistnVariable slopeDistn = new SlopeDistnVariable("slope distn", data,
																					"x", "y", maxSlope.decimals);
		data.addVariable("slopeDistn", slopeDistn);
		
		return data;
	}
	
	protected XPanel dataPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("Center", scatterplotPanel());
		thePanel.add("North", yNamePanel(vertAxisInfo.axis));
		thePanel.add("South", summaryPanel());
		return thePanel;
	}
	
	private XPanel yNamePanel(NumCatAxis yAxis) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		XLabel yVariateName = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
		yVariateName.setFont(yAxis.getFont());
		thePanel.add(yVariateName);
		
		return thePanel;
	}
	
	private XPanel scatterplotPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			axisGenerator.changeRandomAxis(horizAxisInfo, xVariable, this);
			axisGenerator.changeRandomAxis(vertAxisInfo, yVariable, this);
			generateNewData(data);
			
			thePanel.add("Bottom", horizAxisInfo.axis);
			thePanel.add("Left", vertAxisInfo.axis);
			
			SampleLineView theView = new SampleLineView(data, this, (HorizAxis)horizAxisInfo.axis,
																												(VertAxis)vertAxisInfo.axis, "x", "y", null);
			theView.setShowData(true);
			theView.setShowModel(false);
			theView.lockBackground(Color.white);
			
			thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private XPanel summaryPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
			
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
				topPanel.add(new SlopeDistnView(data, this, "slopeDistn", new NumValue(999, 0), SlopeDistnView.COUNT));
				topPanel.add(new SlopeDistnView(data, this, "slopeDistn", maxSlope, SlopeDistnView.SLOPE));
			thePanel.add(topPanel);
			thePanel.add(new SlopeDistnView(data, this, "slopeDistn", maxSlope, SlopeDistnView.SLOPE_SD));
		return thePanel;
	}
	
	private void generateNewData(DataSet data) {
		double newXMean = (horizAxisInfo.axis.minOnAxis + horizAxisInfo.axis.maxOnAxis) * 0.5;
		double newYMean = (vertAxisInfo.axis.minOnAxis + vertAxisInfo.axis.maxOnAxis) * 0.5;
		double newXSD = (horizAxisInfo.axis.maxOnAxis - horizAxisInfo.axis.minOnAxis) * 0.2;
		double newYSD = (vertAxisInfo.axis.maxOnAxis - vertAxisInfo.axis.minOnAxis) * 0.2;
		
		double newR = corrGenerator.getNewValue();
		dataGenerator.setParameters(newXMean, newXSD, newYMean, newYSD, newR);
		
		int newSampleSize = sampleSizeGenerator.getNewSampleSize();
		dataGenerator.setSampleSize(newSampleSize);
		
		SlopeDistnVariable slopeDistn = (SlopeDistnVariable)data.getVariable("slopeDistn");
		slopeDistn.resetSource();
		
		double vals[][] = dataGenerator.generate();
		synchronized (data) {
			xVariable.setValues(vals[0]);
			yVariable.setValues(vals[1]);
			data.variableChanged("x");
			data.variableChanged("y");
		}
	}
	
	protected XPanel workingPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 4));
		
			FormulaContext bigGreenContext = new FormulaContext(Color.green, getBigFont(), this);
		thePanel.add("North", new linMod.TCalcPanel(new NumValue(-99.999, 3), bigGreenContext));
		thePanel.add("Center", new DistnLookupPanel(DistnLookupPanel.T_DISTN, this,
																									getBackground()));
		
		return thePanel;
	}
	
	protected void generateQuestion() {
		axisGenerator.changeRandomAxis(horizAxisInfo, xVariable, this);
		axisGenerator.changeRandomAxis(vertAxisInfo, yVariable, this);
		generateNewData(data);
		
		setupAnswer();
		
		valueEdit.reset(answer, getAccuracy(exactSlop), getAccuracy(approxSlop));
		message.changeText(getAnswerStrings());
		
		double ans = answer.toDouble();
		int conclusionOption = (ans > 0.1) ? 0
									: (ans > 0.05) ? 1
									: (ans > 0.01) ? 2 : 3;
		conclusionPanel.changeCorrectOption(conclusionOption);
		conclusionPanel.reset();
	}
	
	protected void setupAnswer() {
		tValue = new NumValue(test.evaluateStatistic(), 3);
		answer = new NumValue(test.evaluatePValue(), 4);
	}
	
	protected String[] getAnswerStrings() {
		int n = ((NumVariable)data.getVariable("x")).noOfValues();
		
		String solnString = "t = b1 / sd(b1) = " + tValue.toString()
										+ ", with " + String.valueOf(n-2)
										+ " degrees of freedom, and the exact tail area is "
										+ answer.toString() + ".";
		String adviceString = "The p-value is the sum of the two tail areas further from zero than t.";
		
		String accuracyString = (new NumValue(approxSlop * 100, 0)).toString();
		
		String answerString[] = new String[5];
		
		answerString[LinkedAnswerEditPanel.NONE] = "Find the p-value to within " + accuracyString
										+ "% of the correct value. Then select your conclusion.";
		answerString[LinkedAnswerEditPanel.EXACT] = "P-value is correct!  " + solnString;
		answerString[LinkedAnswerEditPanel.WRONG] = "P-value is wrong.  Find t = b1 / sd(b1). " + adviceString;
		answerString[LinkedAnswerEditPanel.UNKNOWN] = "Error! You have not typed a valid number.";
		answerString[LinkedAnswerEditPanel.CLOSE] = "P-value is close enough!  " + solnString;
		return answerString;
	}
	
	protected XPanel bottomPanel() {
		XPanel thePanel = new InsetPanel(0, 10, 0, 0);
		thePanel.setLayout(new BorderLayout(0, 10));
				
			XPanel answerPanel = new XPanel();
			answerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				XPanel innerPanel = new InsetPanel(10, 5);
				innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
					
					XPanel ans = answerEditPanel(HORIZ_ANSWER);
				innerPanel.add(ans);
				
				innerPanel.lockBackground(kAnswerBackground);
			answerPanel.add(innerPanel);
			
		thePanel.add("North", answerPanel);
		thePanel.add("Center", super.bottomPanel());
		
		return thePanel;
	}
}