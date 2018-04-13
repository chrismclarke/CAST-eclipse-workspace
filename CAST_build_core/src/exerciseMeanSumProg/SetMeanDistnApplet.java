package exerciseMeanSumProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import random.*;
import exercise2.*;
import formula.*;

import exerciseMeanSum.*;


public class SetMeanDistnApplet extends ExerciseApplet {
	static final private boolean STAT_DISTN = true;
	static final private boolean POPN_DISTN = false;
	
	static final private int MEAN_VALUE = 0;
	static final private int SUM_VALUE = 1;
	static final private int SINGLE_VALUE = 2;
	
	static final private double kHighestProb = 0.995;
	
	static public MFormula rootNFormula(XApplet applet) {
		FormulaContext stdContext = new FormulaContext(null, null, applet);
		return new MRoot(new MText("n", stdContext), stdContext);
	}
	
	static public MFormula invRootNFormula(XApplet applet) {
		FormulaContext stdContext = new FormulaContext(null, null, applet);
		MFormula rootN = new MRoot(new MText("n", stdContext), stdContext);
		return new MRatio(new MText("1", stdContext), rootN, stdContext);
	}
	
//---------------------------------------------------------------------
		
	private HorizAxis popnAxis, statAxis;
	private SimpleDensityView popnView, statView;
	
	private XChoice[] distnChoice = null;
	private int[] choicePermutation = new int[3];
	
	private double popnShape, popnScale, popnOffset;
	
	private Random random01;
	
//================================================
	
	protected void createDisplay() {
		random01 = new Random(nextSeed());
		
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
//		registerParameter("index", "int");					//	always registered
		registerParameter("axis", "string");
		registerParameter("varName", "string");
		registerParameter("n", "int");
		registerParameter("popnOffset", "const");
		registerParameter("popnShape", "const");
		registerParameter("meanName", "string");
		registerParameter("sumName", "string");
		registerParameter("singleName", "string");
		registerParameter("meanSumType", "choice");
	}
	
	private int getMeanSumType() {
		return getIntParam("meanSumType");
//		return questionExtraVersion;
	}
	
	private String getAxisInfo() {
		return getStringParam("axis");
	}
	
	private String getVarName() {
		return getStringParam("varName");
	}
	
	private String getStatisticName() {
		switch (getMeanSumType()) {
			case 0:
				return getStringParam("meanName");
			case 1:
				return getStringParam("sumName");
			case 2:
				return getStringParam("singleName");
		}
		return null;
		
//		StringTokenizer st = new StringTokenizer(getStringParam("statisticNames"), "*");
//		for (int i=0 ; i<getMeanSumType() ; i++)
//			st.nextToken();
//		return st.nextToken();
	}
	
	private int getN() {
		if (getMeanSumType() == SINGLE_VALUE)
			return 1;
		else
			return getIntParam("n");
	}
	
	private double getPopnOffset() {
		return getDoubleParam("popnOffset");
	}
	
	private double getPopnShape() {
		return getDoubleParam("popnShape");
	}
	
	
//-----------------------------------------------------------
	
	private XChoice getCenterChoice() {
		return distnChoice[choicePermutation[0]];
	}
	
	private XChoice getSpreadChoice() {
		return distnChoice[choicePermutation[1]];
	}
	
	private XChoice getShapeChoice() {
		return distnChoice[choicePermutation[2]];
	}
	
