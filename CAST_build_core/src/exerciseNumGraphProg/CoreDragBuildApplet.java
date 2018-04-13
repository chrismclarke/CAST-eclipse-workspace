package exerciseNumGraphProg;

import java.awt.*;

import dataView.*;
import utils.*;
import coreVariables.*;
import random.*;
import exercise2.*;
import valueList.*;

import exerciseNumGraph.*;


abstract public class CoreDragBuildApplet extends ExerciseApplet {
	protected ValueUsage valuesUsed = new ValueUsage();
	
	private RandomNormal generator;
	
	protected ScrollValueList theList;
	protected DataView theView;
	
//================================================
	
	protected void createDisplay() {
		valuesUsed = new ValueUsage();
		registerStatusItem("usage", valuesUsed);
		
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
		
		valuesUsed.setListAndView(data, theList, theView);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
//		registerParameter("index", "int");					//	always registered
		registerParameter("mean", "const");
		registerParameter("sd", "const");
		registerParameter("count", "int");
		registerParameter("axis", "string");
		registerParameter("varName", "string");
		registerParameter("decimals", "int");
	}
	
	protected NumValue getMean() {
		return getNumValueParam("mean");
	}
	
	protected NumValue getSD() {
		return getNumValueParam("sd");
	}
	
	protected int getCount() {
		return getIntParam("count");
	}
	
	protected String getAxisInfo() {
		return getStringParam("axis");
	}
	
	protected int getDecimals() {
		return getIntParam("decimals");
	}
	
	protected String getVarName() {
		return getStringParam("varName");
	}
	
	
//-----------------------------------------------------------
	
	protected void setDisplayForQuestion() {
		valuesUsed.initialise();
	}
	
	protected void setDataForQuestion() {
		int n = getCount();
		NumSampleVariable coreVar = (NumSampleVariable)data.getVariable("base");
		coreVar.setSampleSize(n);
		coreVar.generateNextSample();
		
		ScaledVariable yVar = (ScaledVariable)data.getVariable("y");
		yVar.setScale(getMean().toDouble(), getSD().toDouble(), getDecimals());
		yVar.name = getVarName();
		
		data.variableChanged("y");
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			generator = new RandomNormal(10, 0.0, 1.0, 2.0);		//	+/- 2 SD
			generator.setSeed(nextSeed());
			NumSampleVariable baseVar = new NumSampleVariable("Std normal", generator, 9);
			baseVar.generateNextSample();
		data.addVariable("base", baseVar);
		
			ScaledVariable yVar = new ScaledVariable("", baseVar, "base", 0.0, 1.0, 9);
			yVar.setRoundValues(true);
		data.addVariable("y", yVar);
		
		return data;
	}
	
	
//-----------------------------------------------------------
	
	abstract protected boolean[] inWrongStack();
	abstract protected void showCorrectCrosses();
	
	protected boolean[] inWrongPositions() {
		return null;
	}
	
	protected boolean completedCrosses() {
		boolean[] alreadyUsed = valuesUsed.getUsage();
		for (int i=0 ; i<alreadyUsed.length ; i++)
			if (!alreadyUsed[i])
				return false;
		return true;
	}
	
	protected int assessAnswer() {
		if (!completedCrosses())
			return ANS_INCOMPLETE;
		else {
			boolean wrongCross[] = inWrongStack();
			boolean anyWrong = false;
			for (int i=0 ; i<wrongCross.length ; i++)
				anyWrong = anyWrong || wrongCross[i];
			
			if (anyWrong)
				return ANS_WRONG;
			else {
				boolean wrongPosition[] = inWrongPositions();
				if (wrongPosition == null)
					return ANS_CORRECT;
				else {
					anyWrong = false;
					for (int i=0 ; i<wrongCross.length ; i++)
						anyWrong = anyWrong || wrongPosition[i];
					
					return anyWrong ? ANS_CLOSE : ANS_CORRECT;
				}
			}
		}
	}
	
	protected void giveFeedback() {
		if (result == ANS_WRONG) {
			boolean wrongCross[] = inWrongStack();
			data.setSelection(wrongCross);
		}
		else if (result == ANS_CLOSE) {
			boolean wrongPosition[] = inWrongPositions();
			data.setSelection(wrongPosition);
		}
	}
	
	protected void showCorrectWorking() {
		data.clearSelection();
		
		valuesUsed.setAllUsed();
		showCorrectCrosses();
		
		data.variableChanged("base");
	}
	
	protected double getMark() {
		int ans = assessAnswer();
		if (ans == ANS_CORRECT)
			return 1;
		else if (ans == ANS_WRONG) {
			boolean wrongCross[] = inWrongStack();
			int nWrong = 0;
			for (int i=0 ; i<wrongCross.length ; i++)
				if (wrongCross[i])
					nWrong ++;
			if (nWrong == 1)
				return 0.7;
			else
				return 0;
		}
		else if (ans == ANS_CLOSE)
			return 0.8;
		else
			return 0;
	}
}