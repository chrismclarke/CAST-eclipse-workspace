package exerciseCategProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;
import exercise2.*;

import cat.*;
import exerciseCateg.*;


public class FindPiePercentApplet extends ExerciseApplet {
	
	static final private int PROB_5_10 = 0;
	static final private int PROB_20_30 = 1;
	static final private int PROB_42_58 = 2;
	static final private int PROB_70_80 = 3;
	static final private int PROB_90_95 = 4;
	

//	static final private int kNoOfValues = 1000;
	static final private int kNoOfValues = 200;
	
	
	private PieDrawer pieDrawer = new PieDrawer();
	private CatKey3View keyView;
	private PieHiliteView pieView;
	private PiePercentChoicePanel percentChoice;
	
	private RandomInteger random;
	private RandomMultinomial randomMulti;
	
//================================================
	
	protected void createDisplay() {
		random = new RandomInteger(0, 1, 1, nextSeed());
		randomMulti = new RandomMultinomial("1 0.3 0.3 0.4");
		randomMulti.setSeed(nextSeed());
		
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
			XPanel localWorkingPanel = getWorkingPanels(data);		//	CoreMatchApplet has variable workingPanel
		add("Center", localWorkingPanel);
				
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
	
//-----------------------------------------------------------------
	
	protected void registerParameterTypes() {
		registerParameter("varName", "string");
		registerParameter("categories", "string");
		registerParameter("answer", "choice");
		registerParameter("cutoffType", "choice");
		registerParameter("ordinalPareto", "choice");
		registerParameter("cutoff", "critical");
		registerParameter("defaultProbs", "string");
//		registerParameter("nLowCats", "int");
//		registerParameter("nHighCats", "int");
	}
	
	protected void addTypeDelimiters() {
		addType("critical", ",");
	}
	
	protected Object createConstObject(String baseType, String valueString) {
		if (baseType.equals("critical"))
			return CategoryParam.createConstObject(valueString, getCategories(), isOrdinalNotPareto(), isLess());
		else
			return super.createConstObject(baseType, valueString);
	}
	
	protected Object createRandomObject(String baseType, String paramString, Object oldParam) {
		if (baseType.equals("critical"))
			return CategoryParam.createRandomObject(paramString, getCategories(), isOrdinalNotPareto(), isLess(), this);
		else
			return super.createRandomObject(baseType, paramString, oldParam);
	}
	
	private String getVarName() {
		return getStringParam("varName");
	}
	
	private Value[] getCategories() {
		return underscoreToSpaces(getStringParam("categories"));
	}
	
	private boolean isOrdinalNotPareto() {
		return getIntParam("ordinalPareto") == 0;
	}
	
	private boolean isLess() {
		return getIntParam("cutoffType") == 0;
	}
	
	private Value[] underscoreToSpaces(String labelString) {
		StringTokenizer st = new StringTokenizer(labelString);
		Value label[] = new Value[st.countTokens()];
		for (int i=0 ; i<label.length ; i++) {
			String oneLabelString = st.nextToken().replace('_', ' ');
			label[i] = new LabelValue(oneLabelString);
		}
		
		return label;
	}
	
	private int getAnswer() {
		if (getObjectParam("answer") == null)
			return -1;				//		Pareto ordering and default probs
		else
			return getIntParam("answer");
	}
	
	private int getLowCritical() {
		return isLess() ? 0 : ((CategoryParam)getObjectParam("cutoff")).intValue();
	}
	
	private int getHighCritical() {
		int nCats = new StringTokenizer(getStringParam("categories")).countTokens();
		return isLess() ? ((CategoryParam)getObjectParam("cutoff")).intValue() : (nCats - 1);
	}
	
	private double[] getDefaultProbs() {
		StringTokenizer st = new StringTokenizer(getStringParam("defaultProbs"));
		double probs[] = new double[st.countTokens()];
		for (int i=0 ; i<probs.length ; i++)
			probs[i] = Double.parseDouble(st.nextToken());
		return probs;
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 16));
		
			pieView = new PieHiliteView(data, this, "y", pieDrawer);
			pieView.setFont(getBigBoldFont());
		thePanel.add("Center", pieView);
		
			XPanel keyPanel = new XPanel();
			keyPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				
				keyView = new CatKey3View(data, this, "y");
//				keyView.setShowHeading(false);
				keyView.setReverseOrder();
				keyView.setFont(getBigFont());
			keyPanel.add(keyView);
		
		thePanel.add("West", keyPanel);
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
				XPanel innerPanel = new InsetPanel(25, 5);
				innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
					
					XLabel probLabel = new XLabel("Percentage is:", XLabel.LEFT, this);
					probLabel.setFont(getStandardBoldFont());
				innerPanel.add(probLabel);
					
					percentChoice = new PiePercentChoicePanel(this);
					percentChoice.setFont(getStandardBoldFont());
					registerStatusItem("percentChoice", percentChoice);
				innerPanel.add(percentChoice);
			
				innerPanel.lockBackground(kAnswerBackground);
			choicePanel.add(innerPanel);
			
