package statistic2Prog;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import random.*;
import coreVariables.*;
import exercise.*;

import statistic2.*;


public class SDHistoExerciseApplet extends XApplet {
	static final private String SAMP_SIZE_LIMITS_PARAM = "sampSizeLimits";
	static final private String AXIS_COUNT_PARAM = "axisCount";
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String HISTO_CLASS_PARAM = "histoClass";
	
	static final private String ABOUT_DATA_PARAM = "aboutData";
	static final private String QUESTION_PARAM = "question";
	static final private String SD_DECIMALS_PARAM = "sdDecimals";
	static final private String PIX_WIDTH_PARAM = "pixWidth";
	static final private String MAX_ANSWER_PARAM = "maxAnswer";
	static final private String MAX_MESSAGE_PARAM = "maxMessage";
	
	
	static final private int kMaxDataNameWidth = 80;
	static final private int kMaxQnButtonWidth = 130;
	
	
	private GuessSDProblem problem;
	private int axisCount;
	
	private MultiHorizAxis axis;
	private BasicDataView dataView;
	
	public void setupApplet() {
		DataSet data = getData();
		
		axisCount = Integer.parseInt(getParameter(AXIS_COUNT_PARAM));
		StringTokenizer st = new StringTokenizer(getParameter(SAMP_SIZE_LIMITS_PARAM));
		int sampSizeMin = Integer.parseInt(st.nextToken());
		int sampSizeMax = Integer.parseInt(st.nextToken());
		int sdDecimals = Integer.parseInt(getParameter(SD_DECIMALS_PARAM));
		problem = new GuessSDProblem(data, getParameter(ABOUT_DATA_PARAM),
						getParameter(QUESTION_PARAM), readParameterStrings(HISTO_CLASS_PARAM, axisCount),
						sampSizeMin, sampSizeMax, sdDecimals, "base", "y");
			
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
		
			XPanel workingPanel = getWorkingPanel(data, sdDecimals);
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
				
					NumValue maxAnswer = new NumValue(getParameter(MAX_ANSWER_PARAM));
					AnswerPanel answerPanel = new AnswerPanel("Answer =", maxAnswer, this, AnswerPanel.HORIZONTAL);
				
				answerControlPanel.add(answerPanel);
				answerControlPanel.add(new ProblemButton(problem, this, ProblemButton.CHECK));
				answerControlPanel.add(new ProblemButton(problem, this, ProblemButton.TELL_ME));
			
			mainPanel.add(answerControlPanel);
				
				XTextArea message = problem.createMessageArea(pixWidth, getParameter(MAX_MESSAGE_PARAM), this);
				dataView.setLinkedMessage(message);
			mainPanel.add(message);
		
		add("South", mainPanel);
		
		problem.setLinkedComponents(answerPanel, workingPanel);
		problem.changeData();
		problem.changeQuestion();
	}
	
	private String[] readParameterStrings(String paramName, int nStrings) {
		String s[] = new String[nStrings];
		for (int i=0 ; i<nStrings ; i++)
			s[i] = getParameter(paramName + (i+1));
		return s;
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
			RandomNormal generator = new RandomNormal(10, 0.0, 1.0, 3.0);
			NumSampleVariable baseVar = new NumSampleVariable("Std normal", generator, 9);
			baseVar.generateNextSample();
		data.addVariable("base", baseVar);
		
			ScaledVariable yVar = new ScaledVariable(getParameter(VAR_NAME_PARAM), baseVar,
																																"base", 0.0, 1.0, 9);
		data.addVariable("y", yVar);
		return data;
	}
	
	private XPanel getWorkingPanel(DataSet data, int sdDecimals) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			axis = new MultiHorizAxis(this, axisCount);
			axis.readNumLabels(getParameter(AXIS_INFO_PARAM + "1"));
			for (int i=2 ; i<=axisCount ; i++)
				axis.readExtraNumLabels(getParameter(AXIS_INFO_PARAM + i));
			axis.setChangeMinMax(true);
			axis.setAxisName(data.getVariable("y").name);
			
		thePanel.add("Bottom", axis);
		
			dataView = getDataView(data, axis, "y", sdDecimals);
			dataView.lockBackground(Color.white);
			
		thePanel.add("Center", dataView);
		
		problem.setLinkedViewAxis(dataView, axis);
		
		return thePanel;
	}
	
	protected BasicDataView getDataView(DataSet data, MultiHorizAxis axis, String yKey,
																															int sdDecimals) {
		return new BasicHistoView(data, this, axis, yKey, 0.0, 1.0, sdDecimals, sdDecimals);
													//	classes will be set properly later by problem.changeQuestion()
	}
	
//	protected NumValue[] readDoubleLimits(String paramName) {
//		NumValue minMax[] = new NumValue[2];
//		StringTokenizer st = new StringTokenizer(getParameter(paramName));
//		minMax[0] = new NumValue(st.nextToken());
//		minMax[1] = new NumValue(st.nextToken());
//		return minMax;
//	}
//	
//	protected int[] readIntLimits(String paramName) {
//		int minMax[] = new int[2];
//		StringTokenizer st = new StringTokenizer(getParameter(paramName));
//		minMax[0] = Integer.parseInt(st.nextToken());
//		minMax[1] = Integer.parseInt(st.nextToken());
//		return minMax;
//	}
}