package exerciseTestProg;

import java.awt.*;

import dataView.*;
import utils.*;
import distn.*;
import exercise2.*;


public class TTestApplet extends NormTestApplet {
	static final private int kDefaultDf = 20;
	
	private XNumberEditPanel dfEdit;
	
	protected double getQuantile(double cumulative) {		//	used for random generation of observed mean
		return TTable.quantile(cumulative, getCorrectDf());
	}
	
//-----------------------------------------------------------
	
	protected int getCorrectDf() {
		return getSampleSize() - 1;
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			TDistnVariable distnVar = new TDistnVariable(getDistnName(), kDefaultDf);	//	default T(20 df)
		data.addVariable("distn", distnVar);
		
		return data;
	}
	
//-----------------------------------------------------------
	
	protected String getTestStatLetter() {
		return  "t";
	}
	
	protected String getSdString() {
		return  "s";
	}
	
	protected String getDistnName() {
		return  "T distribution";
	}
	
	protected String getShortDistnName() {
		return  "t(" + getCorrectDf() + ")";
	}
	
	protected String getTestStatName() {
		return  translate("t-value");
	}
		
//-----------------------------------------------------------
	
	protected XPanel statisticPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		
		thePanel.add("Center", super.statisticPanel());
		
			XPanel dfPanel = new XPanel();
			dfPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				dfEdit = new XNumberEditPanel("df =", String.valueOf(kDefaultDf), 3, this);
				dfEdit.setIntegerType(1, Integer.MAX_VALUE);
				registerStatusItem("df", dfEdit);
				
			dfPanel.add(dfEdit);
		
		thePanel.add("West", dfPanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		dfEdit.setIntegerValue(kDefaultDf);
		
		super.setDisplayForQuestion();
	}
	
	protected void setDataForQuestion() {
		TDistnVariable distnVar = (TDistnVariable)data.getVariable("distn");
		distnVar.setDF(kDefaultDf);
		
		super.setDataForQuestion();
	}
	
//-----------------------------------------------------------	
	
	protected void insertPValueInstructions(MessagePanel messagePanel) {
		messagePanel.insertText("#bullet#  Type the degrees of freedom for the test. Then type an expression for the observed mean's " + getTestStatName());
		messagePanel.insertText(", click ");
		messagePanel.insertBoldText("Calculate");
		messagePanel.insertText(", then use this test statistic to find the p-value for the test. (Use the function sqrt() to find a square root.)\n");
	}
	
	protected void insertWrongPValueMessage(MessagePanel messagePanel) {
		int dfAttempt = dfEdit.getIntValue();
		if (dfAttempt != getCorrectDf()) {
			messagePanel.insertRedHeading("p-value is wrong!\n");
			
			messagePanel.insertText("(You have correctly specified the hypotheses.)\n");
			messagePanel.insertText("You have not specified the correct degrees of freedom for the t statistic -- they should be one less than the sample size.");
		}
		else
			super.insertWrongPValueMessage(messagePanel);
	}
	
	protected void insertPvalueMessage(MessagePanel messagePanel) {
		int df = getCorrectDf();
		messagePanel.insertText("The test has " + df + " degrees of freedom. ");
		
		super.insertPvalueMessage(messagePanel);
	}
	
//-----------------------------------------------------------
	
	
	protected double cumulativeProbability(double mean) {
		NumValue nullMean = getNullMean();
		int df = getCorrectDf();
		int n = getSampleSize();
		NumValue sd = getSd();
		
		return TTable.cumulative((mean - nullMean.toDouble()) * Math.sqrt(n) / sd.toDouble(), df);
	}
	
	protected void showCorrectWorking() {
		int df = getCorrectDf();
		TDistnVariable distnVar = (TDistnVariable)data.getVariable("distn");
		distnVar.setDF(df);
		data.variableChanged("distn");
		
		dfEdit.setIntegerValue(df);
		
		super.showCorrectWorking();
	}
	
//-----------------------------------------------------------
	
	private boolean localAction(Object target) {
		if (target == dfEdit) {
			int dfAttempt = dfEdit.getIntValue();
			TDistnVariable distnVar = (TDistnVariable)data.getVariable("distn");
			if (dfAttempt != distnVar.getDF()) {
				distnVar.setDF(dfAttempt);
				data.variableChanged("distn");
			}
			
			noteChangedWorking();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (localAction(evt.target))
			return true;
		else
			return super.action(evt, what);
	}
}