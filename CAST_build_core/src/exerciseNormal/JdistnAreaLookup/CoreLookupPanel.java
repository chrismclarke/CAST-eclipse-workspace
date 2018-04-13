package exerciseNormal.JdistnAreaLookup;

import java.awt.*;

import java.util.*;
import exercise2.*;

import dataView.*;
import axis.*;
import utils.*;

import exerciseNormalProg.*;


abstract public class CoreLookupPanel extends XPanel implements ExerciseConstants, StatusInterface {
	static final private String kZAxisInfo = "-3.5 3.5 -3 1";
	
	static final public boolean HIGH_AND_LOW = true;
	static final public boolean HIGH_ONLY = false;
	
	private String kZName;
	
	private CoreLookupApplet exerciseApplet;
	private boolean highAndLow;
	
	private DataSet data;
	private String distnKey;
	
	private HorizAxis theAxis;
	protected CoreDistnLookupView distnView;
	private LimitEditPanel editPanel;
	
	public CoreLookupPanel(DataSet data, String distnKey, CoreLookupApplet exerciseApplet,
																																	boolean highAndLow) {
		kZName = exerciseApplet.translate("z-score");
		this.exerciseApplet = exerciseApplet;
		this.data = data;
		this.distnKey = distnKey;
		this.highAndLow = highAndLow;
		
		setupPanel();
	}
	
	public double getMin() {
		return theAxis.minOnAxis;
	}
	
	public double getMax() {
		return theAxis.maxOnAxis;
	}
	
	protected void setupPanel() {
		setLayout(new BorderLayout(0, 2));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
			
				theAxis = new HorizAxis(exerciseApplet);
				setupAxis();
			mainPanel.add("Bottom", theAxis);
			
				distnView = getDistnLookupView(data, distnKey, exerciseApplet, theAxis, highAndLow);
				distnView.lockBackground(Color.white);
			mainPanel.add("Center", distnView);
			
		add("Center", mainPanel);
		
			editPanel = new LimitEditPanel(exerciseApplet, data, distnKey, this, highAndLow);
			editPanel.setInitialValues(exerciseApplet.getMaxValue());
			editPanel.setForeground(Color.red);
			distnView.setLinkedEdit(editPanel);
		
		add("North", editPanel);
	}
	
	abstract protected CoreDistnLookupView getDistnLookupView(DataSet data, String distnKey,
																	ExerciseApplet exerciseApplet, HorizAxis theAxis, boolean highAndLow);
	
	private void setupAxis() {
		String axisInfo = exerciseApplet.getAxisInfo();
		if (axisInfo == null) {
			theAxis.readNumLabels(kZAxisInfo);
			theAxis.setAxisName(kZName);
		}
		else {
			theAxis.readNumLabels(axisInfo);
			String varName = exerciseApplet.getVarName();
			if (varName.length() > 0)
				theAxis.setAxisName("Distn of " + varName);
			theAxis.invalidate();
		}
	}
	
	public void resetPanel() {
		resetPanel(exerciseApplet.getMaxValue());
	}
	
	public void resetPanel(NumValue maxValue) {
		if (exerciseApplet.getAxisInfo() != null) {
			distnView.reset();
			setupAxis();
		}
		editPanel.setInitialValues(maxValue);
		validate();
	}
	
	public void setPending(boolean lowNotHigh, boolean pending) {
		distnView.setPending(lowNotHigh, pending);
	}
	
	public void noteChanged() {
		exerciseApplet.noteChangedWorking();
	}
	
	public NumValue getLowValue() {
		return editPanel.getLowValue();
	}
	
	public NumValue getHighValue() {
		return editPanel.getHighValue();
	}
	
	public String getStatus() {
		NumValue lowSelection = getLowValue();
		NumValue highSelection = getHighValue();
		return (lowSelection == null ? "null" : lowSelection.toString()) + " " + (highSelection == null ? "null" : highSelection.toString());
	}
	
	public void setStatus(String status) {
		StringTokenizer st = new StringTokenizer(status);
		String lowString = st.nextToken();
		NumValue lowSelection = lowString.equals("null") ? null : new NumValue(lowString);
		String highString = st.nextToken();
		NumValue highSelection = highString.equals("null") ? null : new NumValue(highString);
		
		DistnVariable distn = (DistnVariable)data.getVariable(distnKey);
		if (lowSelection != null)
			distn.setMinSelection(lowSelection.toDouble());
		if (highSelection != null)
			distn.setMaxSelection(highSelection.toDouble());
		data.variableChanged(distnKey);
		
		if (lowSelection == null)
			editPanel.setHighValue(highSelection.toDouble());
		else
			editPanel.setValues(lowSelection, highSelection);
	}
	
	public void showAnswer(NumValue lowVal, NumValue highVal) {
		if (lowVal == null)
			showAnswer(highVal, highVal);
		else if (highVal == null)
			showAnswer(lowVal, lowVal);
		else if (lowVal.toDouble() > highVal.toDouble())
			showAnswer(highVal, lowVal);
		else {
			DistnVariable distn = (DistnVariable)data.getVariable(distnKey);
			distn.setMinSelection(lowVal.toDouble());
			distn.setMaxSelection(highVal.toDouble());
			data.variableChanged(distnKey);
			
			editPanel.setValues(lowVal, highVal);
		}
	}
	
}