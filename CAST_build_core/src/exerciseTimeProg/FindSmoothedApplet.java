package exerciseTimeProg;

import java.awt.*;

import dataView.*;
import utils.*;
import expression.*;
import exercise2.*;
import valueList.*;
import coreVariables.*;

import time.*;
import exerciseTime.*;


public class FindSmoothedApplet extends ExerciseApplet {
	private SmoothScrollList valueList;
	
	private ExpressionResultPanel result1Panel, result2Panel;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(data));
				
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
		registerParameter("values", "string");
		registerParameter("firstYear", "int");
		registerParameter("maMissingIndex", "int");
		registerParameter("maRunLength", "int");
		registerParameter("esMissingIndex", "int");
		registerParameter("esConst", "const");
		registerParameter("extraDecimals", "int");
	}
	
	private String getVarName() {
		return getStringParam("varName");
	}
	
	private String getValues() {
		return getStringParam("values");
	}
	
	private int getFirstYear() {
		return getIntParam("firstYear");
	}
	
	private int getMaMissingIndex() {
		return getIntParam("maMissingIndex");
	}
	
	private int getMaRunLength() {
		return getIntParam("maRunLength");
	}
	
	private int getEsMissingIndex() {
		return getIntParam("esMissingIndex");
	}
	
	private NumValue getEsConst() {
		return getNumValueParam("esConst");
	}
	
	private int getExtraDecimals() {
		return getIntParam("extraDecimals");
	}
	
	
//-----------------------------------------------------------

	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 6));
		
		thePanel.add("Center", listPanel(data));	
		
		thePanel.add("South", resultPanels());	
		
		return thePanel;
	}
	
	private XPanel listPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new CenterFillLayout(CenterFillLayout.FILL_VERT));
		
			valueList = new SmoothScrollList(data, this, true);
			valueList.addVariableToList("year", ScrollValueList.RAW_VALUE);
			valueList.addVariableToList("y", ScrollValueList.RAW_VALUE);
			valueList.addVariableToList("es", ScrollValueList.RAW_VALUE);
			valueList.addVariableToList("ma", ScrollValueList.RAW_VALUE);
		thePanel.add(valueList);
		
		return thePanel;
	}

/*
	private XPanel resultPanels() {
		XPanel eqnsPanel = new XPanel();
		eqnsPanel.setLayout(new ProportionLayout(0.5, 4, ProportionLayout.HORIZONTAL));
		
			result1Panel = new ExpressionResultPanel("Calculator", 3, 30, "Result =", 6, 4,
																													ExpressionResultPanel.VERTICAL, this);
		eqnsPanel.add(ProportionLayout.LEFT, result1Panel);
		
			result2Panel = new ExpressionResultPanel("Calculator", 3, 30, "Result =", 6, 4,
																													ExpressionResultPanel.VERTICAL, this);
		eqnsPanel.add(ProportionLayout.RIGHT, result2Panel);
		
		return eqnsPanel;
	}
*/

	private XPanel resultPanels() {
		XPanel eqnsPanel = new InsetPanel(2, 2);
		eqnsPanel.setLayout(new ProportionLayout(0.5, 4, ProportionLayout.VERTICAL));
		
			result1Panel = new ExpressionResultPanel(null, 2, 30, "Value A =", 6,
																													ExpressionResultPanel.HORIZONTAL, this);
			registerStatusItem("es", result1Panel);
		eqnsPanel.add(ProportionLayout.TOP, result1Panel);
		
			result2Panel = new ExpressionResultPanel(null, 2, 30, "Value B =", 6,
																													ExpressionResultPanel.HORIZONTAL, this);
			registerStatusItem("ma", result2Panel);
		eqnsPanel.add(ProportionLayout.BOTTOM, result2Panel);
		
		eqnsPanel.lockBackground(kWorkingBackground);
		
		return eqnsPanel;
	}
	
	protected void setDisplayForQuestion() {
		NumVariable yVar = (NumVariable)data.getVariable("y");
		int resultDecimals = yVar.getMaxDecimals() + getExtraDecimals();
		
		result1Panel.clear();
		result1Panel.setResultDecimals(resultDecimals);
		result2Panel.clear();
		result2Panel.setResultDecimals(resultDecimals);
		
		int[] missingIndex = new int[4];
		missingIndex[0] = -1;
		missingIndex[1] = -1;
		missingIndex[2] = getEsMissingIndex();
		missingIndex[3] = getMaMissingIndex();
		
		Value[] missingText = {null, null, new LabelValue("? A ?"), new LabelValue("? B ?")};
		
		valueList.setMissing(missingIndex, missingText);
		
		valueList.resetVariables();
		validate();
		valueList.repaint();
		
		valueList.scrollToEnd();
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			NumVariable yVar = new NumVariable("Y");
		data.addVariable("y", yVar);
		
			IndexVariable indexVar = new IndexVariable("Index", 9);
		data.addVariable("index", indexVar);
		
			ScaledVariable yearVar = new ScaledVariable("Year", indexVar, "index", 1900, 1.0, 0);
		data.addVariable("year", yearVar);
		
			ExpSmoothVariable esVar = new ExpSmoothVariable("ES", data, "y");
		data.addVariable("es", esVar);
		
			MeanMedianVariable maVar = new MeanMedianVariable("MA", data, "y", false) ;
		data.addVariable("ma", maVar);
		
		return data;
	}
	
	
