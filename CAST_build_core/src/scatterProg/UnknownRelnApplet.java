package scatterProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import imageGroups.TickCrossImages;
import coreGraphics.*;


public class UnknownRelnApplet extends ScatterApplet {
	protected ScatterUnknownView theView;
	private DotPlotView yDotView;
	
	private XChoice dataTypeChoice;
	private int currentChoice;
	private int noOfOptions;
	
	public void setupApplet() {
		TickCrossImages.loadCrossAndTick(this);
		super.setupApplet();
	}
	
	protected DataSet readData() {
		DataSet data = readCoreData();
		
		noOfOptions = 1;
		while (true) {
			String nextVals = getParameter("y" + (noOfOptions+1) + "Values");
			String nextName = getParameter("y" + (noOfOptions+1) + "VarName");
			if (nextVals == null || nextName == null)
				break;
			noOfOptions++;
			data.addNumVariable("y" + noOfOptions, nextName, nextVals);
		}
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = super.displayPanel(data);
		
		DotPlotView xDotView = new DotPlotView(data, this, theHorizAxis, 0.6);
		xDotView.setActiveNumVariable("x");
		thePanel.add("BottomMargin", xDotView);
		
		yDotView = new DotPlotView(data, this, theVertAxis, 0.6);
		yDotView.setActiveNumVariable("y");
		thePanel.add("LeftMargin", yDotView);
		
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		theView = new ScatterUnknownView(data, this, theHorizAxis, theVertAxis, "x", null);
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));
		
		dataTypeChoice = new XChoice(this);
		dataTypeChoice.addItem("unknown relationship");
		dataTypeChoice.addItem(data.getVariable("y").name);
		for (int i=2 ; i<=noOfOptions ; i++)
			dataTypeChoice.addItem(data.getVariable("y" + i).name);
		dataTypeChoice.select(0);
		currentChoice = 0;
		controlPanel.add(dataTypeChoice);
		
		return controlPanel;
	}

	
	private boolean localAction(Object target) {
		if (target == dataTypeChoice) {
			int newDataIndex = dataTypeChoice.getSelectedIndex();
			if (newDataIndex != currentChoice) {
				String newKey = (newDataIndex == 0) ? null
									: (newDataIndex == 1) ? "y"
									: "y" + newDataIndex;
				theView.changeVariables(newKey, null);
				currentChoice = newDataIndex;
				data.variableChanged("y");
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