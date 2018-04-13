package loessProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;

import regnProg.*;
import regn.*;
import regnView.*;
import loess.*;


public class TransformXLSApplet extends MultipleScatterApplet {
	static final private String TRANSFORM_AXIS_PARAM = "transAxis";
	static final protected String MAX_TRANSFORMED_PARAM = "maxTransformed";
	static final protected String HORIZ_AXIS2_PARAM = "horizAxis2";
	static final private String BETA_EXTREMES_PARAM = "betaExtremes";
	
	static final protected int TRANSFORM = 0;
	static final protected int NO_TRANSFORM = 1;
	
	protected DualTransHorizAxis xTransformAxis;
	protected VertAxis yAxis;
	
	protected NumValue bigIntercept, bigSlope;
	protected int interceptSigDigits, slopeSigDigits;
	protected int interceptMaxDecimals, slopeMaxDecimals;
	
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
	
	protected HorizAxis createTransformAxis(DataSet data, String xAxisInfo, int transformType) {
		String transAxisString = getParameter(TRANSFORM_AXIS_PARAM);
		
		xTransformAxis = new DualTransHorizAxis(this, transAxisString);
		xTransformAxis.readNumLabels(xAxisInfo);
		if (transformType == TRANSFORM)
			xTransformAxis.setLinkedData(data, true);
		String maxTransformedString = getParameter(MAX_TRANSFORMED_PARAM);
		if (maxTransformedString != null)
			xTransformAxis.setMaxTransformed(maxTransformedString);
		
		PowerLinearModel lsLineVariable = new PowerLinearModel("lsLine", data, "x", xTransformAxis);
		data.addVariable("lsLine", lsLineVariable);	//	transformed model cannot be created until
																	// axis has been created
		return xTransformAxis;
	}
	
	protected HorizAxis createHorizAxis(DataSet data, String xAxisInfo, int plotIndex) {
		if (plotIndex == 0) {
			HorizAxis theAxis = createTransformAxis(data, xAxisInfo, TRANSFORM);
			PowerLinearModel lsLineVariable = (PowerLinearModel)data.getVariable("lsLine");
			lsLineVariable.setLSParams("y", interceptSigDigits,
									interceptMaxDecimals, slopeSigDigits, slopeMaxDecimals, 3, 3);
			return theAxis;
		}
		else
			return super.createHorizAxis(data, getParameter(HORIZ_AXIS2_PARAM), plotIndex);
	}
	
	protected VertAxis createVertAxis(DataSet data, String yAxisInfo, int plotIndex) {
		VertAxis theYAxis = super.createVertAxis(data, yAxisInfo, plotIndex);
		if (plotIndex == 0) {
			yAxis = theYAxis;
			yAxis.setAxisName(data.getVariable("y").name);
		}
		return theYAxis;
	}
	
	private XLabel createHeading(String s) {
		XLabel theLabel = new XLabel(s, XLabel.CENTER, this);
		theLabel.setFont(getBigBoldFont());
		return theLabel;
	}
	
	protected double leftProportion() {
		return 0.55;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(leftProportion(), 5));
		
		XPanel leftPanel = new XPanel();
		leftPanel.setLayout(new BorderLayout());
		leftPanel.add("North", createHeading(translate("Least Squares Fit")));
		leftPanel.add("Center", createPlotPanel(data, false, "x", "y", null,
							getParameter(X_AXIS_INFO_PARAM), getParameter(Y_AXIS_INFO_PARAM), 0));
		thePanel.add("Left", leftPanel);
		
		XPanel rightPanel = new XPanel();
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add("North", createHeading(translate("Original Scale")));
		rightPanel.add("Center", createPlotPanel(data, false, "x", "y", null,
							getParameter(X_AXIS_INFO_PARAM), getParameter(Y_AXIS_INFO_PARAM), 1));
		thePanel.add("Right", rightPanel);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		LinearPowerEquationView theEqn = new LinearPowerEquationView(data, this,
							"lsLine", data.getVariable("y").name, data.getVariable("x").name, 
							bigIntercept, bigIntercept, bigSlope, bigSlope, xTransformAxis, yAxis);
		thePanel.add(theEqn);
		
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
																									int plotIndex) {
		return new LSScatterView(data, this, theHorizAxis, theVertAxis, "x", "y", "lsLine");
	}
}