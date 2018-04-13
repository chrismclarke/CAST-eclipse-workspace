package qnUtils;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import imageGroups.*;


abstract public class CoreQuestionApplet extends XApplet {
	static final private String ACCURACY_PARAM = "accuracy";
	static final private String GENERATOR_SEED_PARAM = "randomSeed";
	
	static final protected int HORIZ_ANSWER = 0;
	static final protected int VERT_ANSWER = 1;
	
	static final private String[] kConclusionString = {"None", "Weak", "Moderate", "Strong"};
	
	static final private Color kDarkBlue = new Color(0x000099);
	static final protected Color kWorkingBackground = new Color(0xDDDDEE);
	static final protected Color kAnswerBackground = new Color(0xEEEEDD);
	
	protected Font questionFont;
	protected Font calculationFont;
	
	protected String approxString;
	protected double exactSlop, approxSlop;
	
	protected String tailString;
	
	protected XTextArea message;
	protected LinkedAnswerEditPanel valueEdit;
	protected AnswerChoicePanel conclusionPanel;
	private XButton checkButton, answerButton, anotherButton;
	
	protected Random generator;
	
	protected NumValue answer;
	
	protected void initialiseGenerator() {
		if (generator == null) {
			generator = new Random();
			String seedString = getParameter(GENERATOR_SEED_PARAM);
			if (seedString != null) {
				long seed = Long.parseLong(seedString);
				generator.setSeed(seed);
			}
		}
	}
	
	public void setupApplet() {
		loadParameterImages();
		if (testNotProb())
			ScalesImages.loadScales(this);
		TickCrossImages.loadCrossAndTick(this);
		
		questionFont = getStandardFont();
		calculationFont = getBigFont();
		
		initialiseGenerator();
		
		setLayout(new BorderLayout(0, 20));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(questionProportion(), 5));
			mainPanel.add("Left", leftPanel());
			mainPanel.add("Right", rightPanel());
		add("Center", mainPanel);
		
		add("South", bottomPanel());
	}
	
	protected void loadParameterImages() {
		MeanSDImages.loadMeanSD(this);
	}
	
	protected double questionProportion() {
		return 0.5;
	}
	
	abstract protected XPanel dataPanel();
	abstract protected XPanel workingPanel();
	
	abstract protected boolean testNotProb();
	abstract protected void generateQuestion();
	abstract protected String[] getAnswerStrings();
	abstract protected void setupAnswer();
	
	protected XPanel bottomPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		message = new XTextArea(getAnswerStrings(), 0, 450, this);
		thePanel.add("Center", message);
		message.setFont(getStandardFont());
		message.lockBackground(Color.white);
		message.setForeground(Color.red);
		
		valueEdit.setLinkedText(message);
			
		return thePanel;
	}
	
	private XPanel leftPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 6));
		
		XLabel title = new XLabel(translate("Question"), XLabel.CENTER, this);
		title.setFont(getBigBoldFont());
		title.setForeground(kDarkBlue);
		thePanel.add("North", title);
		
		thePanel.add("Center", dataPanel());
		return thePanel;
	}
	
	private XPanel rightPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 4));
		
		XLabel title = new XLabel("Working", XLabel.CENTER, this);
		title.setFont(getBigBoldFont());
		title.setForeground(kDarkBlue);
		thePanel.add("North", title);
		
		thePanel.add("Center", workingPanel());
		
		return thePanel;
	}
	
	protected String valueLabel() {
		return null;
	}
	
	protected XPanel answerEditPanel(int orientation) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
		
		setupAnswer();
		
		StringTokenizer st = new StringTokenizer(getParameter(ACCURACY_PARAM));
		String exactString = st.nextToken();
		approxString = st.nextToken();
		exactSlop = (Double.parseDouble(exactString));
		approxSlop = (Double.parseDouble(approxString));
		
		XPanel typePanel = new XPanel();
		if (orientation == HORIZ_ANSWER)
			typePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		else
			typePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																			VerticalLayout.VERT_CENTER, 5));
		
		String answerLabel = valueLabel();
		if (answerLabel == null)
			answerLabel = (testNotProb() ? translate("p-value") : translate("prob")) + " = ";
		
		valueEdit = new LinkedAnswerEditPanel(answerLabel, answer, getAccuracy(exactSlop),
											getAccuracy(approxSlop), new NumValue(0.5, 1), 5, null, this);
		valueEdit.setFont(getBigFont());
		
		if (orientation == HORIZ_ANSWER)
			valueEdit.setVerticalValue();
		
		typePanel.add(valueEdit);
		
		if (testNotProb()) {
			double ans = answer.toDouble();
			int conclusionOption = (ans > 0.1) ? 0
										: (ans > 0.05) ? 1
										: (ans > 0.01) ? 2 : 3;
			conclusionPanel = new AnswerChoicePanel("evidence against H0:", kConclusionString,
																								conclusionOption, this);
			typePanel.add(conclusionPanel);
		}
		
		XPanel buttonPanel = new XPanel();
		buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
			XPanel buttonPanel2 = new XPanel();
			
			buttonPanel2.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
				checkButton = new XButton("Check", this);
			buttonPanel2.add(checkButton);
				answerButton = new XButton("Tell Me", this);
			buttonPanel2.add(answerButton);
			
		buttonPanel.add(buttonPanel2);
		
			anotherButton = new XButton("Another question", this);
		
		buttonPanel.add(anotherButton);
		
		if (orientation == HORIZ_ANSWER)
			thePanel.add("West", typePanel);
		else
			thePanel.add("North", typePanel);
			
		thePanel.add("Center", buttonPanel);
		
		return thePanel;
	}
	
	protected double getAccuracy(double slopValue) {
		return Math.max(0.001, slopValue * answer.toDouble());
	}
	
	protected String nextRandomValue(StringTokenizer st) {
		NumValue minVal = new NumValue(st.nextToken());
		NumValue maxVal = new NumValue(st.nextToken());
		double m1 = minVal.toDouble();
		double m2 = maxVal.toDouble();
		double value = m1 + generator.nextDouble() * (m2 - m1);
		int decs = minVal.decimals;
		return new NumValue(value, decs).toString();
	}

	
	private boolean localAction(Object target) {
		if (target == checkButton) {
			valueEdit.checkAnswer();
			if (conclusionPanel != null)
				conclusionPanel.checkAnswer();
			return true;
		}
		else if (target == answerButton) {
			valueEdit.setCorrectAnswer();
			if (conclusionPanel != null)
				conclusionPanel.setCorrectAnswer();
			return true;
		}
		else if (target == anotherButton) {
			generateQuestion();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}