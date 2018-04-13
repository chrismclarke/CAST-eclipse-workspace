package estimationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import formula.*;

import estimation.*;


public class PoissonLrtApplet extends XApplet {
	static final private String LAMBDA_PARAM = "lambda";
	static final private String DATA_AXIS_PARAM = "dataAxis";
	static final private String PROB_AXIS_PARAM = "probAxis";
	static final private String LAMBDA_AXIS_PARAM = "lambdaAxis";
	static final private String LOG_LIKELIHOOD_AXIS_PARAM = "loglikelihoodAxis";
	static final private String DECIMALS_PARAM = "decimals";
	
	private DataSet data;
	private int counts[];
	
	private ParameterSlider paramSlider;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 0));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.64, 0, ProportionLayout.VERTICAL));
			
				XPanel topPanel = new XPanel();
				topPanel.setLayout(new BorderLayout(0, 0));
				topPanel.add("North", new XLabel(translate("Log-likelihood") + MText.expandText(", #ell#(#lambda#)"), XLabel.LEFT, this));
				topPanel.add("Center", logLikelihoodPanel(data, "poisson", "y"));
			mainPanel.add(ProportionLayout.TOP, topPanel);
			
				XPanel bottomPanel = new XPanel();
				bottomPanel.setLayout(new BorderLayout(0, 0));
				bottomPanel.add("North", new XLabel(translate("Poisson probability function"), XLabel.LEFT, this));
				bottomPanel.add("Center", barchartPanel(data, "poisson", "y"));
			mainPanel.add(ProportionLayout.BOTTOM, bottomPanel);
		
		add("Center", mainPanel);
		
		add("South", sliderPanel());
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			PoissonDistnVariable poissonDistn = new PoissonDistnVariable("poisson");
		data.addVariable("poisson", poissonDistn);
		
		poissonDistn.setMinSelection(-2);		//	so no bars are selected
		poissonDistn.setMaxSelection(-1);
		
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
		countAxis.setAxisName(translate("Number of events"));
		thePanel.add("Bottom", countAxis);
		
		VertAxis probAxis = new VertAxis(this);
		probAxis.readNumLabels(getParameter(PROB_AXIS_PARAM));
		thePanel.add("Left", probAxis);
		
		DataModelBarView barChart = new DataModelBarView(data, this, distnKey, dataKey, countAxis);
		barChart.setFixedMaxProb(probAxis.maxOnAxis);
		barChart.lockBackground(Color.white);
		thePanel.add("Center", barChart);
		
		return thePanel;
	}
	
	private double findDataMean(DataSet data, String yKey) {
		NumVariable yVar = (NumVariable)data.getVariable(yKey);
		ValueEnumeration ye = yVar.values();
		double sum = 0.0;
		while (ye.hasMoreValues())
			sum += ye.nextDouble();
		return sum / yVar.noOfValues();
	}
	
	protected XPanel logLikelihoodPanel(DataSet data, String distnKey, String yKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis piAxis = new HorizAxis(this);
		piAxis.readNumLabels(getParameter(LAMBDA_AXIS_PARAM));
		piAxis.setAxisName(translate("Null hypothesis parameter") + MText.expandText(", #lambda##sub0#"));
		thePanel.add("Bottom", piAxis);
		
		VertAxis loglikelihoodAxis = new VertAxis(this);
		loglikelihoodAxis.readNumLabels(getParameter(LOG_LIKELIHOOD_AXIS_PARAM));
		thePanel.add("Left", loglikelihoodAxis);
		
		PoissonLikelihoodFinder likelihoodFinder = new PoissonLikelihoodFinder(data, "poisson", counts);
		StringTokenizer st = new StringTokenizer(getParameter(DECIMALS_PARAM));
		int likelihoodDecimals = Integer.parseInt(st.nextToken());
		int pValueDecimals = Integer.parseInt(st.nextToken());
		LikelihoodView likelihood = new LogLikelihoodTestView(data, this, "poisson", likelihoodFinder,
																							piAxis, loglikelihoodAxis, findDataMean(data, yKey),
																							likelihoodDecimals, pValueDecimals);
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
		
		PoissonDistnVariable distn = (PoissonDistnVariable)data.getVariable("poisson");
		distn.setLambda(paramSlider.getParameter());
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == paramSlider) {
			PoissonDistnVariable distn = (PoissonDistnVariable)data.getVariable("poisson");
			distn.setLambda(paramSlider.getParameter());
			data.variableChanged("poisson");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}