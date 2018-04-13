package exerciseNumGraphProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreVariables.*;
import random.*;
import exercise2.*;
import distn.*;
import formula.*;


import exerciseNormal.*;
import exerciseNumGraph.*;


public class HistoAreaApplet extends ExerciseApplet implements IntervalConstants {
	static final protected Color kTemplateColor = new Color(0x000099);	//	dark blue
	static final protected Color kTemplateBackground = new Color(0xFFE594);
	
	static final private double kLowTailProb = 0.005;
	static final private double kMinWidthPropn = 0.8;
	
	static final protected double kEps = 0.0002;
	static final protected double kRoughEps = 0.002;
	static final protected double kEyeballEps = 0.1;
	
	private RandomGamma generator;
	
	private HorizAxis valAxis;
	private VertAxis countAxis;
	private DragHistoView histoView;
	
	private PropnTemplatePanel propnTemplate;
	
	protected ResultValuePanel resultPanel;
	
//================================================
	
	protected String getAnswerString() {
		return hasOption("showCounts") ? "Answer =" : "Proportion =";
	}
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(data));
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
				resultPanel = new ResultValuePanel(this, getAnswerString(), 6);
			registerStatusItem("answer", resultPanel);
			bottomPanel.add(resultPanel);
			
			bottomPanel.add(createMarkingPanel(hasOption("allowHints") && !isTestMode() ? ALLOW_HINTS : NO_HINTS));
			
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add(messagePanel);
		
		add("South", bottomPanel);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
