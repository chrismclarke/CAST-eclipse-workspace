package exerciseRegnProg;

import java.awt.*;
import javax.swing.border.*;

import dataView.*;
import utils.*;
import exercise2.*;
import formula.*;


public class ResponseDistnApplet extends ExerciseApplet {
	
	static final private int kEquationFontSize = 16;
	
	private MessagePanel equationPanel;
	private XNumberEditPanel meanEdit, sdEdit;
	
	private boolean meanIsWrong, sdIsWrong;
	
//================================================
	
	protected class ModelQuestionPanel extends QuestionPanel {
		public ModelQuestionPanel(ExerciseApplet exerciseApplet) {
			super(exerciseApplet);
		}
		
		protected void setQuestionText(String qn) {
			int eqnStartIndex = qn.indexOf("*eqn*");
			String startText = qn.substring(0, eqnStartIndex);
			
			String eqn = getEquation() + MText.expandText("  +  #epsilon#");
			
			insertText(startText + "\n");
			setAlignment(false);
			insertBoldBlueText(eqn + "\n");
			setAlignment(true);
			if (eqnStartIndex + 5 < qn.length()) {
				String endText = qn.substring(eqnStartIndex + 5);
				insertText(endText);
			}
		}
	}
	
	private String getEquation() {
		String s = getYName() + "  =  " + getIntercept() + " + " + getSlope() + " " + getXName();
		return s.replace("+ -", "- ");
	}
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 10));
				questionPanel = new ModelQuestionPanel(this);
			topPanel.add(questionPanel);
				
			topPanel.add(getWorkingPanels(null));
			
			topPanel.add(createMarkingPanel(NO_HINTS));
			
		add("North", topPanel);
				
			message = new ExerciseMessagePanel(this);
			
		add("Center", message);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		registerParameter("yName", "string");
		registerParameter("xName", "string");
		registerParameter("intercept", "const");
		registerParameter("slope", "const");
		registerParameter("errorSd", "const");
		registerParameter("x0", "const");
		registerParameter("maxMean", "const");
		registerParameter("meanSlop", "const");
		registerParameter("sdSlop", "const");
	}
	
	private String getYName() {
		return getStringParam("yName");
	}
	
	private String getXName() {
		return getStringParam("xName");
	}
	
	private NumValue getIntercept() {
		return getNumValueParam("intercept");
	}
	
	private NumValue getSlope() {
		return getNumValueParam("slope");
	}
	
	private NumValue getErrorSd() {
		return getNumValueParam("errorSd");
	}
	
	private NumValue getX0() {
		return getNumValueParam("x0");
	}
	
	private NumValue getMaxMean() {
		return getNumValueParam("maxMean");
	}
	
	private double getMeanSlop() {
		return getDoubleParam("meanSlop");
	}
	
	private double getSdSlop() {
		return getDoubleParam("sdSlop");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new InsetPanel(5, 20);
		thePanel.setLayout(new BorderLayout(0, 10));
			
			Border insetBorder = new EmptyBorder(20, 0, 20, 0);
			equationPanel = new MessagePanel(null, this, false, kEquationFontSize, insetBorder) {
						protected void fillContent() {
							setAlignment(false);
							
							insertText(MText.expandText(getYName() + "  #sim#  normal ( #mu#="));
							
							Font editFont = new Font(getStandardFont().getName(), getStandardFont().getStyle(),
																																							kEquationFontSize);
							meanEdit = new XNumberEditPanel(null, 4, ResponseDistnApplet.this);
							meanEdit.setFont(editFont);
							registerStatusItem("mean", meanEdit);
							insertEdit(meanEdit);
							
							insertText(MText.expandText(", #sigma#="));
							
							sdEdit = new XNumberEditPanel(null, 4, ResponseDistnApplet.this);
							sdEdit.setFont(editFont);
							registerStatusItem("sd", sdEdit);
							insertEdit(sdEdit);
							
							insertText(")");
						}
						
						private boolean localAction(Object target) {
							if (target instanceof XNumberEditPanel) {
								ResponseDistnApplet.this.noteChangedWorking();
								return true;
							}
							return false;
						}
						
						@SuppressWarnings("deprecation")
						public boolean action(Event evt, Object what) {
							return localAction(evt.target);
						}
			};
			equationPanel.setTextBackground(kAnswerBackground);
			thePanel.add("Center", equationPanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		equationPanel.changeContent();
		meanEdit.clearValue();
		sdEdit.clearValue();
	}
	
	protected void setDataForQuestion() {
	}
	
	protected DataSet getData() {
		return null;
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Type the mean and standard deviation of " + getYName() + " into the text edit boxes above.\n");
				messagePanel.insertText("(You may need to use a calculator.)");
				break;
			case ANS_TOLD:
			case ANS_CORRECT:
				messagePanel.insertRedHeading(result == ANS_TOLD ? "Answer\n" : "Correct!\n");
				messagePanel.insertText("The mean " + getYName() + " is found with the equation\n");
				String eqn = getEquation();
				messagePanel.setAlignment(false);
				messagePanel.insertText(eqn + "\n");
				messagePanel.setAlignment(true);
				messagePanel.insertText("by substituting " + getXName() + " = " + getX0() + "\n");
				messagePanel.insertText("The standard deviation of " + getYName() + " is the same as the error standard deviation, " + getErrorSd() + ", whatever the value of " + getXName() + ".");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertHeading("Incomplete answer!\n");
				messagePanel.insertText("You must type values into the two text edit boxes above.");
				break;
			case ANS_INVALID:
				messagePanel.insertHeading("Wrong!\n");
				messagePanel.insertText("Standard deviations cannot be negative.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				if (meanIsWrong)
					messagePanel.insertText("Use the equation of the model for " + getYName()
															+ " to find its mean when " + getXName() + " = " + getX0() + "\n");
				if (sdIsWrong)
					messagePanel.insertText("The standard deviation of the error provides the standard deviation of " + getYName() + ".");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 0;			//		in Center of BorderLayout
	}
	
//-----------------------------------------------------------
	
	private double getCorrectMean() {
		return getIntercept().toDouble() + getSlope().toDouble() * getX0().toDouble();
	}
	
	protected int assessAnswer() {
		if (meanEdit.isClear() || sdEdit.isClear())
			return ANS_INCOMPLETE;
		
		if (sdEdit.getDoubleValue() <= 0.0)
			return ANS_INVALID;
		
		double correctMean = getCorrectMean();
		double attemptMean = meanEdit.getDoubleValue();
		meanIsWrong = (Math.abs(correctMean - attemptMean) > getMeanSlop());
			
		double correctSd = getErrorSd().toDouble();
		double attemptSd = sdEdit.getDoubleValue();
		sdIsWrong = (Math.abs(correctSd - attemptSd) > getSdSlop());
		
		if (meanIsWrong || sdIsWrong)
			return ANS_WRONG;
		
		return ANS_CORRECT;
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		NumValue yMean = new NumValue(getCorrectMean(), getMaxMean().decimals);
		meanEdit.setDoubleValue(yMean);
		sdEdit.setDoubleValue(getErrorSd());
	}
	
	protected double getMark() {
		int ans = assessAnswer();
		return (ans == ANS_CORRECT) ? 1 : 0;
	}
	
}