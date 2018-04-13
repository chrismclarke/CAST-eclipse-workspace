package estimationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import formula.*;

import estimation.*;


public class BinomialLikelihoodApplet extends XApplet {
	static final private String BINOM_PARAM = "binom";
	static final private String DATA_AXIS_PARAM = "dataAxis";
	static final private String LIKELIHOOD_AXIS_PARAM = "likelihoodAxis";
	
	static final protected String kZeroOneAxisParams = "0 1 0.0 0.2";
	
	static final private Color kDistnColor = new Color(0xCCBBBB);
	static final private Color kHighlightColor = new Color(0x0000FF);
	
	protected int n, x;
	protected NumValue startPi;
	
	protected DataSet data;
	
	protected ParameterSlider paramSlider;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 0));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL));
			
				XPanel topPanel = new XPanel();
				topPanel.setLayout(new BorderLayout(0, 0));
				topPanel.add("North", new XLabel(translate("Prob of observed data (Likelihood)"), XLabel.LEFT, this));
				topPanel.add("Center", likelihoodPanel(data, false));
			
			mainPanel.add(ProportionLayout.TOP, topPanel);
			
				XPanel bottomPanel = new XPanel();
				bottomPanel.setLayout(new BorderLayout(0, 0));
				bottomPanel.add("North", new XLabel(translate("Binomial probability function"), XLabel.LEFT, this));
				bottomPanel.add("Center", barchartPanel(data, x));
			mainPanel.add(ProportionLayout.BOTTOM, bottomPanel);
		
		add("Center", mainPanel);
		
		add("South", sliderPanel());
	}
	
	protected DataSet getData() {
		StringTokenizer st = new StringTokenizer(getParameter(BINOM_PARAM));
		n = Integer.parseInt(st.nextToken());
		x = Integer.parseInt(st.nextToken());
		startPi = new NumValue(st.nextToken());
		
		DataSet data = new DataSet();
		
			BinomialDistnVariable binom = new BinomialDistnVariable("binom");
			binom.setCount(n);
			binom.setProb(startPi.toDouble());
		data.addVariable("binom", binom);
		
		binom.setMinSelection(x - 0.5);
		binom.setMaxSelection(x + 0.5);
		
		return data;
	}
	
	private XPanel barchartPanel(DataSet data, int x) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis nAxis = new HorizAxis(this);
		nAxis.readNumLabels(getParameter(DATA_AXIS_PARAM));
		nAxis.setAxisName(translate("Binomial values"));
		thePanel.add("Bottom", nAxis);
		
		VertAxis probAxis = new VertAxis(this);
		probAxis.readNumLabels(kZeroOneAxisParams);
		thePanel.add("Left", probAxis);
		
		BarChartSelected barChart = new BarChartSelected(data, this, "binom", nAxis, x);
		barChart.setDensityColor(kDistnColor);
		barChart.setHighlightColor(kHighlightColor);
		barChart.lockBackground(Color.white);
		thePanel.add("Center", barChart);
		
		return thePanel;
	}
	
	protected XPanel likelihoodPanel(DataSet data, boolean showSlope) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis piAxis = new HorizAxis(this);
		piAxis.readNumLabels(kZeroOneAxisParams);
		piAxis.setAxisName(getParamAxisName());
		thePanel.add("Bottom", piAxis);
		
		VertAxis likelihoodAxis = new VertAxis(this);
		likelihoodAxis.readNumLabels(getParameter(LIKELIHOOD_AXIS_PARAM));
		thePanel.add("Left", likelihoodAxis);
		
		BinomialLikelihoodFinder likelihoodFinder = new BinomialLikelihoodFinder(data, "binom", n, x);
		LikelihoodView likelihood = new LikelihoodView(data, this, "binom", likelihoodFinder, piAxis, likelihoodAxis);
		if (showSlope)
			likelihood.setDrawSlope(true);
		likelihood.lockBackground(Color.white);
		thePanel.add("Center", likelihood);
		
		return thePanel;
	}
	
	protected String getParamAxisName() {
		return translate("Unknown parameter") + MText.expandText(", #pi#");
	}
	
	protected XPanel sliderPanel() {
		XPanel thePanel = new InsetPanel(30, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
		paramSlider = new ParameterSlider(new NumValue(0, startPi.decimals), new NumValue(1, startPi.decimals), startPi,
																			translate("Probability of success") + MText.expandText(", #pi#"), this);
		thePanel.add("Center", paramSlider);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == paramSlider) {
			BinomialDistnVariable distn = (BinomialDistnVariable)data.getVariable("binom");
			distn.setProb(paramSlider.getParameter().toDouble());
			data.variableChanged("binom");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}