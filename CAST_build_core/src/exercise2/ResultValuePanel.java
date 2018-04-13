package exercise2;

import java.awt.*;

import dataView.*;
import utils.*;


public class ResultValuePanel extends XPanel implements ExerciseConstants, StatusInterface {
	private ExerciseApplet exerciseApplet;
	protected XNumberEditPanel edit;
	
	public ResultValuePanel(ExerciseApplet exerciseApplet, String label, int columns) {
		this(exerciseApplet, label, null, columns);
	}
	
	public ResultValuePanel(ExerciseApplet exerciseApplet, String label, String units, int columns) {
		this.exerciseApplet = exerciseApplet;
		setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			edit = new XNumberEditPanel(label, units, null, columns, exerciseApplet);
		add(edit);
	}
	
	public void changeUnits(String units) {
		edit.setUnits(units);
	}
	
	public void changeLabel(String label) {
		edit.setLabelText(label);
	}
	
	public void clear() {
		edit.clearValue();
	}
	
	public boolean isClear() {
		return edit.isClear();
	}
	
	public void showAnswer(NumValue answer) {
		edit.setDoubleValue(answer);
	}
	
	public NumValue getAttempt() {
		return edit.getNumValue();
	}
	
	public void setFont(Font f) {
		super.setFont(f);
		if (edit != null)
			edit.setFont(f);
	}
	
	public String getStatus() {
		if (isClear())
			return "";
		else
			return getAttempt().toString();
	}
	
	public void setStatus(String status) {
		if (status == null || status.length() == 0)
			clear();
		else
			showAnswer(new NumValue(status));
	}
	
	private boolean localAction(Object target) {
		if (target == edit) {
			exerciseApplet.noteChangedWorking();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}