//-----------------------------------------------------------

	
	protected void setDataForQuestion() {
		NumVariable yVar = (NumVariable)data.getVariable("y");
		yVar.name = getVarName();
		yVar.readValues(getValues());
		
		IndexVariable indexVar = (IndexVariable)data.getVariable("index");
		indexVar.setNoOfValues(yVar.noOfValues());
		
		ScaledVariable yearVar = (ScaledVariable)data.getVariable("year");
		yearVar.setParam(0, getFirstYear() - 1);
		
		ExpSmoothVariable esVar = (ExpSmoothVariable)data.getVariable("es");
		NumValue esConst = getEsConst() ;
		esVar.name = "ES(" + esConst + ")";
		esVar.setSmoothConst(esConst.toDouble());
		esVar.setExtraDecimals(getExtraDecimals());
		
		MeanMedianVariable maVar = (MeanMedianVariable)data.getVariable("ma");
		int runLength = getMaRunLength();
		maVar.name = "MA(" + runLength + ")";
		maVar.setMeanRun(runLength);
		maVar.setExtraDecimals(getExtraDecimals());
	}
	
	
//-----------------------------------------------------------
	
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		int esYear = getFirstYear() + getEsMissingIndex();
//		NumValue esConst = getEsConst();
//		NumValue negEsConst = new NumValue(1 - esConst.toDouble(), esConst.decimals);
		
		int maMissingIndex = getMaMissingIndex();
		int maYear = getFirstYear() + maMissingIndex;
//		int runLength = getMaRunLength();
//		int firstYear = maYear - (runLength / 2);
//		int lastYear = maYear + (runLength / 2);
		
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Type the missing values from the last two columns (yellow) into the edit boxes on the right above.\n");
				messagePanel.insertText("The boxes to their left can be used to perform the calculations -- type expressions such as \"1 + 2 * 3\" then click ");
				messagePanel.insertBoldText("Calculate");
				messagePanel.insertText(".");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertRedText("You must type values for both of the missing values in the boxes on the right above.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer");
				insertCorrectEsMessage(messagePanel);
				insertCorrectMaMessage(messagePanel);
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!");
				insertCorrectEsMessage(messagePanel);
				insertCorrectMaMessage(messagePanel);
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Only one smoothed value is correct!\n");
				if (esCorrect())
					messagePanel.insertText("Your exponentially smoothed value is correct but you have incorrectly calculated the moving average for " + maYear + ".");
				else
					messagePanel.insertText("Your moving average value is correct but you have incorrectly calculated the exponentially smoothed value for " + esYear + ".");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("Your exponentially smoothed value for " + esYear + " and your moving average for " + maYear + " are both incorrect (or inaccurately calculated).");
				break;
		}
	}
	
	private void insertCorrectEsMessage(MessagePanel messagePanel) {
		int esYear = getFirstYear() + getEsMissingIndex();
		NumValue esConst = getEsConst();
		NumValue negEsConst = new NumValue(1 - esConst.toDouble(), esConst.decimals);
		messagePanel.insertText("\nThe exponentially smoothed value for " + esYear + " is " + esConst + " times (the actual " + esYear + " value) plus " + negEsConst + " times (the smoothed value for " + (esYear - 1) + ").");
	}
	
	private void insertCorrectMaMessage(MessagePanel messagePanel) {
		int maMissingIndex = getMaMissingIndex();
		int maYear = getFirstYear() + maMissingIndex;
		int runLength = getMaRunLength();
		int firstYear = maYear - (runLength / 2);
		int lastYear = maYear + (runLength / 2);
		
		messagePanel.insertText("\nThe moving average value for " + maYear + " is the sum of the values from " + firstYear + " to " + lastYear);
		if (runLength % 2 == 0)
			messagePanel.insertText(" (with half weights on the end values)");
		messagePanel.insertText(" divided by " + runLength + ".");
	}
	
	protected int getMessageHeight() {
		return 150;
	}
	
	
