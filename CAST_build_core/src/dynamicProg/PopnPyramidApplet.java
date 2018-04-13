package dynamicProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import dynamic.*;


public class PopnPyramidApplet extends XApplet {
	static final private String YEARS_PARAM = "years";
	static final private String YEAR_LABELS_PARAM = "yearLabels";
	static final protected String FREQ_AXIS_PARAM = "freqAxis";
	static final protected String FREQ_AXIS_NAME_PARAM = "freqAxisName";
	static final private String LEFT_VAR_NAME_PARAM = "leftVarName";
	static final private String LEFT_VALUES_PARAM = "leftValues";
	static final private String RIGHT_VAR_NAME_PARAM = "rightVarName";
	static final private String RIGHT_VALUES_PARAM = "rightValues";
	
	
	private DataSet data;
	
	protected PyramidView theView;
	
	protected int startYear, endYear, yearStep;
	protected YearSlider yearSlider;
	
	
	public void setupApplet() {
		data = readData();
		readYearInfo();
		
		setLayout(new BorderLayout(0, 10));
		
		add("Center", displayPanel(data));
		
		add("North", topPanel(data));
		
		add("South", controlPanel(data));
	}
	
	protected DataSet readData() {
		data = new DataSet();
		
			NumSeriesVariable leftVar = new NumSeriesVariable(getParameter(LEFT_VAR_NAME_PARAM));
			leftVar.readValues(getParameter(LEFT_VALUES_PARAM));
		
		data.addVariable("left", leftVar);
		
			NumSeriesVariable rightVar = new NumSeriesVariable(getParameter(RIGHT_VAR_NAME_PARAM));
			rightVar.readValues(getParameter(RIGHT_VALUES_PARAM));
		
		data.addVariable("right", rightVar);
		
		return data;
	}
	
	private void readYearInfo() {
		StringTokenizer st = new StringTokenizer(getParameter(YEARS_PARAM));
		startYear = Integer.parseInt(st.nextToken());
		endYear = Integer.parseInt(st.nextToken());
		yearStep = Integer.parseInt(st.nextToken());
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			StringTokenizer st = new StringTokenizer(getParameter(FREQ_AXIS_PARAM));
			int classWidth = Integer.parseInt(st.nextToken());
			int freqMax = Integer.parseInt(st.nextToken());
			int axisMax = Integer.parseInt(st.nextToken());
			int axisStep = Integer.parseInt(st.nextToken());
			LabelValue freqLabel = new LabelValue(getParameter(FREQ_AXIS_NAME_PARAM));
		
			theView = new PyramidView(data, this, "left", "right", classWidth, freqMax, axisMax, axisStep, freqLabel);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected String yearSliderName() {
		return "Year";
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 4));
			
			StringTokenizer st = new StringTokenizer(getParameter(YEAR_LABELS_PARAM));
			int startYearLabel = Integer.parseInt(st.nextToken());
			int labelStep = Integer.parseInt(st.nextToken());
		
			yearSlider = new YearSlider(yearSliderName(), startYear, endYear, startYear, yearStep,
																															startYearLabel, labelStep, this);
			
		thePanel.add(yearSlider);
		
		return thePanel;
	}
	
	protected void yearIndexChange() {
		double yearIndex = (yearSlider.getYear() - startYear) / (double)yearStep;
		
		NumSeriesVariable leftVar = (NumSeriesVariable)data.getVariable("left");
		leftVar.setSeriesIndex(yearIndex);
		
		NumSeriesVariable rightVar = (NumSeriesVariable)data.getVariable("right");
		rightVar.setSeriesIndex(yearIndex);
	}
	
	private boolean localAction(Object target) {
		if (target == yearSlider) {
			yearIndexChange();
			data.variableChanged("left");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}