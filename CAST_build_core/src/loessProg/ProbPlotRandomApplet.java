package loessProg;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;

import regnView.*;


public class ProbPlotRandomApplet extends CoreDiagnosticApplet {
	static final private String RESID_AXIS_INFO_PARAM = "residAxis";
	static final private String NSCORE_AXIS_INFO_PARAM = "nscoreAxis";
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		addSimulationVariables(data, "x", "y");
		
		return data;
	}
	
	protected String residualName() {
		return translate("Ordered residual");
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.HORIZONTAL,
																					ProportionLayout.TOTAL));
		
		thePanel.add("Left", createPlotPanel(data, false, "x", "response", null,
				getParameter(X_AXIS_INFO_PARAM), getParameter(Y_AXIS_INFO_PARAM), 0));
		thePanel.add("Right", createPlotPanel(data, false, "nscore", "resid", null,
					getParameter(NSCORE_AXIS_INFO_PARAM), getParameter(RESID_AXIS_INFO_PARAM), 1));
		
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
																									int plotIndex) {
		switch (plotIndex) {
			case 0:
				return new LSScatterView(data, this, theHorizAxis, theVertAxis, "x", "response", "lsLine");
			default:
				return new ScatterView(data, this, theHorizAxis, theVertAxis, "nscore", "resid");
		}
	}
}