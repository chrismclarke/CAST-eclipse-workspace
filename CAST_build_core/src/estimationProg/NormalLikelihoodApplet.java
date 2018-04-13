package estimationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import formula.*;

import estimation.*;


public class NormalLikelihoodApplet extends XApplet {
	static final private String NORMAL_PARAM = "normal";
	static final private String DATA_AXIS_PARAM = "dataAxis";
	static final private String DENSITY_AXIS_PARAM = "densityAxis";
	static final private String PARAM_AXIS_PARAM = "paramAxis";
	static final private String LOG_LIKELIHOOD_AXIS_PARAM = "loglikelihoodAxis";
	
	private NumValue startMu, minMu, maxMu, sigma;
	
	private DataSet data;
	
	private ParameterSlider paramSlider;
	private XButton bestButton;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 0));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL));
			
				XPanel topPanel = new XPanel();
				topPanel.setLayout(new BorderLayout(0, 0));
				topPanel.add("North", new XLabel(translate("Log-likelihood") + MText.expandText(", #ell#(#mu#)"), XLabel.LEFT, this));
				topPanel.add("Center", logLikelihoodPanel(data, "normal", "y"));
			mainPanel.add(ProportionLayout.TOP, topPanel);
			
				XPanel bottomPanel = new XPanel();
				bottomPanel.setLayout(new BorderLayout(0, 0));
				bottomPanel.add("North", new XLabel(translate("Normal probability density"), XLabel.LEFT, this));
				bottomPanel.add("Center", densityPanel(data, "normal", "y"));
			mainPanel.add(ProportionLayout.BOTTOM, bottomPanel);
		
		add("Center", mainPanel);
		
		add("South", sliderPanel());
	}
	
	protected DataSet getData() {
		StringTokenizer st = new StringTokenizer(getParameter(NORMAL_PARAM));
		minMu = new NumValue(st.nextToken());
		maxMu = new NumValue(st.nextToken());
		startMu = new NumValue(st.nextToken());
		sigma = new NumValue(st.nextToken());
		
		DataSet data = new DataSet();
		
			NormalDistnVariable normalDistn = new NormalDistnVariable("normal");
			normalDistn.setMean(startMu.toDouble());
			normalDistn.setSD(sigma.toDouble());
			normalDistn.setDecimals(Math.max(sigma.decimals, startMu.decimals));
		data.addVariable("normal", normalDistn);
		
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		return data;
	}
	
	private XPanel densityPanel(DataSet data, String distnKey, String dataKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis valueAxis = new HorizAxis(this);
		valueAxis.readNumLabels(getParameter(DATA_AXIS_PARAM));
		valueAxis.setAxisName(translate("Data values") + ", X");
		thePanel.add("Bottom", valueAxis);
		
		VertAxis densityAxis = new VertAxis(this);
		densityAxis.readNumLabels(getParameter(DENSITY_AXIS_PARAM));
		thePanel.add("Left", densityAxis);
		
		DataDensityView densityView = new DataDensityView(data, this, distnKey, dataKey, valueAxis, densityAxis);
		densityView.lockBackground(Color.white);
		thePanel.add("Center", densityView);
		
		return thePanel;
	}
	
	protected XPanel logLikelihoodPanel(DataSet data, String distnKey, String yKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis paramAxis = new HorizAxis(this);
		paramAxis.readNumLabels(getParameter(PARAM_AXIS_PARAM));
		paramAxis.setAxisName(translate("Unknown parameter") + MText.expandText(", #mu#"));
		thePanel.add("Bottom", paramAxis);
		
		VertAxis loglikelihoodAxis = new VertAxis(this);
		loglikelihoodAxis.readNumLabels(getParameter(LOG_LIKELIHOOD_AXIS_PARAM));
		thePanel.add("Left", loglikelihoodAxis);
		NumVariable yVar = (NumVariable)data.getVariable("y");
		double yValues[] = new double[yVar.noOfValues()];
		for (int i=0; i<yValues.length ; i++)
			yValues[i] = yVar.doubleValueAt(i);
		NormalLikelihoodFinder likelihoodFinder = new NormalLikelihoodFinder(data, "normal", yValues);
		LikelihoodView likelihood = new LogLikelihoodView(data, this, "normal", likelihoodFinder,																											paramAxis, loglikelihoodAxis);
		likelihood.setDrawSlope(true);
		likelihood.lockBackground(Color.white);
		thePanel.add("Center", likelihood);
		
		return thePanel;
	}
	
	protected XPanel sliderPanel() {
		XPanel thePanel = new InsetPanel(30, 0);
		thePanel.setLayout(new BorderLayout(0, 20));
		
			paramSlider = new ParameterSlider(minMu, maxMu, startMu,
											translate("Normal distribution mean") + MText.expandText(", #mu#"), this);
		thePanel.add("Center", paramSlider);
		
			bestButton = new XButton(translate("Max likelihood"), this);
		thePanel.add("East", bestButton);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == paramSlider) {
			NormalDistnVariable distn = (NormalDistnVariable)data.getVariable("normal");
			distn.setMean(paramSlider.getParameter().toDouble());
			data.variableChanged("normal");
			return true;
		}
		else if (target == bestButton) {
			NumVariable y = (NumVariable)getData().getVariable("y");
			double sum = 0.0;
			int n = y.noOfValues();
			for (int i=0 ; i<n ; i++)
				sum += y.doubleValueAt(i);
			
			double mean = sum / n;
			
			NormalDistnVariable distn = (NormalDistnVariable)data.getVariable("normal");
			distn.setMean(mean);
			data.variableChanged("normal");
			
			paramSlider.setParameter(mean);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}