	private void resetChoices() {
		permute(choicePermutation, random01);
		
		XChoice centerChoice = getCenterChoice();
		centerChoice.clearItems();
		centerChoice.addItem("Lower mean");
		centerChoice.addItem("Same mean");
		centerChoice.addItem("Higher mean");
		centerChoice.changeLabel(translate("Centre") + ":");
		
		XChoice spreadChoice = getSpreadChoice();
		spreadChoice.clearItems();
		spreadChoice.addItem("Smaller st devn");
		spreadChoice.addItem("Same st devn");
		spreadChoice.addItem("Bigger st devn");
		spreadChoice.changeLabel(translate("Spread") + ":");
		
		XChoice shapeChoice = getShapeChoice();
		shapeChoice.clearItems();
		shapeChoice.addItem("Less skew");
		shapeChoice.addItem("Same skewness");
		shapeChoice.addItem("More skew");
		shapeChoice.changeLabel("Skewness:");
		
		int meanSumType = getMeanSumType();
		int correctCenter = (meanSumType == SUM_VALUE) ? 2 : 1;
		int correctSpread = (meanSumType == MEAN_VALUE) ? 0 : (meanSumType == SUM_VALUE) ? 2 : 1;
		int correctShape = (meanSumType == SINGLE_VALUE) ? 1 : 0;
		
		RandomInteger randomInt = new RandomInteger(0, 2, 3, nextSeed());
		int menuChoice[];
		do {
			menuChoice = randomInt.generate();
		} while (menuChoice[0] == correctCenter && menuChoice[1] == correctSpread && menuChoice[2] == correctShape);
		
		centerChoice.select(menuChoice[0]);
		spreadChoice.select(menuChoice[1]);
		shapeChoice.select(menuChoice[2]);
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 4));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL));
			
			topPanel.add(ProportionLayout.TOP, createDensityPanel(data, "popn", "Popn distribution:", POPN_DISTN));
			topPanel.add(ProportionLayout.BOTTOM, createDensityPanel(data, "stat", "", STAT_DISTN));
			
		thePanel.add("Center", topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			
			distnChoice = new XChoice[3];
			for (int i=0 ; i<3 ; i++) {
				distnChoice[i] = new XChoice("", XChoice.VERTICAL_LEFT, this);
				registerStatusItem("distnChoice" + i, distnChoice[i]);
				bottomPanel.add(distnChoice[i]);
			}
			
		thePanel.add("South", bottomPanel);
		
		return thePanel;
	}
	
	private XPanel createDensityPanel(DataSet data, String distnKey, String distnTitle, boolean statNotPopn) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
			
			HorizAxis axis = new HorizAxis(this);
		thePanel.add("Bottom", axis);
		
			SimpleDensityView theView = new SimpleDensityView(data, this, distnKey, axis);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		if (statNotPopn) {
			statAxis = axis;
			statView = theView;
		}
		else {
			popnAxis = axis;
			popnView = theView;
		}
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		popnAxis.readNumLabels(getAxisInfo());
		popnAxis.setAxisName(getVarName());
		popnAxis.invalidate();
		
		statAxis.readNumLabels(getAxisInfo());
		statAxis.setAxisName(getStatisticName());
		statAxis.invalidate();
		
		resetChoices();
		setStatDistn();
		
		popnView.resetDisplay();
		popnView.repaint();
		
		statView.resetDisplay();
		statView.repaint();
	}
	
	protected void setDataForQuestion() {
		GammaDistnVariable popnDistn = (GammaDistnVariable)data.getVariable("popn");
		
		popnShape = getPopnShape();
		popnDistn.setShape(popnShape);
		
		popnOffset = getPopnOffset();
		popnDistn.setZeroPos(popnOffset);
		
		StringTokenizer st = new StringTokenizer(getAxisInfo());
		st.nextToken();
		double axisMax = Double.parseDouble(st.nextToken());
		
		int n = getN();
		n = Math.max(n, 2);							//	for a single value, we want to allow a bigger axis so mean can be increased by pop-up
		double sumShape = popnShape * n;
		
		double highSumQuantile = GammaDistnVariable.gammaQuant(kHighestProb, sumShape);
		popnScale = (axisMax - n * popnOffset) / highSumQuantile;
		popnDistn.setScale(popnScale);
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		int meanSumType = getMeanSumType();
		
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Select the distn from the pop-up menus.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				switch (meanSumType) {
					case MEAN_VALUE:
						messagePanel.insertText("The sample mean of n values has a distribution whose mean equals the population mean. However its standard deviation is ");
						messagePanel.insertFormula(invRootNFormula(this));
						messagePanel.insertText(" times the population sd (i.e. lower) and its shape is closer to a nomal distribution.");
						break;
					case SUM_VALUE:
						messagePanel.insertText("The total of n values has a distribution whose mean is n times that of the population. Its standard deviation is also ");
						messagePanel.insertFormula(rootNFormula(this));
						messagePanel.insertText(" times the population sd (i.e. higher) and its shape is closer to a normal distribution.");
						break;
					case SINGLE_VALUE:
						messagePanel.insertText("A single value that is sampled from a population has a distribution that is identical to the population distribution.");
						break;
				}
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have found the correct distribution for ");
				messagePanel.insertText((meanSumType == MEAN_VALUE) ? "the sample mean" : (meanSumType == SUM_VALUE) ? "the sum of the sample values" : "a single sampled value");
				messagePanel.insertText(".");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!");
				
				int centerSelection = getCenterChoice().getSelectedIndex();
				int spreadSelection = getSpreadChoice().getSelectedIndex();
				int shapeSelection = getShapeChoice().getSelectedIndex();
				
				switch (meanSumType) {
					case MEAN_VALUE:
						if (centerSelection != 1) {
							messagePanel.insertBoldBlueText("\nMean: ");
							messagePanel.insertRedText("The sample mean has a distribution with the same mean as the population.");
						}
						if (spreadSelection != 0) {
							messagePanel.insertBoldBlueText("\nSt devn: ");
							messagePanel.insertRedText("As the sample size increases, the sample mean becomes less variable.");
						}
						if (shapeSelection != 0) {
							messagePanel.insertBoldBlueText("\nShape of distn: ");
							messagePanel.insertRedText("As the sample size increases, sample mean's distribution becomes closer to a normal distribution (more symmetric).");
						}
						break;
					case SUM_VALUE:
						if (centerSelection != 2) {
							messagePanel.insertBoldBlueText("\nMean: ");
							messagePanel.insertRedText("As the sample size increases, the total of the sample values is expected to increase.");
						}
						if (spreadSelection != 2) {
							messagePanel.insertBoldBlueText("\nSt devn: ");
							messagePanel.insertRedText("As the sample size increases, the total of the sample values becomes more variable.");
						}
						if (shapeSelection != 0) {
							messagePanel.insertBoldBlueText("\nShape of distn: ");
							messagePanel.insertRedText("As the sample size increases, the distribution of the sample total becomes closer to a normal distribution (more symmetric).");
						}
						break;
					case SINGLE_VALUE:
						messagePanel.insertRedText("\nA single value that is sampled from a population has the same distribution as the population.");
						break;
				}

				break;
		}
	}
	
	protected int getMessageHeight() {
		return 140;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			GammaDistnVariable popnDistn = new GammaDistnVariable(translate("Population"));
			popnDistn.setScale(0.15);
		data.addVariable("popn", popnDistn);
		
			GammaDistnVariable sampDistn = new GammaDistnVariable("Statistic");
			sampDistn.setScale(0.15);
		data.addVariable("stat", sampDistn);
		
		return data;
	}
	
	public void setFixedQuestionSeed(long seed) {
		super.setFixedQuestionSeed(seed);
		random01.setSeed(nextSeed());
	}
	
	
