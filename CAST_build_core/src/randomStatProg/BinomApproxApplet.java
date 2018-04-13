package randomStatProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import coreGraphics.*;
import formula.*;

import distribution.*;
import randomStat.*;


public class BinomApproxApplet extends XApplet {
	static final private String N_PARAM = "n";
	
	static final private String kProbAxisInfo = "-0.05 1.05 0 0.2";
	static final private int kMeanSDDecimals = 2;
	
	static final private double kStartProb = 0.5;
	
	static final private Color kMeanSdBackground = new Color(0xDDDDEE);
	
	private ParameterSlider nSlider, pSlider;
	private DataSet data;
	private BinomialCountAxis nAxis, nAxis2;
	private DiscreteDensityAxis densityAxis;
	
	private DiscreteProbView barChart;
	private DistnDensityView density;
	
	public void setupApplet() {
//		MeanSDImages.loadMeanSD(this);
		FitEstImages.loadFitEst(this);
		
		String nParamString = getParameter(N_PARAM);
		StringTokenizer st = new StringTokenizer(nParamString);
		int maxN = Integer.parseInt(st.nextToken());
		int startN = Integer.parseInt(st.nextToken());
		
		data = getData(startN);
		
		setLayout(new BorderLayout(10, 15));
		add("North", controlPanel(data, startN, maxN));
		add("Center", displayPanel(data));
		add("East", propnPanel(data, maxN));
	}
	
	private DataSet getData(int startN) {
		data = new DataSet();
		BinomialDistnVariable binom = new BinomialDistnVariable("binom");
		binom.setCount(startN);
		binom.setProb(kStartProb);
		data.addVariable("binom", binom);
		
		NormalDistnVariable norm = new NormalDistnVariable("norm");
		norm.setMean(startN * kStartProb);
		norm.setSD(Math.sqrt(startN * kStartProb * (1.0 * kStartProb)));
		norm.setDecimals(kMeanSDDecimals);
		data.addVariable("norm", norm);
		
		binom.setMinSelection(Double.NEGATIVE_INFINITY);
		binom.setMaxSelection(0.0);
		
		norm.setMinSelection(Double.NEGATIVE_INFINITY);
		norm.setMaxSelection(0.0);
		
		return data;
	}
	
	private XPanel controlPanel(DataSet data, int startN, int maxN) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
		thePanel.add("North", sliderPanel(maxN, startN, ProportionLayout.HORIZONTAL));
		thePanel.add("Center", meanSDPanel(data));
		
		return thePanel;
	}
	
	private XPanel sliderPanel(int maxN, int startN, int sliderOrientation) {
		XPanel thePanel = new XPanel();
		int sliderGap = (sliderOrientation == ProportionLayout.HORIZONTAL) ? 5 : 10;
		thePanel.setLayout(new ProportionLayout(0.5, sliderGap, sliderOrientation, ProportionLayout.TOTAL));
		
			nSlider = new ParameterSlider(new NumValue(1, 0), new NumValue(maxN, 0),
									new NumValue(startN, 0), "n", this);
		thePanel.add("Left", nSlider);
		
			String pi = MText.expandText("#pi#");
			pSlider = new ParameterSlider(new NumValue(0, 2), new NumValue(1, 2),
										new NumValue(0.5, 2), pi, this);
			pSlider.setForeground(Color.blue);
		thePanel.add("Right", pSlider);
		return thePanel;
	}
	
	private XPanel meanSDPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel innerPanel = new InsetPanel(10, 6);
			innerPanel.setLayout(new EqualSpacingLayout(EqualSpacingLayout.HORIZONTAL, 20));
		
				SampleMeanSDView meanValue = new SampleMeanSDView(data, this, "norm", SampleMeanSDView.SUCCESS_MEAN,
																																							SampleMeanSDView.MEAN_DISTN);
				meanValue.setForeground(Color.red);
			innerPanel.add(meanValue);
		
				SampleMeanSDView sdValue = new SampleMeanSDView(data, this, "norm", SampleMeanSDView.SUCCESS_SD,
																																							SampleMeanSDView.MEAN_DISTN);
				sdValue.setForeground(Color.red);
			innerPanel.add(sdValue);
			innerPanel.lockBackground(kMeanSdBackground);
		
		thePanel.add(innerPanel);
		
		return thePanel;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.VERTICAL));
		
		thePanel.add(ProportionLayout.TOP, barchartPanel(data, DiscreteProbView.DRAG_CUMULATIVE));
		thePanel.add(ProportionLayout.BOTTOM, distnPanel(data));
		return thePanel;
	}
	
	private XPanel propnPanel(DataSet data, int maxN) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.VERTICAL));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				DiscreteProbValueView leftView = new DiscreteProbValueView(data, "binom", this, maxN);
				leftView.setFont(getBigFont());
			topPanel.add(leftView);
		
		thePanel.add(ProportionLayout.TOP, topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				DiscreteProbValueView rightView = new DiscreteProbValueView(data, "norm", this, maxN);
				rightView.setFont(getBigFont());
			bottomPanel.add(rightView);
		
		thePanel.add(ProportionLayout.BOTTOM, bottomPanel);
		return thePanel;
	}
	
	private XPanel barchartPanel(DataSet data, int dragType) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis pAxis = new HorizAxis(this);
		pAxis.readNumLabels(kProbAxisInfo);
