package exerciseCategProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import exercise2.*;
import formula.*;


import exerciseNumGraph.*;
import exerciseCateg.*;


public class FindProbFromBarsApplet extends ExerciseApplet {
	static final protected Color kTemplateColor = new Color(0x000099);	//	dark blue
	static final protected Color kTemplateBackground = new Color(0xFFE594);
	
	static final protected double kEps = 0.0005;
	static final protected double kRoughEps = 0.005;
	static final private int kPropnDecimals = 3;
	
	
	private PropnTemplatePanel propnTemplate;
	private ResultValuePanel resultPanel;
	
	private HorizAxis valAxis;
	private VertAxis countAxis;
	private BarCountView barView;
	
//-----------------------------------------------------------
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(data));
		
		add("South", getBottomPanel());
	}
	
	protected XPanel getBottomPanel() {
		XPanel thePanel = new InsetPanel(0, 20, 0, 0);
		thePanel.setLayout(new BorderLayout(0, 4));
		
			XPanel answerPanel = new XPanel();
			answerPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
				resultPanel = new ResultValuePanel(this, "Answer =", 6);
				registerStatusItem("answer", resultPanel);
			answerPanel.add(resultPanel);
			
			answerPanel.add(createMarkingPanel(NO_HINTS));
		
		thePanel.add("North", answerPanel);
		
			XPanel messagePanel = new XPanel();
			messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
				
				message = new ExerciseMessagePanel(this);
			messagePanel.add(message);
		thePanel.add("Center", messagePanel);
		return thePanel;
	}
		
	protected XPanel getPropnTemplatePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
			XPanel templatePanel = new InsetPanel(10, 5);
			templatePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			templatePanel.lockBackground(kTemplateBackground);
				
				FormulaContext stdContext = new FormulaContext(kTemplateColor, getStandardFont(), this);
				propnTemplate = new PropnTemplatePanel(null, 5, stdContext);
				propnTemplate.lockBackground(kTemplateBackground);
				registerStatusItem("propnTemplate", propnTemplate);
			
			templatePanel.add(propnTemplate);
			
		thePanel.add(templatePanel);
		return thePanel;
	}
	
//-----------------------------------------------------------
	
	class Successes {
		private boolean[] success;
		private Value[] categories;
		
		Successes(boolean[] success, Value[] categories) {
			this.success = success;
			this.categories = categories;
		}
		
		public String toString() {
			String successString = null;
			int nSuccess = 0;
			for (int i=success.length-1 ; i>=0 ; i--)
				if (success[i]) {
					if (nSuccess == 0)
						successString = categories[i].toString();
					else if (nSuccess == 1)
						successString = categories[i].toString() + " or " + successString;
					else
						successString = categories[i].toString() + ", " + successString;
					
					nSuccess ++;
				}
			if (nSuccess > 1)
				successString = "either " + successString;
			return successString;
		}
		
		public boolean[] getSuccesses() {
			return success;
		}
	}
	
	protected void addTypeDelimiters() {
		addType("successes", "*");
	}
	
	protected Object createConstObject(String baseType, String valueString) {
		if (baseType.equals("successes")) {
			Value[] categories = getCategories();
			boolean success[] = new boolean[categories.length];
			
			StringTokenizer st = new StringTokenizer(valueString, ",");
			while (st.hasMoreTokens())
				success[Integer.parseInt(st.nextToken())] = true;
			return new Successes(success, categories);
		}
		else
			return super.createConstObject(baseType, valueString);
	}
	
	protected Object createRandomObject(String baseType, String paramString, Object oldParam) {
		if (baseType.equals("successes")) {
			Value[] categories = getCategories();
			int nSuccess = getNSuccessCats();
			boolean success[] = new boolean[categories.length];
			
			if (isOrdinal()) {		//	selected cats are successive
				RandomInteger randomCat = new RandomInteger(0, categories.length - nSuccess, 1, nextSeed());
				int firstCat = randomCat.generateOne();
				for (int i=0 ; i<nSuccess ; i++)
					success[firstCat + i] = true;
			}
			else {
				RandomInteger randomCat = new RandomInteger(0, categories.length - 1, 1, nextSeed());
				
				for (int i=0 ; i<nSuccess ; i++) {
					int catIndex;
					do {
						catIndex = randomCat.generateOne();
					} while (success[catIndex]);
					success[catIndex] = true;
				}
			}
			
			return new Successes(success, categories);
		}
		else
			return super.createRandomObject(baseType, paramString, oldParam);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
//		registerParameter("index", "int");					//	always registered
		registerParameter("varName", "string");
		registerParameter("itemsName", "string");
		registerParameter("categories", "string");
		registerParameter("counts", "string");
		registerParameter("countAxis", "string");
		registerParameter("nSuccessCats", "choice");
		registerParameter("successes", "successes");
		registerParameter("ordinal", "boolean");
	}
	
	protected String getVarName() {
		return getStringParam("varName");
	}
	
	protected String getItemsName() {
		return getStringParam("itemsName");
	}
	
	protected Value[] getCategories() {
		StringTokenizer st = new StringTokenizer(getStringParam("categories"));
		Value label[] = new Value[st.countTokens()];
		for (int i=0 ; i<label.length ; i++) {
			String labelString = st.nextToken().replace('_', ' ');
			label[i] = new LabelValue(labelString);
		}
		
		return label;
	}
	
	private int[] getCounts() {
		StringTokenizer st = new StringTokenizer(getStringParam("counts"));
		int counts[] = new int[st.countTokens()];
		for (int i=0 ; i<counts.length ; i++)
			counts[i] = Integer.parseInt(st.nextToken());
		
		return counts;
	}
	
	private String getCountAxis() {
		return getStringParam("countAxis");
	}
	
	protected int getNSuccessCats() {
		return getIntParam("nSuccessCats");
	}
	
	protected boolean[] getSuccesses() {
		return ((Successes)getObjectParam("successes")).getSuccesses();
	}
	
	private String getSuccessNames() {
		return ((Successes)getObjectParam("successes")).toString();
	}
	
	private boolean isOrdinal() {
		return getBooleanParam("ordinal");
	}
	
