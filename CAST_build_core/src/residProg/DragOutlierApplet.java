package residProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import models.*;

import regnProg.*;
import resid.*;


public class DragOutlierApplet extends MultipleScatterApplet {
	static final protected String RESID_AXIS_INFO_PARAM = "residAxis";
	
	protected DragLeverageView dragView;
	
	private XChoice lineAdjustChoice;
	private int currentChoice = 0;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
			LinearModel lsLine = new LinearModel("LS line", data, "x");
			lsLine.updateLSParams("y");
		data.addVariable("lsLine", lsLine);
		
			ResidValueVariable resid = new ResidValueVariable(translate("Residual"), data, "x", "y",
																			"lsLine", 3);
		data.addVariable("resid", resid);
		
		data.setSelection(resid.noOfValues() - 1);
		
		return data;
	}
	
	protected XPanel choicePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
			lineAdjustChoice = new XChoice(this);
			lineAdjustChoice.addItem(translate("What you would like to see") + "...");
			lineAdjustChoice.addItem(translate("What you actually get") + "...");
			
			thePanel.add(lineAdjustChoice);
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout (new BorderLayout());
		
		thePanel.add("North", choicePanel());
		
			XPanel mainPanel = new XPanel();
			
			mainPanel.setLayout(new ProportionLayout(0.5, 5));
				
			mainPanel.add(ProportionLayout.LEFT, createPlotPanel(data, false, "x", "y", null,
								getParameter(X_AXIS_INFO_PARAM), getParameter(Y_AXIS_INFO_PARAM), 0));
			
			mainPanel.add(ProportionLayout.RIGHT, createPlotPanel(data, false, "x", "resid", null,
								getParameter(X_AXIS_INFO_PARAM), getParameter(RESID_AXIS_INFO_PARAM), 1));
		
		thePanel.add("Center", mainPanel);
		
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
																									int plotIndex) {
		if (plotIndex == 0) {
			dragView = new DragLeverageView(data, this, theHorizAxis, theVertAxis, "x", "y", "lsLine");
			return dragView;
		}
		else {
			HiliteOneResidualView theView = new HiliteOneResidualView(data, this, theHorizAxis,
																															theVertAxis, "x", "resid", null);
			theView.setAllowSelectPoint(false);
			return theView;
		}
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == lineAdjustChoice) {
			int newChoice = lineAdjustChoice.getSelectedIndex();
			if (newChoice != currentChoice) {
				currentChoice = newChoice;
				dragView.setAdjustLS(newChoice == 1);
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