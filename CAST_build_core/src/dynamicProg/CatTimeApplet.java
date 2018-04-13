package dynamicProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;

import cat.*;
import dynamic.*;


public class CatTimeApplet extends XApplet {
	static final private String YEARS_PARAM = "years";
	static final private String YEAR_LABELS_PARAM = "yearLabels";
	static final private String YEAR_NAME_PARAM = "yearName";
	
	static final private String COUNT_AXIS_INFO_PARAM = "countAxis";
	static final private String CUM_AXIS_INFO_PARAM = "cumCountAxis";
	static final private String PERCENT_AXIS_INFO_PARAM = "percentAxis";
	static final private String YEAR_AXIS_INFO_PARAM = "yearAxis";
	
	static final private String kPercentageAxisInfo = "0 100 0 20";
	
	private String kPercentageString;
	
	private DataSet data;
	
	private PieSizeView thePieChart;
	private BarSizeView theBarChart;
	private PropnSeriesView theSeriesChart;
	
	private XLabel barFreqAxisTitle, timeFreqAxisTitle;
	private MultiVertAxis barVertAxis, seriesVertAxis;
	
	private XPanel chartPanel;
	private CardLayout chartPanelLayout;
	
	private int startYear, endYear, yearStep;
	private YearSlider yearSlider;
	
	private XCheckbox onlyPercentagesCheck;
	private XChoice chartChoice;
	private int currentChartChoice;
	
	
	public void setupApplet() {
		kPercentageString = translate("Percentage");
		data = readData();
		
		setLayout(new BorderLayout(0, 10));
		
		add("North", keyPanel(data));
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		readYearInfo();
		
			NumSeriesVariable yVar = new NumSeriesVariable(getParameter(VAR_NAME_PARAM));
			yVar.readValues(getParameter(VALUES_PARAM));
		
		data.addVariable("y", yVar);
		
		data.addCatVariable("key", getParameter(CAT_NAME_PARAM), getParameter(CAT_VALUES_PARAM),
																																getParameter(CAT_LABELS_PARAM));
		
		return data;
	}
	
	private void readYearInfo() {
		StringTokenizer st = new StringTokenizer(getParameter(YEARS_PARAM));
		startYear = Integer.parseInt(st.nextToken());
		endYear = Integer.parseInt(st.nextToken());
		yearStep = Integer.parseInt(st.nextToken());
	}
	
	protected XPanel piePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			thePieChart = new PieSizeView(data, this, "y", CatKey3View.kCatColour);
		thePanel.add("Center", thePieChart);
		
