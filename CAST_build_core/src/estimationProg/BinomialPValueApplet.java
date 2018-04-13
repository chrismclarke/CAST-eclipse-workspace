package estimationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import distn.*;
import utils.*;
import formula.*;

import estimation.*;


public class BinomialPValueApplet extends XApplet {
	static final private String X_AXIS_PARAM = "xAxis";
	static final private String X_AXIS_NAME_PARAM = "xAxisName";
	static final private String PROB_AXIS_PARAM = "probAxis";
	static final private String N_TRIALS_PARAM = "nTrials";
	static final private String N_SUCCESS_PARAM = "nSuccess";
	static final private String SIG_LEVEL_PARAM = "sigLevel";
	static final private String PROB_LIMITS_PARAM = "probLimits";
	
	private DataSet data;
	
	private int nSuccess;
	
	private ParameterSlider probSlider;
	private DiscretePValueView barChart;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 0));
		
		add("North", topPanel());
		
		add("South", sliderPanel());
		
		add("Center", barchartPanel(data, this));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		BinomialDistnVariable binomVar = new BinomialDistnVariable("x");
		int nTrials = Integer.parseInt(getParameter(N_TRIALS_PARAM));
		nSuccess = Integer.parseInt(getParameter(N_SUCCESS_PARAM));
		binomVar.setCount(nTrials);
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
	
	private XPanel barchartPanel(DataSet data, XApplet applet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis xAxis = new HorizAxis(applet);
		xAxis.readNumLabels(getParameter(X_AXIS_PARAM));
		xAxis.setAxisName(getParameter(X_AXIS_NAME_PARAM));
		thePanel.add("Bottom", xAxis);
		
		VertAxis probAxis = new VertAxis(applet);
		probAxis.readNumLabels(getParameter(PROB_AXIS_PARAM));
		thePanel.add("Left", probAxis);
		
		barChart = new DiscretePValueView(data, applet, "distn", probAxis, xAxis,
																				nSuccess, new NumValue(getParameter(SIG_LEVEL_PARAM)));
		barChart.lockBackground(Color.white);
		thePanel.add("Center", barChart);
		
		return thePanel;
	}
	
	private XPanel sliderPanel() {
		XPanel thePanel = new InsetPanel(40, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
		StringTokenizer st = new StringTokenizer(getParameter(PROB_LIMITS_PARAM));
		probSlider = new ParameterSlider(new NumValue(st.nextToken()), new NumValue(st.nextToken()),
																	new NumValue(st.nextToken()), MText.expandText("#pi##sub0#"), this);
		
		thePanel.add("Center", probSlider);
		
		BinomialDistnVariable binomVar = (BinomialDistnVariable)data.getVariable("distn");
		double newP = probSlider.getParameter().toDouble();
		binomVar.setProb(newP);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == probSlider) {
			BinomialDistnVariable binomVar = (BinomialDistnVariable)data.getVariable("distn");
			double newP = probSlider.getParameter().toDouble();
			binomVar.setProb(newP);
			data.variableChanged("distn");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}