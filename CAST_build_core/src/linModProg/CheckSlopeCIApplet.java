package linModProg;

import java.awt.*;

import dataView.*;
import utils.*;
import qnUtils.*;
import axis.*;
import distn.*;
import formula.*;

import inference.*;
import linMod.*;
import corrProg.*;



public class CheckSlopeCIApplet extends CheckCorrApplet {
	static final private String MAX_SLOPE_PARAM = "maxSlope";
	
	static final private NumValue kMaxCount = new NumValue(9999, 0);
	
	static final private Color kGreenColor = new Color(0x006600);
	
	private NumValue maxSlope;
	private double accuracyProportion;			//		accuracy < 1.0
	
	protected DataSet createData() {
		DataSet data = super.createData();
		
		maxSlope = new NumValue(getParameter(MAX_SLOPE_PARAM));
		SlopeDistnVariable slopeDistn = new SlopeDistnVariable("slopeDistn", data, "x", "y",
																									maxSlope.decimals);
		data.addVariable("slopeDistn", slopeDistn);
		return data;
	}
	
	protected void readAccuracy() {
		accuracyProportion = Double.parseDouble(getParameter(ACCURACY_PARAM));
	}
	
	protected String valueLabel() {
		SlopeDistnVariable slopeDistn = (SlopeDistnVariable)getData().getVariable("slopeDistn");
		
//		LSEstimate lsCalc = new LSEstimate(getData(), "x", "y");
		NumValue theSlope = slopeDistn.getMean();
		return "95% CI is " + theSlope.toString() + " \u00b1 ";
	}
	
	protected void changeRandomParams(DataSet data) {
		SlopeDistnVariable slopeDistn = (SlopeDistnVariable)data.getVariable("slopeDistn");
		slopeDistn.resetSource();
		
		super.changeRandomParams(data);
	}
	
	protected NumValue evalAnswer(DataSet data, int correctDecimals) {
		SlopeDistnVariable slopeDistn = (SlopeDistnVariable)getData().getVariable("slopeDistn");
		
		NumValue sd = slopeDistn.getSD();
		int nVals = slopeDistn.getN();
		
		return new NumValue(TTable.quantile(0.975, nVals - 2) * sd.toDouble(), correctDecimals);
	}
	
	protected double evalExactSlop(NumValue answer, DataSet data) {
		return 0.0005 * answer.toDouble();
	}
	
	protected double evalApproxSlop(NumValue answer, DataSet data) {
		return (1.0 - accuracyProportion) * answer.toDouble();
	}
	
	protected String[] answerStrings(NumValue answer) {
		String[] answerString = new String[5];
		answerString[LinkedAnswerEditPanel.NONE] = "Find the 95% confidence interval for the slope.";
		answerString[LinkedAnswerEditPanel.EXACT] = "Correct!!";
		answerString[LinkedAnswerEditPanel.UNKNOWN] = "You have not typed a number.";
		answerString[LinkedAnswerEditPanel.WRONG] = "Your answer is not close enough. Find the t-value with (n-2) degrees of freedom. Multiply t by sd(b1).";
		answerString[LinkedAnswerEditPanel.CLOSE] = "Close enough!! The exact value is plus or minus "
																					+ answer.toString() + ".";
		return answerString;
	}
	
	protected DataView getDataView(DataSet data, HorizAxis horizAxis, VertAxis vertAxis,
																					String xKey, String yKey) {
		SampleLineView theView = new SampleLineView(data, this, horizAxis, vertAxis, xKey, yKey, "model");
		theView.setShowData(true);
		theView.setShowModel(false);
		return theView;
	}
	
	protected XPanel viewPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("Center", super.viewPanel(data));
		
		XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new BorderLayout());
			rightPanel.add("North", parameterPanel(data));
			rightPanel.add("Center", workingPanel(data));
		thePanel.add("East", rightPanel);
		return thePanel;
	}
	
	private XPanel parameterPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 3));
		
		thePanel.add(new SlopeDistnView(data, this, "slopeDistn", kMaxCount, SlopeDistnView.COUNT));
		thePanel.add(new SlopeDistnView(data, this, "slopeDistn", maxSlope, SlopeDistnView.SLOPE));
		thePanel.add(new SlopeDistnView(data, this, "slopeDistn", maxSlope, SlopeDistnView.SLOPE_SD));
		return thePanel;
	}
	
	private XPanel workingPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 12));
		
		thePanel.add(new TLookupPanel(this, "Working"));
		
			XPanel plusMinusPanel = new InsetPanel(0, 5);
			plusMinusPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				FormulaContext bigGreenContext = new FormulaContext(kGreenColor, getBigFont(), this);
				PlusMinusCalcPanel plusMinus = new PlusMinusCalcPanel(maxSlope, bigGreenContext);
			plusMinusPanel.add(plusMinus);
		
			plusMinusPanel.lockBackground(kWorkingBackground);
		thePanel.add(plusMinusPanel);
		return thePanel;
	}
}