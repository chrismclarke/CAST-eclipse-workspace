package loessProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;

import regn.*;
import loess.*;


public class LogLogLSApplet extends TransformXLSApplet {
	static final private String TRANSFORM_X_AXIS_PARAM = "transXAxis";
	static final private String MAX_X_TRANSFORMED_PARAM = "maxXTransformed";
	static final private String TRANSFORM_Y_AXIS_PARAM = "transYAxis";
	static final private String MAX_Y_TRANSFORMED_PARAM = "maxYTransformed";
	static final private String VERT_AXIS2_PARAM = "vertAxis2";
	
	static final private String MAX_VALUES_PARAM = "maxValues";	//	max x, linear fit and fit
	static final private String FACTOR_DECIMALS_PARAM = "factorDecimals";
	
	static final private Color kDarkGreen = new Color(0x006600);
	
	private XChoice calcChoice;
	private int currentCalcIndex;
	private XPanel calcPanel;
	private CardLayout calcCardLayout;
	
	protected DualTransVertAxis yTransformAxis;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		data.addVariable("select", new SelectionVariable(getParameter(X_VAR_NAME_PARAM)));
		data.setSelection("select", Double.NaN, Double.NaN);
		return data;
	}
	
	protected double leftProportion() {
		return 0.6;
	}
	
	protected HorizAxis createHorizAxis(DataSet data, String xAxisInfo, int plotIndex) {
		if (plotIndex == 0) {
			String transAxisString = getParameter(TRANSFORM_X_AXIS_PARAM);
			
			xTransformAxis = new DualTransHorizAxis(this, transAxisString);
			xTransformAxis.readNumLabels(xAxisInfo);
			String maxTransformedString = getParameter(MAX_X_TRANSFORMED_PARAM);
			if (maxTransformedString != null)
				xTransformAxis.setMaxTransformed(maxTransformedString);
			xTransformAxis.setPower(0.0);
			return xTransformAxis;
		}
		else
			return super.createHorizAxis(data, getParameter(HORIZ_AXIS2_PARAM), plotIndex);
	}
	
	protected VertAxis createVertAxis(DataSet data, String yAxisInfo, int plotIndex) {
		if (plotIndex == 0) {
			String transAxisString = getParameter(TRANSFORM_Y_AXIS_PARAM);
			
			yTransformAxis = new DualTransVertAxis(this, transAxisString);
			yTransformAxis.readNumLabels(yAxisInfo);
			String maxTransformedString = getParameter(MAX_Y_TRANSFORMED_PARAM);
			if (maxTransformedString != null)
				yTransformAxis.setMaxTransformed(maxTransformedString);
			yTransformAxis.setPower(0.0);
			
			yTransformAxis.setAxisName(data.getVariable("y").name);
			yAxis = yTransformAxis;
			return yTransformAxis;
		}
		else
			return super.createVertAxis(data, getParameter(VERT_AXIS2_PARAM), plotIndex);
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
																									int plotIndex) {
		if (plotIndex == 0) {
			PowerLinearModel lsLineVariable = new PowerLinearModel("lsLine", data, "x", theHorizAxis, theVertAxis);
			data.addVariable("lsLine", lsLineVariable);	//	transformed model cannot be created
																		// until both axes have been created
			lsLineVariable.setLSParams("y", interceptSigDigits,
									interceptMaxDecimals, slopeSigDigits, slopeMaxDecimals, 3, 3);
			
		}
		return new LSPredictScatterView(data, this, theHorizAxis, theVertAxis, "x", "y", "lsLine", "select");
	}
	
	protected XPanel controlPanel(DataSet data) {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_VALUES_PARAM));
		NumValue maxX = new NumValue(st.nextToken());
		NumValue maxlogFit = new NumValue(st.nextToken());
		NumValue maxFit = new NumValue(st.nextToken());
		
		int factorDecimals = Integer.parseInt(getParameter(FACTOR_DECIMALS_PARAM));
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 5));
				LinearPowerEquationView theEqn = new LinearPowerEquationView(data, this,
									"lsLine", data.getVariable("y").name, data.getVariable("x").name, 
									bigIntercept, bigIntercept, bigSlope, bigSlope, xTransformAxis, yAxis);
			theEqn.setFont(getStandardBoldFont());
			topPanel.add(theEqn);
		thePanel.add("North", topPanel);
			
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT,
																		VerticalLayout.VERT_CENTER, 3));
			XLabel choiceLabel = new XLabel(translate("Show prediction using") + "...", XLabel.LEFT, this);
			choiceLabel.setFont(getStandardBoldFont());
			leftPanel.add(choiceLabel);
			
			calcChoice = new XChoice(this);
			calcChoice.addItem(translate("Using log scale"));
			calcChoice.addItem(translate("Using original scale"));
			currentCalcIndex = 0;
			leftPanel.add(calcChoice);
			
		thePanel.add("West", leftPanel);
			
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																		VerticalLayout.VERT_CENTER, 3));
				SelectionValueView xValueView = new SelectionValueView(data, "select", maxX, this);
				xValueView.setFont(getBigFont());
				xValueView.setForeground(Color.blue);
			rightPanel.add(xValueView);
		
				calcPanel = new XPanel();
				calcCardLayout = new CardLayout(2, 2);
				calcPanel.setLayout(calcCardLayout);
					XPanel logPanel = new XPanel();
					logPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																				VerticalLayout.VERT_CENTER, 0));
						FitEquationValueView fitEqnView = new FitEquationValueView(data, "y", "lsLine", xValueView, maxlogFit, this);
						fitEqnView.setForeground(kDarkGreen);
						fitEqnView.setFont(getBigFont());
					logPanel.add(fitEqnView);
					
						FitEquation2ValueView fitEqn2View = new FitEquation2ValueView(data, "y", fitEqnView, maxFit, this);
						fitEqn2View.setForeground(Color.red);
						fitEqn2View.setFont(getBigFont());
					logPanel.add(fitEqn2View);
				calcPanel.add("Log", logPanel);
				
					XPanel rawPanel = new XPanel();
					rawPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																				VerticalLayout.VERT_CENTER, 0));
						FitEquation3ValueView fitEqn3View = new FitEquation3ValueView(data, "y", "lsLine",
																															xValueView, maxFit, factorDecimals, this);
						fitEqn3View.setFont(getBigFont());
						fitEqn3View.setForeground(Color.red);
					rawPanel.add(fitEqn3View);
				calcPanel.add("Raw", rawPanel);
			rightPanel.add(calcPanel);
			
		thePanel.add("Center", rightPanel);
			
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == calcChoice) {
			int newCalcIndex = calcChoice.getSelectedIndex();
			if (newCalcIndex != currentCalcIndex) {
				currentCalcIndex = newCalcIndex;
				if (currentCalcIndex == 0)
					calcCardLayout.first(calcPanel);
				else
					calcCardLayout.last(calcPanel);
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}