package loessProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import coreGraphics.*;

import regn.*;
import linMod.*;
import loess.*;


public class TransPIApplet extends ScatterApplet {
	static final private String X_LIMITS_PARAM = "xLimits";
	static final private String MAX_PREDICT_PARAM = "maxPrediction";
	static final private String TRANSFORM_X_AXIS_PARAM = "transXAxis";
	static final protected String MAX_X_TRANSFORMED_PARAM = "maxXTransformed";
	static final private String PREDICT_NAME_PARAM = "predictName";
	
	private NumValue maxPrediction, maxX;
	
	private XValueSlider xSlider;
	private TransPIView theView;
	private PIValueView piView;
	
	public void setupApplet() {
		StringTokenizer theParams = new StringTokenizer(getParameter(X_LIMITS_PARAM));
		NumValue minX = new NumValue(theParams.nextToken());
		maxX = new NumValue(theParams.nextToken());
		NumValue xStep = new NumValue(theParams.nextToken());
		NumValue startX = new NumValue(theParams.nextToken());
		xSlider = new XValueSlider(minX, maxX, xStep, startX, XValueSlider.NO_SHOW_VALUES, this);
		
		maxPrediction = new NumValue(getParameter(MAX_PREDICT_PARAM));
		
		super.setupApplet();
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			thePanel.add("Center", xSlider);
			
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
				piView = new PIValueView(data, this, "x", maxX, maxPrediction, xSlider, theView,
																											getParameter(PREDICT_NAME_PARAM));
				piView.setForeground(TransPIView.kIntervalColor);
				piView.setFont(getStandardBoldFont());
				topPanel.add(piView);
			
			thePanel.add("North", topPanel);
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		theView = new TransPIView(data, this, theHorizAxis, theVertAxis, "x", "y", xSlider);
		return theView;
	}
	
	protected void setHorizAxisPower(HorizAxis xTransformAxis, DataSet data) {
		xTransformAxis.setPower(3.0);
		xTransformAxis.setLinkedData(data, true);
	}
	
	protected HorizAxis createHorizAxis(DataSet data) {
		String transAxisString = getParameter(TRANSFORM_X_AXIS_PARAM);
		String xAxisInfo = getParameter(X_AXIS_INFO_PARAM);
		
		DualTransHorizAxis xTransformAxis = new DualTransHorizAxis(this, transAxisString);
		xTransformAxis.readNumLabels(xAxisInfo);
		xTransformAxis.setAxisName(getParameter(X_VAR_NAME_PARAM));
		setHorizAxisPower(xTransformAxis, data);
		String maxTransformedString = getParameter(MAX_X_TRANSFORMED_PARAM);
		if (maxTransformedString != null)
			xTransformAxis.setMaxTransformed(maxTransformedString);
		
		PowerLinearModel lsLineVariable = new PowerLinearModel("lsLine", data, "x", xTransformAxis);
		lsLineVariable.setLSParams("y", 0, 0, 0, 0, 3, 3);
		data.addVariable("lsLine", lsLineVariable);	//	transformed model cannot be created until
																	// axis has been created
		return xTransformAxis;
	}
	
	private boolean localAction(Object target) {
		if (target == xSlider) {
			theView.repaint();
			piView.repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}