		thePanel.add("South", choicePanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		permute(pieDrawer.getColorPerm());
		
		CatVariable yVar = null;
		int nCats = 0;
		
		yVar = (CatVariable)data.getVariable("y");
		nCats = yVar.noOfCategories();
		
		Color catColor[] = new Color[nCats];
		for (int i=0 ; i<nCats ; i++)
			catColor[i] = pieDrawer.getCatColor(i);
		keyView.setCatColour(catColor);
		
		pieView.clearHilite();
		
		int counts[] = yVar.getCounts();
		int total = yVar.noOfValues();
		int ansCount = 0;
		int lowIndex = getLowCritical();
		int highIndex = getHighCritical();
		for (int i=lowIndex ; i<=highIndex ; i++)
			ansCount += counts[i];
		int correctPercent = (ansCount * 100) / total;
		percentChoice.changeOptions(correctPercent);
		percentChoice.clearRadioButtons();
		
		data.variableChanged("y");
	}
	
	
	protected void setDataForQuestion() {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		yVar.name = getVarName();
		
		int[] count;
		Value[] catNames;
		if (isOrdinalNotPareto()) {
			catNames = getCategories();
			count = getOrdinalCounts();
		}
		else {
			Value rawCatNames[] = getCategories();
			int perm[] = createPermutation(rawCatNames.length);
			catNames = new Value[rawCatNames.length];
			for (int i=0 ; i<rawCatNames.length ; i++)
				catNames[i] = rawCatNames[perm[i]];
			count = getParetoCounts();
		}
		
		yVar.setLabels(catNames);
		yVar.setCounts(count);
	}
	
	private int[] getParetoCounts() {
		double[] probs = getDefaultProbs();
		randomMulti.setSampleSize(kNoOfValues);
		randomMulti.setProbs(probs);
		
		int lowIndex = getLowCritical();
		int highIndex = getHighCritical();
		
//		boolean goodCounts = false;
		int[] count = randomMulti.generate();
		sortCounts(count, 0, count.length);
		
		int actualCount = 0;
		for (int i=lowIndex ; i<=highIndex ; i++)
			actualCount += count[i];
		
		if (actualCount < (kNoOfValues * 15) / 100)
			random.setMinMax(5, 10);
		else if (actualCount < (kNoOfValues * 36) / 100)
			random.setMinMax(20, 30);
		else if (actualCount < (kNoOfValues * 64) / 100)
			random.setMinMax(42, 58);
		else if (actualCount < (kNoOfValues * 85) / 100)
			random.setMinMax(70, 80);
		else
			random.setMinMax(90, 95);
		
		int targetPercent = random.generateOne();
		int targetCount = (targetPercent * kNoOfValues) / 100;
		
//		System.out.println("lowIndex = " + lowIndex + ", highIndex = " + highIndex);
//		printIntArray("count[] = ", count);
//		System.out.println("\nactualCount = " + actualCount + ", targetCount = " + targetCount);
		
		while (actualCount != targetCount) {
			if (actualCount < targetCount) {
				probs = getTargetProbs(lowIndex, highIndex);
				boolean[] inNonTarget = new boolean[count.length];
				for (int i=0 ; i<count.length ; i++)
					inNonTarget[i] = (i < lowIndex || i > highIndex);
				moveCrosses(inNonTarget, probs, targetCount - actualCount, count);
				
//				printIntArray("count after move to target[] = ", count);
			}
			else {
				probs = getNonTargetProbs(lowIndex, highIndex);
				boolean[] inTarget = new boolean[count.length];
				for (int i=0 ; i<count.length ; i++)
					inTarget[i] = (i >= lowIndex && i <= highIndex);
				moveCrosses(inTarget, probs, actualCount - targetCount, count);
				
//				printIntArray("count after move from target[] = ", count);
			}
			
			sortCounts(count, 0, count.length);
				
//			printIntArray("count after sort[] = ", count);
				
			actualCount = 0;
			for (int i=lowIndex ; i<=highIndex ; i++)
				actualCount += count[i];
		}
		return count;
	}
	
	private void sortCounts(int[] count, int start, int endPlusOne) {
		for (int i=start+1 ; i<endPlusOne ; i++) 
			for (int j=i ; j>start ; j--)
				if (count[j] > count[j - 1]) {
					int temp = count[j];
					count[j] = count[j - 1];
					count[j - 1] = temp;
				}
				else
					break;
	}

/*	
	private void printIntArray(String label, int[] array) {
		System.out.print(label);
		for (int i=0 ; i<array.length ; i++)
			System.out.print(array[i] + " ");
		System.out.println("");
	}
	
	private void printDoubleArray(String label, double[] array) {
		System.out.print(label);
		for (int i=0 ; i<array.length ; i++)
			System.out.print(array[i] + " ");
		System.out.println("");
	}
	
	private void printBooleanArray(String label, boolean[] array) {
		System.out.print(label);
		for (int i=0 ; i<array.length ; i++)
			System.out.print(array[i] + " ");
		System.out.println("");
	}
*/
	
