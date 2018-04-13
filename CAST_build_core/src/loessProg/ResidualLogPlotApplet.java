package loessProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import valueList.*;
import models.*;

import regn.*;
import regnProg.*;
import loess.*;


public class ResidualLogPlotApplet extends ChangeValuesApplet {
	static final protected String TRANSFORM_X_AXIS_PARAM = "transXAxis";
	static final protected String MAX_X_TRANSFORMED_PARAM = "maxXTransformed";
	static final protected String TRANSFORM_Y_AXIS_PARAM = "transYAxis";
	static final protected String MAX_Y_TRANSFORMED_PARAM = "maxYTransformed";
	static final protected String RESID_AXIS_INFO_PARAM = "residAxis";
	static final private String BETA_EXTREMES_PARAM = "betaExtremes";
	static final private String FIT_RESID_DECIMALS_PARAM = "fitResidDecimals";
	
	static final private Color kDarkRed = new Color(0x990033);
	
	private NumValue bigIntercept, bigSlope;
	private int interceptSigDigits, slopeSigDigits;
	private int interceptMaxDecimals, slopeMaxDecimals;
	
	private DualTransHorizAxis xTransformAxis;
	private DualTransVertAxis yTransformAxis;
	
	private XCheckbox deleteOutlierCheck;
	
	public void setupApplet() {
		StringTokenizer st = new StringTokenizer(getParameter(BETA_EXTREMES_PARAM));
		interceptSigDigits = Integer.parseInt(st.nextToken());
		interceptMaxDecimals = Integer.parseInt(st.nextToken());
		bigIntercept = new NumValue(st.nextToken());
		slopeSigDigits = Integer.parseInt(st.nextToken());
		slopeMaxDecimals = Integer.parseInt(st.nextToken());
		bigSlope = new NumValue(st.nextToken());
		
		super.setupApplet();
	}
	
	protected LinearModel createModel(DataSet data) {
		return null;
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		int fitResidDecimals = Integer.parseInt(getParameter(FIT_RESID_DECIMALS_PARAM));
		NumVariable yVar = (NumVariable)data.getVariable("y");
		yVar.setDecimals(fitResidDecimals);
		data.addVariable("resid", new ResidLogLogValueVariable("Residual, e", data,
																	"x", "y", "lsLine", fitResidDecimals));
		data.addVariable("fit", new FittedValueVariable(
												translate("Fitted") + " " + getParameter(Y_VAR_NAME_PARAM), data,
												"x", "lsLine", fitResidDecimals));
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.6, 20, ProportionLayout.HORIZONTAL, ProportionLayout.REMAINDER));
		
		thePanel.add("Left", createPlotPanel(data, false, "x", "y", null,
							getParameter(X_AXIS_INFO_PARAM), getParameter(Y_AXIS_INFO_PARAM), 0));
		thePanel.add("Right", createPlotPanel(data, false, "x", "resid", null,
							getParameter(X_AXIS_INFO_PARAM), getParameter(RESID_AXIS_INFO_PARAM), 1));
		return thePanel;
	}
	
	protected HorizAxis createHorizAxis(DataSet data, String xAxisInfo, int plotIndex) {
		String transAxisString = getParameter(TRANSFORM_X_AXIS_PARAM);
		
		DualTransHorizAxis xAxis = new DualTransHorizAxis(this, transAxisString);
		if (plotIndex == 0)
			xTransformAxis = xAxis;
		xAxis.readNumLabels(xAxisInfo);
		String maxTransformedString = getParameter(MAX_X_TRANSFORMED_PARAM);
		if (maxTransformedString != null)
			xAxis.setMaxTransformed(maxTransformedString);
		xAxis.setAxisName(data.getVariable("x").name);
		xAxis.setPower(0.0);
		return xAxis;
	}
	
	protected VertAxis createVertAxis(DataSet data, String yAxisInfo, int plotIndex) {
		if (plotIndex == 0) {
			String transAxisString = getParameter(TRANSFORM_Y_AXIS_PARAM);
			
			yTransformAxis = new DualTransVertAxis(this, transAxisString);
			yTransformAxis.readNumLabels(yAxisInfo);
			String maxTransformedString = getParameter(MAX_Y_TRANSFORMED_PARAM);
			if (maxTransformedString != null)
				yTransformAxis.setMaxTransformed(maxTransformedString);
			yTransformAxis.setAxisName(data.getVariable("y").name);
			yTransformAxis.setPower(0.0);
			return yTransformAxis;
		}
		else {
			VertAxis theAxis = super.createVertAxis(data, yAxisInfo, plotIndex);
			theAxis.setAxisName(data.getVariable("resid").name);
			return theAxis;
		}
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
																									int plotIndex) {
		if (plotIndex == 0) {
			PowerLinearModel lsLineVariable = new PowerLinearModel("lsLine", data, "x", theHorizAxis, theVertAxis);
			data.addVariable("lsLine", lsLineVariable);	//	transformed model cannot be created
																		// until both axes has been created
			lsLineVariable.setLSParams("y", interceptSigDigits,
									interceptMaxDecimals, slopeSigDigits, slopeMaxDecimals, 3, 3);
			return new HiliteResidualView(data, this, theHorizAxis, theVertAxis, "x", "y", "lsLine", null);
		}
		else
			return new HiliteResidualView(data, this, theHorizAxis, theVertAxis, "x", "resid", null, null);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.45, 0, ProportionLayout.HORIZONTAL, ProportionLayout.REMAINDER));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.RIGHT,
																			VerticalLayout.VERT_CENTER, 2));
			
				OneValueView yView = new OneValueView(data, "y", this);
				yView.setForeground(Color.blue);
			leftPanel.add(yView);
				
				OneValueView fitView = new OneValueView(data, "fit", this);
				fitView.setForeground(HiliteResidualView.darkGreen);
			leftPanel.add(fitView);
				
		thePanel.add("Left", leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																			VerticalLayout.VERT_CENTER, 10));
				LinearPowerEquationView theEqn = new LinearPowerEquationView(data, this,
								"lsLine", data.getVariable("y").name, data.getVariable("x").name, 
								bigIntercept, bigIntercept, bigSlope, bigSlope, xTransformAxis, yTransformAxis);
				theEqn.setForeground(kDarkRed);
				theEqn.setFont(getStandardBoldFont());
			rightPanel.add(theEqn);
				deleteOutlierCheck = new XCheckbox(translate("Delete outlier"), this);
				deleteOutlierCheck.setState(false);
			rightPanel.add(deleteOutlierCheck);
		thePanel.add("Right", rightPanel);
		
		return thePanel;
	}
	
	protected void updateLSParams() {
		PowerLinearModel modelVariable = (PowerLinearModel)data.getVariable("lsLine");
		modelVariable.setLSParams("y", interceptSigDigits,
									interceptMaxDecimals, slopeSigDigits, slopeMaxDecimals, 3, 3);
	}
	
	private boolean localAction(Object target) {
		if (target == deleteOutlierCheck) {
			boolean doChange = deleteOutlierCheck.getState();
			doChanges(change1, doChange);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}