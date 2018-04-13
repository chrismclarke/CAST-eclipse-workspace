package inferenceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import valueList.*;
import utils.*;
import distn.*;
import formula.*;
import imageUtils.*;


class NSlider extends XSlider {
	public NSlider(int minN, int maxN, int startN, XApplet applet) {
		super(String.valueOf(minN), String.valueOf(maxN), applet.translate("Sample size") + ", n = ", minN, maxN, startN, applet);
		setFont(applet.getStandardBoldFont());
	}
	
	protected Value translateValue(int val) {
		return new NumValue(val, 0);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return translateValue(getMaxValue()).stringWidth(g);
	}
}


public class FindNForMeanApplet extends XApplet {
	static final private String MAX_WIDTH_PARAM = "maxWidth";
	static final private String N_LIMITS_PARAM = "nLimits";
	static final private String GUESS_LIMITS_PARAM = "guessLimits";
	
	static final private Color kNColor = Color.blue;
	static final private Color kParamGuessColor = new Color(0x009900);
	
	private DataSet data;
	private NSlider nSlider;
	private ParameterSlider guessSlider;
	
	protected NumValue maxWidth;
	
	public void setupApplet() {
		setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 0));
			
			StringTokenizer st = new StringTokenizer(getParameter(N_LIMITS_PARAM));
			int minN = Integer.parseInt(st.nextToken());
			int maxN = Integer.parseInt(st.nextToken());
			int startN = Integer.parseInt(st.nextToken());
			nSlider = new NSlider(minN, maxN, startN, this);
			nSlider.setForeground(kNColor);
		add(nSlider);
		
			st = new StringTokenizer(getParameter(GUESS_LIMITS_PARAM));
			String lowGuess = st.nextToken();
			String highGuess = st.nextToken();
			double startGuess = Double.parseDouble(st.nextToken());
			int noOfSteps = Integer.parseInt(st.nextToken());
			
			guessSlider = getGuessSlider(lowGuess, highGuess, startGuess, noOfSteps);
//			guessSlider.setSliderColor(XSlider.GREEN);
			guessSlider.setForeground(kParamGuessColor);
			guessSlider.setFont(getStandardBoldFont());
		add(guessSlider);
		
		data = getData();
		
			XPanel sePanel = new XPanel();
			sePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			OneValueView seView = getSeValueView(data);
			seView.setFont(getBigFont());
			sePanel.add(seView);
		add(sePanel);
		
			XPanel plusMinusPanel = new XPanel();
			plusMinusPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			OneValueView pmView = getPlusMinusValueView(data);
			pmView.setFont(getBigFont());
			plusMinusPanel.add(pmView);
		add(plusMinusPanel);
		
			XPanel widthPanel = new XPanel();
			widthPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			OneValueView widthView = new OneValueImageView(data, "ciWidth", this, "ci/ciWidth.png", 11, maxWidth);
			widthView.setFont(getBigFont());
			widthPanel.add(widthView);
		add(widthPanel);
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		maxWidth = new NumValue(getParameter(MAX_WIDTH_PARAM));
		int decimals = maxWidth.decimals;
		
			NumVariable seVar = new NumVariable("se");
			seVar.readValues("1.0");
			seVar.setDecimals(decimals);
		data.addVariable("se", seVar);
		
			NumVariable plusMinusVar = new NumVariable("plusMinus");
			plusMinusVar.readValues("1.0");
			plusMinusVar.setDecimals(decimals);
		data.addVariable("plusMinus", plusMinusVar);
		
			NumVariable widthVar = new NumVariable("ciWidth");
			widthVar.readValues("1.0");
			widthVar.setDecimals(decimals);
		data.addVariable("ciWidth", widthVar);
		
		setValuesFromSliders(seVar, plusMinusVar, widthVar, nSlider.getValue(),
																											guessSlider.getParameter().toDouble());
		
		data.setSelection(0);
		return data;
	}
	
	protected void setValuesFromSliders(NumVariable seVar, NumVariable plusMinusVar,
																			NumVariable widthVar, int n, double guessParam) {
		double se = guessParam / Math.sqrt(n);
		double plusMinus = TTable.quantile(0.975, n - 1) * se;
		double width = 2.0 * plusMinus;
		
		NumValue seValue = (NumValue)seVar.valueAt(0);
		seValue.setValue(se);
		
		NumValue plusMinusValue = (NumValue)plusMinusVar.valueAt(0);
		plusMinusValue.setValue(plusMinus);
		
		NumValue widthValue = (NumValue)widthVar.valueAt(0);
		widthValue.setValue(width);
	}
	
	protected ParameterSlider getGuessSlider(String lowGuess, String highGuess,
																										double startGuess, int noOfSteps) {
		StringTokenizer st = new StringTokenizer(translate("Guess at value of * (and s)"), "*");
		String sigmaGuess = st.nextToken() + MText.expandText("#sigma#") + st.nextToken();
		return new ParameterSlider(new NumValue(lowGuess), new NumValue(highGuess),
									new NumValue(startGuess), noOfSteps, sigmaGuess, this);
	}
	
	protected OneValueImageView getSeValueView(DataSet data) {
		return new OneValueImageView(data, "se", this, "ci/seMeanFormula.png", 19, maxWidth);
	}
	
	protected OneValueImageView getPlusMinusValueView(DataSet data) {
		return new OneValueImageView(data, "plusMinus", this, "ci/ciMean95.png", 19, maxWidth);
	}
	
	private boolean localAction(Object target) {
		if (target == nSlider || target == guessSlider) {
			NumVariable seVar = (NumVariable)data.getVariable("se");
			NumVariable plusMinusVar = (NumVariable)data.getVariable("plusMinus");
			NumVariable widthVar = (NumVariable)data.getVariable("ciWidth");
			
			setValuesFromSliders(seVar, plusMinusVar, widthVar, nSlider.getValue(),
																										guessSlider.getParameter().toDouble());
			data.valueChanged(0);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}