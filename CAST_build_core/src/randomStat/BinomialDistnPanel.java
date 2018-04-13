package randomStat;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import utils.*;
import formula.*;

import distribution.*;

public class BinomialDistnPanel extends XPanel {
	static final private String kProbAxisInfo = "-0.05 1.05 0 0.2";
	
	private ParameterSlider nSlider, pSlider;
	
	private DataSet data;
	private HorizAxis pAxis;
	private BinomialCountAxis nAxis;
	
	public BinomialDistnPanel(XApplet applet, int maxN, int startN, int sliderOrientation,
																							int dragType) {
//		MeanSDImages.loadMeanSD(applet);
	
		data = new DataSet();
		BinomialDistnVariable y = new BinomialDistnVariable("x");
		y.setCount(startN);
		y.setProb(0.5);
		data.addVariable("distn", y);
		data.setSelection("distn", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		
		setLayout(new BorderLayout(0, 10));
		
		add("North", sliderPanel(maxN, startN, sliderOrientation, applet));
		add("Center", barchartPanel(data, applet, dragType));
		XPanel probPanel = probPanel(data, maxN, applet);
		if (probPanel != null)
			add("South", probPanel);
	}
	
	private XPanel sliderPanel(int maxN, int startN, int sliderOrientation, XApplet applet) {
		XPanel thePanel = new XPanel();
		int sliderGap = (sliderOrientation == ProportionLayout.HORIZONTAL) ? 5 : 0;
		thePanel.setLayout(new ProportionLayout(0.5, sliderGap, sliderOrientation, ProportionLayout.TOTAL));
		
			nSlider = new ParameterSlider(new NumValue(1, 0), new NumValue(maxN, 0),
									new NumValue(startN, 0), "n", applet);
		thePanel.add("Left", nSlider);
		
			String pi = MText.expandText("#pi#");
			pSlider = new ParameterSlider(new NumValue(0, 2), new NumValue(1, 2),
										new NumValue(0.5, 2), pi, applet);
			pSlider.setForeground(Color.blue);
			
		thePanel.add("Right", pSlider);
		return thePanel;
	}
	
	private XPanel barchartPanel(DataSet data, XApplet applet, int dragType) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		pAxis = new HorizAxis(applet);
		pAxis.readNumLabels(kProbAxisInfo);
		pAxis.setAxisName("p = " + applet.translate("propn") + "(" + applet.translate("success") + ")");
		thePanel.add("Bottom", pAxis);
		
		nAxis = new BinomialCountAxis(data, "distn", applet);
		nAxis.setAxisName("x = #" + applet.translate("success"));
		thePanel.add("Bottom", nAxis);
		
		thePanel.add("Center", barChartView(data, applet, dragType, pAxis, nAxis));
		
		VertAxis vertAxis = getVertAxis(applet);
		if (vertAxis != null)
			thePanel.add("Left", vertAxis);
		
		return thePanel;
	}
	
	protected VertAxis getVertAxis(XApplet applet) {
		return null;		//	overridden for subclass that displays CDF
	}
	
	protected DataView barChartView(DataSet data, XApplet applet, int dragType, HorizAxis pAxis,
																	BinomialCountAxis nAxis) {
		DiscreteProbView barChart = new DiscreteProbView(data, applet, "distn", null, pAxis, nAxis, dragType);
		barChart.lockBackground(Color.white);
		return barChart;
	}
	
	protected XPanel probPanel(DataSet data, int maxN, XApplet applet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		DiscreteProbValueView probView = new DiscreteProbValueView(data, "distn", applet, maxN);
		probView.setFont(applet.getBigFont());
		thePanel.add(probView);
		return thePanel;
	}
	
	public Insets insets() {
		return new Insets(3, 3, 3, 3);
	}

	
	private boolean localAction(Object target) {
		BinomialDistnVariable y = (BinomialDistnVariable)data.getVariable("distn");
		if (target == pSlider) {
			double newP = pSlider.getParameter().toDouble();
			y.setProb(newP);
			data.variableChanged("distn");
			return true;
		}
		else if (target == nSlider) {
			int newN = (int)Math.round(nSlider.getParameter().toDouble());
			y.setCount(newN);
			data.variableChanged("distn");
			nAxis.setCountLabels();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}