//-----------------------------------------------------------
	
	private boolean esCorrect() {
		NumValue esAttempt = result1Panel.getAttempt();
		NumValue esCorrect = ((NumValue)((NumVariable)data.getVariable("es")).valueAt(getEsMissingIndex()));
		
		double maxExactError = 2 * Math.pow(10.0, -esCorrect.decimals);
		double esError = Math.abs(esAttempt.toDouble() - esCorrect.toDouble());
		
		return esError < maxExactError;
	}
	
	private boolean maCorrect() {
		NumValue maAttempt = result2Panel.getAttempt();
		NumValue maCorrect = ((NumValue)((NumVariable)data.getVariable("ma")).valueAt(getMaMissingIndex()));
		
		double maxExactError = 2 * Math.pow(10.0, -maCorrect.decimals);
		double maError = Math.abs(maAttempt.toDouble() - maCorrect.toDouble());
		
		return maError < maxExactError;
	}
	
	protected int assessAnswer() {
		if (result1Panel.isClear() || result2Panel.isClear())
			return ANS_INCOMPLETE;
		
		boolean esCorrect = esCorrect();
		boolean maCorrect = maCorrect();
		
		if (esCorrect && maCorrect)
			return ANS_CORRECT;
		
		if (esCorrect || maCorrect)
			return ANS_CLOSE;
		
		return ANS_WRONG;
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		NumVariable yVar = (NumVariable)data.getVariable("y");
		
		NumValue esConst = getEsConst();
		NumValue negEsConst = new NumValue(1 - esConst.toDouble(), esConst.decimals);
		int esMissingIndex = getEsMissingIndex();
		
		NumValue currentY = (NumValue)yVar.valueAt(esMissingIndex);
		
		NumVariable esVar = (NumVariable)data.getVariable("es");
		NumValue esLag = (NumValue)esVar.valueAt(esMissingIndex - 1);
		
		NumValue esResult = (NumValue)esVar.valueAt(esMissingIndex);
		
		String esExpression = esConst + " * " + currentY + " + " + negEsConst + " * " + esLag;
		result1Panel.showAnswer(esResult, esExpression);
		
		int maMissingIndex = getMaMissingIndex();
		int runLength = getMaRunLength();
		int firstIndex = maMissingIndex - (runLength / 2);
		int lastIndex = maMissingIndex + (runLength / 2);
		boolean evenRunLength = runLength % 2 == 0;
		
		NumVariable maVar = (NumVariable)data.getVariable("ma");
		NumValue maResult = (NumValue)maVar.valueAt(maMissingIndex);
		
		String maExpression = "(" + (NumValue)yVar.valueAt(firstIndex);
		if (evenRunLength)
			maExpression += "/2";
		
		for (int i=firstIndex+1 ; i<=lastIndex ; i++)
			maExpression += " + " + (NumValue)yVar.valueAt(i);
		if (evenRunLength)
			maExpression += "/2";
		
		maExpression += ") / " + runLength;
		result2Panel.showAnswer(maResult, maExpression);
	}
	
	protected double getMark() {
		double mark = 0.0;
		if (!result1Panel.isClear() && esCorrect())
			mark += 0.5;
		if (!result2Panel.isClear() && maCorrect())
			mark += 0.5;
		return mark;
	}
}