//		registerParameter("index", "int");					//	always registered

		registerParameter("shape", "const");
		registerParameter("count", "int");
		registerParameter("axis", "string");
		registerParameter("varName", "string");
		registerParameter("intervalType", "int");
		registerParameter("cut-offs", "cut-offs");
		registerParameter("classInfo", "string");
	}
	
	protected void addTypeDelimiters() {
		addType("cut-offs", "*");
	}
	
	protected Object createConstObject(String baseType, String valueString) {
//		if (baseType.equals("cut-offs")) {
//			int questionType = tailType();
//			StringTokenizer pst = new StringTokenizer(valueString, ",");
//			NumValue lowLimit = new NumValue(pst.nextToken());
//			NumValue highLimit = new NumValue(pst.nextToken());
//			return new IntervalLimits(lowLimit.toDouble(), highLimit.toDouble(),
//															Math.max(lowLimit.decimals, highLimit.decimals), questionType);
//		}
//		else
			return super.createConstObject(baseType, valueString);
	}
	
	private double[] findClassStarts() {
		StringTokenizer st = new StringTokenizer(getClassInfo());
		double class0Start = Double.parseDouble(st.nextToken());
		double classWidth = Double.parseDouble(st.nextToken());
		
		st = new StringTokenizer(getAxisInfo());
		st.nextToken();			//	minimum
		double max = Double.parseDouble(st.nextToken());
		
		int nClasses = (int)Math.round(Math.floor((max - class0Start) / classWidth));
		double classStart[] = new double[nClasses + 1];
		for (int i=0 ; i<=nClasses ; i++)
			classStart[i] = class0Start + i * classWidth;
		return classStart;
	}
	
	protected Object createRandomObject(String baseType, String paramString, Object oldParam) {
		if (baseType.equals("cut-offs")) {				//		must have paramString == ":" to be recognised as random object
			int decimals = getClassDecimals();
			
			double classStarts[] = findClassStarts();
			RandomInteger generator = new RandomInteger(1, classStarts.length - 2, 1, nextSeed());
			
			int index1 = generator.generateOne();
			int index2 = generator.generateOne();
			while (index2 == index1)
				index2 = generator.generateOne();
			
			int lowIndex = Math.min(index1, index2);
			int highIndex = Math.max(index1, index2);
			
			IntervalLimits limits = null;
			int questionType = tailType();
			switch (questionType) {
				case LESS_THAN:
				case LESS_THAN_SIMPLE:
					limits = new IntervalLimits(Double.NEGATIVE_INFINITY, classStarts[index1], decimals, questionType);
					break;
				case GREATER_THAN:
				case GREATER_THAN_SIMPLE:
					limits = new IntervalLimits(classStarts[index1], Double.POSITIVE_INFINITY, decimals, questionType);
					break;
				case BETWEEN:
					limits = new IntervalLimits(classStarts[lowIndex], classStarts[highIndex], decimals, questionType);
					break;
				case OUTSIDE:
					limits = new IntervalLimits(classStarts[highIndex], classStarts[lowIndex], decimals, questionType);
					break;
			}
			return limits;
		}
		else
			return super.createRandomObject(baseType, paramString, oldParam);
	}
	
	protected double getShapeValue() {
		return getDoubleParam("shape");
	}
	
	protected int getCount() {
		return getIntParam("count");
	}
	
	protected String getAxisInfo() {
		return getStringParam("axis");
	}
	
	protected String getVarName() {
		return getStringParam("varName");
	}
	
	protected String getClassInfo() {
		return getStringParam("classInfo");
	}
	
	protected int tailType() {
		return getIntParam("intervalType");
	}
	
	protected IntervalLimits getLimits() {
		return (IntervalLimits)getObjectParam("cut-offs");
	}
	
	protected int getClassDecimals() {
		StringTokenizer st = new StringTokenizer(getClassInfo());
		int startDecimals = new NumValue(st.nextToken()).decimals;
		int widthDecimals = new NumValue(st.nextToken()).decimals;
		return Math.max(startDecimals, widthDecimals);
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			XPanel histoPanel = new XPanel();
			histoPanel.setLayout(new AxisLayout());
			
				valAxis = new HorizAxis(this);
			histoPanel.add("Bottom", valAxis);
			
				countAxis = new VertAxis(this);
				countAxis.show(false);
			histoPanel.add("Left", countAxis);
			
				histoView = new DragHistoView(data, this, "y", valAxis, countAxis);
				histoView.lockBackground(Color.white);
				histoView.setCanDrag(false);
				histoView.setShowCounts(hasOption("showCounts"));
			histoPanel.add("Center", histoView);
			
		thePanel.add("Center", histoPanel);
		
		if (hasOption("showCounts")) {
			FormulaContext stdContext = new FormulaContext(kTemplateColor, getStandardFont(), this);
			propnTemplate = new PropnTemplatePanel("Proportion of values =", stdContext);
			propnTemplate.lockBackground(kTemplateBackground);
			registerStatusItem("propnTemplate", propnTemplate);
			thePanel.add("South", propnTemplate);
		}
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		valAxis.readNumLabels(getAxisInfo());
		valAxis.setAxisName(getVarName());
		valAxis.invalidate();
		
		StringTokenizer st = new StringTokenizer(getClassInfo());
		double class0Start = Double.parseDouble(st.nextToken());
		double classWidth = Double.parseDouble(st.nextToken());
		histoView.changeClasses(class0Start, classWidth);
		histoView.setCorrectCounts();
		if (hasOption("showCounts") || !hasHints)
			histoView.clearSelection();
		else {
			IntervalLimits limits = getLimits();
			boolean selected[] = getSelectedClasses(limits);
			histoView.setSelectedBars(selected);
		}
		
		int maxCount = histoView.maxCount();
		String axisString = "0 " + (maxCount + 1) + " " + (maxCount + 2) + " 1";
		countAxis.readNumLabels(axisString);
		countAxis.invalidate();
		
		if (propnTemplate != null)
			propnTemplate.setValues(new NumValue(1, 0), new NumValue(1, 0));
		
		resultPanel.clear();
	}
	
	
	protected void setDataForQuestion() {
		NumSampleVariable coreVar = (NumSampleVariable)data.getVariable("base");
		RandomGamma gammaGenerator = (RandomGamma)coreVar.getGenerator();
		
		double shape = getShapeValue();
		double absShape = Math.abs(shape);
		gammaGenerator.setShape(absShape);
		
		double lowQuantile = GammaDistnVariable.gammaQuant(kLowTailProb, absShape);
		double highQuantile = GammaDistnVariable.gammaQuant(1.0 - kLowTailProb, absShape);
		gammaGenerator.setTruncation(lowQuantile, highQuantile);
		
		int n = getCount();
		coreVar.setSampleSize(n);
		coreVar.generateNextSample();
		
		NumValue sortedCore[] = coreVar.getSortedData();
		double coreMin = sortedCore[0].toDouble();
		double coreMax = sortedCore[sortedCore.length - 1].toDouble();
		
		StringTokenizer st = new StringTokenizer(getAxisInfo());
		double axisMin = Double.parseDouble(st.nextToken());
		double axisMax = Double.parseDouble(st.nextToken());
		
		Random uniformGenerator = new Random(nextSeed());
		double minWidth = kMinWidthPropn * (axisMax - axisMin);
		double dataMin = axisMin + uniformGenerator.nextDouble() * (axisMax - axisMin - minWidth);
		double dataMax = dataMin + minWidth + uniformGenerator.nextDouble() * (axisMax - dataMin - minWidth);
		
		if (shape < 0.0) {
			double temp = dataMax;
			dataMax = dataMin;
			dataMin = temp;
		}
		
		double factor = (dataMax - dataMin) / (coreMax - coreMin);
		double shift = dataMin - coreMin * factor;
		
		ScaledVariable yVar = (ScaledVariable)data.getVariable("y");
		yVar.name = getVarName();
		yVar.setScale(shift, factor, 9);
		yVar.clearSortedValues();
		
		data.variableChanged("base");
	}
	