//		pAxis.setAxisName("p");
		thePanel.add("Bottom", pAxis);
		
		nAxis = new BinomialCountAxis(data, "binom", this);
		nAxis.setAxisName("x = #" + translate("success"));
		thePanel.add("Bottom", nAxis);
		
		barChart = new DiscreteProbView(data, this, "binom", "norm", pAxis, nAxis, dragType);
		barChart.lockBackground(Color.white);
		thePanel.add("Center", barChart);
		
		return thePanel;
	}
	
	private XPanel distnPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis pAxis = new HorizAxis(this);
		pAxis.readNumLabels(kProbAxisInfo);
//		pAxis.setAxisName("p");
		thePanel.add("Bottom", pAxis);
		
		nAxis2 = new BinomialCountAxis(data, "binom", this);
		nAxis2.setAxisName("x = #" + translate("success"));
		thePanel.add("Bottom", nAxis2);
		
		densityAxis = new DiscreteDensityAxis(barChart, this);
		thePanel.add("Left", densityAxis);
		
		density = new DistnDensityView(data, this, nAxis, densityAxis, "norm");
		density.lockBackground(Color.white);
		thePanel.add("Center", density);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		BinomialDistnVariable y = (BinomialDistnVariable)data.getVariable("binom");
		NormalDistnVariable yN = (NormalDistnVariable)data.getVariable("norm");
		if (target == pSlider) {
			double newP = pSlider.getParameter().toDouble();
			y.setProb(newP);
			data.variableChanged("binom");
			
			int n = y.getCount();
			double maxSel = yN.getMaxSelection();
			yN.setMean(n * newP);
			yN.setSD(Math.sqrt(n * newP * (1.0 - newP)));
			yN.setMinSelection(Double.NEGATIVE_INFINITY);
			yN.setMaxSelection(maxSel);
			
			densityAxis.setMaxDensity();
			data.variableChanged("norm");
			
			return true;
		}
		else if (target == nSlider) {
			int newN = (int)Math.round(nSlider.getParameter().toDouble());
			y.setCount(newN);
			nAxis.setCountLabels();
			data.variableChanged("binom");
			
			double p = y.getProb();
			double maxSel = yN.getMaxSelection();
			yN.setMean(newN * p);
			yN.setSD(Math.sqrt(newN * p * (1.0 - p)));
			yN.setMinSelection(Double.NEGATIVE_INFINITY);
			yN.setMaxSelection(maxSel);
			
			densityAxis.setMaxDensity();
			nAxis2.setCountLabels();
			data.variableChanged("norm");
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}