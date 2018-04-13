package exercisePercentProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import exercise2.*;
import formula.*;

import percentile.*;
import exercisePercent.*;


public class CumulativeRateApplet extends PercentRateApplet {
	private PropnWorkingPanel propnWorkingPanel;
	
	private RefDataSet refData;
	
//================================================

	protected void createDisplay() {
		refData = new RefDataSet(data);
		registerStatusItem("refValue", refData);
		
		setLayout(new BorderLayout(0, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 5));
			
				questionPanel = new QuestionPanel(this);
			topPanel.add(questionPanel);
				
		add("North", topPanel);
			
		add("Center", getWorkingPanels(data, refData));
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 10));
				
				XPanel answerPanel = new XPanel();
				answerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
					XLabel answerLabel = new XLabel("Answer:", XLabel.LEFT, this);
					answerLabel.setFont(getStandardBoldFont());
					answerLabel.setForeground(kAnswerLabelColor);
				answerPanel.add(answerLabel);
				
					resultPanel = new ResultValuePanel(this, "", "", 6);
					registerStatusItem("answer", resultPanel);
				answerPanel.add(resultPanel);
				
			bottomPanel.add(answerPanel);
			
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
		super.registerParameterTypes();
		registerParameter("cutoff", "const");
		registerParameter("maxCutoff", "const");
		registerParameter("axis", "string");
		registerParameter("shortVarName", "string");
		registerParameter("longVarName", "string");
		registerParameter("directionName", "string");
		registerParameter("data", "string");
	}
	
	private NumValue getCutoff() {
		return getNumValueParam("cutoff");
	}
	
	private NumValue getMaxCutoff() {
		return getNumValueParam("maxCutoff");
	}
	
	private String getAxisInfo() {
		return getStringParam("axis");
	}
	
	private String getShortVarName() {
		return getStringParam("shortVarName");
	}
	
	private String getLongVarName() {
		return getStringParam("longVarName");
	}
	
	private int getDirection() {			//		0 = less, 1 = more
		String directionString = getStringParam("directionName");
		return (directionString.equals("less") || directionString.equals("before")) ? 0 : 1;
	}
	
	private String getDataValues() {
		return getStringParam("data");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data, DataSet refData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			propnWorkingPanel = new PropnWorkingPanel(data, refData, this, getAxisInfo(), getLongVarName(), getMaxCutoff());
		thePanel.add("Center", propnWorkingPanel);
			
		if (hasOption("template")) {
			FormulaContext stdContext = new FormulaContext(kTemplateColor, getStandardFont(), this);
			multTemplate = new MultTemplatePanel(stdContext);
			multTemplate.lockBackground(kTemplateBackground);
			
			thePanel.add("South", multTemplate);
		}
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		propnWorkingPanel.changeData(getAxisInfo(), getLongVarName(), getMaxCutoff());
		
		if (multTemplate != null)
			multTemplate.setValues(kOneValue, kOneValue, kOneValue);
		
		String label = null;
		String units = null;
		String eventString = getShortVarName() + ((getDirection() == 0) ? " less than " : " greater than ");
		eventString += getCutoff();
		switch (getQuestionType()) {
			case PROPN:
				label = "Proportion of " + getTrialName() + "s with " + eventString + " is";
				break;
			case PERCENT:
				label = "Percentage of " + getTrialName() + "s with " + eventString + " is";
				units = "%";
				break;
			case RATE:
				label = "Rate of " + getTrialName() + "s with " + eventString + " is";
				units = getTrialName() + "s per " + getTargetTrials() + " " + getTrialName() + "s";
				break;
			case RETURN_PERIOD:
				label = "A " + getTrialName() + " with " + eventString + " is called a";
				units = "-" + getTrialName() + " event";
				break;
		}
		resultPanel.changeLabel(label);
		resultPanel.changeUnits(units);
		resultPanel.clear();
		resultPanel.invalidate();
		
		resultPanel.clear();
		
		data.variableChanged("y");
		refData.changedRefValue();
		
		validate();
	}
	
	protected void setDataForQuestion() {
		NumValue cutoff = getCutoff();
		StringTokenizer st = new StringTokenizer(getAxisInfo());
		double axisMin = Double.parseDouble(st.nextToken());
		double axisMax = Double.parseDouble(st.nextToken());
		refData.setRefValue((axisMax + axisMin) / 2, cutoff.decimals);
		
		NumVariable yVar = (NumVariable)data.getVariable("y");
		yVar.readValues(getDataValues());
		yVar.name = getShortVarName();
	}
	
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
			NumVariable yVar = new NumVariable("");
		data.addVariable("y", yVar);
		return data;
	}
	
	
//-----------------------------------------------------------
	
	protected int getNTrials() {
		NumVariable yVar = (NumVariable)data.getVariable("y");
		return yVar.noOfValues();
	}
	
	protected int getNSuccess() {
		double cutoff = getCutoff().toDouble();
		NumVariable yVar = (NumVariable)data.getVariable("y");
		ValueEnumeration ye = yVar.values();
		int nLess = 0;
		int n = 0;
		while (ye.hasMoreValues()) {
			if (ye.nextDouble() <= cutoff)
				nLess ++;
				n ++;
		}
		return (getDirection() == 0) ? nLess : (n - nLess);
	}
	
	protected String getSuccessName() {
		return getTrialName() + "s with " +  getShortVarName()
														+ ((getDirection() == 0) ? " less than " : " greater than ") + getCutoff();
	}
	
	protected void insertComment(MessagePanel messagePanel) {
		if (multTemplate != null)
			switch (result) {
				case ANS_UNCHECKED:
					messagePanel.insertText("\n(You may find parts of the template useful for any calculation required.)");
					break;
				case ANS_TOLD:
					messagePanel.insertText("\nThe template shows part of the calculation.");
					break;
			}
	}
	
	
//-----------------------------------------------------------
	
	protected void showCorrectWorking() {
		propnWorkingPanel.setReferenceValue(getCutoff());
		super.showCorrectWorking();
	}
}