//-----------------------------------------------------------
	
	protected String getLowerPropnString() {
		return translate("proportion");
	}
	
	protected String getUpperPropnsString() {
		return translate("Proportions");
	}
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		NumValue correct = null;
		int n = 0, nTotal = 0;
		if (result != ANS_UNCHECKED) {
			n = getCorrectCount();
			nTotal = getNValues();
			correct = new NumValue(n / (double)nTotal, 3);
		}
		boolean showCounts = hasOption("showCounts");
		String propnProbString = getLowerPropnString();
		switch (result) {
			case ANS_UNCHECKED:
				if (showCounts)
					messagePanel.insertText("Type the required " + propnProbString + " into the box above.");
				else {
					messagePanel.insertText("Use the shape of the histogram to estimate the required " + propnProbString + " to within 0.1 of the correct value.");
					if (hasHints)
						messagePanel.insertText("\n(What proportion of the total area is highlighted?)");
				}
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText("You must type a " + propnProbString + " into the answer box above.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText(getUpperPropnsString() + " cannot be less than zero or more than one.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				if (showCounts)
					messagePanel.insertText(n + " out of the " + nTotal + " values are in the specified range.");
				else
					messagePanel.insertText("The exact histogram area above the specified range is " + correct + " of the total area.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				if (showCounts)
					messagePanel.insertText(n + " out of the " + nTotal + " values are in the specified range.");
				else
					messagePanel.insertText("The exact histogram area above the specified range is " + correct + " of the total area.");
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Not close enough!\n");
				messagePanel.insertText("Your answer is close, but you should be able to specify the " + propnProbString + " correct to 4 decimal digits.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				if (showCounts)
					messagePanel.insertText("Count the number of values in the selected classes, then divide by " + nTotal + ".");
				else {
					messagePanel.insertText("Estimate the proportion of the total histogram area that is in the selected classes.");
					if (hasHints)
						messagePanel.insertText("\n(What proportion of the total area is highlighted?)");
				}
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 100;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			generator = new RandomGamma(10, 1.0, 1.0, 3.0);
			generator.setSeed(nextSeed());
			NumSampleVariable baseVar = new NumSampleVariable("Std normal", generator, 9);
			baseVar.generateNextSample();
		data.addVariable("base", baseVar);
		
			ScaledVariable yVar = new ScaledVariable("", baseVar, "base", 0.0, 1.0, 9);
//			yVar.setRoundValues(true);
		data.addVariable("y", yVar);
		
		return data;
	}
	
	
	private double getAttempt() {
		return resultPanel.getAttempt().toDouble();
	}
	
	private boolean[] getSelectedClasses(IntervalLimits limits) {
		double classStarts[] = histoView.getClassStarts();
		boolean selected[] = new boolean[classStarts.length - 1];
		
		double start, end;
		if (limits.startVal == null) {
			start = Double.NEGATIVE_INFINITY;
			end = limits.endVal.toDouble();
		}
		else if (limits.endVal == null) {
			start = limits.startVal.toDouble();
			end = Double.POSITIVE_INFINITY;
		}
		else {
			start = limits.startVal.toDouble();
			end = limits.endVal.toDouble();
		}
		
		for (int i=0 ; i<selected.length ; i++) {
			double midY = (classStarts[i] + classStarts[i + 1]) / 2;
			selected[i] = (midY > start) == (midY < end);
			if (start > end)
				selected[i] = !selected[i];
		}
		return selected;
	}
	
	private int countValues(IntervalLimits limits) {
		boolean selected[] = getSelectedClasses(limits);
		double counts[] = histoView.getClassCounts();
		
		int n = 0;
		for (int i=0 ; i<selected.length ; i++)
			if (selected[i])
				n += (int)Math.round(counts[i]);
		return n;
	}
	
	
//-----------------------------------------------------------

	private int getCorrectCount() {
		IntervalLimits limits = getLimits();
		return countValues(limits);
	}
	
	private int getNValues() {
		return ((NumVariable)data.getVariable("y")).noOfValues();
	}
	
	protected int assessAnswer() {
		double attempt = getAttempt();
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else if(attempt < 0.0 || attempt > 1.0)
			return ANS_INVALID;
		else {
			double correct = getCorrectCount() / (double)getNValues();
			
			if (hasOption("showCounts"))
				return (Math.abs(correct - attempt) <= kEps) ? ANS_CORRECT : (Math.abs(correct - attempt) <= kRoughEps) ? ANS_CLOSE : ANS_WRONG;
			else
				return (Math.abs(correct - attempt) <= kEyeballEps) ? ANS_CORRECT : ANS_WRONG;
		}
	}
	
	protected void giveFeedback() {
		if ((result == ANS_CLOSE || result == ANS_WRONG) && !hasHints) {
			boolean selected[] = getSelectedClasses(getLimits());
			histoView.setSelectedBars(selected);
			histoView.repaint();
		}
	}
	
	protected void showCorrectWorking() {
		int n = getCorrectCount();
		int nTotal = getNValues();
		double correct = n / (double)nTotal;
		
		NumValue probValue = new NumValue(correct, 4);
		resultPanel.showAnswer(probValue);
		
		if (propnTemplate != null)
			propnTemplate.setValues(new NumValue(n, 0), new NumValue(nTotal, 0));
		
		if (hasOption("showCounts") || !hasHints) {
			boolean selected[] = getSelectedClasses(getLimits());
			histoView.setSelectedBars(selected);
			histoView.repaint();
		}
	}
	
	protected double getMark() {
		int ans = assessAnswer();
		return (ans == ANS_CORRECT) ? 1 : (ans == ANS_CORRECT) ? 0.7 : 0;
	}
	
	public void showHints(boolean hasHints) {
		super.showHints(hasHints);
		if (hasHints) {
			boolean selected[] = getSelectedClasses(getLimits());
			histoView.setSelectedBars(selected);
		}
		else
			histoView.clearSelection();
		histoView.repaint();
		message.changeContent();
	}
	
}