package graphicsProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import coreVariables.*;

import graphics.*;


public class BarVariationsApplet extends XApplet {
	static final private String PERCENT_DECS_PARAM = "percentDecs";
	static final private String AXIS_INFO_PARAM = "yAxis";
	static final private String PERCENT_INFO_PARAM = "percentAxis";
	static final private String SMALL_AXIS_FONT_PARAM = "smallAxisFont";
	static final private String CHART_Y_POWER_PARAM = "chartYPower";
	static final private String CHART_Y_NAME_PARAM = "chartYName";
	static final private String CHART_Y_DECS_PARAM = "chartYDecimals";
	
	static final protected Color kTableBackground = new Color(0xDDDDEE);
	static final protected Color kTableValueColor = new Color(0x990000);
	
	private boolean showPropns;
	private int percentDecs;
	
	private XPanel chartPanel;
	private CardLayout chartPanelLayout;
	
	private HorizBarView horizChart;
	private VertBarView vertChart;
	
	private XChoice chartChoice;
	private int currentChartIndex = 0;
	private XCheckbox showValuesCheck;
	private XCheckbox inBarsCheck;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(0, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
			
			topPanel.add(tablePanel(data));
			topPanel.add(controlPanel(data));
		
		add("North", topPanel);
		add("Center", displayPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
			NumVariable yVar = new NumVariable(getParameter(VAR_NAME_PARAM));
			yVar.readValues(getParameter(VALUES_PARAM));
		data.addVariable("y", yVar);
		data.addLabelVariable("label", getParameter(LABEL_NAME_PARAM), getParameter(LABELS_PARAM));
		
			String yPowerString = getParameter(CHART_Y_POWER_PARAM);
			int yPower = (yPowerString == null) ? 0 : Integer.parseInt(yPowerString);
			String yChartName = (yPowerString == null) ? yVar.name : getParameter(CHART_Y_NAME_PARAM);
			int yChartDecimals = (yPowerString == null) ? yVar.getMaxDecimals()
																						: Integer.parseInt(getParameter(CHART_Y_DECS_PARAM));
		
			ScaledVariable yChart = new ScaledVariable(yChartName, yVar, "y", 0.0,
																										Math.pow(10.0, -yPower), yChartDecimals);
		data.addVariable("yChart", yChart);
		
		String propnDecString = getParameter(PERCENT_DECS_PARAM);
		showPropns = propnDecString !=  null;
		percentDecs = showPropns ? Integer.parseInt(propnDecString) : 0;
		return data;
	}
	
	protected XPanel tablePanel(DataSet data) {
			XPanel innerPanel = new InsetPanel(10, 5);
			innerPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																									VerticalLayout.VERT_CENTER, 0));
		
				TableView theTable = new TableView(data, this, "y", "label", showPropns, percentDecs);
				theTable.setValueColor(kTableValueColor);
				
			innerPanel.add(theTable);
		
			innerPanel.lockBackground(kTableBackground);
		return innerPanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		chartPanel = new XPanel();
		chartPanelLayout = new CardLayout();
		chartPanel.setLayout(chartPanelLayout);
		
		chartPanel.add("vert", vertBarPanel(data));
		chartPanel.add("horiz", horizBarPanel(data));
		
