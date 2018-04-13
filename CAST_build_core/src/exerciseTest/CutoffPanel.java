package exerciseTest;

import java.awt.*;
import java.util.*;

import exercise2.*;
import dataView.*;
import coreGraphics.*;
import utils.*;


public class CutoffPanel extends XPanel implements StatusInterface {
	static final public int LOW_TAIL = 0;
	static final public int HIGH_TAIL = 1;
	
	static final public int HORIZONTAL = 0;
	static final public int VERTICAL = 1;
	
	private ExerciseApplet applet;
//	private DataSet theData;
//	private String yKey;
	
	private XNumberEditPanel cutoffEdit;
	private PropnOneSideView propnLessEq, propnMoreEq;
	
	private DataView linkedView;
	
	public CutoffPanel(DataSet theData, String yKey, ExerciseApplet applet) {
		this(theData, yKey, HORIZONTAL, applet);
	}
	
	public CutoffPanel(DataSet theData, String yKey, int orientation, ExerciseApplet applet) {
		this.applet = applet;
		
		cutoffEdit = new XNumberEditPanel("Cutoff =", "-0.5", 5, applet);
		cutoffEdit.setFont(applet.getStandardBoldFont());
		propnLessEq = new PropnOneSideView(theData, yKey, PropnOneSideView.LESS_EQUAL, null, applet);
		propnMoreEq = new PropnOneSideView(theData, yKey, PropnOneSideView.GREATER_EQUAL, null, applet);
		
		XPanel editPlus = new InsetPanel(3, 3);
		editPlus.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		editPlus.add(cutoffEdit);
		editPlus.lockBackground(ExerciseApplet.kWorkingBackground);
		
		if (orientation == HORIZONTAL) {
			setLayout(new BorderLayout(0, 3));
				
				XPanel editPanel = new XPanel();
				editPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				editPanel.add(editPlus);
				
			add("North", editPanel);
			
				XPanel propnPanel = new XPanel();
				propnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
				
				propnPanel.add(propnLessEq);
				propnPanel.add(propnMoreEq);
				
			add("Center", propnPanel);
		}
		else {
			setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
			add(editPlus);
			add(propnLessEq);
			add(propnMoreEq);
		}
	}
	
	public NumValue getCutoff() {
		return cutoffEdit.getNumValue();
	}
	
	public void setLinkedView(DataView linkedView) {
		this.linkedView = linkedView;
	}
	
	public void setDiscrete(boolean discrete) {
		propnLessEq.setDiscrete(discrete);
		propnMoreEq.setDiscrete(discrete);
	}
	
	public void setVariableName(String variableName) {
		propnLessEq.setVariableName(variableName);
		propnMoreEq.setVariableName(variableName);
	}
	
	private void highlightLinkedView(boolean hiliteBackground, double highlightVal, boolean lowHighlight, Color highlightBackgroundColor) {
		BackgroundHiliteInterface linkedHiliteView = (BackgroundHiliteInterface)linkedView;
		linkedHiliteView.setHighlightBackground(hiliteBackground);
		linkedHiliteView.setCrossHighlight(highlightVal, lowHighlight ? StackedDiscreteView.LOW_HIGHLIGHT
																																				: StackedDiscreteView.HIGH_HIGHLIGHT);
		if (highlightBackgroundColor != null)
			linkedHiliteView.setHighlightColor(highlightBackgroundColor);
	}
	
	public void clearCutoff() {
		cutoffEdit.clearValue();
		propnLessEq.setCutoff(null);
		propnMoreEq.setCutoff(null);
		
		highlightLinkedView(false, Double.NEGATIVE_INFINITY, true, Color.white);
		linkedView.repaint();
	}
	
	private void setCutoffFromEdit() {
		NumValue cutoff = cutoffEdit.getNumValue();
		
		propnLessEq.setCutoff(cutoff);
		propnMoreEq.setCutoff(cutoff);
		validate();
		repaint();
		
		highlightLinkedView(true, cutoff.toDouble(), true, Color.white);
		linkedView.repaint();
	}
	
	public void setCorrectCutoff(NumValue cutoff, int tail) {
		cutoffEdit.setDoubleValue(cutoff);
		propnLessEq.setCutoff(cutoff);
		propnMoreEq.setCutoff(cutoff);
		validate();
		repaint();
		
		highlightLinkedView(true, cutoff.toDouble(), tail == LOW_TAIL, StackedDiscreteView.kPaleRedColor);
		linkedView.repaint();
	}
	
	public String getStatus() {
		NumValue cutoff = cutoffEdit.getNumValue();
		return cutoff.toDouble() + " " + cutoff.decimals;
	}
	
	public void setStatus(String status) {
		StringTokenizer st = new StringTokenizer(status);
		if (st.hasMoreTokens()) {
			NumValue cutoff = new NumValue(Double.parseDouble(st.nextToken()), Integer.parseInt(st.nextToken()));
			cutoffEdit.setDoubleValue(cutoff);
			setCutoffFromEdit();
		}
		else
			clearCutoff();
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (evt.target == cutoffEdit) {
			if (cutoffEdit.isClear() || evt.arg != null && ((Boolean)evt.arg).booleanValue()) {
				propnLessEq.setCutoff(null);
				propnMoreEq.setCutoff(null);
				validate();
				
				highlightLinkedView(false, Double.NEGATIVE_INFINITY, true, null);
				linkedView.repaint();
			}
			else
				setCutoffFromEdit();
			
			applet.noteChangedWorking();
			return true;
		}
		else
			return super.action(evt, what);
	}
	
}