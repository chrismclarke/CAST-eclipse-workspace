package timeProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;

import time.*;


public class RunningMultiApplet extends BasicTimeApplet {
	static final private String SMOOTH_VAR_NAME_PARAM = "smoothName";
	static final private String SMOOTH2_VAR_NAME_PARAM = "smooth2Name";
	static final private String SHOW_ENDS_PARAM = "showEnds";
	static final private String DRAG_INDEX_PARAM = "dragIndex";
	
	static final private String kSmoothKey[] = {"smooth", "smooth2"};
	static final private boolean kSmooth0Only[] = {true, false};
	
	static final private Color kColor0Only[] = {Color.blue, Color.red};
	static final private Color kColor1Only[] = {Color.red, Color.blue};
	static final private Color kColorBoth[] = {Color.blue, Color.red};
	
	static final private Color kOutlierBackground = new Color(0xDDDDEE);
	
	private MeanMedianVariable theMeanVariable;
	private MeanMedianVariable theMedianVariable;
	private MedianRunSlider theRunSlider;
	
	private XCheckbox meanCheck, medianCheck, outlierCheck;
	private SmoothingKey theKey;
	private int dragIndex;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		String showEndsString = getParameter(SHOW_ENDS_PARAM);
		boolean showEnds = (showEndsString == null) || showEndsString.equals("true");
		theMeanVariable = new MeanMedianVariable(getParameter(SMOOTH_VAR_NAME_PARAM),
																								data, "y", showEnds);
		theMeanVariable.setExtraDecimals(2);
		data.addVariable(kSmoothKey[0], theMeanVariable);
		
		theMedianVariable = new MeanMedianVariable(getParameter(SMOOTH2_VAR_NAME_PARAM),
																								data, "y", showEnds);
		data.addVariable(kSmoothKey[1], theMedianVariable);
		
		dragIndex = Integer.parseInt(getParameter(DRAG_INDEX_PARAM));
		
		return data;
	}
	
	protected String getCrossKey() {
		return "y";
	}
	
	protected String[] getLineKeys() {
		String keys[] = {kSmoothKey[0]};
		return keys;
	}
	
	protected boolean showDataValue() {
		return false;
	}
	
	protected boolean showSmoothedValue() {
		return false;
	}
	
	protected TimeView createTimeView(TimeAxis theHorizAxis, VertAxis theVertAxis) {
		TimeDragView theView = new TimeDragView(getData(), this, theHorizAxis, theVertAxis);
		return theView;
	}
	
	protected XPanel valuePanel(DataSet data) {
		return null;
	}
	
	private XPanel checkPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new EqualSpacingLayout());
		
			XPanel checkPanel = new XPanel();
			checkPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
				XPanel titlePanel = new XPanel();
				titlePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
					XLabel title = new XLabel(translate("Smooth with running"), XLabel.LEFT, this);
					title.setFont(getStandardBoldFont());
				titlePanel.add(title);
				
			checkPanel.add(titlePanel);
				
				XPanel meanMedianPanel = new XPanel();
				meanMedianPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
					
					meanCheck = new XCheckbox(translate("Means"), this);
					meanCheck.setState(true);
				meanMedianPanel.add(meanCheck);
					medianCheck = new XCheckbox(translate("Medians"), this);
				meanMedianPanel.add(medianCheck);
				
			checkPanel.add(meanMedianPanel);
		
		thePanel.add(checkPanel);
				
			theRunSlider = new MedianRunSlider(translate("Run length"), 9, this);
		thePanel.add(theRunSlider);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 10, 0, 0);
		thePanel.setLayout(new BorderLayout(0, 10));
		
		thePanel.add("Center", checkPanel());
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 60, 0));
		
				theKey = new SmoothingKey(data, kSmoothKey, this, getView());
				theKey.setKeyDraw(kSmooth0Only);
				getView().setLineColors(kColor0Only);
				
			bottomPanel.add(theKey);
			
				XPanel outlierPanel = new InsetPanel(20, 5);
				outlierPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				
					outlierCheck = new XCheckbox(translate("Drag outlier"), this);
				outlierPanel.add(outlierCheck);
				outlierPanel.lockBackground(kOutlierBackground);
			
			bottomPanel.add(outlierPanel);
		
		thePanel.add("South", bottomPanel);
		
		return thePanel;
	}
	
	private void setSmoothing() {
		boolean smoothing[] = new boolean[2];
		smoothing[0] = meanCheck.getState();
		smoothing[1] = medianCheck.getState();
		theKey.setKeyDraw(smoothing);
		if (smoothing[0] && smoothing[1]) {
			getView().setSmoothedVariable(kSmoothKey[0]);
			getView().addSmoothedVariable(kSmoothKey[1]);
			getView().setLineColors(kColorBoth);
		}
		else if (smoothing[0]) {
			getView().setSmoothedVariable(kSmoothKey[0]);
			getView().setLineColors(kColor0Only);
		}
		else if (smoothing[1]) {
			getView().setSmoothedVariable(kSmoothKey[1]);
			getView().setLineColors(kColor1Only);
		}
		else
			getView().setSmoothedVariable(null);
	}

	
	private boolean localAction(Object target) {
		if (target == theRunSlider) {
			int runLength = theRunSlider.getRunLength();
			synchronized (getData()) {
				theMeanVariable.setMeanRun(runLength);
				theMedianVariable.setMedianRun(runLength);
			}
			return true;
		}
		else if (target == meanCheck) {
			setSmoothing();
			getView().repaint();
			return true;
		}
		else if (target == medianCheck) {
			setSmoothing();
			getView().repaint();
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