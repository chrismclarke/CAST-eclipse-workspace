package estimationProg;

import java.awt.*;

import dataView.*;
import utils.*;

import estimation.*;


public class NewtonRaphsonApplet extends XApplet {
//	static final private String N_PARAM = "nValues";
//	static final private String SUM_PARAM = "sumX";
	static final private String ITERATIONS_PARAM = "iterations";
	static final private String INITIAL_GUESS_PARAM = "initialGuess";
	
	static final private int kThetaDecimals = 4;
	static final private int kD1Decimals = 3;
	static final private int kD2Decimals = 2;
	
//	private int n;
//	private double sum;
	private int iterations;
	
	private DataSet data;
	
	private XNumberEditPanel startTheta;
	private XButton showIterationsButton;
	
	public void setupApplet() {
//		n = Integer.parseInt(getParameter(N_PARAM));
//		sum = Double.parseDouble(getParameter(SUM_PARAM));
		iterations = Integer.parseInt(getParameter(ITERATIONS_PARAM));
		
		data = getData();
		
		setLayout(new BorderLayout(0, 10));
		
		add("North", initialValuePanel());
		add("Center", new NewtonRaphsonTableView(data, this, "theta", "d1", "d2"));
		
		updateIterations();
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		NumVariable theta = new NumVariable("theta");
		NumVariable firstDeriv = new NumVariable("d1");
		NumVariable secondDeriv = new NumVariable("d2");
		
		for (int i=0 ; i<=iterations ; i++) {
			theta.addValue(new NumValue(0.0, kThetaDecimals));
			firstDeriv.addValue(new NumValue(0.0, kD1Decimals));
			secondDeriv.addValue(new NumValue(0.0, kD2Decimals));
		}
		data.addVariable("theta", theta);
		data.addVariable("d1", firstDeriv);
		data.addVariable("d2", secondDeriv);
		
		return data;
	}
	
	private XPanel initialValuePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
			startTheta = new XNumberEditPanel(translate("Initial guess") + ", \u03B8\u2080 =", getParameter(INITIAL_GUESS_PARAM), 6, this);
			startTheta.setFont(getBigFont());
			startTheta.setDoubleType(0.0, 1.0);
		thePanel.add(startTheta);
		
			showIterationsButton = new XButton(translate("Show iterations"), this);
		thePanel.add(showIterationsButton);
		
		return thePanel;
	}
	
	private void updateIterations() {
		double theta0 = startTheta.getDoubleValue();
		
		NumVariable theta = (NumVariable)data.getVariable("theta");
		NumVariable d1 = (NumVariable)data.getVariable("d1");
		NumVariable d2 = (NumVariable)data.getVariable("d2");
		
		((NumValue)theta.valueAt(0)).setValue(theta0);
		double thetaI = theta0;
		for (int i=0 ; i<iterations ; i++) {
			double logVal = Math.log(1 - thetaI);
			double d1Value = 95 / thetaI + 30 / ((1 - thetaI) * logVal);
			double d2Value = - 95 / Math.pow(thetaI, 2) + 30 * (1 + logVal) / Math.pow((1 - thetaI) * logVal, 2);
			thetaI -= d1Value / d2Value;
			
			((NumValue)d1.valueAt(i)).setValue(d1Value);
			((NumValue)d2.valueAt(i)).setValue(d2Value);
			((NumValue)theta.valueAt(i + 1)).setValue(thetaI);
		}
		
		data.variableChanged("theta");		//	enough to get iterations table to repaint
	}
	
	private boolean localAction(Object target) {
		if (target == showIterationsButton) {
			updateIterations();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}