	private void moveCrosses(boolean[] inFrom, double[] toProbs, int nMoveValues, int[] count) {
//		printBooleanArray("Moving " + nMoveValues + " crosses from: ", inFrom);
//		printDoubleArray("To probs[] = ", toProbs);
		
		randomMulti.setSampleSize(nMoveValues);
		randomMulti.setProbs(toProbs);
		
		int extraCount[] = randomMulti.generate();
		for (int i=0 ; i<count.length ; i++)
			count[i] += extraCount[i];
		
//		printIntArray("count after adding extras[]: ", count);
		
		for (int i=0 ; i<nMoveValues ; i++) {
			int nFrom = 0;
			for (int j=0 ; j<count.length ; j++)
				if (inFrom[j])
					nFrom += count[j];
			random.setMinMax(0, nFrom - 1);
			int deleteIndex = random.generateOne();
			
			for (int j=0 ; j<count.length ; j++)
				if (inFrom[j]) {
					if (deleteIndex < count[j]) {
						count[j] --;
						break;
					}
					else
						deleteIndex -= count[j];
				}
		}
		
//		printIntArray("count after deleting[]: ", count);
	}
	
	private int[] getOrdinalCounts() {
		switch (getAnswer()) {
			case PROB_5_10:
				random.setMinMax(5, 10);
				break;
			case PROB_20_30:
				random.setMinMax(20, 30);
				break;
			case PROB_42_58:
				random.setMinMax(42, 58);
				break;
			case PROB_70_80:
				random.setMinMax(70, 80);
				break;
			case PROB_90_95:
				random.setMinMax(90, 95);
				break;
		}
		
		int targetPercent = random.generateOne();
		int targetCount = (targetPercent * kNoOfValues) / 100;
		
		int lowIndex = getLowCritical();
		int highIndex = getHighCritical();
		
		double[] probs = getNonTargetProbs(lowIndex, highIndex);
		randomMulti.setSampleSize(kNoOfValues - targetCount);
		randomMulti.setProbs(probs);
		
		int count[] = randomMulti.generate();		//	not in target
		
		probs = getTargetProbs(lowIndex, highIndex);
		randomMulti.setSampleSize(targetCount);
		randomMulti.setProbs(probs);
		
		int otherCount[] = randomMulti.generate();		//	in target
		
		for (int i=0 ; i<count.length ; i++)
			count[i] += otherCount[i];
		
		return count;
	}
	
	private double[] getNonTargetProbs(int lowIndex, int highIndex) {
		double[] probs = getDefaultProbs();
		double selectedProbs = 0.0;
		for (int i=lowIndex ; i<=highIndex ; i++)
			selectedProbs += probs[i];
		
		for (int i=0 ; i<probs.length ; i++)
			if (i < lowIndex || i > highIndex)
				probs[i] /= (1 - selectedProbs);
			else
				probs[i] = 0;
		return probs;
	}
	
	private double[] getTargetProbs(int lowIndex, int highIndex) {
		double[] probs = getDefaultProbs();
		double selectedProbs = 0.0;
		for (int i=lowIndex ; i<=highIndex ; i++)
			selectedProbs += probs[i];
		
		for (int i=0 ; i<probs.length ; i++)
			if (i < lowIndex || i > highIndex)
				probs[i] = 0;
			else
				probs[i] /= selectedProbs;
		return probs;
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("What is the correct probability?");
				break;
			case ANS_INCOMPLETE:
			case ANS_INVALID:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText(percentChoice.getSelectedOptionMessage());
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText(percentChoice.getSelectedOptionMessage());
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("Yes. " + percentChoice.getSelectedOptionMessage());
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				int correct = percentChoice.getCorrectPercent();
				int attempt = percentChoice.getSelectedPercent();
				if (correct < attempt)
					messagePanel.insertRedText("No. The categories span much less than " + attempt + "% of the pie chart's area.");
				else
					messagePanel.insertRedText("No. The categories span much more than " + attempt + "% of the pie chart's area.");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 100;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			CatVariable yVar = new CatVariable("", true);
		data.addVariable("y", yVar);
		
		return data;
	}
	
	public void setFixedQuestionSeed(long seed) {
		super.setFixedQuestionSeed(seed);
		random.setSeed(nextSeed());
		randomMulti.setSeed(nextSeed());
		percentChoice.setRandomSeed(nextSeed());
	}
	
//-----------------------------------------------------------
	
	public boolean noteChangedWorking() {
		boolean changed = super.noteChangedWorking();
		if (changed) {
			pieView.clearHilite();
			pieView.repaint();
		}
		return changed;
	}
	
	protected int assessAnswer() {
		return percentChoice.checkCorrect();
	}
	
	protected void giveFeedback() {
		if (result == ANS_CORRECT || result == ANS_WRONG) {
			pieView.setHilite(getLowCritical(), getHighCritical(), result == ANS_CORRECT);
			pieView.repaint();
		}
	}
	
	protected void showCorrectWorking() {
		percentChoice.showAnswer();
		
		pieView.setHilite(getLowCritical(), getHighCritical(), true);
		pieView.repaint();
	}
	
	protected double getMark() {
		return (assessAnswer() == ANS_CORRECT) ? 1 : 0;
	}
	
}