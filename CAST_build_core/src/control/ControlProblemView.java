package control;

import java.awt.*;

import dataView.*;
import valueList.ProportionView;


public class ControlProblemView extends ProportionView {
//	static public final String CONTROL_PROBLEM = "controlProblem";
	
	private ControlLimitAxis controlAxis;
	private int problemFlags;
	
	public ControlProblemView(DataSet theData, String variableKey, XApplet applet,
								ControlLimitAxis controlAxis, int problemFlags) {
		super(theData, variableKey, applet);
		this.controlAxis = controlAxis;
		this.problemFlags = problemFlags;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return 0;
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return ControlledEnumeration.maxProblemWidth(g, problemFlags, getApplet());
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
	}
	
	protected String getValueString() {
		CoreVariable variable = getVariable(variableKey);
		ControlledEnumeration e = new ControlledEnumeration((NumVariable)variable, controlAxis,
																								problemFlags, getApplet());
		FlagEnumeration fe = getSelection().getEnumeration();
		boolean gotSelection = false;
		ControlProblem selectedProblem = null;
		while (e.hasMoreValues()) {
			@SuppressWarnings("unused")
			Value nextVal = e.nextValue();
			boolean nextSel = fe.nextFlag();
			if (nextSel) {
				if (gotSelection)
					return null;
				gotSelection = true;
				selectedProblem = e.getControlProblem();
			}
		}
		if (gotSelection && selectedProblem != null)
			return selectedProblem.getDescription();
		else
			return null;
	}
}
