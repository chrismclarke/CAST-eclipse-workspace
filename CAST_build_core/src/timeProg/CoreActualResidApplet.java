package timeProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;

import time.*;

abstract public class CoreActualResidApplet extends XApplet {
	static final protected String Y_AXIS_INFO_PARAM = "yAxis";
	static final protected String RESID_AXIS_INFO_PARAM = "residAxis";
	static final private String TIME_INFO_PARAM = "timeAxis";
	static final protected String TIME_NAME_PARAM = "timeAxisName";
	static final private String LABEL_AXES_PARAM = "labelAxes";
	static final private String TIME_SEQUENCE_PARAM = "timeSequence";
	static final private String DECIMALS_PARAM = "decimals";
	
	static final private NumValue kZero = new NumValue(0.0, 1);
	
	static final private int MEAN_MODEL = 0;
	static final private int LINEAR_MODEL = 1;
	static final private int QUADRATIC_MODEL = 2;
	
	private NumValue lsParam[][] = new NumValue[3][];
	
	protected DataSet data;
	protected boolean labelAxes = false;
	private int fitDecs;
	
	protected TimeView seriesView[] = new TimeView[2];
	private int noOfViews = 0;
	
	private XChoice modelChoice;
	private int selectedModel = 0;
	
	
	public void setupApplet() {
		data = readData();
		
		String labelAxesParam = getParameter(LABEL_AXES_PARAM);
		labelAxes = labelAxesParam != null && labelAxesParam.equals("true");
	}
	
	protected QuadraticModel setUpModel(DataSet data, NumVariable yVar, NumVariable timeVar) {
		StringTokenizer st = new StringTokenizer(getParameter(DECIMALS_PARAM));
		int intDecs = Integer.parseInt(st.nextToken());
		int slopeDecs = Integer.parseInt(st.nextToken());
		int curvatureDecs = Integer.parseInt(st.nextToken());
		fitDecs = Integer.parseInt(st.nextToken());
		
		ValueEnumeration ye = yVar.values();
		ValueEnumeration xe = timeVar.values();
		double sx = 0.0;
		double sy = 0.0;
		double sxx = 0.0;
		double sxy = 0.0;
		int n = 0;
		while (ye.hasMoreValues()) {
			double nextY = ye.nextDouble();
			if (!Double.isNaN(nextY)) {
				double nextX = xe.nextDouble();
				sx += nextX;
				sy += nextY;
				sxx += nextX * nextX;
				sxy += nextX * nextY;
				n ++;
			}
		}
		sxx -= sx * sx / n;
		sxy -= sx * sy / n;
		
		for (int i=0 ; i<3 ; i++)
			lsParam[i] = new NumValue[3];
		
		lsParam[MEAN_MODEL][0] = new NumValue(sy / n, intDecs);
		lsParam[MEAN_MODEL][1] = lsParam[MEAN_MODEL][2] = kZero;
		
		lsParam[LINEAR_MODEL][0] = new NumValue((sy - sx * sxy / sxx) / n, intDecs);
		lsParam[LINEAR_MODEL][1] = new NumValue(sxy / sxx, slopeDecs);
		lsParam[LINEAR_MODEL][2] = kZero;
		
		QuadraticModel model = new QuadraticModel("Model", data, "time");
		model.setLSParams("y", intDecs, slopeDecs, curvatureDecs, 0);
		lsParam[QUADRATIC_MODEL][0] = model.getIntercept();
		lsParam[QUADRATIC_MODEL][1] = model.getSlope();
		lsParam[QUADRATIC_MODEL][2] = model.getCurvature();
		
		model.setParameters(lsParam[MEAN_MODEL][0], lsParam[MEAN_MODEL][1], lsParam[MEAN_MODEL][2]);
		
		return model;
	} 
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		String yName = getParameter(VAR_NAME_PARAM);
		NumVariable yVar = new NumVariable(yName);
		yVar.readValues(getParameter(VALUES_PARAM));
		data.addVariable("y", yVar);
		
		NumVariable timeVar = new NumVariable(getParameter(TIME_NAME_PARAM));
		timeVar.readSequence(getParameter(TIME_SEQUENCE_PARAM));
		data.addVariable("time", timeVar);
		
		QuadraticModel model = setUpModel(data, yVar, timeVar);
		data.addVariable("model", model);
		
		FittedValueVariable fit = new FittedValueVariable("Fit", data, "time", "model", fitDecs);
		data.addVariable("fit", fit);
		
		ResidValueVariable resid = new ResidValueVariable(translate("Residual"), data, "time", "y",
																																		"model", fitDecs);
		data.addVariable("resid", resid);
		
		return data;
	}
	
	protected XPanel addYAxisName(XPanel normalPanel, DataSet data, String yKey, VertAxis vertAxis,
																																	Color yColor) {
		if (labelAxes) {
			XPanel superPanel = new XPanel();
			superPanel.setLayout(new BorderLayout());
				
				XLabel yName = new XLabel(data.getVariable(yKey).name, XLabel.LEFT, this);
				yName.setFont(vertAxis.getFont());
				if (yColor != null)
					yName.setForeground(yColor);
			superPanel.add("North", yName);
			
			superPanel.add("Center", normalPanel);
			
			return superPanel;
		}
		else
			return normalPanel;
	}
	
	abstract protected TimeView getTimeView(DataSet data, IndexTimeAxis horizAxis,
																																				VertAxis vertAxis);
	
	protected XPanel timeSeriesPanel(DataSet data, String yKey, String fitKey, String yAxisParam) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		VertAxis vertAxis = new VertAxis(this);
		vertAxis.readNumLabels(getParameter(yAxisParam));
		thePanel.add("Left", vertAxis);
		
		IndexTimeAxis horizAxis = new IndexTimeAxis(this, ((NumVariable)data.getVariable("y")).noOfValues());
		horizAxis.setTimeScale(getParameter(TIME_INFO_PARAM));
		if (labelAxes)
			horizAxis.setAxisName(getParameter(TIME_NAME_PARAM));
		thePanel.add("Bottom", horizAxis);
		
		TimeView theView = getTimeView(data, horizAxis, vertAxis);
		seriesView[noOfViews++] = theView;
		theView.setActiveNumVariable(yKey);
		theView.setSmoothedVariable(fitKey);
		theView.lockBackground(Color.white);
		
		thePanel.add("Center", theView);
		
		thePanel = addYAxisName(thePanel, data, yKey, vertAxis, null);
		return thePanel;
	}
	
	protected XPanel modelChoicePanel(boolean isVertical) {
		XPanel menuPanel = new XPanel();
		menuPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
//		menuPanel.add(new XLabel("Trend", XLabel.LEFT, this));
		
			modelChoice = new XChoice(translate("Detrending") + ":", isVertical ? XChoice.VERTICAL_LEFT : XChoice.HORIZONTAL, this);
			modelChoice.addItem(translate("None"));
			modelChoice.addItem(translate("Linear"));
			modelChoice.addItem(translate("Quadratic"));
			modelChoice.select(0);
		
		menuPanel.add(modelChoice);
		
		return menuPanel;
	}

	
	private boolean localAction(Object target) {
		if (target == modelChoice) {
			int newModelType = modelChoice.getSelectedIndex();
			if (newModelType != selectedModel) {
				selectedModel = newModelType;
				
				QuadraticModel model = (QuadraticModel)data.getVariable("model");
				model.setParameters(lsParam[selectedModel][0], lsParam[selectedModel][1],
																															lsParam[selectedModel][2]);
				data.variableChanged("model");
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