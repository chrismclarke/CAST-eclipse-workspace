package regnProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;

import regn.*;
import regnView.*;


public class ErrorSdEstApplet extends XApplet {
	static final private String X_AXIS_INFO_PARAM = "horizAxis";
	static final private String Y_AXIS_INFO_PARAM = "vertAxis";
	static final private String ERROR_AXIS_INFO_PARAM = "errorAxis";
	
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String Y_VALUES_PARAM = "yValues";
	
	static final protected String INTERCEPT_PARAM = "interceptLimits";
	static final protected String SLOPE_PARAM = "slopeLimits";
	static final protected String BIGGEST_RSS_PARAM = "biggestRss";
	static final protected String BIGGEST_ERROR_SD_PARAM = "biggestErrorSD";
	
	static final private Color kDarkRed = new Color(0x990000);
	static final private Color kDarkBlue = new Color(0x000099);
	
	private NumValue intMin, intMax, intStart, slopeMin, slopeMax, slopeStart;
	
	private DataSet data;
	private VertAxis yAxis, errorAxis;
	private XButton lsButton;
	
	private XPanel errorSdEstPanel;
	private CardLayout errorSdEstLayout;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(10, 0));
		add("Center", plotPanel(data));
		add("South", summaryPanel(data));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
		
		StringTokenizer paramLimits = new StringTokenizer(getParameter(INTERCEPT_PARAM));
		intMin = new NumValue(paramLimits.nextToken());
		intMax = new NumValue(paramLimits.nextToken());
		intStart = new NumValue(paramLimits.nextToken());
		
		paramLimits = new StringTokenizer(getParameter(SLOPE_PARAM));
		slopeMin = new NumValue(paramLimits.nextToken());
		slopeMax = new NumValue(paramLimits.nextToken());
		slopeStart = new NumValue(paramLimits.nextToken());
		
		LinearModel model = new LinearModel("model", data, "x", intStart, slopeStart, new NumValue(0.0, 0));
		data.addVariable("model", model);
		
		ResidValueVariable resid = new ResidValueVariable(translate("Residual") + ", e",
																												data, "x", "y", "model", 0);
		data.addVariable("resid", resid);
		
		return data;
	}
	
	private XPanel plotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new BorderLayout(0, 0));
			leftPanel.add("Center", scatterPanel(data));
			leftPanel.add("North", yNamePanel(data));
		thePanel.add("Center", leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new BorderLayout(0, 0));
			rightPanel.add("Center", errorDotPanel(data));
			rightPanel.add("North", errorNamePanel(data));
			
				XPanel bottomPanel = new InsetPanel(0, 4, 0, 0);
				bottomPanel.add(ssqPanel(data));
			rightPanel.add("South", bottomPanel);
			
		thePanel.add("East", rightPanel);
		return thePanel;
	}
	
	private XPanel yNamePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
			XLabel yVariateName = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
			yVariateName.setFont(yAxis.getFont());
		
		thePanel.add(yVariateName);
		return thePanel;
	}
	
	private XPanel scatterPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis xAxis = new HorizAxis(this);
			xAxis.readNumLabels(getParameter(X_AXIS_INFO_PARAM));
			xAxis.setAxisName(getParameter(X_VAR_NAME_PARAM));
		thePanel.add("Bottom", xAxis);
		
			yAxis = new VertAxis(this);
			yAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
		thePanel.add("Left", yAxis);
		
			DragLineView theView = new DragLineView(data, this, xAxis, yAxis, "x", "y", "model");
			theView.setResidualColor(Color.red);
			
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	private XPanel errorNamePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
			XLabel errorName = new XLabel(translate("Residual") + ", e", XLabel.LEFT, this);
			errorName.setForeground(kDarkRed);
			errorName.setFont(errorAxis.getFont());
		
		thePanel.add(errorName);
		return thePanel;
	}
	
	private XPanel errorDotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FixedSizeLayout(100, 100));
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new AxisLayout());
			
				errorAxis = new VertAxis(this);
				errorAxis.readNumLabels(getParameter(ERROR_AXIS_INFO_PARAM));
			innerPanel.add("Left", errorAxis);
			
				ResidDotPlotView theView = new ResidDotPlotView(data, this, errorAxis);
				theView.setActiveNumVariable("resid");
				theView.lockBackground(Color.white);
			innerPanel.add("Center", theView);
		
		thePanel.add(innerPanel);
		return thePanel;
	}
	
	private XPanel ssqPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
			NumValue biggestRss = new NumValue(getParameter(BIGGEST_RSS_PARAM));
			ResidSsq2View rssView = new ResidSsq2View(data, this, "resid", biggestRss);
			rssView.setForeground(kDarkBlue);
		thePanel.add(rssView);
		
		return thePanel;
	}
	
	private XPanel summaryPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																																	ProportionLayout.REMAINDER));
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
			leftPanel.add(new LinearEquationView(data, this, "model", getParameter(Y_VAR_NAME_PARAM),
															getParameter(X_VAR_NAME_PARAM), intMin, intMax, slopeMin, slopeMax));
		
		
			lsButton = new XButton(translate("Least squares"), this);
			leftPanel.add(lsButton);
		thePanel.add(ProportionLayout.LEFT, leftPanel);
		
		thePanel.add(ProportionLayout.RIGHT, errorSdEstPanel(data));
		return thePanel;
	}
	
/*
	private XPanel lsControlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
		thePanel.add(new LinearEquationView(data, this, "model", getParameter(Y_VAR_NAME_PARAM),
														getParameter(X_VAR_NAME_PARAM), intMin, intMax, slopeMin, slopeMax));
		
		
			lsButton = new XButton(translate("Least squares"), this);
		thePanel.add(lsButton);
		return thePanel;
	}
*/
	
	private XPanel errorSdEstPanel(DataSet data) {
		errorSdEstPanel = new XPanel();
		errorSdEstLayout = new CardLayout();
		errorSdEstPanel.setLayout(errorSdEstLayout);
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
				NumValue biggestErrorSD = new NumValue(getParameter(BIGGEST_ERROR_SD_PARAM));
				ErrorSdEstView errorSDEstView = new ErrorSdEstView(data, this, "resid", biggestErrorSD);
			
			innerPanel.add(errorSDEstView);
		
		errorSdEstPanel.add("blank", new XPanel());
		errorSdEstPanel.add("sdEst", innerPanel);
		
		return errorSdEstPanel;
	}
	
	public void hideErrorSdEst() {
		errorSdEstLayout.show(errorSdEstPanel, "blank");
	}
	
	private void showErrorSdEst() {
		errorSdEstLayout.show(errorSdEstPanel, "sdEst");
	}

	
	private boolean localAction(Object target) {
		if (target == lsButton) {
			LinearModel model = (LinearModel)data.getVariable("model");
			model.setLSParams("y", intStart.decimals, slopeStart.decimals, 0);
			data.variableChanged("model");
			showErrorSdEst();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}