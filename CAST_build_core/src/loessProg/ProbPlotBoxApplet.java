package loessProg;

import java.awt.*;

import dataView.*;
import utils.*;
import coreGraphics.*;

import loess.*;
import boxPlot.*;


public class ProbPlotBoxApplet extends ScatterApplet {
	private String yValues[];
	
	private XChoice dataChoice;
	private int currentDataIndex = 0;
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = super.displayPanel(data);
		
		BoxView xBoxView = new BoxDotHiliteView(data, this, theHorizAxis);
		xBoxView.setActiveNumVariable("x");
		thePanel.add("BottomMargin", xBoxView);
		
		BoxView yBoxView = new BoxDotHiliteView(data, this, theVertAxis);
		yBoxView.setActiveNumVariable("y");
		thePanel.add("LeftMargin", yBoxView);
		
		return thePanel;
	}
	
	protected DataSet readCoreData() {
		DataSet data = new DataSet();
		
		dataChoice = new XChoice(this);
		int noOfDataSets = 0;
		while (true) {
			String paramName = Y_VAR_NAME_PARAM;
			if (noOfDataSets > 0)
				paramName += (noOfDataSets + 1);
			String varName = getParameter(paramName);
			if (varName == null)
				break;
			else {
				dataChoice.addItem(varName);
				noOfDataSets ++;
			}
		}
		yValues = new String[noOfDataSets];
		
		for (int i=0 ; i<noOfDataSets ; i++) {
			String paramName = Y_VALUES_PARAM;
			if (i > 0)
				paramName += (i+1);
			yValues[i] = getParameter(paramName);
		}
		
		data.addNumVariable("y", dataChoice.getItem(0), yValues[0]);
		
		data.addVariable("x", new NormalScoreVariable(getParameter(X_VAR_NAME_PARAM), data, "y", 0));
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));		//	label needs to be wider than initial label
		if (labelAxes) {
			yVariateName = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
			yVariateName.setFont(theVertAxis.getFont());
			thePanel.add("Center", yVariateName);
		}
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER));
		
		thePanel.add(dataChoice);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == dataChoice) {
			int newDataIndex = dataChoice.getSelectedIndex();
			if (newDataIndex != currentDataIndex) {
				currentDataIndex = newDataIndex;
				NumVariable yVar = (NumVariable)data.getVariable("y");
				yVar.readValues(yValues[currentDataIndex]);
				data.variableChanged("y");
				yVariateName.setText(dataChoice.getSelectedItem());
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}