package sportProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;

import normal.*;
import sport.*;


public class RawGolfScoreApplet extends XApplet {
	static final private String HORIZ_AXIS_PARAM = "horizAxis";
	static final private String PROB_AXIS_PARAM = "probAxis";
	static final private String DISTN_NAME_PARAM = "distnName";
	
	static final private String MEAN_LIMITS_PARAM = "meanLimits";
	static final protected String SD_SCALING_PARAM = "sdScaling";
	static final private String MAX_X_PARAM = "maxX";
	
	protected NumValue minMean, maxMean;
	protected NumValue initialMean;
	
	protected DataSet data;
	private VertAxis theProbAxis;
	
	protected ParameterSlider meanSlider;
	
	public void setupApplet() {
		readDistnInfo();
		data = getData();
		
		setLayout(new BorderLayout(0, 0));
		add("Center", displayPanel(data));
		add("North", topPanel(data));
		add("South", bottomPanel(data));
	}
	
	private void readDistnInfo() {
		StringTokenizer st = new StringTokenizer(getParameter(MEAN_LIMITS_PARAM));
		minMean = new NumValue(st.nextToken());
		maxMean = new NumValue(st.nextToken());
		initialMean = new NumValue(st.nextToken());
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		String distnName = getParameter(DISTN_NAME_PARAM);
		ScaledNormalDistnVariable y = new ScaledNormalDistnVariable(distnName,
																			getParameter(SD_SCALING_PARAM));
		y.setMean(initialMean);
		data.addVariable("distn", y);
		
		return data;
	}
	
	protected String getDisplayDistnKey() {
		return "distn";
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 8));
		
		NumValue maxX = new NumValue(getParameter(MAX_X_PARAM));
		NormProbView probView = new NormProbView(data, this, getDisplayDistnKey(), maxX);
		probView.setRoundX(true);
		thePanel.add(probView);
		
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(HORIZ_AXIS_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		theHorizAxis.setAxisName(data.getVariable(getDisplayDistnKey()).name);
		thePanel.add("Bottom", theHorizAxis);
		
		theProbAxis = new VertAxis(this);
		labelInfo = getParameter(PROB_AXIS_PARAM);
		theProbAxis.readNumLabels(labelInfo);
		thePanel.add("Left", theProbAxis);
		
		DataView theView = getDataView(data, theHorizAxis, theProbAxis, getDisplayDistnKey());
		theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected DataView getDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theProbAxis,
																									String distnKey) {
		return new DistnDensityView(data, this, theHorizAxis, theProbAxis, distnKey,
																			DistnDensityView.NO_SHOW_MEANSD, DistnDensityView.MAX_DRAG);
	}
	
	protected XPanel bottomPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.25, 0, ProportionLayout.HORIZONTAL,
																					ProportionLayout.TOTAL));
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new ProportionLayout(0.66667, 0, ProportionLayout.HORIZONTAL,
																					ProportionLayout.TOTAL));
		
				meanSlider = new ParameterSlider(minMean, maxMean, initialMean, translate("mean score"),
																		ParameterSlider.SHOW_MIN_MAX, this);
			rightPanel.add("Left", meanSlider);
		thePanel.add("Right", rightPanel);
		
		return thePanel;
	}
	
	protected void changeRawMean(NumValue newMean) {
		ScaledNormalDistnVariable y = (ScaledNormalDistnVariable)data.getVariable("distn");
		y.setMean(newMean);
		data.variableChanged("distn");
	}

	
	private boolean localAction(Object target) {
		if (target == meanSlider) {
			NumValue mean = meanSlider.getParameter();
			changeRawMean(mean);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}