package timeProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;

import time.*;


public class RunningMMApplet extends BasicTimeApplet {
	static final private String SMOOTH_VAR_NAME_PARAM = "smoothName";
	static final private String SHOW_ENDS_PARAM = "showEnds";
	static final private String DRAG_INDEX_PARAM = "dragIndex";
	
	private MeanMedianVariable theSmoothedVariable;
	private MeanRunSlider theMeanSlider;
	private MedianRunSlider theMedianSlider;
	
	private XCheckbox outlierCheck;
	
	private int dragIndex;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		String showEndsString = getParameter(SHOW_ENDS_PARAM);
		boolean showEnds = (showEndsString == null) || showEndsString.equals("true");
		theSmoothedVariable = new MeanMedianVariable(getParameter(SMOOTH_VAR_NAME_PARAM),
																								data, "y", showEnds);
		data.addVariable("smooth", theSmoothedVariable);
		
		dragIndex = Integer.parseInt(getParameter(DRAG_INDEX_PARAM));
		
		return data;
	}
	
	protected TimeView createTimeView(TimeAxis theHorizAxis, VertAxis theVertAxis) {
		TimeDragView theView = new TimeDragView(getData(), this, theHorizAxis, theVertAxis);
		return theView;
	}
	
	protected XPanel valuePanel(DataSet data) {
		return null;
	}
	
	protected String getCrossKey() {
		return "y";
	}
	
	protected String[] getLineKeys() {
		String keys[] = {"smooth"};
		return keys;
	}
	
	protected boolean showDataValue() {
		return true;
	}
	
	protected boolean showSmoothedValue() {
		return true;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		XPanel sliderPanel = new XPanel();
		sliderPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 3));
		
		theMedianSlider = new MedianRunSlider(translate("Running median of"), 9, this);
		sliderPanel.add(theMedianSlider);
		theMeanSlider = new MeanRunSlider("..." + translate("then mean of"), 9, false, this);
		sliderPanel.add(theMeanSlider);
		
		thePanel.add("Center", sliderPanel);
		
			XPanel bottomPanel = new InsetPanel(0, 10, 0, 0);
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
				outlierCheck = new XCheckbox(translate("Drag outlier"), this);
		
			bottomPanel.add(outlierCheck);
		
		thePanel.add("South", bottomPanel);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == theMedianSlider) {
			theSmoothedVariable.setMedianRun(theMedianSlider.getRunLength());
			return true;
		}
		else if (target == theMeanSlider) {
			theSmoothedVariable.setMeanRun(theMeanSlider.getRunLength());
			return true;
		}
		else if (target == outlierCheck) {
			if (outlierCheck.getState())
				((TimeDragView)getView()).setDragIndex(dragIndex);
			else
				((TimeDragView)getView()).setDragIndex(-1);
			getView().repaint();
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean  action(Event  evt, Object  what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}