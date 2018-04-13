package estimationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import distn.*;
import utils.*;
import formula.*;

import estimation.*;


public class MedianTestApplet extends XApplet {
	static final private String DATA_AXIS_PARAM = "dataAxis";
	static final private String BINOM_AXIS_PARAM = "binomAxis";
	static final private String PROB_AXIS_PARAM = "probAxis";
	static final private String SIG_LEVEL_PARAM = "sigLevel";
	static final private String MEDIAN_LIMITS_PARAM = "medianLimits";
	
	static final private String kValuesBelowString = "values less than";
	
	private DataSet data;
	
	private ParameterSlider medianSlider;
	private DiscretePValueView barChart;
	private StackCumulativeView dataView;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 0));
		
		add("North", topPanel());
		
		add("South", sliderPanel());
		
		XPanel mainPanel = new XPanel();
		mainPanel.setLayout(new ProportionLayout(0.7, 10, ProportionLayout.VERTICAL));
			mainPanel.add(ProportionLayout.BOTTOM, dataPanel(data, this));
			mainPanel.add(ProportionLayout.TOP, barchartPanel(data, this));
		
		add("Center", mainPanel);
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
			NumVariable yVar = new NumVariable(getParameter(VAR_NAME_PARAM));
			yVar.readValues(getParameter(VALUES_PARAM));
		data.addVariable("y", yVar);
		
			BinomialDistnVariable binomVar = new BinomialDistnVariable("x");
			int nTrials = yVar.noOfValues();
			binomVar.setCount(nTrials);
			binomVar.setProb(0.5);
		data.addVariable("distn", binomVar);
		
		return data;
	}
	
	private XPanel topPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			XLabel probLabel = new XLabel("p(x)", XLabel.LEFT, this);
		thePanel.add(probLabel);
		return thePanel;
	}
	
	private XPanel dataPanel(DataSet data, XApplet applet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis yAxis = new HorizAxis(applet);
		yAxis.readNumLabels(getParameter(DATA_AXIS_PARAM));
		yAxis.setAxisName(data.getVariable("y").name);
		thePanel.add("Bottom", yAxis);
		
		dataView = new StackCumulativeView(data, this, yAxis, "y");
		NumValue cutoff = medianSlider.getParameter();
		dataView.setCutoff(cutoff);
		dataView.lockBackground(Color.white);
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	private XPanel barchartPanel(DataSet data, XApplet applet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis xAxis = new HorizAxis(applet);
		xAxis.readNumLabels(getParameter(BINOM_AXIS_PARAM));
		xAxis.setAxisName("Y = " + translate(kValuesBelowString) + MText.expandText(" #theta#"));
		thePanel.add("Bottom", xAxis);
		
		VertAxis probAxis = new VertAxis(applet);
		probAxis.readNumLabels(getParameter(PROB_AXIS_PARAM));
		thePanel.add("Left", probAxis);
		
		int nBelow = dataView.getNBelowCutoff();
		barChart = new DiscretePValueView(data, applet, "distn", probAxis, xAxis,
																				nBelow, new NumValue(getParameter(SIG_LEVEL_PARAM)));
		barChart.lockBackground(Color.white);
		thePanel.add("Center", barChart);
		
		return thePanel;
	}
	
	private XPanel sliderPanel() {
		XPanel thePanel = new InsetPanel(40, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
		StringTokenizer st = new StringTokenizer(getParameter(MEDIAN_LIMITS_PARAM));
		medianSlider = new ParameterSlider(new NumValue(st.nextToken()), new NumValue(st.nextToken()),
														new NumValue(st.nextToken()), MText.expandText("#theta##sub0#"), this);
		
		thePanel.add("Center", medianSlider);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == medianSlider) {
			NumValue newMedian = medianSlider.getParameter();
			dataView.setCutoff(newMedian);
			
			int nBelow = dataView.getNBelowCutoff();
			barChart.setObservedX(nBelow);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}