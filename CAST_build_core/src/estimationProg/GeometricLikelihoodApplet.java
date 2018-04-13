package estimationProg;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import formula.*;

import estimation.*;


public class GeometricLikelihoodApplet extends XApplet {
	static final private String GEOM_PARAM = "geom";
	static final private String DATA_AXIS_PARAM = "dataAxis";
	static final private String LOG_LIKELIHOOD_AXIS_PARAM = "loglikelihoodAxis";
	
	static final protected String kZeroOneAxisParams = "0 1 0.0 0.2";
	
	private NumValue startPi;
	
	private DataSet data;
	private int counts[];
	
	private ParameterSlider paramSlider;
	private XButton bestButton;
	
	private DataModelBarView barChart;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 0));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL));
			
				XPanel topPanel = new XPanel();
				topPanel.setLayout(new BorderLayout(0, 0));
				topPanel.add("North", new XLabel(translate("Log-likelihood") + MText.expandText(", #ell#(#pi#)"), XLabel.LEFT, this));
				topPanel.add("Center", logLikelihoodPanel(data, "geom", "y"));
			mainPanel.add(ProportionLayout.TOP, topPanel);
			
				XPanel bottomPanel = new XPanel();
				bottomPanel.setLayout(new BorderLayout(0, 0));
				bottomPanel.add("North", new XLabel(translate("Geometric probability function"), XLabel.LEFT, this));
				bottomPanel.add("Center", barchartPanel(data, "geom", "y"));
			mainPanel.add(ProportionLayout.BOTTOM, bottomPanel);
		
		add("Center", mainPanel);
		
		add("South", sliderPanel());
	}
	
	protected DataSet getData() {
		startPi = new NumValue(getParameter(GEOM_PARAM));
		
		DataSet data = new DataSet();
		
			GeometricDistnVariable geomDistn = new GeometricDistnVariable("geom");
			geomDistn.setPSuccess(startPi);
		data.addVariable("geom", geomDistn);
		
		geomDistn.setMinSelection(-2);		//	so no bars are selected
		geomDistn.setMaxSelection(-1);
		
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
		countAxis.setAxisName(translate("Trials to first success"));
		thePanel.add("Bottom", countAxis);
		
		VertAxis probAxis = new VertAxis(this);
		probAxis.readNumLabels(kZeroOneAxisParams);
		thePanel.add("Left", probAxis);
		
		barChart = new DataModelBarView(data, this, distnKey, dataKey, countAxis);
		barChart.lockBackground(Color.white);
		thePanel.add("Center", barChart);
		
		return thePanel;
	}
	
	protected XPanel logLikelihoodPanel(DataSet data, String distnKey, String yKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis piAxis = new HorizAxis(this);
		piAxis.readNumLabels(kZeroOneAxisParams);
		piAxis.setAxisName(translate("Unknown parameter") + MText.expandText(", #pi#"));
		thePanel.add("Bottom", piAxis);
		
		VertAxis loglikelihoodAxis = new VertAxis(this);
		loglikelihoodAxis.readNumLabels(getParameter(LOG_LIKELIHOOD_AXIS_PARAM));
		thePanel.add("Left", loglikelihoodAxis);
		
		GeometricLikelihoodFinder likelihoodFinder = new GeometricLikelihoodFinder(data, "geom", counts);
		LikelihoodView likelihood = new LogLikelihoodView(data, this, "geom", likelihoodFinder,
																											piAxis, loglikelihoodAxis);
		likelihood.setDrawSlope(true);
		likelihood.lockBackground(Color.white);
		thePanel.add("Center", likelihood);
		
		return thePanel;
	}
	
	protected XPanel sliderPanel() {
		XPanel thePanel = new InsetPanel(30, 0);
		thePanel.setLayout(new BorderLayout(0, 20));
		
			paramSlider = new ParameterSlider(new NumValue(0, startPi.decimals), new NumValue(1, startPi.decimals), startPi,
																			translate("Probability of success") + MText.expandText(", #pi#"), this) {
															protected void DoMousePressed(MouseEvent e) {
																barChart.setDimCrosses(true);
															}
															
															protected void DoMouseReleased(MouseEvent e) {
																barChart.setDimCrosses(false);
															}
														};
		thePanel.add("Center", paramSlider);
		
			bestButton = new XButton(translate("Max likelihood"), this);
		thePanel.add("East", bestButton);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == paramSlider) {
			GeometricDistnVariable distn = (GeometricDistnVariable)data.getVariable("geom");
			distn.setPSuccess(paramSlider.getParameter());
			data.variableChanged("geom");
			return true;
		}
		else if (target == bestButton) {
			NumVariable y = (NumVariable)getData().getVariable("y");
			double sum = 0.0;
			int n = y.noOfValues();
			for (int i=0 ; i<n ; i++)
				sum += y.doubleValueAt(i);
			
			double mean = sum / n;
			
			GeometricDistnVariable distn = (GeometricDistnVariable)data.getVariable("geom");
			NumValue bestProb = new NumValue(1 / mean, startPi.decimals);
			distn.setPSuccess(bestProb);
			data.variableChanged("geom");
			
			paramSlider.setParameter(bestProb.toDouble());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}