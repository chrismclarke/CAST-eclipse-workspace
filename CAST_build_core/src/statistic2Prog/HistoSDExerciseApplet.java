package statistic2Prog;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import exercise.*;

import statistic2.*;


public class HistoSDExerciseApplet extends XApplet {
	static final private String SAMP_SIZE_LIMITS_PARAM = "sampSizeLimits";
	static final private String AXIS_COUNT_PARAM = "axisCount";
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String HISTO_CLASS_PARAM = "histoClass";
	static final private String AXIS_NAME_PARAM = "histoAxisName";
	
	static final private String ABOUT_DATA_PARAM = "aboutData";
	static final private String QUESTION_PARAM = "question";
	static final private String DECIMALS_PARAM = "decimals";
	static final private String PIX_WIDTH_PARAM = "pixWidth";
	static final private String MAX_MESSAGE_PARAM = "maxMessage";
	static final private String MAX_MEAN_PARAM = "maxMean";
	static final private String MAX_SD_PARAM = "maxSD";
	static final private String SLOP_PROPNS_PARAM = "slopPropns";
	
	
	static final private int kMaxDataNameWidth = 80;
	static final private int kMaxQnButtonWidth = 130;
	
	
	private DrawMeanSDProblem problem;
	private int axisCount;
	
	private MultiHorizAxis axis;
	private BasicDataView dataView;
	
	private XChoice axisChoice;
	
	public void setupApplet() {
		DataSet data = getData();
		
		axisChoice = new XChoice(this);			//	must be created before getWorkingPanel() is called
		
		axisCount = Integer.parseInt(getParameter(AXIS_COUNT_PARAM));
		problem = readProblem(data, axisCount);
			
			int pixWidth = Integer.parseInt(getParameter(PIX_WIDTH_PARAM));
		
		setLayout(new BorderLayout(0, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(10, 10));
				XLabel dataLabel = new XLabel(translate("Data") + ":", XLabel.LEFT, this);
				dataLabel.setFont(getStandardBoldFont());
			topPanel.add("West", dataLabel);
			
				TextCanvas dataDescription = problem.createDataCanvas(pixWidth - kMaxDataNameWidth, this);
				dataDescription.setFont(getStandardFont());
				dataDescription.lockBackground(Color.white);
			topPanel.add("Center", dataDescription);
			
		add("North", topPanel);
		
			XPanel workingPanel = getWorkingPanel(data);
		add("Center", workingPanel);
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 5));
				
				XPanel axisChoicePanel = new XPanel();
				axisChoicePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
					XLabel choiceLabel = new XLabel("Axis includes values...", XLabel.RIGHT, this);
					choiceLabel.setFont(getStandardBoldFont());
				axisChoicePanel.add(choiceLabel);
					
					axisChoice.addItem(getParameter(AXIS_NAME_PARAM + "1"));
					for (int i=2 ; i<=axisCount ; i++)
						axisChoice.addItem(getParameter(AXIS_NAME_PARAM + i));
				
				axisChoicePanel.add(axisChoice);
				
			mainPanel.add(axisChoicePanel);
				
				XPanel questionPanel = new XPanel();
				questionPanel.setLayout(new BorderLayout(10, 0));
			
					TextCanvas questionDescription = problem.createQuestionCanvas(pixWidth - kMaxQnButtonWidth, this);
					questionDescription.setFont(getStandardFont());
					questionDescription.lockBackground(Color.white);
					
				questionPanel.add("Center", questionDescription);
				
					XPanel anotherButtonPanel = new XPanel();
					anotherButtonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
					anotherButtonPanel.add(new ProblemButton(problem, this, ProblemButton.ANOTHER, "Another Question"));
				questionPanel.add("East", anotherButtonPanel);
					
			topPanel.add("South", questionPanel);
																//	out of order!! Question cannot be created until
																//	after workingPanel has been created.
			
				XPanel answerControlPanel = new XPanel();
				answerControlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
				
