package qnUtils;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import imageGroups.TickCrossImages;


abstract public class CheckValueApplet extends XApplet {
	static final private String ANSWER_DECIMALS_PARAM = "answerDecimals";
	static final private String DECIMALS_PARAM = "decimals";
								//		parameter changed from "decimals" to "answerDecimals" to avoid
								//		clash of defns in CoreModelDataSet, but old parameter also retained
								//		for backward compatibility
	static final private String MESSAGE_LOCATION_PARAM = "message";
	
	static final private int kDefaultChars = 12;
	
	static final protected Color kAnswerBackground = new Color(0xEEEEDD);
	static final protected Color kWorkingBackground = new Color(0xDDDDEE);
	
	private DataSet data;
	
	protected LinkedAnswerEditPanel valueEdit;
	private XButton checkButton;
	private XButton answerButton;
	private XTextArea message;
	
	private String answerString[];
	private NumValue correctValue;
	private double exactSlop, approxSlop;
	private int maxAnswerChars;
	protected int correctDecimals;
	
	abstract protected DataSet readData();
	abstract protected NumValue evalAnswer(DataSet data, int correctDecimals);
	abstract protected double evalExactSlop(NumValue answer, DataSet data);
	abstract protected double evalApproxSlop(NumValue answer, DataSet data);
	abstract protected String[] answerStrings(NumValue answer);
	abstract protected XPanel viewPanel(DataSet data);
	abstract protected String valueLabel();
	
	protected DataSet getData() {
		return data;
	}
	
	public void setupApplet() {
		TickCrossImages.loadCrossAndTick(this);
		
		data = readData();
		
		initialiseAnswer(data);
		
		boolean sideMessage = true;
		boolean messageBesideControls = false;
		int messageCharWidth = kDefaultChars;
		int messagePixWidth = 0;
		try {
			StringTokenizer messageParams = new StringTokenizer(getParameter(MESSAGE_LOCATION_PARAM));
			String location = messageParams.nextToken();
			messageCharWidth = messagePixWidth = Integer.parseInt(messageParams.nextToken());
			sideMessage = location.equals("side");
			messageBesideControls = location.equals("withControls");
		} catch (Exception e) {
		}
		
		setLayout(new BorderLayout());
		add("Center", topPanel(data, sideMessage, messageCharWidth));
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout());
			if (!messageBesideControls)
				bottomPanel.add("Center", controlPanel(data));
			bottomPanel.add("South", editPanel(data, sideMessage, messageBesideControls, messagePixWidth));
		
		add("South", bottomPanel);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 3));
		return thePanel;
	}
	
	private void initialiseAnswer(DataSet data) {
		String decimalsString = getParameter(ANSWER_DECIMALS_PARAM);
		if (decimalsString == null)
			decimalsString = getParameter(DECIMALS_PARAM);
		StringTokenizer theValues = new StringTokenizer(decimalsString);
		correctDecimals = Integer.parseInt(theValues.nextToken());
		maxAnswerChars = Integer.parseInt(theValues.nextToken());
		
		correctValue = evalAnswer(data, correctDecimals);
		exactSlop = evalExactSlop(correctValue, data);
		approxSlop = evalApproxSlop(correctValue, data);
		
		answerString = answerStrings(correctValue);
	}
	
	private XPanel messagePanel(int charWidth, int pixWidth) {
		XPanel messagePanel = new XPanel();
		messagePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		message = new XTextArea(answerString, charWidth, pixWidth, this);
		messagePanel.add(message);
		message.setFont(getStandardFont());
		message.lockBackground(Color.white);
		message.setForeground(Color.red);
		return messagePanel;
	}
	
	private XPanel topPanel(DataSet data, boolean sideMessage, int messageCharWidth) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(5, 0));
		thePanel.add("Center", viewPanel(data));
		
		if (sideMessage) {
			XPanel messagePanel = messagePanel(messageCharWidth, 0);
			thePanel.add("East", messagePanel);
		}
		return thePanel;
	}
	
	private XPanel editPanel(DataSet data, boolean sideMessage, boolean messageBesideControls, int messagePixWidth) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		if (!sideMessage) {
			if (messageBesideControls) {
				XPanel topPanel = new XPanel();
				topPanel.setLayout(new BorderLayout());
					topPanel.add("West", messagePanel(0, messagePixWidth));
					topPanel.add("Center", controlPanel(data));
				thePanel.add("North", topPanel);
			}
			else
				thePanel.add("North", messagePanel(0, messagePixWidth));
		}
		
			XPanel valuePanel = new XPanel();
			valuePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				XPanel innerPanel = new InsetPanel(6, 2);
				innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			
					valueEdit = new LinkedAnswerEditPanel(valueLabel(), correctValue, exactSlop, approxSlop,
																new NumValue(0.0, 1), maxAnswerChars, message, this);
					valueEdit.setFont(getBigFont());
				innerPanel.add(valueEdit);
				
					checkButton = new XButton("Check", this);
				innerPanel.add(checkButton);
				
					answerButton = new XButton("Tell Me", this);
				innerPanel.add(answerButton);
				
				innerPanel.lockBackground(kAnswerBackground);
			valuePanel.add(innerPanel);
		
		thePanel.add("Center", valuePanel);
		return thePanel;
	}
	
	protected void resetForNewData() {
		initialiseAnswer(data);
		message.changeText(answerString);
		valueEdit.reset(correctValue, exactSlop, approxSlop);
	}
	
	protected void setLabelText(String newText) {
		valueEdit.setLabelText(newText);
	}
	
	protected void changeForNewAnswerType(int newAnswerType) {
	}

	
	private boolean localAction(Object target) {
		if (target == checkButton) {
			int oldAnswerType = valueEdit.getCurrentAnswer();
			valueEdit.checkAnswer();
			int newAnswerType = valueEdit.getCurrentAnswer();
			if (oldAnswerType != newAnswerType)
				changeForNewAnswerType(newAnswerType);
			return true;
		}
		else if (target == answerButton) {
			int oldAnswerType = valueEdit.getCurrentAnswer();
			valueEdit.setCorrectAnswer();
			int newAnswerType = valueEdit.getCurrentAnswer();
			if (oldAnswerType != newAnswerType)
				changeForNewAnswerType(newAnswerType);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}