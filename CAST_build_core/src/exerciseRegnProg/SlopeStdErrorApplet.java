package exerciseRegnProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import exercise2.*;
import distn.*;
import valueList.*;

import regn.*;
import exerciseBivarProg.*;



public class SlopeStdErrorApplet extends FindResidualApplet {
	private LinearEquationView eqnView;
	private FixedValueView seView;
	
	private boolean usedT;
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 8));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
			XPanel mainPanel = new InsetPanel(10, 0);
			mainPanel.setLayout(new BorderLayout(10, 0));
			mainPanel.add("Center", getWorkingPanels(data));
			
		add("Center", mainPanel);
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
				XPanel innerPanel = new XPanel();
				innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					XPanel answerPanel = new InsetPanel(30, 20);
					answerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
						resultPanel = new ResultValuePanel(this, "The LS slope should be within ", "of the model's slope", 6);
						resultPanel.setFont(getBigFont());
						registerStatusItem("answer", resultPanel);
					answerPanel.add(resultPanel);
					answerPanel.lockBackground(kAnswerBackground);
				innerPanel.add(answerPanel);
			bottomPanel.add(innerPanel);
			
			bottomPanel.add(createMarkingPanel(NO_HINTS));
			
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add(messagePanel);
		
		add("South", bottomPanel);
	}
	
	
//================================================

	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("answerSlop", "const");
		registerParameter("maxSe", "const");
	}
	
	private double getAnswerSlop() {
		return getDoubleParam("answerSlop");
	}
	
	private NumValue getMaxSe() {
		return getNumValueParam("maxSe");
	}
	
	
//-----------------------------------------------------------

	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(30, 0));
		
		thePanel.add("Center", getScatterPanel(data));
		
			XPanel eqnPanel = new InsetPanel(0, 6, 0, 0);
			eqnPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
			
				eqnView = new LinearEquationView(data, this, "ls", "", "", null, null, null, null);
				eqnView.setFont(getBigFont());
			eqnPanel.add(eqnView);
			
				seView = new FixedValueView("se(slope) =", getMaxSe(), 0.0, this);
				seView.setFont(getBigFont());
			eqnPanel.add(seView);
		
		thePanel.add("East", eqnPanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		super.setDisplayForQuestion();
		
		eqnView.setExplanName(getXVarName());
		eqnView.setYName(getYVarName());
		LinearModel ls = (LinearModel)data.getVariable("ls");
		NumValue intercept = ls.getIntercept();
		NumValue slope = ls.getSlope();
		eqnView.setMinMaxParams(intercept, intercept, slope, slope);		//	does invalidate()
		
		seView.setValue(getSe());
		seView.setMaxValue(getMaxSe());		//	does revalidate()
	}
	
	protected void doSelection(int n) {
		data.variableChanged("y");
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Type a value for how close you expect the least squares slope to be to the underlying model's slope.\n(Note that only an approximate value is required here. There is no need to use t-values.)");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error\n");
				messagePanel.insertText("You must type a value into the text edit box above.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The underlying models slope is likely to be within two standard errors of the least squares estimate (with about 95% probability).");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				if (usedT)
					messagePanel.insertRedText("You seem to have used a t-value to answer the question. Using twice the standard error would have been acceptable here.");
				else
					messagePanel.insertText("The underlying models slope is likely to be within two standard errors of the least squares estimate (with about 95% probability).");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("The standard error of the any estimate gives information about its accuracy. Use the standard error of the least squares slope to find how close it should be to the underlying model's slope.");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
	
//-----------------------------------------------------------
	
	private double getAttempt() {
		return resultPanel.getAttempt().toDouble();
	}
	
	private NumValue getSe() {
		LinearModel ls = (LinearModel)data.getVariable("ls");
		double slopeSe = ls.getSeSlope();
		double factor = 1.0;
		int slopeDecimals = getMaxSe().decimals;
		for (int i=0 ; i<slopeDecimals ; i++) {
			slopeSe *= 10.0;
			factor /= 10.0;
		}
		return new NumValue(Math.rint(slopeSe) * factor, slopeDecimals);
	}
	
	protected int assessAnswer() {
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else {
			double attempt = getAttempt();
			double correctSe = getSe().toDouble();
			double answerSlop = getAnswerSlop();
			if (Math.abs(attempt - 2.0 * correctSe) < answerSlop) {
				usedT = false;
				return ANS_CORRECT;
			}
			
			int df = getCount() - 2;
			double t = TTable.quantile(0.975, df);
			if (Math.abs(attempt - t * correctSe) < answerSlop) {
				usedT = true;
				return ANS_CORRECT;
			}
			
			return ANS_WRONG;
		}
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		NumValue se = getSe();
		resultPanel.showAnswer(new NumValue(2.0 * se.toDouble(), se.decimals));
	}
	
	protected double getMark() {
		int ans = assessAnswer();
		return (ans == ANS_CORRECT) ? 1 : 0;
	}
}