				answerControlPanel.add(new ProblemButton(problem, this, ProblemButton.CHECK));
				answerControlPanel.add(new ProblemButton(problem, this, ProblemButton.TELL_ME));
					
					AnswerPanel answerPanel = new AnswerPanel(this);
				answerControlPanel.add(answerPanel);
			
			mainPanel.add(answerControlPanel);
					
				XTextArea message = problem.createMessageArea(pixWidth, getParameter(MAX_MESSAGE_PARAM), this);
				dataView.setLinkedMessage(message);
			mainPanel.add(message);
		
		add("South", mainPanel);
		
		problem.setLinkedComponents(answerPanel, workingPanel);
		problem.changeData();
		problem.changeQuestion();
	}
	
	private DrawMeanSDProblem readProblem(DataSet data, int axisCount) {
		StringTokenizer st = new StringTokenizer(getParameter(SAMP_SIZE_LIMITS_PARAM));
		int sampSizeMin = Integer.parseInt(st.nextToken());
		int sampSizeMax = Integer.parseInt(st.nextToken());
		NumValue maxMean = new NumValue(getParameter(MAX_MEAN_PARAM));
		NumValue maxSD = new NumValue(getParameter(MAX_SD_PARAM));
		DrawMeanSDProblem problem = new DrawMeanSDProblem(data, getParameter(ABOUT_DATA_PARAM),
						getParameter(QUESTION_PARAM), readParameterStrings(HISTO_CLASS_PARAM, axisCount),
						sampSizeMin, sampSizeMax, maxMean, maxSD);
		
		String slopPropnString = getParameter(SLOP_PROPNS_PARAM);
		if (slopPropnString != null) {
			st = new StringTokenizer(slopPropnString);
			double meanSlopPropn = Double.parseDouble(st.nextToken());
			double sdSlopPropn = Double.parseDouble(st.nextToken());
			problem.setSlopPropns(meanSlopPropn, sdSlopPropn);
		}
		return problem;
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
			NumVariable dummyVar = new NumVariable(getParameter(VAR_NAME_PARAM));
		data.addVariable("y", dummyVar);
		return data;
	}
	
	private String[] readParameterStrings(String paramName, int nStrings) {
		String s[] = new String[nStrings];
		for (int i=0 ; i<nStrings ; i++)
			s[i] = getParameter(paramName + (i+1));
		return s;
	}
	
	private XPanel getWorkingPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			axis = new MultiHorizAxis(this, axisCount);
			axis.readNumLabels(getParameter(AXIS_INFO_PARAM + "1"));
			for (int i=2 ; i<=axisCount ; i++)
				axis.readExtraNumLabels(getParameter(AXIS_INFO_PARAM + i));
			axis.setChangeMinMax(true);
			axis.setAxisName(data.getVariable("y").name);
			
		thePanel.add("Bottom", axis);
		
			StringTokenizer st = new StringTokenizer(getParameter(DECIMALS_PARAM));
			int meanDecimals = Integer.parseInt(st.nextToken());
			int sdDecimals = Integer.parseInt(st.nextToken());
			dataView = getDataView(data, axis, meanDecimals, sdDecimals);
			dataView.lockBackground(Color.white);
			
		thePanel.add("Center", dataView);
		
		problem.setLinkedViewAxis(dataView, axis, axisChoice);
		
		return thePanel;
	}
	
	protected BasicDataView getDataView(DataSet data, MultiHorizAxis axis, int meanDecimals,
																														int sdDecimals) {
		return new BasicHistoView(data, this, axis, null, 0.0, 1.0, meanDecimals, sdDecimals);
													//	classes will be set properly later by problem.changeQuestion()
	}

	
	private boolean localAction(Object target) {
		if (target == axisChoice) {
			int newAxisIndex = axisChoice.getSelectedIndex();
			problem.changeAxis(newAxisIndex);
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
	
}