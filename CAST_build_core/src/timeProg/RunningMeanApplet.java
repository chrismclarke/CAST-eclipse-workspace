package timeProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;

import time.*;


class MeanRunSlider extends XValueAdjuster {
	boolean allowEvenRun;
	private int maxRun;
	
	public MeanRunSlider(String title, int maxRun, boolean allowEvenRun, XApplet applet) {
		super(title, 1, allowEvenRun ? maxRun : (maxRun / 2 + 1), 1, applet);
		this.allowEvenRun = allowEvenRun;
		this.maxRun = maxRun;
	}
	
	public void allowEvenRuns(boolean allowEvenRun) {
		int runLength = getRunLength();
		this.allowEvenRun = allowEvenRun;
		
		setValues(1, allowEvenRun ? maxRun : (maxRun / 2 + 1), allowEvenRun ? runLength : (runLength / 2 + 1));
	}
	
	protected Value translateValue(int scrollValue) {
		return new NumValue(getRunLength(scrollValue), 0);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return translateValue(getMaxValue()).stringWidth(g);
	}
	
	public int getRunLength(int scrollValue) {
		return allowEvenRun ? scrollValue : (scrollValue * 2 - 1);
	}
	
	public int getRunLength() {
		return getRunLength(getValue());
	}
}

public class RunningMeanApplet extends BasicTimeApplet {
	static final private String SMOOTH_VAR_NAME_PARAM = "smoothName";
	static final private String RUNS_PARAM = "runs";
	static final private String SHOW_ENDS_PARAM = "showEnds";
	static final private String ORDER_LABEL_PARAM = "maOrderLabel";
	
	private MeanMedianVariable theMeanVariable;
	private MeanRunSlider theRunSlider;
	
	private XCheckbox allowEvenCheck;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		String showEndsString = getParameter(SHOW_ENDS_PARAM);
		boolean showEnds = (showEndsString == null) || showEndsString.equals("true");
		theMeanVariable = new MeanMedianVariable(getParameter(SMOOTH_VAR_NAME_PARAM),
																								data, "y", showEnds);
		theMeanVariable.setExtraDecimals(2);
		data.addVariable("smooth", theMeanVariable);
		
		return data;
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
	
	protected TimeView createTimeView(TimeAxis theHorizAxis, VertAxis theVertAxis) {
		TimeView theView = new MeanTimeView(getData(), this, theHorizAxis, theVertAxis);
		theView.setSourceShading(true);
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		boolean allowEvenRuns = false;
		boolean evenOddCheck = false;
		int maxRuns = 9;
		String runInfo = getParameter(RUNS_PARAM);
		if (runInfo != null) {
			StringTokenizer theRuns = new StringTokenizer(runInfo);
			maxRuns = Integer.parseInt(theRuns.nextToken());
			if (theRuns.hasMoreTokens()) {
				String runLengthString = theRuns.nextToken();
				allowEvenRuns = runLengthString.equals("even");
				evenOddCheck = runLengthString.equals("check");
			}
		}
	
		XPanel mainPanel = super.controlPanel(data);
		
//		thePanel.setLayout(new BorderLayout(10, 3));
		mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 3));
		
		String orderLabelString = getParameter(ORDER_LABEL_PARAM);
		if (orderLabelString == null)
			orderLabelString = translate("Running mean of");
		theRunSlider = new MeanRunSlider(orderLabelString, maxRuns, allowEvenRuns, this);
		mainPanel.add(theRunSlider);
//		mainPanel.add(createShadingCheck());
		
		if (evenOddCheck) {
			XPanel thePanel = new XPanel();
			thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 6));
			
			thePanel.add(mainPanel);
			
				allowEvenCheck = new XCheckbox(translate("Allow even run length"), this);
			thePanel.add(allowEvenCheck);
			
			return thePanel;
		}
		else
			return mainPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == theRunSlider) {
			theMeanVariable.setMeanRun(theRunSlider.getRunLength());
			return true;
		}
		else if (target == allowEvenCheck) {
			theRunSlider.allowEvenRuns(allowEvenCheck.getState());
			return true;
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