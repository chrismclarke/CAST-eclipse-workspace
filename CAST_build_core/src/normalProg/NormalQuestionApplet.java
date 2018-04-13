package normalProg;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import qnUtils.*;
import coreGraphics.*;
import imageGroups.*;

import normal.*;
//import sampling.*;


public class NormalQuestionApplet extends XApplet implements KeyListener {
	static final protected String QUESTION_PARAM = "question";
	static final private String INITIAL_PARAM = "initialValues";
	static final protected String PARAM_RANGE_PARAM = "paramRange";
	
	static final private Color kDarkBlue = new Color(0x000066);
	
	private Font questionFont;
	protected Font calculationFont;
	
	protected Component meanEdit, sdEdit, xEdit, x2Edit;
	protected DataSet data;
	protected QuestionPanel question;
	
	private String oldText = null;
	
	public void setupApplet() {
		MeanSDImages.loadMeanSD(this);
		questionFont = getStandardFont();
		calculationFont = getBigFont();
		
		data = getData();
		
		setLayout(new BorderLayout());
		
		XPanel mainPanel = new XPanel();
		mainPanel.setLayout(new ProportionLayout(0.5, 5));
		mainPanel.add("Left", questionPanel(data));
		mainPanel.add("Right", rightPanel(data));
		add("Center", mainPanel);
		
		add("South", answerPanel(data));
		
		checkEditValues();
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		NormalDistnVariable y = new NormalDistnVariable("Z");
		y.setParams("0 1");
		data.addVariable("distn", y);
		
		return data;
	}
	
	protected XPanel answerPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		NormalCalcView calc = new NormalCalcView(data, "distn", this, (JTextField)meanEdit,
																(JTextField)sdEdit, (JTextField)xEdit, (JTextField)x2Edit, probDirection());
		calc.setFont(calculationFont);
		thePanel.add(calc);
		
		return thePanel;
	}
	
	protected void addXFields(StringTokenizer st, QuestionPanel question) {
		if (questionIsEditable()) {
			xEdit = new JTextField(st.nextToken(), 4);
			xEdit.addKeyListener(this);
			xEdit.setBackground(Color.white);
		}
		else {
			xEdit = new XLabel(st.nextToken(), XLabel.LEFT, this);
			xEdit.setFont(questionFont);
		}
		question.add(QuestionLayout.X1_VALUE, xEdit);
		
		if (probDirection() == NormalCalcView.BETWEEN) {
			if (questionIsEditable()) {
				x2Edit = new JTextField(st.nextToken(), 4);
				x2Edit.addKeyListener(this);
				x2Edit.setBackground(Color.white);
			}
			else {
				x2Edit = new XLabel(st.nextToken(), XLabel.LEFT, this);
				x2Edit.setFont(questionFont);
			}
			question.add(QuestionLayout.X2_VALUE, x2Edit);
		}
	}
	
	protected boolean questionIsEditable() {
		return true;
	}
	
	protected int probDirection() {
		return NormalCalcView.BELOW;
	}
	
	protected XPanel questionPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		XLabel title = new XLabel(translate("Question"), XLabel.CENTER, this);
		title.setFont(getBigBoldFont());
		title.setForeground(kDarkBlue);
		thePanel.add("North", title);
		
		StringTokenizer st = new StringTokenizer(getParameter(INITIAL_PARAM));
		
		question = new QuestionPanel(this, QUESTION_PARAM, PARAM_RANGE_PARAM);
		question.lockBackground(getBackground());
		question.setFont(questionFont);
		
		if (questionIsEditable()) {
			meanEdit = new JTextField(st.nextToken(), 4);
			meanEdit.addKeyListener(this);
//			meanEdit.lockBackground(Color.white);
		}
		else {
			meanEdit = new XLabel(st.nextToken(), XLabel.LEFT, this);
			meanEdit.setFont(questionFont);
		}
		meanEdit.setForeground(Color.blue);
		question.add(QuestionLayout.MEAN, meanEdit);
		
		if (questionIsEditable()) {
			sdEdit = new JTextField(st.nextToken(), 4);
			sdEdit.addKeyListener(this);
//			sdEdit.lockBackground(Color.white);
		}
		else {
			sdEdit = new XLabel(st.nextToken(), XLabel.LEFT, this);
			sdEdit.setFont(questionFont);
		}
		sdEdit.setForeground(Color.red);
		question.add(QuestionLayout.SD, sdEdit);
		
		addXFields(st, question);
		
		thePanel.add("Center", question);
		return thePanel;
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
		thePanel.add("Center", displayPanel(data));
		
		ZCalcCanvas zCalc = new ZCalcCanvas(data, this, (JTextField)meanEdit,
														(JTextField)sdEdit, (JTextField)xEdit, ZCalcCanvas.NO_SUB);
		zCalc.setFont(calculationFont);
		thePanel.add("North", zCalc);
		
		return thePanel;
	}
	
	protected DistnDensityView getView(DataSet data, HorizAxis theHorizAxis, VertAxis theProbAxis) {
		return new DistnDensityView(data, this, theHorizAxis, theProbAxis, "distn");
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		theHorizAxis.readNumLabels("-3.5 3.5 -3 1");
		theHorizAxis.setAxisName("Z");
		thePanel.add("Bottom", theHorizAxis);
		
		VertAxis theProbAxis = new VertAxis(this);
		theProbAxis.readNumLabels("0 0.5 7 0.1");
		thePanel.add("Left", theProbAxis);
		theProbAxis.show(false);
		
		DistnDensityView theView = getView(data, theHorizAxis, theProbAxis);
		
		theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected double getZ(Component xField) {
		double mean = new NumValue(((JTextField)meanEdit).getText()).toDouble();
		double sd;
		try {
			sd = new NumValue(((JTextField)sdEdit).getText()).toDouble();
			if (sd <= 0.0)
				throw new NumberFormatException();
		} catch (NumberFormatException e) {
			sd = 1.0;
			((JTextField)sdEdit).setText("1.0");
		}
		double x;
		try {
			x = new NumValue(((JTextField)xField).getText()).toDouble();
		} catch (NumberFormatException e) {
			x = Double.NEGATIVE_INFINITY;
		}
		return (x - mean) / sd;
	}
	
	protected void checkEditValues() {
		double z = getZ(xEdit);
		data.setSelection("distn", Double.NEGATIVE_INFINITY, z);
	}
	
	public void keyTyped(KeyEvent e) {
//		String newText = edit.getText();
//		System.out.println("key typed: now = " + newText);
	}

	public void keyPressed(KeyEvent e) {
		if (oldText == null) {
			JTextField edit = (JTextField)e.getSource();
			oldText = edit.getText();
//		System.out.println("key pressed: old = " + oldText);
		}
	}

	public void keyReleased(KeyEvent e) {
		JTextField edit = (JTextField)e.getSource();
		String newText = edit.getText();
//		System.out.println("key released: text = " + newText);
		try {
			new NumValue(newText);		//	throws exception if badly formatted text
			checkEditValues();
		} catch (NumberFormatException ex) {
			edit.setText(oldText);
		}
		oldText = null;
	}
}