package statistic2Prog;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import valueList.*;

import statistic2.*;


class CorrelPredictionSlider extends XNoValueSlider {
	static final private int kStepMax = 100;
	
	private DataSet data;
	private String predictKey;
	
	CorrelPredictionSlider(XApplet applet, DataSet data, String predictKey, 
																																	double initialCorr) {
		super(applet.translate("Worthless"), applet.translate("Good"),
																			applet.translate("Accuracy of forecast"), 0, kStepMax,
																			(int)Math.round(kStepMax * initialCorr), applet);
		this.data = data;
		this.predictKey = predictKey;
	}

	
	private boolean localAction(Object target) {
		CatPredictionVariable predictVar = (CatPredictionVariable)data.getVariable(predictKey);
		
		double newCorrel = getValue() * 0.01;
		predictVar.setCorrel(newCorrel);
		data.variableChanged(predictKey);
		return true;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}


public class CatPredictionApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String CUTOFF_PARAM = "cutoff";
	static final private String PREDICT_VAR_NAME_PARAM = "predictVarName";
	static final private String PREDICT_GROUP_NAMES_PARAM = "predictGroupNames";
	static final private String START_CORREL_PARAM = "startCorrel";
	static final private String YEAR_PARAM = "year";
	static final private String S_DECIMALS_PARAM = "sDecimals";
	
	private double startCorrel;
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new BorderLayout(0, 2));
			
			XLabel forecastName = new XLabel(data.getVariable("predictCat").name, XLabel.LEFT, this);
			forecastName.setFont(getStandardFont());
		add("North", forecastName);
			
		add("Center", dataDisplay(data));
		add("South", controlPanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
			StringTokenizer st = new StringTokenizer(getParameter(CUTOFF_PARAM));
			int nCutoffs = st.countTokens();
			double cutoff[] = new double[nCutoffs];
			for (int i=0 ; i<nCutoffs ; i++)
				cutoff[i] = Double.parseDouble(st.nextToken());
		
			startCorrel = Double.parseDouble(getParameter(START_CORREL_PARAM));
		
			CatPredictionVariable predictVar = new CatPredictionVariable(
							getParameter(PREDICT_VAR_NAME_PARAM), data, "y", startCorrel, cutoff);
			predictVar.readLabels(getParameter(PREDICT_GROUP_NAMES_PARAM));
			
		data.addVariable("predictCat", predictVar);
		
			NumVariable year = new NumVariable("Year");
			year.readSequence(getParameter(YEAR_PARAM));
		data.addVariable("year", year);
		
		return data;
	}
	
	private XPanel dataDisplay(DataSet data) {
		XPanel thePanel = new XPanel();
		
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis numAxis = new HorizAxis(this);
			numAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
			numAxis.setAxisName(data.getVariable("y").name);
			
		thePanel.add("Bottom", numAxis);
		
			VertAxis groupAxis = new VertAxis(this);
			CatVariable catVar = (CatVariable)data.getVariable("predictCat");
			groupAxis.setCatLabels(catVar);
			
		thePanel.add("Left", groupAxis);
		
			int sDecimals = Integer.parseInt(getParameter(S_DECIMALS_PARAM));
			PredictGroupDotView dataView = new PredictGroupDotView(data, this, "y", numAxis, groupAxis, sDecimals);
			dataView.setStickyDrag(true);
			dataView.lockBackground(Color.white);
			
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		
		thePanel.setLayout(new BorderLayout(0, 12));
		
			XPanel valuePanel = new XPanel();
			valuePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 7, 0));
			valuePanel.add(new OneValueView(data, "year", this));
			valuePanel.add(new OneValueView(data, "predictCat", this));
			valuePanel.add(new OneValueView(data, "y", this));
		thePanel.add("Center", valuePanel);
		
			XPanel sliderPanel = new InsetPanel(60, 0);
			sliderPanel.setLayout(new BorderLayout(0, 0));
				CorrelPredictionSlider predictSlider = new CorrelPredictionSlider(this,
																											data, "predictCat", startCorrel);
				predictSlider.setFont(getStandardBoldFont());
			sliderPanel.add(predictSlider);
			
		thePanel.add("South", sliderPanel);
		
		return thePanel;
	}
}