package exerciseEstimProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;
import exercise2.*;
import formula.*;


import cat.*;
import exerciseEstim.*;


public class FindPropnSeApplet extends ExerciseApplet {
	static final private NumValue kOneValue = new NumValue(1, 0);
	static final private NumValue kHalfValue = new NumValue(0.5, 1);
	
	static final private NumValue kInitMaxPropn = new NumValue(0.9999, 4);
	
	static final private double kDummyProbArray[] = {1.0, 0.0};
	
	private RandomMultinomial generator;
	
	protected NumValue maxPropn;
	
	private XLabel varName;
	private FreqTableView tableView;
	protected PropnSeTemplatePanel seTemplate;
	private XNumberEditPanel estimateView;
	
	private ResultValuePanel resultPanel;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new BorderLayout(0, 0));
			
			mainPanel.add("Center", getWorkingPanels(data));
			mainPanel.add("South", getAnswerPanel());
		
		add("Center", mainPanel);
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
			bottomPanel.add(createMarkingPanel(NO_HINTS));
			
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add(messagePanel);
		
		add("South", bottomPanel);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		registerParameter("varName", "string");
		registerParameter("catLabels", "array");
		registerParameter("noOfValues", "int");
		registerParameter("probs", "string");
		registerParameter("targetCat", "int");
		registerParameter("maxPropn", "const");
	}
	
	private String getVarName() {
		return getStringParam("varName");
	}
	
	private StringArray getCatLabels() {
		return getArrayParam("catLabels");
	}
	
	private int getNoOfValues() {
		return getIntParam("noOfValues");
	}
	
	private double[] getProbs() {
		StringTokenizer st = new StringTokenizer(getStringParam("probs"));
		double[] p = new double[st.countTokens()];
		for (int i=0 ; i<p.length ; i++)
			p[i] = Double.parseDouble(st.nextToken());
		return p;
	}
	
	private int getTargetCat() {
		return getIntParam("targetCat");
	}
	
	private NumValue getMaxPropn() {
		return getNumValueParam("maxPropn");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getAnswerPanel() {
		XPanel ansPanel = new InsetPanel(10, 12, 10, 2);
		ansPanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.HORIZONTAL,
																														ProportionLayout.TOTAL));
			
			estimateView = new XNumberEditPanel("Estimate =", "", 4, this);
			estimateView.setFont(getBigFont());
			estimateView.disable();
				
		ansPanel.add(ProportionLayout.LEFT, estimateView);
				
			resultPanel = new ResultValuePanel(this, translate("Standard error") + " =", "", 6);
			resultPanel.setFont(getBigFont());
			registerStatusItem("se", resultPanel);
			
		ansPanel.add(ProportionLayout.RIGHT, resultPanel);
		return ansPanel;
	}
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 15));
		
		thePanel.add("Center", new PieView(data, this, "y", CatDataView.NO_DRAG));
		thePanel.add("East", freqTablePanel(data));
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
				XPanel innerPanel = new InsetPanel(20, 2);
				innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
					seTemplate = new PropnSeTemplatePanel(kInitMaxPropn, getTemplateType(), stdContext);
					registerStatusItem("seTemplate", seTemplate);
				
				innerPanel.add(seTemplate);
				
				if (!fullWidthTemplateHighlight())
					innerPanel.lockBackground(kWorkingBackground);
			bottomPanel.add(innerPanel);
			
			if (fullWidthTemplateHighlight())
				bottomPanel.lockBackground(kWorkingBackground);
		thePanel.add("South", bottomPanel);
		
		return thePanel;
	}
	
	protected boolean fullWidthTemplateHighlight() {
		return false;
	}
	
	protected int getTemplateType() {
		return PropnSeTemplatePanel.SE_ONLY;
	}
	
	private XPanel freqTablePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
			varName = new XLabel("", XLabel.CENTER, this);
			varName.setFont(getStandardBoldFont());
		thePanel.add(varName);
		
			tableView = new FreqTableView(data, this, "y", CatDataView.NO_DRAG,
											kInitMaxPropn.decimals, FreqTableView.LONG_HEADINGS, FreqTableView.PROPN,
											translate("Frequency"), false);
			
		thePanel.add(tableView);
		return thePanel;
	}
	
	protected void clearResult() {
		resultPanel.clear();
	}
	
	protected void setDisplayForQuestion() {
		maxPropn = getMaxPropn();
		
		CatVariable v = (CatVariable)data.getVariable("y");
		varName.setText(v.name);
		varName.invalidate();
		
		tableView.setDecimals(maxPropn.decimals);
		tableView.invalidate();
		
		seTemplate.setValues(kHalfValue, kHalfValue, kOneValue);
		seTemplate.changeMaxValue(maxPropn);
		
		if (estimateView != null)
			estimateView.setDoubleValue(new NumValue(evaluateProb(), maxPropn.decimals));
		
		data.variableChanged("y");
		
		clearResult();
	}
	
	protected void setDataForQuestion() {
		int n = getNoOfValues();
		
		generator.setProbs(getProbs());
		
		CatSampleVariable yVar = (CatSampleVariable)data.getVariable("y");
		StringArray catLabels = getCatLabels();
		int nLabels = catLabels.getNoOfStrings();
		LabelValue labels[] = new LabelValue[nLabels];
		for (int i=0 ; i<nLabels ; i++)
			labels[i] = new LabelValue(catLabels.getValue(i));
			
		yVar.setLabels(labels);
		yVar.setSampleSize(n);
		yVar.name = getVarName();
		for (int i=0 ; i<10 ; i++) {
			yVar.generateNextSample();			//	try a max of 10 times to get sample for which normal approx is OK
			if (normalApproxOK(yVar))
				break;
		}
	}
	
	private boolean normalApproxOK(CatVariable yVar) {
		int count[] = yVar.getCounts();
		int targetCat = getTargetCat();
		int n = getNoOfValues();
		return count[targetCat] >= 5 && (n - count[targetCat]) >= 5;
		
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Use the template to help find the standard error of the sample proportion then type it into the box above.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertRedText("You must type a value for the standard error in the box above.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText("Standard errors can never be negative.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The standard error of the sample proportion is given by the formula,\n");
				insertSeFormula(messagePanel);
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly found the standard error of the sample proportion,\n");
				insertSeFormula(messagePanel);
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Close!");
				analyseMistake(messagePanel);
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!");
				analyseMistake(messagePanel);
				break;
		}
	}
	
	protected void analyseMistake(MessagePanel messagePanel) {
		String templateResultString = seTemplate.getResult().toString();
		String correctString = new NumValue(evaluateSe(), maxPropn.decimals).toString();
		
		if (templateResultString.equals(correctString))
			messagePanel.insertRedText("\nYou seem to have correctly evaluated the standard error in the template but have not copied it into the result box.");
		else
			analyseSeTemplate(messagePanel);
	}
	
	protected void analyseSeTemplate(MessagePanel messagePanel) {
		int n = evaluateN();
		double templateN = seTemplate.getN().toDouble();
		if (n != templateN)
			messagePanel.insertRedText("\nThe denominator in the standard error template should be the sample size, n = " + n + ".");
		double templateP = seTemplate.getP().toDouble();
		double templateQ = seTemplate.getQ().toDouble();
		double slop = 0.5 * Math.pow(0.1, maxPropn.decimals);
		double p = evaluateProb();
		FormulaContext context = new FormulaContext(Color.red, null, this);
		messagePanel.insertRedText("\nThe numerator under the square root in the template should be ");
		messagePanel.insertFormula(pqFormula(context));
		if (Math.abs(p - templateP) > slop && Math.abs(p - templateQ) > slop)
			messagePanel.insertRedText(". You do not seem to have used the correct proportion in it, p = " + new NumValue(p, maxPropn.decimals) + ".");
		else if (Math.abs(templateP + templateQ - 1) > slop)
			messagePanel.insertRedText(" and should multiply p = " + new NumValue(p, maxPropn.decimals) + " with one minus it, " + new NumValue(1 - p, maxPropn.decimals) + ".");
		else
			messagePanel.insertRedText(" and you are close to this.");
	}
	
	private MFormula pqFormula(FormulaContext context) {
		MFormula q = new MBracket(new MBinary(MBinary.MINUS, new MText("1", context), new MText("p", context), context), context);
		return new MBinary(MBinary.TIMES, new MText("p", context), q, context);
	}
	
	protected MFormula seFormula(FormulaContext context) {
		MFormula pq = pqFormula(context);
		MFormula var = new MRatio(pq, new MText("n", context), context);
		return new MRoot(var, context);
	}
	
	protected void insertSeFormula(MessagePanel messagePanel) {
		FormulaContext context = new FormulaContext(null, null, this);
		MFormula se = seFormula(context);
		
		messagePanel.setAlignment(MessagePanel.CENTER_ALIGN);
		messagePanel.insertFormula(se);
	}
	
	protected int getMessageHeight() {
		return 150;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
			
			generator = new RandomMultinomial(10, kDummyProbArray);
			generator.setSeed(nextSeed());
			CatSampleVariable yVar = new CatSampleVariable("Y", generator, Variable.USES_REPEATS);
			yVar.readLabels("A B");
			yVar.generateNextSample();
		data.addVariable("y", yVar);
		
		return data;
	}
	
	private double getAttempt() {
		return resultPanel.getAttempt().toDouble();
	}
	
	protected double evaluateProb() {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		int count[] = yVar.getCounts();
		int targetCat = getTargetCat();
		int n = getNoOfValues();
		return count[targetCat] / (double)n;
	}
	
	protected int evaluateN() {
		return getNoOfValues();
	}
	
	protected double evaluateSe() {
		double p = evaluateProb();
		int n = evaluateN();
		return Math.sqrt(p * (1 - p) / n);
	}
	
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		double attempt = getAttempt();
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else if (attempt < 0.0)
			return ANS_INVALID;
			
		double correct = evaluateSe();
		double absError = Math.abs(attempt - correct);
		
		double exactSlop = 0.5 * Math.pow(0.1, maxPropn.decimals);
		double approxSlop = exactSlop * 5;
		
		return (absError < exactSlop) ? ANS_CORRECT
						: (absError < approxSlop) ? ANS_CLOSE : ANS_WRONG;
	}
	
	protected void giveFeedback() {
	}
	
	protected void showResult() {
		resultPanel.showAnswer(new NumValue(evaluateSe(), maxPropn.decimals));
	}
	
	protected void showCorrectWorking() {
		NumValue pVal = new NumValue(evaluateProb(), maxPropn.decimals);
		NumValue qVal = new NumValue(1 - pVal.toDouble(), maxPropn.decimals);
		NumValue nVal = new NumValue(evaluateN(), 0);
		
		seTemplate.setValues(pVal, qVal, nVal);
		
		showResult();
	}
	
	protected double getMark() {
		int result = assessAnswer();
		
		return (result == ANS_CORRECT) ? 1.0 : (result == ANS_CLOSE) ? 0.5 : 0;
	}
}