package inferenceProg;

import java.awt.*;

import dataView.*;
import qnUtils.*;
import axis.*;
import random.*;
import distn.*;
import coreGraphics.*;
import imageGroups.*;

import sampling.*;


public class CheckCIApplet extends MultiCheckApplet {
	static final protected String ACCURACY_PARAM = "accuracy";
	
	private RandomNormal dataGenerator;
	
	private AxisChoice horizAxisInfo = new AxisChoice(null, 0, AxisChoice.HORIZONTAL);
	private NumVariable yVariable;
	private SummaryView countView, meanView, sdView;
	
	private double accuracyProportion;			//		accuracy < 1.0
	private boolean alwaysShowSD = false;
	
	public void setupApplet() {
		MeanSDImages.loadMeanSD(this);
		super.setupApplet();
	}
	
	protected DataSet createData() {
		DataSet data = new DataSet();
		dataGenerator = new RandomNormal(1, 0.0, 1.0, 2.5);
		
		yVariable = new NumVariable(getParameter(VAR_NAME_PARAM));
		data.addVariable("y", yVariable);
		return data;
	}
	
	protected void readAccuracy() {
		accuracyProportion = Double.parseDouble(getParameter(ACCURACY_PARAM));
	}
	
	protected String valueLabel() {
		double theMean = SummaryView.evaluateStatistic((Variable)yVariable, null, null, SummaryView.SAMPLE, SummaryView.MEAN);
		String meanString = new NumValue(theMean, correctDecimals).toString();
		return "95% CI is " + meanString + " \u00b1 ";
	}
	
	protected void changeRandomParams(DataSet data) {
		axisGenerator.changeRandomAxis(horizAxisInfo, yVariable, this);
		
		double newMean = (horizAxisInfo.axis.minOnAxis + horizAxisInfo.axis.maxOnAxis) * 0.5;
		double newSD = (horizAxisInfo.axis.maxOnAxis - horizAxisInfo.axis.minOnAxis) * 0.2;
		dataGenerator.setParameters(newMean, newSD);
		
		int newSampleSize = sampleSizeGenerator.getNewSampleSize();
		dataGenerator.setSampleSize(newSampleSize);
		
		double vals[] = dataGenerator.generate();
		synchronized (data) {
			yVariable.setValues(vals);
			data.variableChanged("y");
		}
	}
	
	protected NumValue evalAnswer(DataSet data, int correctDecimals) {
		ValueEnumeration ye = yVariable.values();
		double sy = 0.0;
		double syy = 0.0;
		int nVals = 0;
		while (ye.hasMoreValues()) {
			double yVal = ye.nextDouble();
			sy += yVal;
			syy += yVal * yVal;
			nVals++;
		}
		double sd = Math.sqrt((syy - sy * sy / nVals) / (nVals - 1) / (double)nVals);
		return new NumValue(TTable.quantile(0.975, nVals - 1) * sd, correctDecimals);
	}
	
	protected double evalExactSlop(NumValue answer, DataSet data) {
		return 0.0005 * answer.toDouble();
	}
	
	protected double evalApproxSlop(NumValue answer, DataSet data) {
		return (1.0 - accuracyProportion) * answer.toDouble();
	}
	
	protected String[] answerStrings(NumValue answer) {
		String[] answerString = new String[5];
		answerString[LinkedAnswerEditPanel.NONE] = "Guess an approximate 95% confidence interval for the population mean.";
		answerString[LinkedAnswerEditPanel.EXACT] = "Correct!!";
		answerString[LinkedAnswerEditPanel.UNKNOWN] = "You have not typed a number.";
		answerString[LinkedAnswerEditPanel.WRONG] = "Your answer is not close enough. Find the width of an interval including about 95% of the data and divide by root(n). Then divide by 2.";
		answerString[LinkedAnswerEditPanel.CLOSE] = "Close enough!! The exact value is plus or minus "
																					+ answer.toString() + ".";
		return answerString;
	}
	
	protected XPanel viewPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("Center", dotPlotPanel(data));
		thePanel.add("South", summaryPanel(data));
		
		return thePanel;
	}
	
	protected XPanel dotPlotPanel(DataSet data) {
		XPanel viewPanel = new XPanel();
		viewPanel.setLayout(new AxisLayout());
		
		viewPanel.add("Bottom", horizAxisInfo.axis);
		
		DataView theView = new StackedDotPlotView(data, this, horizAxisInfo.axis);
		viewPanel.add("Center", theView);
		theView.lockBackground(Color.white);
		
		return viewPanel;
	}
	
	protected XPanel summaryPanel(DataSet data) {
		XPanel statisticPanel = new XPanel();
		statisticPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		
		countView = new SummaryView(data, this, "y", null, SummaryView.COUNT, correctDecimals, SummaryView.SAMPLE);
		statisticPanel.add(countView);
		
		meanView = new SummaryView(data, this, "y", null, SummaryView.MEAN, correctDecimals, SummaryView.SAMPLE);
		meanView.setForeground(Color.blue);
		statisticPanel.add(meanView);
		
		sdView = new SummaryView(data, this, "y", null, SummaryView.SD, correctDecimals, SummaryView.SAMPLE);
		sdView.setForeground(Color.red);
		if (!alwaysShowSD)
			sdView.show(false);
		statisticPanel.add(sdView);
		
		return statisticPanel;
	}
	
	protected void changeForNewAnswerType(int newAnswerType) {
		if (alwaysShowSD)
			return;
		if (newAnswerType == LinkedAnswerEditPanel.NONE || newAnswerType == LinkedAnswerEditPanel.UNKNOWN)
			sdView.show(false);
		else
			sdView.show(true);
	}
	
	protected void setAlwaysShowSD() {
		alwaysShowSD = true;
	}
}