package randomStatProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import coreGraphics.*;

import distribution.*;
import randomStat.*;


public class JS_BinomApproxApplet extends XApplet {
	static final private String BINOM_PARAM = "binom";
	static final private String AXIS_PARAM = "axis";
	static final private String XVALUE_PARAM = "xValue";
	
	static final private int kMeanSDDecimals = 4;
	
	static final private Color kMeanSdBackground = new Color(0xDDDDEE);
	
	static final private Color kDistnColor = new Color(0xCCBBBB);
	static final private Color kHighlightColor = new Color(0x0000FF);
	
	public void setupApplet() {
//		MeanSDImages.loadMeanSD(this);
		FitEstImages.loadFitEst(this);
		
		StringTokenizer st = new StringTokenizer(getParameter(BINOM_PARAM));
		int n = Integer.parseInt(st.nextToken());
		double prob = Double.parseDouble(st.nextToken());
		
		DataSet data = getData(n, prob);
		
		setLayout(new BorderLayout(10, 15));
		add("North", meanSDPanel(data));
		add("Center", displayPanel(data));
		add("East", propnPanel(data, n));
	}
	
	private DataSet getData(int n, double prob) {
		DataSet data = new DataSet();
		
			BinomialDistnVariable binom = new BinomialDistnVariable("binom");
			binom.setCount(n);
			binom.setProb(prob);
		data.addVariable("binom", binom);
		
			NormalDistnVariable norm = new NormalDistnVariable("norm");
			norm.setMean(n * prob);
			norm.setSD(Math.sqrt(n * prob * (1.0 - prob)));
			norm.setDecimals(kMeanSDDecimals);
		data.addVariable("norm", norm);
		
		double x = Double.parseDouble(getParameter(XVALUE_PARAM));
		
		binom.setMinSelection(Double.NEGATIVE_INFINITY);
		binom.setMaxSelection(x);
		
		norm.setMinSelection(Double.NEGATIVE_INFINITY);
		norm.setMaxSelection(x);
		
		return data;
	}
	
	private XPanel meanSDPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel innerPanel = new InsetPanel(10, 6);
			innerPanel.setLayout(new EqualSpacingLayout(EqualSpacingLayout.HORIZONTAL, 20));
		
				SampleMeanSDView meanValue = new SampleMeanSDView(data, this, "norm", SampleMeanSDView.SUCCESS_MEAN,
																																							SampleMeanSDView.MEAN_DISTN);
			innerPanel.add(meanValue);
		
				SampleMeanSDView sdValue = new SampleMeanSDView(data, this, "norm", SampleMeanSDView.SUCCESS_SD,
																																							SampleMeanSDView.MEAN_DISTN);
			innerPanel.add(sdValue);
			innerPanel.lockBackground(kMeanSdBackground);
		
		thePanel.add(innerPanel);
		
		return thePanel;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.VERTICAL));
		
		thePanel.add(ProportionLayout.TOP, barchartPanel(data, DiscreteProbView.NO_DRAG));
		thePanel.add(ProportionLayout.BOTTOM, distnPanel(data));
		return thePanel;
	}
	
	private XPanel propnPanel(DataSet data, int n) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.VERTICAL));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				DiscreteProbValueView leftView = new DiscreteProbValueView(data, "binom", this, n);
				leftView.setFont(getBigFont());
			topPanel.add(leftView);
		
		thePanel.add(ProportionLayout.TOP, topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				DiscreteProbValueView rightView = new DiscreteProbValueView(data, "norm", this, n);
				rightView.setFont(getBigFont());
			bottomPanel.add(rightView);
		
		thePanel.add(ProportionLayout.BOTTOM, bottomPanel);
		return thePanel;
	}
	
	private XPanel barchartPanel(DataSet data, int dragType) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis nAxis = new HorizAxis(this);
			nAxis.readNumLabels(getParameter(AXIS_PARAM));
			nAxis.setAxisName(getParameter(VAR_NAME_PARAM));
		thePanel.add("Bottom", nAxis);
		
			DiscreteProbView barChart = new DiscreteProbView(data, this, "binom", null, null, nAxis, dragType);
			barChart.setDensityColor(kDistnColor);
			barChart.setHighlightColor(kHighlightColor);
			barChart.lockBackground(Color.white);
		thePanel.add("Center", barChart);
		
		return thePanel;
	}
	
	private XPanel distnPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis nAxis = new HorizAxis(this);
			nAxis.readNumLabels(getParameter(AXIS_PARAM));
			nAxis.setAxisName(getParameter(VAR_NAME_PARAM));
		thePanel.add("Bottom", nAxis);
		
			SimpleDistnView density = new SimpleDistnView(data, this, nAxis, "norm");
			density.setDensityColor(kHighlightColor);
			density.setHighlightColor(kDistnColor);
			density.lockBackground(Color.white);
			density.setDensityScaling(0.85);
		thePanel.add("Center", density);
		
		return thePanel;
	}
}