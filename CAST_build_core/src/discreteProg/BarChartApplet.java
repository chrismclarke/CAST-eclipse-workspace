package discreteProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import histo.*;


public class BarChartApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String CLASS_INFO_PARAM = "classInfo";
	
	static final private String DENSITY_AXIS_INFO_PARAM = "densityAxis";
	static final private String COUNT_AXIS_INFO_PARAM = "countAxis";
	static final private String PROPN_AXIS_INFO_PARAM = "propnAxis";
	
	private BarChartView theBarChart;
	private DensityAxis2 theDensityAxis;
	private XLabel densityAxisNameLabel;
	
//	private XButton animateButton;
	private XSlider animateSlider;
	
	private XChoice axisTypeChoice;
	private int currentAxisType;
	
	public void setupApplet() {
//		ScrollImages.loadScroll(this);
		
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		setLayout(new BorderLayout());
		
			densityAxisNameLabel = new XLabel("", XLabel.LEFT, this);
			densityAxisNameLabel.setFont(getStandardBoldFont());
			
		add("North", densityAxisNameLabel);
		add("Center", createHisto(data));
		add("South", createControls(data));
		densityAxisNameLabel.setText(theDensityAxis.getAxisName());
	}
	
	private XPanel createHisto(DataSet data) {
		XPanel histoPanel = new XPanel();
		histoPanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			
		histoPanel.add("Bottom", theHorizAxis);
		
			String densityAxisInfo = getParameter(DENSITY_AXIS_INFO_PARAM);
			String countAxisInfo = getParameter(COUNT_AXIS_INFO_PARAM);
			String propnAxisInfo = getParameter(PROPN_AXIS_INFO_PARAM);
			theDensityAxis = new DensityAxis2(DensityAxis2.COUNT_LABELS, densityAxisInfo,
																		countAxisInfo, propnAxisInfo, this);
		histoPanel.add("Left", theDensityAxis);
		
			String classInfo = getParameter(CLASS_INFO_PARAM);
			StringTokenizer theParams = new StringTokenizer(classInfo);
			double class0Start = Double.parseDouble(theParams.nextToken());
			double classWidth = Double.parseDouble(theParams.nextToken());
		
			theBarChart = new BarChartView(data, this, theHorizAxis, theDensityAxis, class0Start, classWidth);
			theBarChart.setBarType(HistoView.VERT_BARS);
			theBarChart.lockBackground(Color.white);
			
		histoPanel.add("Center", theBarChart);
		
		return histoPanel;
	}
	
	private XPanel createControls(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new BorderLayout(5, 2));
		
		XPanel buttonPanel = new XPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));
		
		axisTypeChoice = new XChoice(this);
		axisTypeChoice.addItem(translate("Frequency"));
		axisTypeChoice.addItem(translate("Relative freq"));
		axisTypeChoice.select(0);
		buttonPanel.add(axisTypeChoice);
		
//		animateButton = new XButton("Animate Grouping", this);
//		buttonPanel.add(animateButton);
		
		controlPanel.add("West", buttonPanel);
		
		animateSlider = new XNoValueSlider(translate("Histogram"), translate("Bar chart"), null, 0, BarChartView.kBarIndex,
																																							0, this);
		controlPanel.add("Center", animateSlider);
		
		return controlPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == animateSlider) {
			theBarChart.setFrame(animateSlider.getValue());
			return true;
		}
//		else if (target == animateButton) {
//			theBarChart.doAnimation(animateSlider);
//			return true;
//		}
		else if (target == axisTypeChoice) {
			if (axisTypeChoice.getSelectedIndex() != currentAxisType) {
				currentAxisType = axisTypeChoice.getSelectedIndex();
				int axisType = (currentAxisType == 0) ? DensityAxis2.COUNT_LABELS
																	: DensityAxis2.REL_FREQ_LABELS;
				theDensityAxis.changeLabelType(axisType);
				densityAxisNameLabel.setText(theDensityAxis.getAxisName());
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}