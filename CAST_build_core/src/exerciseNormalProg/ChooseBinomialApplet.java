package exerciseNormalProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import distn.*;
import random.*;
import exercise2.*;

import exerciseNormal.*;


public class ChooseBinomialApplet extends ExerciseApplet {
	static final private double kMinCum = 0.001;
	
	private String distnKey[];
	
	private BinomDistnChoicePanel binomDistnChoice;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 8));
		
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
		registerParameter("successes", "string");
		registerParameter("failures", "string");
		registerParameter("trials", "string");
		registerParameter("nTrials", "int");
		registerParameter("pSuccess", "const");
		registerParameter("cut-off", "cut-off");
		registerParameter("succ-fail", "string");
	}
	
	protected void addTypeDelimiters() {
		addType("cut-off", ",");
	}
	
	protected Object createConstObject(String baseType, String valueString) {
		if (baseType.equals("cut-off"))
			return Integer.valueOf(valueString);
		else
			return super.createConstObject(baseType, valueString);
	}
	
	protected Object createRandomObject(String baseType, String paramString, Object oldParam) {
		if (baseType.equals("cut-off")) {
			double minCum = kMinCum;
			double maxCum = 1 - kMinCum;
			
			int total = getNTrials();
			BinomialDistnVariable tempBin = new BinomialDistnVariable("");
			tempBin.setCount(total);
			tempBin.setProb(getPSuccess());
			double cum = 0;
			int xMin = -1;
			while (cum < minCum) {
				xMin ++;
				cum += tempBin.getScaledProb(xMin);
			}
			cum = 1;
			int xMax = total + 1;
			while (cum > maxCum) {
				xMax --;
				cum -= tempBin.getScaledProb(xMax);
			}
			
			RandomInteger generator = new RandomInteger(xMin, xMax, 1, nextSeed());
			int x;
			do {
				x = generator.generateOne();
			} while (x < 2);
			return Integer.valueOf(x);
		}
		else
			return super.createRandomObject(baseType, paramString, oldParam);
	}
	
	private String getSuccessesName() {
		return getStringParam("successes");
	}
	
	private String getFailuresName() {
		return getStringParam("failures");
	}
	
	private String getSuccFailName() {
		return getStringParam("succ-fail");
	}
	
	private String getTrialsName() {
		return getStringParam("trials");
	}
	
	private int getNTrials() {
		return getIntParam("nTrials");
	}
	
	private double getPSuccess() {
		double p = getDoubleParam("pSuccess");
		return isSuccNotFail() ? p : (1 - p);
	}
	
	private int getCutoff() {
		return getIntParam("cut-off");
	}
	
	private boolean isSuccNotFail() {
		String succFailName = getStringParam("succ-fail");
		return succFailName.equals(getSuccessesName());
	}
	
	
//-----------------------------------------------------------
	
	private String[] getAxisInfo() {
		String correctAxisInfo = getAxisInfo(getNTrials());
		int nDistns = distnKey.length;
		String axisInfo[] = new String[nDistns];
		axisInfo[0] = axisInfo[1] = axisInfo[2] = axisInfo[3] = correctAxisInfo;
		
		if (nDistns > 4) {			//	not generic version
			String shortAxisInfo = getAxisInfo(getCutoff());
			axisInfo[4] = axisInfo[5] = shortAxisInfo;
		}
		
		return axisInfo;
	}
	
	private String getAxisInfo(int n) {
		int step = (n <= 7) ? 1 : (n <= 12) ? 2 : (n <= 25) ? 5 : (n <= 60) ? 10 : (n <= 100) ? 20 : (n <= 250) ? 50 : 100;
		String axisInfo = "-0.5 " + n + ".5 0 " + step;
		if (n % step > 1)
			axisInfo += " " + n;
		return axisInfo;
	}
	
	protected XPanel getWorkingPanels(DataSet data) {
		binomDistnChoice = new BinomDistnChoicePanel(this, data, distnKey, getAxisInfo());
		registerStatusItem("binomDistnChoice", binomDistnChoice);
		
		return binomDistnChoice;
	}
	
	protected void setDisplayForQuestion() {
		if (isSuccNotFail())
			binomDistnChoice.changeOptions(getAxisInfo(), getSuccessesName(), getFailuresName(), getTrialsName());
		else
			binomDistnChoice.changeOptions(getAxisInfo(), getFailuresName(), getSuccessesName(), getTrialsName());
		binomDistnChoice.clearRadioButtons();
		
		if (data != null)
			for (int i=0 ; i<distnKey.length ; i++)
				data.variableChanged(distnKey[i]);
	}
	
	protected void setDataForQuestion() {
		int n = getNTrials();
		double p = getPSuccess();
		String varName = "Number of " + (isSuccNotFail() ? getSuccessesName() : getFailuresName());
		Random rand01 = new Random(nextSeed());
		
		BinomialDistnVariable distn = (BinomialDistnVariable)data.getVariable(distnKey[0]);
		distn.name = varName;
		distn.setCount(n);
		distn.setProb(p);
		
		distn = (BinomialDistnVariable)data.getVariable(distnKey[1]);
		distn.name = varName;
		distn.setCount(n);
		distn.setProb(1 - p);
		
		if (distnKey.length > 4) {
			int x = getCutoff();
			distn = (BinomialDistnVariable)data.getVariable(distnKey[4]);
			distn.name = varName;
			distn.setCount(x);
			distn.setProb(p);
			
			distn = (BinomialDistnVariable)data.getVariable(distnKey[5]);
			distn.name = varName;
			distn.setCount(x);
			distn.setProb(1 - p);
		}
		
		p = Math.min(p, 1 - p);
		if (p > 0.25)
			p = 0.05 + rand01.nextDouble() * 0.15;
		else
		 p = 0.35 + rand01.nextDouble() * 0.1;
		p = Math.rint(p * 100) * 0.01;		//	round to 2 decimals
		
		distn = (BinomialDistnVariable)data.getVariable(distnKey[2]);
		distn.name = varName;
		distn.setCount(n);
		distn.setProb(p);
		
		distn = (BinomialDistnVariable)data.getVariable(distnKey[3]);
		distn.name = varName;
		distn.setCount(n);
		distn.setProb(1 - p);
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		int nDistns = hasOption("correctN") ? 4 : 6;
		distnKey = new String[nDistns];
		
		for (int i=0 ; i<nDistns ; i++) {
			distnKey[i] = "y" + i;
			data.addVariable(distnKey[i], new BinomialDistnVariable(""));
		}
		
		return data;
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("One of the four bar charts above describes the distribution of the number of "
														+ getSuccFailName() + ". Pick the correct bar chart.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error\n");
				messagePanel.insertText("You must select one of the options given for the bar chart. ");
				messagePanel.insertBoldText("(Click a radio button.)");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("This is the answer.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have picked the correct bar chart.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText(binomDistnChoice.getSelectedOptionMessage());
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 100;
	}
	
	
//-----------------------------------------------------------

	
	protected int assessAnswer() {
		return binomDistnChoice.checkCorrect();
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		binomDistnChoice.showAnswer();
	}
	
	protected double getMark() {
		return (binomDistnChoice.checkCorrect() == ANS_CORRECT) ? 1 : 0;
	}
	
}