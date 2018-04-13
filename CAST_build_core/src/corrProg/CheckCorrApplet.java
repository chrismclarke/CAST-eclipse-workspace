package corrProg;

import java.awt.*;

import dataView.*;
import utils.*;
import qnUtils.*;
import axis.*;
import random.*;
import coreGraphics.*;


public class CheckCorrApplet extends MultiCheckApplet {
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final protected String ACCURACY_PARAM = "accuracy";
	
	private RandomBiNormal dataGenerator;
	
	private AxisChoice horizAxisInfo = new AxisChoice(null, 0, AxisChoice.HORIZONTAL);
	private AxisChoice vertAxisInfo = new AxisChoice(null, 0, AxisChoice.VERTICAL);
	
	private NumVariable xVariable, yVariable;
	
	private RandomRectangular randomCorrGenerator;
	
	private double accuracyParam;			//		1.5 is most accurate that can be expected
														//		2.3 is least accurate;
	
	protected DataSet createData() {
		DataSet data = new DataSet();
		randomCorrGenerator = new RandomRectangular(1, -1.0, 1.0);
		
		dataGenerator = new RandomBiNormal(1, 0.0, 1.0, 0.0, 1.0, 0.0, 2.5);
		
		xVariable = new NumVariable(getParameter(X_VAR_NAME_PARAM));
		data.addVariable("x", xVariable);
		yVariable = new NumVariable(getParameter(Y_VAR_NAME_PARAM));
		data.addVariable("y", yVariable);
		return data;
	}
	
	protected void readAccuracy() {
		accuracyParam = Double.parseDouble(getParameter(ACCURACY_PARAM));
	}
	
	protected String valueLabel() {
		return "r  =";
	}
	
	protected void changeRandomParams(DataSet data) {
		axisGenerator.changeRandomAxis(horizAxisInfo, xVariable, this);
		axisGenerator.changeRandomAxis(vertAxisInfo, yVariable, this);
		
		double newXMean = (horizAxisInfo.axis.minOnAxis + horizAxisInfo.axis.maxOnAxis) * 0.5;
		double newYMean = (vertAxisInfo.axis.minOnAxis + vertAxisInfo.axis.maxOnAxis) * 0.5;
		double newXSD = (horizAxisInfo.axis.maxOnAxis - horizAxisInfo.axis.minOnAxis) * 0.2;
		double newYSD = (vertAxisInfo.axis.maxOnAxis - vertAxisInfo.axis.minOnAxis) * 0.2;
		double tempR = randomCorrGenerator.generateOne();
		double newR = Math.sqrt(Math.abs(tempR));
		if (tempR < 0.0)
			newR = -newR;
		dataGenerator.setParameters(newXMean, newXSD, newYMean, newYSD, newR);
		
		int newSampleSize = sampleSizeGenerator.getNewSampleSize();
		dataGenerator.setSampleSize(newSampleSize);
		
		double vals[][] = dataGenerator.generate();
		synchronized (data) {
			xVariable.setValues(vals[0]);
			yVariable.setValues(vals[1]);
			data.variableChanged("x");
			data.variableChanged("y");
		}
	}
	
	protected NumValue evalAnswer(DataSet data, int correctDecimals) {
		ValueEnumeration xe = xVariable.values();
		ValueEnumeration ye = yVariable.values();
		double sx = 0.0;
		double sy = 0.0;
		double sxx = 0.0;
		double syy = 0.0;
		double sxy = 0.0;
		int nVals = 0;
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			double xVal = xe.nextDouble();
			double yVal = ye.nextDouble();
			sx += xVal;
			sy += yVal;
			sxx += xVal * xVal;
			syy += yVal * yVal;
			sxy += xVal * yVal;
			nVals++;
		}
		double corr = (sxy - sx * sy / nVals)
										/ Math.sqrt((sxx - sx * sx / nVals) * (syy - sy * sy / nVals));
		return new NumValue(corr, correctDecimals);
	}
	
	protected double evalExactSlop(NumValue answer, DataSet data) {
		return 0.0005;
	}
	
	protected double evalApproxSlop(NumValue answer, DataSet data) {
		return 0.1 * (2.0 + accuracyParam) * (1.0 - Math.pow(Math.abs(answer.toDouble()), accuracyParam));
//		return 0.5 * (1.0 - Math.abs(answer.toDouble()));
	}
	
	protected String[] answerStrings(NumValue answer) {
		String[] answerString = new String[5];
		answerString[LinkedAnswerEditPanel.NONE] = "Guess the value of the correlation coefficient.";
		answerString[LinkedAnswerEditPanel.EXACT] = "Correct!!";
		answerString[LinkedAnswerEditPanel.UNKNOWN] = "You have not typed a number.";
		answerString[LinkedAnswerEditPanel.WRONG] = "Your answer is not close enough. Look back for examples with different r.";
		answerString[LinkedAnswerEditPanel.CLOSE] = "Close enough!! The exact value is r = "
																					+ answer.toString() + ".";
		return answerString;
	}
	
	protected DataView getDataView(DataSet data, HorizAxis horizAxis, VertAxis vertAxis,
																					String xKey, String yKey) {
		return new ScatterView(data, this, horizAxis, vertAxis, xKey, yKey);
	}
	
	protected XPanel viewPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		XPanel viewPanel = new XPanel();
		viewPanel.setLayout(new AxisLayout());
		
		viewPanel.add("Bottom", horizAxisInfo.axis);
		viewPanel.add("Left", vertAxisInfo.axis);
		
		DataView theView = getDataView(data, (HorizAxis)horizAxisInfo.axis,
																		(VertAxis)vertAxisInfo.axis, "x", "y");
		viewPanel.add("Center", theView);
		theView.lockBackground(Color.white);
		
		thePanel.add("Center", viewPanel);
		thePanel.add("North", yLabelPanel(data));
		return thePanel;
	}
	
	private XPanel yLabelPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		XLabel yVariateName = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
		yVariateName.setFont(vertAxisInfo.axis.getFont());
		thePanel.add(yVariateName);
		return thePanel;
	}
}