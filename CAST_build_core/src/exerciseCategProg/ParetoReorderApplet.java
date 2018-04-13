package exerciseCategProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import exercise2.*;

import exerciseCateg.*;


public class ParetoReorderApplet extends CoreFindPropnApplet {
	static final public int ANS_WRONG_PROPN = ANS_WRONG;
	static final public int ANS_WRONG_PARETO = ANS_WRONG + 1;
	
	
	static final private String kCumAxisString = "0 1.05 0 0.1";
	
	private VertAxis propnAxis, cumAxis;
	private HorizAxis valAxis;
	private ParetoReorderView barView;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(data));
				
		add("South", getBottomPanel());
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("probs", "string");
		registerParameter("sampleSize", "int");
		registerParameter("propnAxis", "string");
	}
	
	private double[] getProbs() {
		if (getObjectParam("probs") == null)
			return null;
			
		StringTokenizer st = new StringTokenizer(getStringParam("probs"));
		double probs[] = new double[st.countTokens()];
		for (int i=0 ; i<probs.length ; i++)
			probs[i] = Double.parseDouble(st.nextToken());
		
		return probs;
	}
	
	private int getSampleSize() {
		return getIntParam("sampleSize");
	}
	
	private String getPropnAxis() {
		return getStringParam("propnAxis");
	}
	
	
//-----------------------------------------------------------
	
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel labelPanel = new XPanel();
			labelPanel.setLayout(new BorderLayout());
			
			labelPanel.add("West", new XLabel(translate("Proportion"), XLabel.LEFT, this));
			labelPanel.add("Center", new XPanel());
			labelPanel.add("East", new XLabel("Cumulative propn", XLabel.RIGHT, this));
			
		thePanel.add("North", labelPanel);
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
			
				valAxis = new HorizAxis(this);
			mainPanel.add("Bottom", valAxis);
			
				propnAxis = new VertAxis(this);
			mainPanel.add("Left", propnAxis);
			
				cumAxis = new VertAxis(this);
				cumAxis.readNumLabels(kCumAxisString);
			mainPanel.add("Right", cumAxis);
				
				barView = new ParetoReorderView(data, this, "y", valAxis, propnAxis, cumAxis);
				barView.lockBackground(Color.white);
				registerStatusItem("barPerm", barView);
				
			mainPanel.add("Center", barView);
				
		thePanel.add("Center", mainPanel);
		
		thePanel.add("South", getPropnTemplatePanel());
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		propnAxis.readNumLabels(getPropnAxis());
		
		CatVariable yVar = (CatVariable)data.getVariable("y");
		int perm[] = createPermutation(yVar.noOfCategories());
		barView.setPermutation(perm);
		
		permuteAxisLabels((CatVariable)data.getVariable("y"), perm);
		
		super.setDisplayForQuestion();
	}
	
	public void permuteAxisLabels(CatVariable yVar, int[] perm) {
		Vector labels = valAxis.getLabels();
		labels.removeAllElements();
		int nCats = yVar.noOfCategories();
		valAxis.noOfCats = nCats;
		valAxis.setAxisName(yVar.name);
		
		for (int i=0 ; i<nCats ; i++) {
			Value catLabel = yVar.getLabel(perm[i]);
			AxisLabel nextAxisLabel = new AxisLabel(catLabel, (i + 0.5) / nCats);
			labels.addElement(nextAxisLabel);
		}
		
		valAxis.resetLabelSizes();
		valAxis.invalidate();
		valAxis.repaint();
	}
	
	
	protected void setDataForQuestion() {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		yVar.name = getVarName();
		yVar.setLabels(getCategories());
		
		double probs[] = getProbs();
		int n = getSampleSize();
		RandomMultinomial generator = new RandomMultinomial(n, probs);
		generator.setSeed(nextSeed());
		
		StringTokenizer st = new StringTokenizer(getPropnAxis());
		st.nextToken();
		double maxAllowed = Double.parseDouble(st.nextToken()) * n;
		int maxCount;
		int iter = 0;
		int counts[];
		do {
			counts = generator.generate();
			maxCount = 0;
			for (int i=0 ; i<counts.length ; i++)
				maxCount = Math.max(maxCount, counts[i]);
			iter ++;
		} while (maxCount > maxAllowed && iter < 10);
		
		yVar.setCounts(counts);
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		int questionType = getQuestionType();
		switch (result) {
			case ANS_INCOMPLETE:
			case ANS_INVALID:
				super.insertMessageContent(messagePanel);
				break;
			case ANS_UNCHECKED:
				messagePanel.insertText("Drag the bars to reorder them into a Pareto diagram.\nThen find the requested proportion and type it in the text-edit box.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The categories have now been arranged in decreasing order of importance (height).");
				int criticalIndex = getCriticalIndex();
				int nLess = getCorrectCount(LESS_EQUALS_CHOICE);
				int nTotal = getNValues();
				messagePanel.insertText("\nThe cumulative proportion for the most common " + (criticalIndex + 1) + " categories is " + nLess + "/" + nTotal + ".");
				if (questionType == GREATER_THAN_CHOICE) {
					CatVariable yVar = (CatVariable)data.getVariable("y");
					int nCats = yVar.noOfCategories();
					messagePanel.insertText(" The cumulative proportion for the ");
					messagePanel.insertBoldText("least");
					messagePanel.insertText(" common " + (nCats - criticalIndex - 1) + " categories is therefore (" + nTotal + "-" + nLess + ")/" + nTotal + ".");
				}
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly formed the Pareto diagram and evaluated the " + (isPercent() ? "percentage" : "proportion") + ".");
				break;
			case ANS_WRONG_PARETO:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText("Some bars are higher than the bars to the left, so this is not the correct Pareto diagram.");
				break;
			case ANS_WRONG_PROPN:
				messagePanel.insertRedHeading("Wrong!\n");
				double attempt = getAttempt();
				double correctPropn = getCorrectCount(questionType) / (double)getNValues();
				if (!showPercentPropnError(messagePanel, attempt, correctPropn))
					messagePanel.insertRedText("You have drawn the correct Pareto diagram but have not calculated the correct " + (isPercent() ? "percentage" : "proportion") + ".");
				break;
			case ANS_CLOSE:
				super.insertMessageContent(messagePanel);
				messagePanel.insertText("\n(Your Pareto diagram is correct.)");
				break;
		}
	}
	
	protected String getCategoriesString() { return null; }	
	protected boolean usesCumulative() { return true; }
	
	protected int getMessageHeight() {
		return 140;
	}
	
//-----------------------------------------------------------
	
	protected int[] getDataCounts() {
		return barView.getCounts();
	}
	
	protected void selectCorrectCounts() {
		barView.sortIntoOrder(getCriticalIndex());
	}
	
	protected int assessAnswer() {
		int counts[] = getDataCounts();
		for (int i=1 ; i<counts.length ; i++)
			if (counts[i] > counts[i - 1])
				return ANS_WRONG_PARETO;
		
		return checkPropn();
	}
	
	protected double getMark() {
		int ans = assessAnswer();
		return (ans == ANS_CORRECT) ? 1 : (ans == ANS_CLOSE) ? 0.8  : (ans == ANS_WRONG_PROPN) ? 0.5 : 0;
	}
	
}