package loessProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import regn.*;
import loess.*;


public class SlugSampleApplet extends TransformXLSApplet {
	private SlugGenerator dataGenerator;
	
	private XButton takeSampleButton;
	
	private void takeSample(DataSet data) {
		dataGenerator.generateNextSample();
		
		NumVariable xVar = (NumVariable)data.getVariable("x");
		NumVariable yVar = (NumVariable)data.getVariable("y");
		xVar.setValues(dataGenerator.getXValues());
		yVar.setValues(dataGenerator.getYValues());
		PowerLinearModel lsLineVariable = (PowerLinearModel)data.getVariable("lsLine");
		if (lsLineVariable != null)
			lsLineVariable.setLSParams("y", interceptSigDigits, interceptMaxDecimals,
									slopeSigDigits, slopeMaxDecimals, 3, 3);
		data.variableChanged("x");
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		NumVariable xVar = new NumVariable(getParameter(X_VAR_NAME_PARAM));
		data.addVariable("x", xVar);
		NumVariable yVar = new NumVariable(getParameter(Y_VAR_NAME_PARAM));
		data.addVariable("y", yVar);
		
		dataGenerator = new SlugGenerator(this);
		takeSample(data);
		
		return data;
	}
	
	protected HorizAxis createHorizAxis(DataSet data, String xAxisInfo, int plotIndex) {
		if (plotIndex == 0) {
			HorizAxis theAxis = createTransformAxis(data, xAxisInfo, NO_TRANSFORM);
			theAxis.setPower(3.0);
			PowerLinearModel lsLineVariable = (PowerLinearModel)data.getVariable("lsLine");
			lsLineVariable.setLSParams("y", interceptSigDigits,
									interceptMaxDecimals, slopeSigDigits, slopeMaxDecimals, 3, 3);
			return theAxis;
		}
		else
			return super.createHorizAxis(data, xAxisInfo, plotIndex);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
		takeSampleButton = new XButton(translate("Take sample"), this);
		thePanel.add(takeSampleButton);
		
		thePanel.add(super.controlPanel(data));
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			takeSample(data);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}