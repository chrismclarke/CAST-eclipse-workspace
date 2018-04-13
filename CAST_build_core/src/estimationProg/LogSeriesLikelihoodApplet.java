package estimationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import formula.*;

import estimation.*;


public class LogSeriesLikelihoodApplet extends XApplet {
	static final private String LOG_SERIES_PARAM = "logSeries";
	static final private String DATA_AXIS_PARAM = "dataAxis";
	static final private String LOG_LIKELIHOOD_AXIS_PARAM = "loglikelihoodAxis";
	static final private String PARAM_AXIS_PARAM = "paramAxis";
	
	static final private String kZeroOneAxisParams = "0 1 0.0 0.2";
	
	static final private Color kBarColor = new Color(0x00FF00);
	
	private NumValue startTheta;
	private NumValue minTheta = null;
	private NumValue maxTheta = null;
	
	private DataSet data;
	private int counts[];
	
	private ParameterSlider paramSlider;
//	private XButton bestButton;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 0));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL));
			
				XPanel topPanel = new XPanel();
				topPanel.setLayout(new BorderLayout(0, 0));
				topPanel.add("North", new XLabel(translate("Log-likelihood") + MText.expandText(", #ell#(#theta#)"), XLabel.LEFT, this));
				topPanel.add("Center", logLikelihoodPanel(data, "logSeries", "y"));
			mainPanel.add(ProportionLayout.TOP, topPanel);
			
				XPanel bottomPanel = new XPanel();
				bottomPanel.setLayout(new BorderLayout(0, 0));
				bottomPanel.add("North", new XLabel(translate("Log-series probability function"), XLabel.LEFT, this));
				bottomPanel.add("Center", barchartPanel(data, "logSeries", "y"));
			mainPanel.add(ProportionLayout.BOTTOM, bottomPanel);
		
		add("Center", mainPanel);
		
		add("South", sliderPanel());
	}
	
	protected DataSet getData() {
		StringTokenizer st = new StringTokenizer(getParameter(LOG_SERIES_PARAM));
		if (st.countTokens() > 1) {
			minTheta = new NumValue(st.nextToken());
			maxTheta = new NumValue(st.nextToken());
		}
		startTheta = new NumValue(st.nextToken());
		
		DataSet data = new DataSet();
		
			LogSeriesDistnVariable logSeriesDistn = new LogSeriesDistnVariable("logSeries");
			logSeriesDistn.setTheta(startTheta);
		data.addVariable("logSeries", logSeriesDistn);
		
		logSeriesDistn.setMinSelection(-2);		//	so no bars are selected
		logSeriesDistn.setMaxSelection(-1);
		
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
			NumVariable y = (NumVariable)data.getVariable("y");
			counts = new int[y.noOfValues()];
			for (int i=0 ; i<counts.length ; i++)
				counts[i] = (int)Math.round(y.doubleValueAt(i));
		
		return data;
	}
	
	private XPanel barchartPanel(DataSet data, String distnKey, String dataKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis countAxis = new HorizAxis(this);
		countAxis.readNumLabels(getParameter(DATA_AXIS_PARAM));
		countAxis.setAxisName(translate("Counts"));
		thePanel.add("Bottom", countAxis);
		
		VertAxis probAxis = new VertAxis(this);
		probAxis.readNumLabels(kZeroOneAxisParams);
		thePanel.add("Left", probAxis);
		
		DataModelBarView barChart = new DataModelBarView(data, this, distnKey, dataKey, countAxis);
		barChart.setWiderBars(3);		// 3 pix wider at both sides so bars can be seen behind crosses
		barChart.setDensityColor(kBarColor);
		barChart.lockBackground(Color.white);
		thePanel.add("Center", barChart);
		
		return thePanel;
	}
	
	protected XPanel logLikelihoodPanel(DataSet data, String distnKey, String yKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis paramAxis = new HorizAxis(this);
		String paramAxisInfo = getParameter(PARAM_AXIS_PARAM);
		paramAxis.readNumLabels(paramAxisInfo == null ? kZeroOneAxisParams : paramAxisInfo);
		paramAxis.setAxisName(translate("Unknown parameter") + MText.expandText(", #theta#"));
		thePanel.add("Bottom", paramAxis);
		
		VertAxis loglikelihoodAxis = new VertAxis(this);
		loglikelihoodAxis.readNumLabels(getParameter(LOG_LIKELIHOOD_AXIS_PARAM));
		thePanel.add("Left", loglikelihoodAxis);
		
		LogSeriesLikelihoodFinder likelihoodFinder = new LogSeriesLikelihoodFinder(data, "logSeries", counts);
		LikelihoodView likelihood = new LogLikelihoodView(data, this, "logSeries", likelihoodFinder,
																											paramAxis, loglikelihoodAxis);
		likelihood.setDrawQuadratic(true);
		likelihood.lockBackground(Color.white);
		thePanel.add("Center", likelihood);
		
		return thePanel;
	}
	
	protected XPanel sliderPanel() {
		XPanel thePanel = new InsetPanel(30, 0);
		thePanel.setLayout(new BorderLayout(0, 20));
		
		if (minTheta == null)
			minTheta = new NumValue(0, startTheta.decimals);
		if (maxTheta == null)
			maxTheta = new NumValue(1, startTheta.decimals);
		
		paramSlider = new ParameterSlider(minTheta, maxTheta, startTheta,
															translate("Unknown parameter") + MText.expandText(", #theta#"), this);
		thePanel.add("Center", paramSlider);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == paramSlider) {
			LogSeriesDistnVariable distn = (LogSeriesDistnVariable)data.getVariable("logSeries");
			distn.setTheta(paramSlider.getParameter());
			data.variableChanged("logSeries");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}