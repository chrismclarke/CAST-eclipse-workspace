package exerciseCategProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;
import exercise2.*;
import valueList.*;

import exerciseCateg.*;


public class BuildContinTableApplet extends ExerciseApplet {
	static final private Color kVariableLabelColor = new Color(0x000099);
	
	private CatValueScroll2List theList;
	
	private BuildContinTableView theTable;
	private XLabel xNameLabel;
	private XVertLabel yNameLabel;
	
	private XButton resetButton, backButton;
	
	
	public void setupApplet() {
		super.setupApplet();
		if (theTable.getCurrentTotal() > 0) {
			resetButton.enable();
			backButton.enable();
		}
	}
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
					
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new BorderLayout(0, 10));
		
				theList = createList(data);
				registerStatusItem("listUsed", theList);
			mainPanel.add("West", theList);
			
			mainPanel.add("Center", getWorkingPanels(data));
			
		add("Center", mainPanel);
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(0, 4));
			
			bottomPanel.add("North", createMarkingPanel(NO_HINTS));
			
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add("Center", messagePanel);
		
		add("South", bottomPanel);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
//		registerParameter("index", "int");					//	always registered
		registerParameter("xVarName", "string");
		registerParameter("xCategories", "string");
		registerParameter("yVarName", "string");
		registerParameter("yCategories", "string");
		registerParameter("jointProbs", "string");
		registerParameter("sampleSize", "int");
	}
	
	private String getXVarName() {
		return getStringParam("xVarName");
	}
	
	private Value[] getXCategories() {
		return underscoreToSpaces(getStringParam("xCategories"));
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
	
	private String getYVarName() {
		return getStringParam("yVarName");
	}
	
	private Value[] getYCategories() {
		return underscoreToSpaces(getStringParam("yCategories"));
	}
	
	private double[] getJointProbs() {		//		order x1y1 x1y2 ... x1yn x2y1 ...
		StringTokenizer st = new StringTokenizer(getStringParam("jointProbs"));
		double probs[] = new double[st.countTokens()];
		for (int i=0 ; i<probs.length ; i++)
			probs[i] = Double.parseDouble(st.nextToken());
		
		return probs;
	}
	
	private int getSampleSize() {
		return getIntParam("sampleSize");
	}

	
//-----------------------------------------------------------
	
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
		
			XPanel tablePanel = new XPanel();
			tablePanel.setLayout(new BorderLayout(6, 0));
			
				XPanel xNamePanel = new InsetPanel(30, 0, 0, 0);
				xNamePanel.setLayout(new BorderLayout(0, 0));
			
					xNameLabel = new XLabel("", XLabel.CENTER, this);
					xNameLabel.setFont(getStandardBoldFont());
					xNameLabel.setForeground(kVariableLabelColor);
				xNamePanel.add("Center", xNameLabel);
				
			tablePanel.add("North", xNamePanel);
			
				yNameLabel = new XVertLabel("", XLabel.CENTER, this);
				yNameLabel.setFont(getStandardBoldFont());
				yNameLabel.setForeground(kVariableLabelColor);
			tablePanel.add("West", yNameLabel);
			
				theTable = new BuildContinTableView(data, this, "y", "x", theList);
				registerStatusItem("continTable", theTable);
			tablePanel.add("Center", theTable);
			
		thePanel.add(tablePanel);
		
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			
				resetButton = new XButton("Start Again", this);
				resetButton.disable();
			buttonPanel.add(resetButton);
			
				backButton = new XButton("Back One Row", this);
				backButton.disable();
			buttonPanel.add(backButton);
		
		thePanel.add(buttonPanel);
		
		return thePanel;
	}
	
	private CatValueScroll2List createList(DataSet data) {
		CatValueScroll2List list = new CatValueScroll2List(data, this, ScrollValueList.HEADING);
		
		list.addVariableToList("x", ScrollValueList.RAW_VALUE);
		list.addVariableToList("y", ScrollValueList.RAW_VALUE);
		
		list.setSelectedCols(0, 1);
		
		return list;
	}
	
	protected void setDisplayForQuestion() {
		theList.resetList();
		
		theTable.resetLayout();
		theTable.resetCounts();
		
		xNameLabel.setText(data.getVariable("x").name);
		yNameLabel.setText(data.getVariable("y").name);
		
		resetButton.disable();
		backButton.disable();
	}
	
	
	protected void setDataForQuestion() {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		yVar.name = getYVarName();
		yVar.setLabels(getYCategories());
		int ny = yVar.noOfCategories();
		
		CatVariable xVar = (CatVariable)data.getVariable("x");
		xVar.name = getXVarName();
		xVar.setLabels(getXCategories());
		int nx = xVar.noOfCategories();
		
		double jointProbs[] = getJointProbs();
		if (jointProbs.length != nx * ny)
			throw new RuntimeException("Joint probs are wrong length : should be " + (nx * ny) + ".");
		int n = getSampleSize();
		RandomMultinomial generator = new RandomMultinomial(n, jointProbs);
		generator.setSeed(nextSeed());
		
		int jointCounts[] = generator.generate();
		
		int yCat[] = new int[n];
		int xCat[] = new int[n];
		
		int index = 0;
		for (int i=0 ; i<jointCounts.length ; i++) {
			int y = i % ny;
			int x = i / ny;
			for (int j=0 ; j<jointCounts[i] ; j++) {
				yCat[index] = y;
				xCat[index ++] = x;
			}
		}
		
		int perm[] = createPermutation(n);
		int xPermCat[] = new int[n];
		int yPermCat[] = new int[n];
		for (int i=0 ; i<n ; i++) {
			xPermCat[i] = xCat[perm[i]];
			yPermCat[i] = yCat[perm[i]];
		}
		
		yVar.setValues(yPermCat);
		xVar.setValues(xPermCat);
		data.variableChanged("x");		//	needed to reset data.selection to correct length
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Incomplete\n");
				CatVariable xVar = (CatVariable)data.getVariable("x");
				messagePanel.insertRedText("There are " + xVar.noOfValues() + " rows in the list, but you have only clicked on the table " + theList.numberCompleted() + " times.\n(The total for the contingency table only shows " + theList.numberCompleted() + ").");
				break;
			case ANS_UNCHECKED:
				messagePanel.insertText("Each click in a cell of the contingency table adds one to its count.\n(Since there are " + getSampleSize() + " individuals, " + getSampleSize() + " clicks will complete the contingency table.)");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The contingency table has been completed above.\n");
				insertExampleExplanation(messagePanel, false);
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly completed the contingency table.\n");
				insertExampleExplanation(messagePanel, true);
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText("Two or more counts in the table are wrong. ");
				insertOneBadCellMessage(messagePanel);
				messagePanel.insertText("\nClick ");
				messagePanel.insertBoldText("Start Again");
				messagePanel.insertText(" then work down the list on the left, clicking in the appropriate cell of the table for each highlighted row.");
				break;
		}
	}
	
	private void insertExampleExplanation(MessagePanel messagePanel, boolean correctNotTold) {
		CatVariable xVar = (CatVariable)data.getVariable("x");
		CatVariable yVar = (CatVariable)data.getVariable("y");
		int[][] counts = xVar.getCounts(yVar);
		for (int i=0 ; i<counts.length ; i++)
			for (int j=0 ; j<counts[i].length ; j++)
				if (counts[i][j] > 0) {
					String cellName = "(" + xVar.getLabel(i) + ", " + yVar.getLabel(j) + ")";
					messagePanel.insertText("For example, " + cellName + " occurs in ");
					if (counts[i][j] == 1)
						messagePanel.insertText("exactly one row");
					else
						messagePanel.insertText(counts[i][j] + " different rows");
					if (correctNotTold)
						messagePanel.insertText(" of the list, so the cell " + cellName + " shows a count of " + counts[i][j] + ".");
					else
						messagePanel.insertText(" of the list, so you should have clicked the cell " + cellName + " in the table this number of times when working down the list. The cell therefore shows a count of " + counts[i][j] + ".");
					return;
				}
	}
	
	private void insertOneBadCellMessage(MessagePanel messagePanel) {
		CatVariable xVar = (CatVariable)data.getVariable("x");
		CatVariable yVar = (CatVariable)data.getVariable("y");
		int[][] correctCounts = xVar.getCounts(yVar);
		int[][] tableCounts = theTable.getCounts();
		for (int i=0 ; i<correctCounts.length ; i++)
			for (int j=0 ; j<correctCounts[i].length ; j++)
				if (correctCounts[i][j] != tableCounts[i][j]) {
					String cellName = "(" + xVar.getLabel(i) + ", " + yVar.getLabel(j) + ")";
					messagePanel.insertRedText("For example, " + cellName + " occurs in ");
					if (correctCounts[i][j] == 1)
						messagePanel.insertRedText("exactly one row");
					else
						messagePanel.insertRedText(correctCounts[i][j] + " different rows");
					messagePanel.insertRedText(" of the list, but the corresponding cell in the contingency table shows a count of " + tableCounts[i][j] + ".");
					theTable.setSelection(i, j);
					return;
				}
	}
	
	protected int getMessageHeight() {
		return 150;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			CatVariable yVar = new CatVariable("");
		data.addVariable("y", yVar);
		
			CatVariable xVar = new CatVariable("");
		data.addVariable("x", xVar);
		
		return data;
	}
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		return theTable.checkCounts();
	}
	
	protected void giveFeedback() {
		if (result == ANS_INCOMPLETE && theTable.getCurrentTotal() == 0) {
			resetButton.disable();
			backButton.disable();
		}
		else {
			resetButton.enable();
			backButton.enable();
		}
	}
	
	protected void showCorrectWorking() {
		theList.completeTable();
		scrollListToHighlight();
		theTable.completeTable();
		
		resetButton.enable();
		backButton.enable();
	}
	
	protected double getMark() {
		return (assessAnswer() == ANS_CORRECT) ? 1 : 0;
	}
	
//-------------------------------------------------------------------------
	
	private void scrollListToHighlight() {
		CatVariable xVar = (CatVariable)data.getVariable("x");
		int nValues = xVar.noOfValues();
		data.setSelection(Math.min(theList.numberCompleted(), nValues - 1));
	}
	
	public void moveToNextValue() {
		theList.selectNextValue();
		scrollListToHighlight();
		resetButton.enable();
		backButton.enable();
		noteChangedWorking();
	}
	
	private boolean localAction(Object target) {
		if (target == backButton) {
			theList.selectPreviousValue();
			scrollListToHighlight();
			theTable.backOneValue();
			noteChangedWorking();
			if (theTable.getCurrentTotal() == 0) {
				resetButton.disable();
				backButton.disable();
			}
			return true;
		}
		else if (target == resetButton) {
			theList.resetList();
			scrollListToHighlight();
			theTable.resetCounts();
			noteChangedWorking();
			resetButton.disable();
			backButton.disable();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}