//-----------------------------------------------------------
	
	private void setStatDistn() {
		XChoice centerChoice = getCenterChoice();
		XChoice spreadChoice = getSpreadChoice();
		XChoice shapeChoice = getShapeChoice();
		
		int centerIndex = centerChoice.getSelectedIndex();
		int spreadIndex = spreadChoice.getSelectedIndex();
		int shapeIndex = shapeChoice.getSelectedIndex();
		
		int n = Math.max(getN(), 2);			//	for single value, we still want to allow different mean & sd from pop-ups
		
		double shapeFactor = (shapeIndex == 0) ? n : (shapeIndex == 1) ? 1 : (1 + (popnShape - 1) / 2) / popnShape;
		double sdFactor = (spreadIndex == 0) ? (1 / Math.sqrt(n)) : (spreadIndex == 1) ? 1 : Math.sqrt(n);
		double meanFactor = (centerIndex == 0) ? 0.5 : (centerIndex == 1) ? 1 : n;
		
		double newShape = popnShape * shapeFactor;
		double newScale = sdFactor / Math.sqrt(shapeFactor) * popnScale;
		double newZero = meanFactor * popnOffset + (meanFactor - sdFactor * Math.sqrt(shapeFactor)) * popnScale * popnShape;
		
		GammaDistnVariable statDistn = (GammaDistnVariable)data.getVariable("stat");
		statDistn.setShape(newShape);
		statDistn.setScale(newScale);
		statDistn.setZeroPos(newZero);
	}
	
	
//-----------------------------------------------------------
	
	private int getCorrectCenter() {
		int meanSumType = getMeanSumType();
		return (meanSumType == SUM_VALUE) ? 2 : 1;
	}
	
	private int getCorrectSpread() {
		int meanSumType = getMeanSumType();
		return (meanSumType == MEAN_VALUE) ? 0 : (meanSumType == SUM_VALUE) ? 2 : 1;
	}
	
	private int getCorrectShape() {
		int meanSumType = getMeanSumType();
		return (meanSumType == SINGLE_VALUE) ? 1 : 0;
	}
	
	private boolean isCorrect(XChoice choice, int correct) {
		return choice.getSelectedIndex() == correct;
	}
	
	protected int assessAnswer() {
		return isCorrect(getCenterChoice(), getCorrectCenter())
									&& isCorrect(getSpreadChoice(), getCorrectSpread())
									&& isCorrect(getShapeChoice(), getCorrectShape()) ? ANS_CORRECT : ANS_WRONG;
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		XChoice centerChoice = getCenterChoice();
		XChoice spreadChoice = getSpreadChoice();
		XChoice shapeChoice = getShapeChoice();
		
		centerChoice.select(getCorrectCenter());
		spreadChoice.select(getCorrectSpread());
		shapeChoice.select(getCorrectShape());
		
		setStatDistn();
		statView.resetDisplay();
		statView.repaint();
	}
	
	protected double getMark() {
		return (assessAnswer() == ANS_CORRECT) ? 1 : 0;
	}
	

	
	private boolean localAction(Object target) {
		XChoice centerChoice = getCenterChoice();
		XChoice spreadChoice = getSpreadChoice();
		XChoice shapeChoice = getShapeChoice();
			
		if (target == centerChoice || target == spreadChoice || target == shapeChoice) {
			setStatDistn();
			
			statView.resetDisplay();
			statView.repaint();
			
			noteChangedWorking();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}