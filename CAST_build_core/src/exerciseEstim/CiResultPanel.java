package exerciseEstim;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import formula.*;
import exercise2.*;


public class CiResultPanel extends XPanel implements ExerciseConstants, StatusInterface {
	static final public int HORIZONTAL = 0;
	static final public int VERTICAL = 1;
	
	static final public int LOW_HIGH_BOUNDS = 0;
	static final public int MEAN_PLUS_MINUS = 1;
	
	private int ciFormat;
	
	private XLabel intervalLabel;
	private XLabel unitsLabel;
	private ResultValuePanel firstValue, secondValue;
	
	public CiResultPanel(ExerciseApplet exerciseApplet, String label, String units,
																								int columns, int orientation, int ciFormat) {
		this.ciFormat = ciFormat;
		String joinerString = (ciFormat == LOW_HIGH_BOUNDS) ? "to" : MText.expandText("#plusMinus#");
		if (orientation == HORIZONTAL) {
			setLayout(new FlowLayout(FlowLayout.CENTER, 2, 0));
			
				firstValue = new ResultValuePanel(exerciseApplet, label, columns);
			add(firstValue);
			
				secondValue = new ResultValuePanel(exerciseApplet, joinerString, units, columns);
			add(secondValue);
		}
		else {
			setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 3));
			
				intervalLabel = new XLabel(label, XLabel.LEFT, exerciseApplet);
			add(intervalLabel);
			
				firstValue = new ResultValuePanel(exerciseApplet, "...", null, columns);
			add(firstValue);
			
				secondValue = new ResultValuePanel(exerciseApplet, joinerString, units, columns);
			add(secondValue);
			
			if (units != null) {
				unitsLabel = new XLabel(units, XLabel.LEFT, exerciseApplet);
				add(unitsLabel);
			}
		}
	}
	
	public CiResultPanel(ExerciseApplet exerciseApplet, String label, String units,
																															int columns, int orientation) {
		this(exerciseApplet, label, units, columns, orientation, LOW_HIGH_BOUNDS);
	}
	
	public CiResultPanel(ExerciseApplet exerciseApplet, String label, int columns,
																																					int orientation) {
		this(exerciseApplet, label, null, columns, orientation);
	}
	
	public CiResultPanel(ExerciseApplet exerciseApplet, String label, int columns) {
		this(exerciseApplet, label, columns, HORIZONTAL);
	}
	
	public CiResultPanel(ExerciseApplet exerciseApplet, String label, String units, int columns) {
		this(exerciseApplet, label, units, columns, HORIZONTAL);
	}
	
//--------------------------------------------------------------------
	
	public void changeUnits(String units) {
		secondValue.changeUnits(units);
	}
	
	public void changeLabel(String label, String innerLabel) {
		firstValue.changeLabel(label);
		secondValue.changeLabel(innerLabel);
	}
	
	public void clear() {
		firstValue.clear();
		secondValue.clear();
	}
	
	public boolean isIncomplete() {
		return firstValue.isClear() || secondValue.isClear();
	}
	
	public void showAnswer(NumValue firstAnswer, NumValue secondAnswer) {
		firstValue.showAnswer(firstAnswer);
		secondValue.showAnswer(secondAnswer);
	}
	
	public NumValue getLowAttempt() {
		if (ciFormat == LOW_HIGH_BOUNDS)
			return firstValue.getAttempt();
		else {
			NumValue mean = firstValue.getAttempt();
			NumValue plusMinus = secondValue.getAttempt();
			return new NumValue(mean.toDouble() - plusMinus.toDouble(), Math.max(mean.decimals, plusMinus.decimals));
		}
	}
	
	public NumValue getHighAttempt() {
		if (ciFormat == LOW_HIGH_BOUNDS)
			return secondValue.getAttempt();
		else {
			NumValue mean = firstValue.getAttempt();
			NumValue plusMinus = secondValue.getAttempt();
			return new NumValue(mean.toDouble() + plusMinus.toDouble(), Math.max(mean.decimals, plusMinus.decimals));
		}
	}
	
	public void setFont(Font f) {
		super.setFont(f);
		if (firstValue != null)
			firstValue.setFont(f);
		if (secondValue != null)
			secondValue.setFont(f);
		if (intervalLabel != null)
			intervalLabel.setFont(f);
		if (unitsLabel != null)
			unitsLabel.setFont(f);
	}
	
	public String getStatus() {
		return firstValue.getStatus() + "," + secondValue.getStatus();
	}
	
	public void setStatus(String status) {
		if (status.charAt(0) == ',') {
			firstValue.setStatus(null);
			secondValue.setStatus(status.substring(1));
		}
		else {
			StringTokenizer st = new StringTokenizer(status, ",");
			firstValue.setStatus(st.nextToken());
			if (st.hasMoreTokens())
				secondValue.setStatus(st.nextToken());
			else
				secondValue.setStatus(null);
		}
	}
}