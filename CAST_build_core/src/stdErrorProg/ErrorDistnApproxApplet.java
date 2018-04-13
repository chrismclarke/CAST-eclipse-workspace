package stdErrorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import formula.*;
import imageUtils.*;

import randomStat.*;
import stdError.*;


public class ErrorDistnApproxApplet extends XApplet {
	static final private String N_PARAM = "n";
	static final private String ERROR_INFO_PARAM = "errorAxis";
	static final private String DECIMALS_PARAM = "decimals";
	
	static final private int kMeanSDDecimals = 2;
	
	static final private double kStartProb = 0.5;
	
	static final private Color kDarkRed = new Color(0x990000);
	static final private Color kStatisticBackground = new Color(0xDDDDEE);
	
	private ParameterSlider nSlider, pSlider;
	private DataSet data;
	
	public void setupApplet() {
//		MeanSDImages.loadMeanSD(this);
		FitEstImages.loadFitEst(this);
		
		StringTokenizer st = new StringTokenizer(getParameter(N_PARAM));
		int maxN = Integer.parseInt(st.nextToken());
		int startN = Integer.parseInt(st.nextToken());
		
		data = getData(startN);
		
		setLayout(new BorderLayout(10, 0));
		add("Center", distnPanel(data));
			
			XPanel controlPanel = new XPanel();
			controlPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 0));
			
			controlPanel.add(sliderPanel(maxN, startN, ProportionLayout.VERTICAL));
			controlPanel.add(meanSDPanel(data));
		
		add("East", controlPanel);
	}
	
	private DataSet getData(int startN) {
		data = new DataSet();
		BinomialDistnVariable binom = new BinomialDistnVariable("binom");
		binom.setCount(startN);
		binom.setProb(kStartProb);
		data.addVariable("binom", binom);
		
		NormalDistnVariable norm = new NormalDistnVariable("norm");
		norm.setMean(0.0);
		norm.setSD(Math.sqrt(kStartProb * (1.0 * kStartProb) / startN));
		norm.setDecimals(kMeanSDDecimals);
		data.addVariable("norm", norm);
		
		return data;
	}
	
	private XPanel sliderPanel(int maxN, int startN, int sliderOrientation) {
		XPanel thePanel = new XPanel();
		int sliderGap = 20;
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
		XPanel thePanel = new InsetPanel(10, 5);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 5));
		
		thePanel.add(new ImageCanvas("ci/normalErrorApproxMean.png", this));
		
			int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
			PErrorSDValueView sdValue = new PErrorSDValueView(data, this, "norm", null, decimals);
			sdValue.setForeground(kDarkRed);
			sdValue.setFont(getBigFont());
		thePanel.add("South", sdValue);
		
		thePanel.lockBackground(kStatisticBackground);
		return thePanel;
	}
	
	private XPanel distnPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 30);
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis errorAxis = new HorizAxis(this);
		errorAxis.readNumLabels(getParameter(ERROR_INFO_PARAM));
		errorAxis.setAxisName(translate("Error"));
		thePanel.add("Bottom", errorAxis);
		
		BinomAndNormalView density = new BinomAndNormalView(data, this, "binom", "norm", errorAxis);
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
			
			int n = y.getCount();
			yN.setSD(Math.sqrt(newP * (1.0 - newP) / n));
			
			data.variableChanged("norm");
			
			return true;
		}
		else if (target == nSlider) {
			int newN = (int)Math.round(nSlider.getParameter().toDouble());
			y.setCount(newN);
			
			double p = y.getProb();
			yN.setSD(Math.sqrt(p * (1.0 - p) / newN));
			
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