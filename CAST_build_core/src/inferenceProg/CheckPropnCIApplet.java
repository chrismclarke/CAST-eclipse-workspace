package inferenceProg;

import java.awt.*;

import dataView.*;
import utils.*;
import qnUtils.*;
import random.*;
import formula.*;

import sampling.*;
import cat.*;
import inference.*;



public class CheckPropnCIApplet extends MultiCheckApplet {
	static final protected String ACCURACY_PARAM = "accuracy";
	
	static final private Color kGreenColor = new Color(0x006600);
	
	private RandomBinomial dataGenerator;
	private RandomRectangular propnGenerator;
	
//	private AxisChoice horizAxisInfo = new AxisChoice(null, 0, AxisChoice.HORIZONTAL);
	private CatVariable yVariable;
	
	private double accuracyProportion;			//		accuracy < 1.0
	
	
	protected void readAxisInfo() {
	}
	
	protected DataSet createData() {
		DataSet data = new DataSet();
		propnGenerator = new RandomRectangular(1, 0.1, 0.9);
		dataGenerator = new RandomBinomial(1, 1, 0.5);
		
		yVariable = new CatVariable(getParameter(CAT_NAME_PARAM), Variable.USES_REPEATS);
		yVariable.readLabels(getParameter(CAT_LABELS_PARAM));
		data.addVariable("y", yVariable);
		changeRandomParams(data);
		return data;
	}
	
	protected void readAccuracy() {
		accuracyProportion = Double.parseDouble(getParameter(ACCURACY_PARAM));
	}
	
	protected String valueLabel() {
		double thePropn = SummaryView.evaluateStatistic((Variable)yVariable, null, null, SummaryView.SAMPLE, SummaryView.PROPN);
		String propnString = new NumValue(thePropn, correctDecimals).toString();
		return "95% CI is " + propnString + " \u00b1 ";
	}
	
	protected void changeRandomParams(DataSet data) {
		double newPropn = propnGenerator.generateOne();
		int newSampleSize = sampleSizeGenerator.getNewSampleSize();
		dataGenerator.setParameters(newSampleSize, newPropn);
		
		int vals[] = new int[2];
		vals[0] = dataGenerator.generateOne();
		vals[1] = newSampleSize - vals[0];
		synchronized (data) {
			yVariable.setCounts(vals);
			data.variableChanged("y");
		}
	}
	
	protected NumValue evalAnswer(DataSet data, int correctDecimals) {
		int counts[] = yVariable.getCounts();
		int nVals = yVariable.noOfValues();
		double p = counts[0] / (double)nVals;
		double sd = Math.sqrt(p * (1.0 - p) / (double)nVals);
		return new NumValue(2.0 * sd, correctDecimals);
	}
	
	protected double evalExactSlop(NumValue answer, DataSet data) {
		return 0.005 * answer.toDouble();
	}
	
	protected double evalApproxSlop(NumValue answer, DataSet data) {
		return (1.0 - accuracyProportion) * answer.toDouble();
	}
	
	protected String[] answerStrings(NumValue answer) {
		String[] answerString = new String[5];
		answerString[LinkedAnswerEditPanel.NONE] = "Find the 95% confidence interval for the proportion of successes.";
		answerString[LinkedAnswerEditPanel.EXACT] = "Correct!!";
		answerString[LinkedAnswerEditPanel.UNKNOWN] = "You have not typed a number.";
		answerString[LinkedAnswerEditPanel.WRONG] = "Your answer is not close enough. The answer is twice root(p * (1-p) / n).";
		answerString[LinkedAnswerEditPanel.CLOSE] = "Close enough!! The exact value is plus or minus "
																					+ answer.toString() + ".";
		return answerString;
	}
	
	protected XPanel viewPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("West", dataTablePanel(data, "y"));
		thePanel.add("Center", dataPieView(data, "y"));
		
		XPanel workingPanel = new XPanel();
		workingPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 2));
			
			FormulaContext bigGreenContext = new FormulaContext(kGreenColor, getBigFont(), this);
		workingPanel.add(new CIPropnCalcPanel(bigGreenContext));
		
		thePanel.add("South", workingPanel);
		
		return thePanel;
	}
	
	private DataView dataPieView(DataSet data, String variableKey) {
		return new PieView(data, this, variableKey, CatDataView.SELECT_ONE);
	
	}
	
	private XPanel dataTablePanel(DataSet data, String variableKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
		CatVariable v = (CatVariable)data.getVariable(variableKey);
		XLabel varName = new XLabel(v.name, XLabel.CENTER, this);
		varName.setFont(getStandardBoldFont());
		thePanel.add(varName);
		
		FreqTableView tableView = new FreqTableView(data, this, variableKey, CatDataView.SELECT_ONE, correctDecimals);
		tableView.lockBackground(Color.white);
		
		thePanel.add(tableView);
		
		return thePanel;
	}
	
	protected void changeForNewAnswerType(int newAnswerType) {
	}
}