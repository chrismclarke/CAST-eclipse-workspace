package inferenceProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import imageGroups.TickCrossImages;
import coreGraphics.*;

import inference.*;


public class UnknownPopApplet extends XApplet {
	static final protected String AXIS_INFO_PARAM = "horizAxis";
	static final protected String INFO_PARAM = "info";
	static final protected String TEXT_WIDTH_PARAM = "textWidth";
	
	private DataSet data;
	
	private HorizAxis popnAxis, sampAxis;
	
	private String varName[];
	private String varValues[];
	private String axisInfo[];
	private String info[];
	
	private XChoice dataSetChoice;
	private int dataSetSelection = 0;
	private XTextArea textInfo;
	
	public void setupApplet() {
		TickCrossImages.loadCrossAndTick(this);
		
		data = getData();
		
		setLayout(new BorderLayout());
		add("South", controlPanel(data));
		add("Center", displayPanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		int noOfDataSets = 0;
		while (true) {
			String suffix = (noOfDataSets == 0) ? "" : String.valueOf(noOfDataSets+1);
			String temp = getParameter(VAR_NAME_PARAM + suffix);
			if (temp == null)
				break;
			else
				noOfDataSets ++;
		}
		varName = new String[noOfDataSets];
		varValues = new String[noOfDataSets];
		axisInfo = new String[noOfDataSets];
		info = new String[noOfDataSets];
		for (int i=0 ; i<noOfDataSets ; i++) {
			String suffix = (i == 0) ? "" : String.valueOf(i+1);
			varName[i] = getParameter(VAR_NAME_PARAM + suffix);
			varValues[i] = getParameter(VALUES_PARAM + suffix);
			axisInfo[i] = getParameter(AXIS_INFO_PARAM + suffix);
			info[i] = getParameter(INFO_PARAM + suffix);
		}
		
		data.addNumVariable("y", varName[0], varValues[0]);
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		
		thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL, ProportionLayout.REMAINDER));
		
		popnAxis = getAxis();
		StackedDotUnknownView popnView = new StackedDotUnknownView(data, this, popnAxis, null, true);
		thePanel.add("Top", dotPlotPanel(popnAxis, popnView, translate("Population")));
		
		sampAxis = getAxis();
		StackedDotPlotView sampView = new StackedDotPlotView(data, this, sampAxis);
		thePanel.add("Bottom", dotPlotPanel(sampAxis, sampView, translate("Sample")));
		
		return thePanel;
	}
	
	private XPanel dotPlotPanel(HorizAxis axis, StackedDotPlotView view, String title) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		XLabel theLabel = new XLabel(title, XLabel.CENTER, this);
		theLabel.setFont(getBigBoldFont());
		thePanel.add("North", theLabel);
		
		XPanel mainPanel = new XPanel();
		mainPanel.setLayout(new AxisLayout());
		
		mainPanel.add("Bottom", axis);
		
		view.lockBackground(Color.white);
		mainPanel.add("Center", view);
		
		thePanel.add("Center", mainPanel);
		
		return thePanel;
	}
	
	private HorizAxis getAxis() {
		HorizAxis theHorizAxis = new HorizAxis(this);
		theHorizAxis.readNumLabels(axisInfo[0]);
		theHorizAxis.setAxisName(varName[0]);
		return theHorizAxis;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
		
		int infoWidth = Integer.parseInt(getParameter(TEXT_WIDTH_PARAM));
		textInfo = new XTextArea(info, 0, infoWidth, this);
		textInfo.lockBackground(Color.white);
		textInfo.setFont(getStandardFont());
		controlPanel.add(textInfo);
		
		dataSetChoice = new XChoice(this);
		for (int i=0 ; i<varName.length ; i++)
			dataSetChoice.addItem(varName[i]);
		controlPanel.add(dataSetChoice);
		
		return controlPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == dataSetChoice) {
			int newChoice = dataSetChoice.getSelectedIndex();
			if (newChoice != dataSetSelection) {
				dataSetSelection = newChoice;
				
				popnAxis.readNumLabels(axisInfo[dataSetSelection]);
				popnAxis.setAxisName(varName[dataSetSelection]);
				popnAxis.repaint();
				
				sampAxis.readNumLabels(axisInfo[dataSetSelection]);
				sampAxis.setAxisName(varName[dataSetSelection]);
				sampAxis.repaint();
				
				NumVariable y = (NumVariable)data.getVariable("y");
				y.name = varName[dataSetSelection];
				y.readValues(varValues[dataSetSelection]);
				data.variableChanged("y");
				
				textInfo.setText(dataSetSelection);
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