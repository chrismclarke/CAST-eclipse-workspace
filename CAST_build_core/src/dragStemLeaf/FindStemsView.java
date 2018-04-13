package dragStemLeaf;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import imageGroups.*;


public class FindStemsView extends StemLeafListView {
//	static public final String FIND_STEMS = "findStems";
	
	static private final int NO_ANSWER = 0;
	static private final int CORRECT_ANSWER = 1;
	static private final int WRONG_ANSWER = 2;
	
	private int correctStem;
	private int answerMark = NO_ANSWER;
	
	public FindStemsView(DataSet theData, int correctStem, XApplet applet) {
		super(theData, applet);
		this.correctStem = correctStem;
	}
	
	public void checkStems() {
		if (stemPower == correctStem)
			answerMark = CORRECT_ANSWER;
		else
			answerMark = WRONG_ANSWER;
		repaint();
	}
	

//-----------------------------------------------------------------------------------
	
	private boolean hasFocus = false;

	public void mousePressed(MouseEvent e) {
		requestFocus();
		super.mousePressed(e);
	}
	
	public void focusGained(FocusEvent e) {
		hasFocus = true;
		drawBorder();
	}
	
	public void focusLost(FocusEvent e) {
		hasFocus = false;
		drawBorder();
	}
	

//-----------------------------------------------------------------------------------
	
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_LEFT) {
			if (stemPower < maxStemPower) {
				stemPower++;
				answerMark = NO_ANSWER;
				repaint();
			}
//			else
//				AudioVisual.beep.play();
		}
		else if (key == KeyEvent.VK_RIGHT) {
			if (stemPower > minStemPower) {
				stemPower--;
				answerMark = NO_ANSWER;
				repaint();
			}
//			else
//				AudioVisual.beep.play();
		}
	}
	
	private static final int kAnswerSize = 36;
	private static final int kAnswerTopBorder = 4;
	
	
	protected int getMinStemPower(StemLeafVariable variable) {
		int maxDecimals = variable.getMaxDecimals();
		minStemPower = 1 - maxDecimals;
		if (maxDecimals > 0 || !variable.zeroUnits())
			minStemPower--;									//	least sig digit is not zero
		return minStemPower;
	}
	
	protected int getMaxStemPower(StemLeafVariable variable) {
		int maxLeftDigits = variable.getMaxLeftDigits();
		if (maxLeftDigits > 1 || !variable.zeroUnits())	//	allow for extra zero
			return maxLeftDigits;
		else
			return 0;
	}
	
	protected int getAnswerHt() {
		return kAnswerSize + kAnswerTopBorder;
	}
	
	protected void drawBorder(Graphics g) {
		Color oldColor = g.getColor();
		if (hasFocus)
			g.setColor(Color.yellow);
		else
			g.setColor(Color.white);
		g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
		g.drawRect(1, 1, getSize().width - 3, getSize().height - 3);
		g.setColor(oldColor);
	}
	
	protected void drawAnswer(Graphics g) {
		int answerHoriz = (getSize().width - kAnswerSize) / 2;
		if (answerMark == CORRECT_ANSWER)
			g.drawImage(TickCrossImages.tick, answerHoriz, listBottom + kAnswerTopBorder, this);
		else if (answerMark == WRONG_ANSWER)
			g.drawImage(TickCrossImages.cross, answerHoriz, listBottom + kAnswerTopBorder, this);
	}
	

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}