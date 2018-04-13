package percentileProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import exercise.*;

import percentile.*;


public class PropnExerciseApplet extends XApplet {
	static final private String FIXED_VALUE_PARAM = "fixedValue";
	static final private String MAX_VALUE_PARAM = "maxValue";
	static final private String MAX_ANSWER_PARAM = "maxAnswer";
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String LONG_VAR_NAME_PARAM = "longVarName";
	static final private String MAX_MESSAGE_PARAM = "maxMessage";
	static final private String PIX_WIDTH_PARAM = "pixWidth";
	
	static final private String ABOUT_DATA_PARAM = "aboutData";
	static final private String NO_OF_QUESTIONS_PARAM = "noOfQuestions";
	static final private String QUESTION_PARAM = "question";
	static final private String BOUNDARY_LIMITS_PARAM = "boundaryLimits";
	static final private String SAMP_SIZE_LIMITS_PARAM = "sampSizeLimits";
	static final private String ABOVE_BELOW_PARAM = "aboveBelowNames";
	
	static final private int kMaxDataNameWidth = 80;
	static final private int kMaxQnButtonWidth = 130;
	
	protected NumValue maxAnswer;
	
	public void setupApplet() {
		DataSet data = getData();
		DataSet refData = getReferenceData(data);
		
			NumValue maxY = new NumValue(getParameter(MAX_VALUE_PARAM));
			String horizAxisInfo = getParameter(AXIS_INFO_PARAM);
			String longVarName = getParameter(LONG_VAR_NAME_PARAM);
				
			maxAnswer = new NumValue(getParameter(MAX_ANSWER_PARAM));
			AnswerPanel answerPanel = new AnswerPanel("Answer =", maxAnswer, this, AnswerPanel.HORIZONTAL,
																																												getUnitsString());
		
			Problem problem = getProblem(data);
			
			int pixWidth = Integer.parseInt(getParameter(PIX_WIDTH_PARAM));
		
		setLayout(new BorderLayout(0, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(10, 0));
				XLabel dataLabel = new XLabel(translate("Data") + ":", XLabel.LEFT, this);
				dataLabel.setFont(getStandardBoldFont());
			topPanel.add("West", dataLabel);
			
				TextCanvas dataDescription = problem.createDataCanvas(pixWidth - kMaxDataNameWidth, this);
				dataDescription.setFont(getStandardFont());
				dataDescription.lockBackground(Color.white);
			topPanel.add("Center", dataDescription);
			
		add("North", topPanel);
		
			XPanel workingPanel = getWorkingPanel(data, refData, horizAxisInfo, longVarName, maxY);
		add("Center", workingPanel);
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 5));
				
				XPanel questionPanel = new XPanel();
				questionPanel.setLayout(new BorderLayout(10, 0));
			
					TextCanvas questionDescription = problem.createQuestionCanvas(pixWidth - kMaxQnButtonWidth, this);
					questionDescription.setFont(getStandardFont());
					questionDescription.lockBackground(Color.white);
					
				questionPanel.add("Center", questionDescription);
				questionPanel.add("East", new ProblemButton(problem, this, ProblemButton.ANOTHER, "Another Question"));
					
			mainPanel.add(questionPanel);
			
				XPanel answerControlPanel = new XPanel();
				answerControlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
				
				answerControlPanel.add(answerPanel);
				answerControlPanel.add(new ProblemButton(problem, this, ProblemButton.CHECK));
				answerControlPanel.add(new ProblemButton(problem, this, ProblemButton.TELL_ME));
			
			mainPanel.add(answerControlPanel);
					
			mainPanel.add(problem.createMessageArea(pixWidth, getParameter(MAX_MESSAGE_PARAM), this));
		
		add("South", mainPanel);
		
		problem.setLinkedComponents(answerPanel, workingPanel);
	}
	
	protected String getUnitsString() {
		return null;
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		return data;
	}
	
	protected DataSet getReferenceData(DataSet data) {
		DataSet referenceData = new DataSet();
		referenceData.addNumVariable("ref", "Reference", getParameter(FIXED_VALUE_PARAM));
		referenceData.setSelection(0);
		return referenceData;
	}
	
	protected XPanel getWorkingPanel(DataSet data, DataSet refData, String horizAxisInfo,
																				String longVarName, NumValue maxY) {
		return new PropnWorkingPanel(data, refData, this, horizAxisInfo, longVarName, maxY);
	}
	
	protected NumValue[] readDoubleLimits(String paramName) {
		NumValue minMax[] = new NumValue[2];
		StringTokenizer st = new StringTokenizer(getParameter(paramName));
		minMax[0] = new NumValue(st.nextToken());
		minMax[1] = new NumValue(st.nextToken());
		return minMax;
	}
	
	protected int[] readIntLimits(String paramName) {
		int minMax[] = new int[2];
		StringTokenizer st = new StringTokenizer(getParameter(paramName));
		minMax[0] = Integer.parseInt(st.nextToken());
		minMax[1] = Integer.parseInt(st.nextToken());
		return minMax;
	}
	
	protected Problem getCoreProblem(DataSet data, String aboutDataText, String[] questionText,
									String aboveBelowStrings, int longestQuestionIndex) {
		NumValue boundaryMinMax[] = readDoubleLimits(BOUNDARY_LIMITS_PARAM);
		int sampSizeMinMax[] = readIntLimits(SAMP_SIZE_LIMITS_PARAM);
		
		return new PropnProblem(data, aboutDataText, questionText, aboveBelowStrings,
												longestQuestionIndex, boundaryMinMax, sampSizeMinMax, maxAnswer.decimals);
	}
	
	private Problem getProblem(DataSet data) {
		String dataText = getParameter(ABOUT_DATA_PARAM);
		
		int noOfQuestions = Integer.parseInt(getParameter(NO_OF_QUESTIONS_PARAM));
		String questionText[] = new String[noOfQuestions];
		int longestIndex = 0;
		for (int i=0 ; i<noOfQuestions ; i++) {
			questionText[i] = getParameter(QUESTION_PARAM + i);
			if (questionText[i].charAt(0) == '#') {
				questionText[i] = questionText[i].substring(1);
				longestIndex = i;
			}
		}
		
		return getCoreProblem(data, dataText, questionText, getParameter(ABOVE_BELOW_PARAM),
																																	longestIndex);
	}
}