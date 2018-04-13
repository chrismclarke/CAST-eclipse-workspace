package regnProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import regn.*;
import regnView.*;


public class TransformLSApplet extends MultipleScatterApplet {
	private TransformVertAxis theTransformAxis;
	
	protected VertAxis createVertAxis(DataSet data, String yAxisInfo, int plotIndex) {
		if (plotIndex == 0) {
			theTransformAxis = new TransformVertAxis(this);
			theTransformAxis.readNumLabels(yAxisInfo);
			theTransformAxis.setLinkedData(data, true);
			PowerLinearModel modelVariable = new PowerLinearModel("model", data, "x", theTransformAxis);
			modelVariable.setLSParams("y", 3, 3, 3, 3, 3, 3);
			data.addVariable("model", modelVariable);		//	transformed model cannot be created until
																		// axis has been created
			return theTransformAxis;
		}
		else {
			VertAxis vertAxis = new VertAxis(this);
			vertAxis.readNumLabels(yAxisInfo);
			return vertAxis;
		}
	}
	
	private XLabel createHeading(String s) {
		XLabel theLabel = new XLabel(s, XLabel.CENTER, this);
		theLabel.setFont(getBigBoldFont());
		return theLabel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.HORIZONTAL, ProportionLayout.REMAINDER));
		
		XPanel leftPanel = new XPanel();
		leftPanel.setLayout(new BorderLayout());
		leftPanel.add("North", createHeading(translate("Least Squares Fit")));
		leftPanel.add("Center", createPlotPanel(data, false, "x", "y", null,
							getParameter(X_AXIS_INFO_PARAM), getParameter(Y_AXIS_INFO_PARAM), 0));
		thePanel.add("Left", leftPanel);
		
		XPanel rightPanel = new XPanel();
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add("North", createHeading("     " + translate("Raw Data") + "     "));
		rightPanel.add("Center", createPlotPanel(data, false, "x", "y", null,
							getParameter(X_AXIS_INFO_PARAM), getParameter(Y_AXIS_INFO_PARAM), 1));
		thePanel.add("Right", rightPanel);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
																									int plotIndex) {
		return new TransResidView(data, this, theHorizAxis, theVertAxis, "x", "y", "model");
	}
}