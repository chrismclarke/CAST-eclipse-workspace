package expression;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;

import dataView.*;
import utils.*;
import exercise2.*;



public class ExpressionResultPanel extends XPanel implements KeyListener, StatusInterface {
	static final private double kDisabledPropn = 0.3;
	
	static final public int HORIZONTAL = 0;
	static final public int VERTICAL = 1;
	
	static final private int kBaseLength = 10;
	static final private int kHalfBaseWidth = 3;
	static final private int kHeadLength = 5;
	static final private int kHalfHeadWidth = 7;
	
	static final private Color kArrowColor = Color.red;
	
	private ExerciseApplet exerciseApplet;

	private ResultValuePanel resultEdit;
	
	private XButton evaluateButton;
	
	private JTextArea eqnEdit;
	private JLabel editLabel = null;
	
	private int resultDecimals = 9;
	
	public ExpressionResultPanel(String label, int rows, int columns, String resultLabel,
														int resultColumns, int orientation, ExerciseApplet exerciseApplet) {
		setLayout(new BorderLayout(0, 0));
		setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		
			eqnEdit = new JTextArea(rows, columns);
			eqnEdit.setLineWrap(true);
			eqnEdit.setWrapStyleWord(true);
			eqnEdit.setBackground(Color.white);
			eqnEdit.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			eqnEdit.setDisabledTextColor(DataView.dimColor(getForeground(), kDisabledPropn));
			eqnEdit.addKeyListener(this);
			
			JScrollPane scroller = new JScrollPane(eqnEdit);
			scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//			scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		
		if (label != null) {
			editLabel = new JLabel(label);
			add(orientation == VERTICAL ? "North" : "West", editLabel);
		}
		
		add("Center", scroller);
		
		if (orientation == VERTICAL)
			add("South", downArrowPanel(resultLabel, resultColumns, exerciseApplet));
		else
			add("East", rightArrowPanel(resultLabel, resultColumns, exerciseApplet));
		
		if (exerciseApplet != null)
			setFont(exerciseApplet.getStandardFont());
		
		this.exerciseApplet = exerciseApplet;
	}
	
	public void setResultDecimals(int resultDecimals) {
		this.resultDecimals = resultDecimals;
	}
	
	
	private XPanel downArrowPanel(String resultLabel, int resultColumns,
																													ExerciseApplet exerciseApplet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER));
		
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
			
				ArrowCanvas arrow1 = new ArrowCanvas(kBaseLength, kHalfBaseWidth, kHeadLength, kHalfHeadWidth,
																																					ArrowCanvas.DOWN);
				arrow1.setForeground(kArrowColor);
			buttonPanel.add(arrow1);
		
				evaluateButton = new XButton("Eval", exerciseApplet);
			buttonPanel.add(evaluateButton);
			
				ArrowCanvas arrow2 = new ArrowCanvas(kBaseLength, kHalfBaseWidth, kHeadLength, kHalfHeadWidth,
																																					ArrowCanvas.DOWN);
				arrow2.setForeground(kArrowColor);
			buttonPanel.add(arrow2);
		
		thePanel.add(buttonPanel);
		
			resultEdit = new ResultValuePanel(exerciseApplet, resultLabel, resultColumns);
		thePanel.add(resultEdit);
		
		return thePanel;
	}
	
	private XPanel rightArrowPanel(String resultLabel, int resultColumns,
																														ExerciseApplet exerciseApplet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER , 0));
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3 , 0));
			
				XPanel buttonPanel = new XPanel();
				buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER));
				
					ArrowCanvas arrow1 = new ArrowCanvas(kBaseLength, kHalfBaseWidth / 2, kHeadLength,
																															kHalfHeadWidth / 2, ArrowCanvas.RIGHT);
					arrow1.setForeground(kArrowColor);
				buttonPanel.add(arrow1);
			
					evaluateButton = new XButton("Calculate", exerciseApplet);
				buttonPanel.add(evaluateButton);
				
					ArrowCanvas arrow2 = new ArrowCanvas(kBaseLength, kHalfBaseWidth / 2, kHeadLength,
																															kHalfHeadWidth / 2, ArrowCanvas.RIGHT);
					arrow2.setForeground(kArrowColor);
				buttonPanel.add(arrow2);
			
			innerPanel.add(buttonPanel);
			
				resultEdit = new ResultValuePanel(exerciseApplet, resultLabel, resultColumns);
			innerPanel.add(resultEdit);
		
		thePanel.add(innerPanel);
		
		return thePanel;
	}
	
	public String getStatus() {
		return resultEdit.getStatus() + "#" + eqnEdit.getText();
	}
	
	public void setStatus(String status) {
		StringTokenizer st = new StringTokenizer(status, "#");
		if (st.hasMoreTokens())
			resultEdit.setStatus(st.nextToken());
		else
			resultEdit.setStatus("");
		
		if (st.hasMoreTokens())
			eqnEdit.setText(st.nextToken());
		else
			eqnEdit.setText("");
	}
	
	public void clear() {
		resultEdit.clear();
		eqnEdit.setText("");
	}
	
	public boolean isClear() {
		return resultEdit.isClear();
	}
	
	public void showAnswer(NumValue answer, String expression) {
		if (answer == null) {
			eqnEdit.setText(expression);
			evaluateExpression();
		}
		else {
			if (expression == null)
				eqnEdit.setText(answer.toString());
			else
				eqnEdit.setText(expression);
			resultEdit.showAnswer(answer);
		}
	}
	
	public NumValue getAttempt() {
		return resultEdit.getAttempt();
	}
	
	public void setFont(Font f) {
		super.setFont(f);
		if (eqnEdit != null)
			eqnEdit.setFont(f);
		if (editLabel != null)
			editLabel.setFont(f);
		
		if (resultEdit != null)
			resultEdit.setFont(f);
	}
	
	public void setLabelFont(Font f) {
		if (editLabel != null)
			editLabel.setFont(f);
	}
	
	public void setColumns(int columns) {
		eqnEdit.setColumns(columns);
	}
	
	
/*
	private Value getValue(int decimals) {
		Expression expr = new Expression(eqnEdit.getText());
		
		return expr.evaluate(decimals);
	}
*/
	
	private void evaluateExpression() {
		Expression expr = new Expression(eqnEdit.getText());
		
		Value result = expr.evaluate(resultDecimals);
		if (result instanceof NumValue) {
			resultEdit.showAnswer((NumValue)result);
			exerciseApplet.noteChangedWorking();
		}
		else {
			resultEdit.clear();
			exerciseApplet.showExpressionError("Error in expression: " + result);
//			System.out.println("Expression error: " + result);
		}
	}
	
//----------------------------------------------------------
	
	public void keyTyped(KeyEvent e) {
		String newText = eqnEdit.getText();
		if (newText.length() > 0 && newText.charAt(newText.length() - 1) == '\n') {
			eqnEdit.setText(newText.substring(0, newText.length() - 1));
		}
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyChar() == KeyEvent.VK_ENTER)
			evaluateExpression();
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (evt.target == evaluateButton) {
			evaluateExpression();
			return true;
		}
		return false;
	}
}