		return thePanel;
	}
	
	private XPanel barPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			barFreqAxisTitle = new XLabel(data.getVariable("y").name, XLabel.LEFT, this);
		thePanel.add("North", barFreqAxisTitle);
			
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
			
				HorizAxis theHorizAxis = new HorizAxis(this);
					CatVariable keyVar = (CatVariable)data.getVariable("key");
				theHorizAxis.setCatLabels(keyVar);
				theHorizAxis.setAxisName(keyVar.name);
			mainPanel.add("Bottom", theHorizAxis);
			
				barVertAxis = new MultiVertAxis(this, 2);
				barVertAxis.readNumLabels(getParameter(COUNT_AXIS_INFO_PARAM));
				barVertAxis.readExtraNumLabels(getParameter(PERCENT_AXIS_INFO_PARAM));
				barVertAxis.setChangeMinMax(true);
			mainPanel.add("Left", barVertAxis);
			
				theBarChart = new BarSizeView(data, this, "y", CatKey3View.kCatColour, theHorizAxis, barVertAxis);
				theBarChart.lockBackground(Color.white);
			mainPanel.add("Center", theBarChart);
		
		thePanel.add("Center", mainPanel);
		
		return thePanel;
	}
	
	private XPanel seriesPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			timeFreqAxisTitle = new XLabel(data.getVariable("y").name, XLabel.LEFT, this);
		thePanel.add("North", timeFreqAxisTitle);
			
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
			
				HorizAxis theHorizAxis = new HorizAxis(this);
				theHorizAxis.readNumLabels(getParameter(YEAR_AXIS_INFO_PARAM));
				theHorizAxis.setAxisName(getParameter(YEAR_NAME_PARAM));
			mainPanel.add("Bottom", theHorizAxis);
			
				seriesVertAxis = new MultiVertAxis(this, 2);
				seriesVertAxis.readNumLabels(getParameter(CUM_AXIS_INFO_PARAM));
				seriesVertAxis.readExtraNumLabels(kPercentageAxisInfo);
				seriesVertAxis.setChangeMinMax(true);
			mainPanel.add("Left", seriesVertAxis);
			
				theSeriesChart = new PropnSeriesView(data, this, "y", CatKey3View.kCatColour,
																				theHorizAxis, seriesVertAxis, startYear, yearStep);
				theSeriesChart.lockBackground(Color.white);
			mainPanel.add("Center", theSeriesChart);
		
		thePanel.add("Center", mainPanel);
		
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		chartPanel = new XPanel();
		chartPanelLayout = new CardLayout();
		chartPanel.setLayout(chartPanelLayout);
		
		chartPanel.add("pie", piePanel(data));
		chartPanel.add("bar", barPanel(data));
		chartPanel.add("series", seriesPanel(data));
		
		return chartPanel;
	}
	
	protected XPanel keyPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
		
			XPanel keyPanel = new XPanel();
			keyPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			keyPanel.add(new SimpleTableView(data, this, "y", "key", CatKey3View.kCatColour, 1));
		thePanel.add(keyPanel);
			
			XPanel controlPanel = new XPanel();
			controlPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
				
				onlyPercentagesCheck = new XCheckbox(translate("Only show percentages"), this);
			controlPanel.add(onlyPercentagesCheck);
				
				XPanel choicePanel = new XPanel();
				choicePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
				
					XLabel chartLabel = new XLabel(translate("Display data as") + ":", XLabel.LEFT, this);
					chartLabel.setFont(getStandardBoldFont());
				choicePanel.add(chartLabel);
				
					chartChoice = new XChoice(this);
					chartChoice.addItem(translate("Pie chart"));
					chartChoice.addItem(translate("Bar chart"));
					chartChoice.addItem(translate("Time series"));
				choicePanel.add(chartChoice);
			
			controlPanel.add(choicePanel);
			
		thePanel.add(controlPanel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 4));
		
			StringTokenizer st = new StringTokenizer(getParameter(YEAR_LABELS_PARAM));
			int startYearLabel = Integer.parseInt(st.nextToken());
			int labelStep = Integer.parseInt(st.nextToken());
		
			yearSlider = new YearSlider("Year", startYear, endYear, startYear, yearStep,
																															startYearLabel, labelStep, this);
			
		thePanel.add(yearSlider);
		
		return thePanel;
	}
	
	private void yearIndexChange() {
		double yearIndex = (yearSlider.getYear() - startYear) / (double)yearStep;
		
		NumSeriesVariable yVar = (NumSeriesVariable)data.getVariable("y");
		yVar.setSeriesIndex(yearIndex);
	}
	
	private boolean localAction(Object target) {
		if (target == yearSlider) {
			yearIndexChange();
			data.variableChanged("y");
			return true;
		}
		else if (target == chartChoice) {
			int newChoice = chartChoice.getSelectedIndex();
			if (newChoice != currentChartChoice) {
				currentChartChoice = newChoice;
				chartPanelLayout.show(chartPanel, (newChoice == 0) ? "pie" : (newChoice == 1) ? "bar" : "series");
			}
			return true;
		}
		else if (target == onlyPercentagesCheck) {
			thePieChart.setOnlyProportions(onlyPercentagesCheck.getState());
			thePieChart.repaint();

			theBarChart.setOnlyProportions(onlyPercentagesCheck.getState());
			barVertAxis.setAlternateLabels(onlyPercentagesCheck.getState() ? 1 : 0);
			theBarChart.repaint();
			barFreqAxisTitle.setText(onlyPercentagesCheck.getState() ? kPercentageString : data.getVariable("y").name);
			
			theSeriesChart.setOnlyProportions(onlyPercentagesCheck.getState());
			seriesVertAxis.setAlternateLabels(onlyPercentagesCheck.getState() ? 1 : 0);
			theSeriesChart.repaint();
			timeFreqAxisTitle.setText(onlyPercentagesCheck.getState() ? kPercentageString : data.getVariable("y").name);
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}