		return chartPanel;
	}
	
	protected XPanel vertBarPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel barPanel = new XPanel();
			barPanel.setLayout(new AxisLayout());
			
				VertAxis valAxis = new VertAxis(this);
				String labelInfo = getParameter(AXIS_INFO_PARAM);
				valAxis.readNumLabels(labelInfo);
			barPanel.add("Left", valAxis);
			
			if (showPropns) {
				VertAxis percentAxis = new VertAxis(this);
				labelInfo = getParameter(PERCENT_INFO_PARAM);
				percentAxis.readNumLabels(labelInfo);
				barPanel.add("Right", percentAxis);
			}
			
				HorizAxis catAxis = new HorizAxis(this);
				LabelVariable labelVariable = (LabelVariable)data.getVariable("label");
				catAxis.setLabelLabels(labelVariable);
				String fontString = getParameter(SMALL_AXIS_FONT_PARAM);
				if (fontString !=  null && fontString.equals("true"))
					catAxis.setFont(getSmallFont());
			barPanel.add("Bottom", catAxis);
			
				vertChart = new VertBarView(data, this, "yChart", catAxis, valAxis);
				vertChart.lockBackground(Color.white);
			barPanel.add("Center", vertChart);
		
		thePanel.add("Center", barPanel);
		
			XPanel labelPanel = new XPanel();
			labelPanel.setLayout(new BorderLayout());
			XLabel yLabel = new XLabel(data.getVariable("yChart").name, XLabel.LEFT, this);
			yLabel.setFont(getStandardBoldFont());
			labelPanel.add("West", yLabel);
			if (showPropns) {
				XLabel percentLabel = new XLabel(translate("Percentage"), XLabel.RIGHT, this);
				percentLabel.setFont(getStandardBoldFont());
				labelPanel.add("East", percentLabel);
			}
		thePanel.add("North", labelPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			
				XLabel catLabel = new XLabel(data.getVariable("label").name, XLabel.RIGHT, this);
				catLabel.setFont(getStandardBoldFont());
			bottomPanel.add(catLabel);
			
		thePanel.add("South", bottomPanel);
		
		return thePanel;
	}
	
	
	protected XPanel horizBarPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel barPanel = new XPanel();
			barPanel.setLayout(new AxisLayout());
			
				HorizAxis valAxis = new HorizAxis(this);
				String labelInfo = getParameter(AXIS_INFO_PARAM);
				valAxis.readNumLabels(labelInfo);
			barPanel.add("Top", valAxis);
			
			if (showPropns) {
				HorizAxis percentAxis = new HorizAxis(this);
				labelInfo = getParameter(PERCENT_INFO_PARAM);
				percentAxis.readNumLabels(labelInfo);
				barPanel.add("Bottom", percentAxis);
			}
			
				VertAxis catAxis = new VertAxis(this);
				LabelVariable labelVariable = (LabelVariable)data.getVariable("label");
				catAxis.setLabelLabels(labelVariable);
			barPanel.add("Left", catAxis);
			
				horizChart = new HorizBarView(data, this, "yChart", catAxis, valAxis);
				horizChart.lockBackground(Color.white);
			barPanel.add("Center", horizChart);
		
		thePanel.add("Center", barPanel);
		
			XPanel labelPanel = new XPanel();
			labelPanel.setLayout(new BorderLayout());
				XLabel catLabel = new XLabel(data.getVariable("label").name, XLabel.RIGHT, this);
				catLabel.setFont(getStandardBoldFont());
			labelPanel.add("West", catLabel);
				XLabel yLabel = new XLabel(data.getVariable("yChart").name, XLabel.LEFT, this);
				yLabel.setFont(getStandardBoldFont());
			labelPanel.add("East", yLabel);
		thePanel.add("North", labelPanel);
		
		if (showPropns) {
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			
				XLabel percentLabel = new XLabel(translate("Percentage"), XLabel.RIGHT, this);
				percentLabel.setFont(getStandardBoldFont());
			bottomPanel.add(percentLabel);
			
			thePanel.add("South", bottomPanel);
		}
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			chartChoice = new XChoice(this);
			chartChoice.addItem(translate("Vertical bars"));
			chartChoice.addItem(translate("Horizontal bars"));
		thePanel.add(chartChoice);
		
			showValuesCheck = new XCheckbox(translate("Show values"), this);
		thePanel.add(showValuesCheck);
			
			inBarsCheck = new XCheckbox(translate("Inside bars"), this);
			inBarsCheck.disable();
		thePanel.add(inBarsCheck);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == chartChoice) {
			int newChoice = chartChoice.getSelectedIndex();
			if (newChoice != currentChartIndex) {
				currentChartIndex = newChoice;
				chartPanelLayout.show(chartPanel, (newChoice == 0) ? "vert" : "horiz");
			}
			return true;
		}
		if (target == showValuesCheck) {
			vertChart.setShowValues(showValuesCheck.getState());
			vertChart.repaint();
			horizChart.setShowValues(showValuesCheck.getState());
			horizChart.repaint();
			if (showValuesCheck.getState())
				inBarsCheck.enable();
			else
				inBarsCheck.disable();
			return true;
		}
		if (target == inBarsCheck) {
			horizChart.setValuesInBars(inBarsCheck.getState());
			horizChart.repaint();
			vertChart.setValuesInBars(inBarsCheck.getState());
			vertChart.repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}