//-----------------------------------------------------------
	
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
		thePanel.add("North", new XLabel(translate("Frequency"), XLabel.LEFT, this));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
			
				valAxis = new HorizAxis(this);
			mainPanel.add("Bottom", valAxis);
			
				countAxis = new VertAxis(this);
			mainPanel.add("Left", countAxis);
				
				barView = new BarCountView(data, this, "y", valAxis, countAxis);
				barView.lockBackground(Color.white);
				
			mainPanel.add("Center", barView);
				
		thePanel.add("Center", mainPanel);
		
		thePanel.add("South", getPropnTemplatePanel());
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		
		valAxis.setCatLabels(yVar);
		
		countAxis.readNumLabels(getCountAxis());
		countAxis.invalidate();
		
		propnTemplate.setValues(new NumValue(1, 0), new NumValue(1, 0));
		
		resultPanel.clear();
		
		data.variableChanged("y");
	}
	
	
	protected void setDataForQuestion() {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		yVar.name = getVarName();
		yVar.setLabels(getCategories());
		yVar.setCounts(getCounts());
	}
	
	
	protected int getMessageHeight() {
		return 150;
	}

		
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Type the requested probability into the box above.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText("You must type a probability into the Answer box above.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("Probabilities must be between 0 and 1.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The probability is the proportion of " + getItemsName()
									+ " in this 'population' whose " + getVarName() + " is " + getSuccessNames()
									+ ". This is\n");
				messagePanel.setAlignment(MessagePanel.CENTER_ALIGN);
				messagePanel.insertFormula(pSuccessFormula());
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly found the probability,\n");
				messagePanel.setAlignment(MessagePanel.CENTER_ALIGN);
				messagePanel.insertFormula(pSuccessFormula());
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Close!\n");
				messagePanel.insertText("Your answer is close, but try to specify the proportion correct to "
																															+ kPropnDecimals + " decimal digits.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("The probability is the proportion of " + getItemsName()
									+ " in this 'population' whose " + getVarName() + " is " + getSuccessNames()
									+ ".\nCount the number of " + getItemsName() + " whose " + getVarName()
									+ " is " + getSuccessNames() + " and divide by the total number of "
									+ getItemsName() + ".");
				break;
		}
	}
	
	private MFormula pSuccessFormula() {
		FormulaContext stdContext = new FormulaContext(null, null, this);
		
		int count[] = getCounts();
		boolean correct[] = getSuccesses();
		
		MFormula numer = null;
		int total = 0;
		for (int i=0 ; i<count.length ; i++) {
			if (correct[i])
				if (numer == null)
					numer = new MConst(new NumValue(count[i], 0), stdContext);
				else
					numer = new MBinary(MBinary.PLUS, numer, new MConst(new NumValue(count[i], 0), stdContext), stdContext);
			total += count[i];
		}
		
		if (numer instanceof MBinary)
			numer = new MBracket(numer, stdContext);
		
		return new MRatio(numer, new MConst(new NumValue(total, 0), stdContext), stdContext);
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			CatVariable yVar = new CatVariable("");
		data.addVariable("y", yVar);
		
		return data;
	}
	
	
	protected double getAttempt() {
		return resultPanel.getAttempt().toDouble();
	}
	
	protected int getCorrectCount() {
		int count[] = getCounts();
		boolean correct[] = getSuccesses();
		
		int sum = 0;
		for (int i=0 ; i<count.length ; i++)
			if (correct[i])
				sum += count[i];
		return sum;
	}
	
	protected int getNValues() {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		return yVar.noOfValues();
	}
	
//-----------------------------------------------------------
	
	
	protected void selectCorrectCounts() {
		if (barView != null) {
			boolean[] correctCats = getSuccesses();
			barView.setSelectedBars(correctCats);
			barView.repaint();
		}
	}
	
	protected int assessAnswer() {
		double attemptPropn = getAttempt();
			
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else if(attemptPropn < 0.0 || attemptPropn > 1)
			return ANS_INVALID;
		else {
			double correctPropn = getCorrectCount() / (double)getNValues();
			
			return (Math.abs(correctPropn - attemptPropn) <= kEps) ? ANS_CORRECT
								: (Math.abs(correctPropn - attemptPropn) <= kRoughEps) ? ANS_CLOSE : ANS_WRONG;
		}
	}
	
	protected void giveFeedback() {
		if (result != ANS_INCOMPLETE && result != ANS_INVALID)
			selectCorrectCounts();
	}
	
	protected void showCorrectWorking() {
		selectCorrectCounts();
		
		int n = getCorrectCount();
		int nTotal = getNValues();
		double correctPropn = n / (double)nTotal;
		
		resultPanel.showAnswer(new NumValue(correctPropn, kPropnDecimals));
		
		if (propnTemplate != null)
			propnTemplate.setValues(new NumValue(n, 0), new NumValue(nTotal, 0));
	}
	
	protected double getMark() {
		int ans = assessAnswer();
		return (ans == ANS_CORRECT) ? 1 : (ans == ANS_CLOSE) ? 0.8 : 0;
	}
	
}