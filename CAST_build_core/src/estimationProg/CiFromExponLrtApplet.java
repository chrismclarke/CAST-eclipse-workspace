package estimationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import formula.*;

import estimation.*;


public class CiFromExponLrtApplet extends XApplet {
	static final private String LAMBDA_PARAM = "lambda";
	static final private String DATA_AXIS_PARAM = "dataAxis";
	static final private String DENSITY_AXIS_PARAM = "densityAxis";
	static final private String LAMBDA_AXIS_PARAM = "lambdaAxis";
	static final private String LOG_LIKELIHOOD_AXIS_PARAM = "loglikelihoodAxis";
	static final private String DECIMALS_PARAM = "decimals";
	static final private String SIG_LEVEL_PARAM = "sigLevel";
	
	private DataSet data;
	private double yValues[];
	
	private ParameterSlider paramSlider;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 0));
		
		XPanel mainPanel = new XPanel();
		mainPanel.setLayout(new ProportionLayout(0.6, 0, ProportionLayout.VERTICAL));
		
		XPanel topPanel = new XPanel();
		topPanel.setLayout(new BorderLayout(0, 0));
		topPanel.add("North", new XLabel(translate("Log-likelihood") + MText.expandText(", #ell#(#lambda#)"), XLabel.LEFT, this));
		topPanel.add("Center", logLikelihoodPanel(data, "expon", "y"));
		mainPanel.add(ProportionLayout.TOP, topPanel);
		
		XPanel bottomPanel = new XPanel();
		bottomPanel.setLayout(new BorderLayout(0, 0));
		bottomPanel.add("North", new XLabel(translate("Exponential probability density"), XLabel.LEFT, this));
		bottomPanel.add("Center", densityPanel(data, "expon", "y"));
		mainPanel.add(ProportionLayout.BOTTOM, bottomPanel);
		
		add("Center", mainPanel);
		
		add("South", sliderPanel());
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		ExponDistnVariable exponDistn = new ExponDistnVariable("expon");
		data.addVariable("expon", exponDistn);
		
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		NumVariable y = (NumVariable)data.getVariable("y");
		yValues = new double[y.noOfValues()];
		for (int i=0 ; i<yValues.length ; i++)
			yValues[i] = y.doubleValueAt(i);
		
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
		densityView.setSupport(0, Double.POSITIVE_INFINITY);
		densityView.lockBackground(Color.white);
		thePanel.add("Center", densityView);
		
		return thePanel;
	}
	
	private double findMle(DataSet data, String yKey) {
		NumVariable yVar = (NumVariable)data.getVariable(yKey);
		ValueEnumeration ye = yVar.values();
		double sum = 0.0;
		while (ye.hasMoreValues())
			sum += ye.nextDouble();
		return yVar.noOfValues() / sum;
	}
	
	protected XPanel logLikelihoodPanel(DataSet data, String distnKey, String yKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis paramAxis = new HorizAxis(this);
			paramAxis.readNumLabels(getParameter(LAMBDA_AXIS_PARAM));
			paramAxis.setAxisName(translate("Null hypothesis parameter") + MText.expandText(", #lambda##sub0#"));
		thePanel.add("Bottom", paramAxis);
		
			VertAxis loglikelihoodAxis = new VertAxis(this);
			loglikelihoodAxis.readNumLabels(getParameter(LOG_LIKELIHOOD_AXIS_PARAM));
		thePanel.add("Left", loglikelihoodAxis);
		
			ExponentialLikelihoodFinder likelihoodFinder = new ExponentialLikelihoodFinder(data, "expon", yValues);
			StringTokenizer st = new StringTokenizer(getParameter(DECIMALS_PARAM));
			int likelihoodDecimals = Integer.parseInt(st.nextToken());
			int pValueDecimals = Integer.parseInt(st.nextToken());
		LogLikelihoodTestView likelihood = new LogLikelihoodTestView(data, this, "expon",
															likelihoodFinder, paramAxis, loglikelihoodAxis, findMle(data, yKey),
															likelihoodDecimals, pValueDecimals);
			double sigLevel = Double.parseDouble(getParameter(SIG_LEVEL_PARAM));
			likelihood.setSigLevel(sigLevel);
			likelihood.lockBackground(Color.white);
		thePanel.add("Center", likelihood);
		
		return thePanel;
	}
	
	protected XPanel sliderPanel() {
		XPanel thePanel = new InsetPanel(20, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
		StringTokenizer st = new StringTokenizer(getParameter(LAMBDA_PARAM));
			paramSlider = new ParameterSlider(new NumValue(st.nextToken()), new NumValue(st.nextToken()),
																new NumValue(st.nextToken()),
																translate("Rate of events") + MText.expandText(", #lambda##sub0#"), this);
		thePanel.add("Center", paramSlider);
		
		ExponDistnVariable distn = (ExponDistnVariable)data.getVariable("expon");
		distn.setLambda(paramSlider.getParameter());
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == paramSlider) {
			ExponDistnVariable distn = (ExponDistnVariable)data.getVariable("expon");
			distn.setLambda(paramSlider.getParameter());
			data.variableChanged("expon");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}