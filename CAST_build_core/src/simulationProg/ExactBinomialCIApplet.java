package simulationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import formula.*;

import distribution.*;
import randomStat.*;


public class ExactBinomialCIApplet extends XApplet {
	static final private String N_X_PARAM = "nx";
	static final private String PI_PARAM = "pi";
	
	static final private String AXIS_INFO_PARAM = "horizAxis";
	
	private int n, x;
	private String minPi, maxPi;
	private double startPi;
	private int nPiSteps;
	
	private ParameterSlider piSlider;
	
	private DataSet data1, data2;
	
	public void setupApplet() {
//		MeanSDImages.loadMeanSD(this);
		
		StringTokenizer st = new StringTokenizer(getParameter(N_X_PARAM));
		n = Integer.parseInt(st.nextToken());
		x = Integer.parseInt(st.nextToken());
		
		st = new StringTokenizer(getParameter(PI_PARAM));
		minPi = st.nextToken();
		maxPi = st.nextToken();
		startPi = Double.parseDouble(st.nextToken());
		nPiSteps = Integer.parseInt(st.nextToken());
		
		data1 = getData(n, startPi, x, true);
		data2 = getData(n, startPi, x, false);
		
		setLayout(new BorderLayout(20, 0));
		add("Center", mainPanel(data1, data2));
		add("South", sliderPanel());
	}
	
	private DataSet getData(int n, double p, double x, boolean upperTail) {
		DataSet data = new DataSet();
		BinomialDistnVariable y = new BinomialDistnVariable("x");
		y.setCount(n);
		y.setProb(p);
		data.addVariable("distn", y);
		data.setSelection("distn", upperTail ? (x - 0.5) : Double.NEGATIVE_INFINITY,
													upperTail ? Double.POSITIVE_INFINITY : (x + 0.5));
		return data;
	}
	
	private XPanel mainPanel(DataSet data1, DataSet data2) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 20, ProportionLayout.VERTICAL,
																					ProportionLayout.TOTAL));
		
		thePanel.add("Top", barchartPanel(data1));
		thePanel.add("Bottom", barchartPanel(data2));
		
		return thePanel;
	}
	
	private XPanel barchartPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(15, 0));
		
			XPanel barChartPanel = new XPanel();
			barChartPanel.setLayout(new AxisLayout());
			
				HorizAxis nAxis = new HorizAxis(this);
				nAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
			barChartPanel.add("Bottom", nAxis);
			
				DiscreteProbView barChart = new DiscreteProbView(data, this, "distn", null, null, nAxis,
											DiscreteProbView.NO_DRAG);
				barChart.lockBackground(Color.white);
				barChart.setHighlightColor(Color.red);
			barChartPanel.add("Center", barChart);
		
		thePanel.add("Center", barChartPanel);
		
			XPanel probViewPanel = new XPanel();
			probViewPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																		VerticalLayout.VERT_CENTER, 0));
			
				DiscreteProbValueView probView = new DiscreteProbValueView(data, "distn", this, n);
				probView.setPrintWithHalves(false);
			probViewPanel.add(probView);
			
		thePanel.add("East", probViewPanel);
		
		return thePanel;
	}
	
	private XPanel sliderPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			String pi = MText.expandText("#pi#");
			piSlider = new ParameterSlider(new NumValue(minPi), new NumValue(maxPi),
										new NumValue(startPi), nPiSteps, pi, this);
		thePanel.add("Center", piSlider);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		BinomialDistnVariable y1 = (BinomialDistnVariable)data1.getVariable("distn");
		BinomialDistnVariable y2 = (BinomialDistnVariable)data2.getVariable("distn");
		if (target == piSlider) {
			double newP = piSlider.getParameter().toDouble();
			y1.setProb(newP);
			y2.setProb(newP);
			data1.variableChanged("distn");
			data2.variableChanged("distn");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}