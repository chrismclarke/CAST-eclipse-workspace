package testProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import valueList.*;
import imageGroups.*;

import distribution.*;
import test.*;
import randomStat.*;


public class SuccessPValueApplet extends XApplet {
	static final private String TEST_PARAM = "testParams";
	static final private String TEST_TAIL_PARAM = "testTail";
	static final private String SUCCESS_NAME_PARAM = "successName";
	
	static final private String kProbAxisInfo = "-0.05 1.05 0 0.1";
	
	static final private int LOW = 0;
	static final private int HIGH = 1;
	static final private int BOTH = 2;
	
	static final private Color kPValueBackground = new Color(0xDDDDEE);
	
	private ParameterSlider xSlider;
	private DataSet data;
	
	private int tail;
	private int n;
	private double pi0;
	
	public void setupApplet() {
		MeanSDImages.loadMeanSD(this);
		
		String paramString = getParameter(TEST_PARAM);
		StringTokenizer st = new StringTokenizer(paramString);
		n = Integer.parseInt(st.nextToken());
		pi0 = Double.parseDouble(st.nextToken());
		
		int startX = Integer.parseInt(st.nextToken());
		
		paramString = getParameter(TEST_TAIL_PARAM);
		if (paramString.equals("low"))
			tail = LOW;
		else if (paramString.equals("high"))
			tail = HIGH;
		else
			tail = BOTH;
		
		data = getData(n, pi0, startX);
		
		setLayout(new BorderLayout(0, 5));
		
			xSlider = new ParameterSlider(new NumValue(0.0, 0), new NumValue(n, 0),
											new NumValue(startX, 0), "x", this);
		add("North", xSlider);
		add("Center", barchartPanel(data));
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				XPanel pValuePanel = new InsetPanel(10, 5);
				pValuePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					ValueView pValueView;
					if (tail == BOTH)
						pValueView = new Prob2PValueView(data, "binom", this, n);
					else
						pValueView = new ProbPValueView(data, "binom", this, n);
					pValueView.setFont(getBigFont());
				
				pValuePanel.add(pValueView);
				
				pValuePanel.lockBackground(kPValueBackground);
			bottomPanel.add(pValuePanel);
			
		add("South", bottomPanel);
	}
	
	private DataSet getData(int n, double pi, int startX) {
		data = new DataSet();
		BinomialDistnVariable binom = new BinomialDistnVariable("binom");
		binom.setCount(n);
		binom.setProb(pi);
		data.addVariable("binom", binom);
		
		setSelection(startX);
		
		return data;
	}
	
	private void setSelection(int x) {
		BinomialDistnVariable binom = (BinomialDistnVariable)data.getVariable("binom");
		boolean highlightLow = true;
		if (tail == HIGH)
			highlightLow = false;
		else if (tail == BOTH) {
			double lowProb = binom.getCumulativeProb(x + 0.5);
			double highProb = 1.0 - binom.getCumulativeProb(x - 0.5);
			if (highProb < lowProb)
				highlightLow = false;
		}
		if (highlightLow) {
			binom.setMinSelection(Double.NEGATIVE_INFINITY);
			binom.setMaxSelection(x + 0.5);
		}
		else {
			binom.setMinSelection(x - 0.5);
			binom.setMaxSelection(Double.POSITIVE_INFINITY);
		}
	}
	
	private XPanel barchartPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis pAxis = new HorizAxis(this);
		pAxis.readNumLabels(kProbAxisInfo);
//		pAxis.setAxisName("p");
		thePanel.add("Bottom", pAxis);
		
		BinomialCountAxis nAxis = new BinomialCountAxis(data, "binom", this);
		nAxis.setAxisName("x = " + translate("count of") + " " + getParameter(SUCCESS_NAME_PARAM));
		thePanel.add("Bottom", nAxis);
		
		DiscreteProbView barChart = new DiscreteProbView(data, this, "binom",
																null, pAxis, nAxis, DiscreteProbView.NO_DRAG);
		barChart.lockBackground(Color.white);
		thePanel.add("Center", barChart);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
//	BinomialDistnVariable y = (BinomialDistnVariable)data.getVariable("binom");
	if (target == xSlider) {
		int newX = (int)Math.round(xSlider.getParameter().toDouble());
		setSelection(newX);
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