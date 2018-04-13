package dotPlotProg;

import java.awt.*;

import axis.*;
import dataView.*;
import random.RandomNormal;
import utils.*;
import coreGraphics.*;


public class StackChangeApplet extends XApplet {
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String SHOW_VAR_NAME_PARAM = "showVarName";
	
	private DataSet data;
	
	private XChoice crossSizeChoice;
	
	private StackedDotPlotView theDotPlot;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 5));
		add("Center", displayPanel(data));
		
		add("South", controlPanel());
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		String valueString = getParameter(VALUES_PARAM);
		if (valueString != null)
			data.addNumVariable("y", getParameter(VAR_NAME_PARAM), valueString);
		else {
			String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
			RandomNormal generator = new RandomNormal(randomInfo);
			double vals[] = generator.generate();
			
			data.addNumVariable("y", getParameter(VAR_NAME_PARAM), vals);
		}
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			String showNameString = getParameter(SHOW_VAR_NAME_PARAM);
			if (showNameString != null && showNameString.equals("true"))
				theHorizAxis.setAxisName(data.getVariable("y").name);
			
		thePanel.add("Bottom", theHorizAxis);
		
			theDotPlot = new StackedDotPlotView(data, this, theHorizAxis);
			theDotPlot.setCrossSize(DataView.LARGE_CROSS);
		thePanel.add("Center", theDotPlot);
			theDotPlot.lockBackground(Color.white);
		
		return thePanel;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			crossSizeChoice = new XChoice(this);
			crossSizeChoice.addItem("Dot");
			crossSizeChoice.addItem("Small cross");
			crossSizeChoice.addItem("Medium cross");
			crossSizeChoice.addItem("Big cross");
			crossSizeChoice.select(3);
		thePanel.add(crossSizeChoice);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == crossSizeChoice) {
			theDotPlot.setCrossSize(crossSizeChoice.getSelectedIndex());
			data.variableChanged("y");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}