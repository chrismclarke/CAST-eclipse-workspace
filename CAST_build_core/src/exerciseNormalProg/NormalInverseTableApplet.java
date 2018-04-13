package exerciseNormalProg;

import java.awt.*;

import dataView.*;
import exercise2.*;
import formula.*;

import exerciseNormal.*;
import exerciseNormal.JtableLookup.*;


public class NormalInverseTableApplet extends NormalInverseZApplet {
//	static final private double kEpsValue = 0.003;
	
	private TablePanel tableLookupPanel;
	private DiffTemplatePanel diffTemplate;
		
//-----------------------------------------------------------

	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
			
			FormulaContext stdContext = new FormulaContext(kTemplateColor, getStandardFont(), this);
			diffTemplate = new DiffTemplatePanel(DiffTemplatePanel.BASIC, stdContext);
			diffTemplate.lockBackground(kTemplateBackground);
			registerStatusItem("diffTemplate", diffTemplate);
		thePanel.add("North", diffTemplate);
		
			tableLookupPanel = new TablePanel(data, "z", this);
			registerStatusItem("tableSelection", tableLookupPanel);
		thePanel.add("Center", tableLookupPanel);
		
			zInverseTemplate = new ZInverseTemplatePanel(getMaxValue(), stdContext);
			zInverseTemplate.lockBackground(kTemplateBackground);
			registerStatusItem("zTemplate", zInverseTemplate);
		thePanel.add("South", zInverseTemplate);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		super.setDisplayForQuestion();
		
		tableLookupPanel.scrollToSelection();
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		insertMessageContent(messagePanel, TABLE, this);
	}
	
	protected int getMessageHeight() {
		return 150;
	}
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else {
			double attempt = getAttempt();
			double attemptZ = (attempt - getMean().toDouble()) / getSD().toDouble();
			
			NumValue percent = getPercent();
			double prob = percent.toDouble() * 0.01;
			if (tailType() == GREATER_THAN)
				prob = 1 - prob;
			
			double correctBounds[] = tableLookupPanel.correctZBounds(prob, hasOption("interpolate"), 0.002);
			double approxBounds[] = tableLookupPanel.correctZBounds(prob, false, 0.0101);
			
//			System.out.println("attemptZ = " + attemptZ);
//			System.out.println("correctBounds = (" + correctBounds[0] + ", " + correctBounds[1] + ")");
//			System.out.println("approxBounds = (" + approxBounds[0] + ", " + approxBounds[1] + ")");
			
			if (attemptZ > correctBounds[0] && attemptZ < correctBounds[1])
				return ANS_CORRECT;
			else if (attemptZ > approxBounds[0] && attemptZ < approxBounds[1])
				return ANS_CLOSE;
			else
				return ANS_WRONG;
		}
	}
	
	protected void showCorrectWorking() {
		NumValue percent = getPercent();
		NumValue prob = new NumValue(percent.toDouble() * 0.01, percent.decimals + 2);
		NumValue correct = new NumValue(evaluatePercentile(percent, "distn"), getMaxValue().decimals);
		NumValue correctZ = new NumValue(evaluatePercentile(percent, "z"), kMaxZValue.decimals);
		
		resultPanel.showAnswer(correct);
		
		int type = tailType();
		if (type == LESS_THAN || type == LESS_THAN_SIMPLE)
			diffTemplate.setValues(prob, kZeroValue);
		else
			diffTemplate.setValues(kOneValue, prob);
		
		NumValue start = new NumValue(Double.NEGATIVE_INFINITY);
		tableLookupPanel.showAnswer(start, correctZ);
		
		zInverseTemplate.setValues(correctZ, getMean(), getSD());
	}
	
	protected double getMark() {
		int markType = assessAnswer();
		return (markType == ANS_CORRECT) ? 1 : (markType == ANS_CLOSE) ? 0.9 : 0;
	}
}