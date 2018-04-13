package timeProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreVariables.*;
import models.*;


public class TimeResidualApplet extends LinearPredictApplet {
	static final private String RESID_LABEL_PARAM = "residAxisLabels";
	static final private String ALLOW_QUADRATIC_PARAM = "allowQuadratic";
	
	static final private int kMaxSlider = 200;
	
	private XNoValueSlider propnSlider;
	
	private double yMean;
	private String yNameText[];
	
	private double getYMean(DataSet data) {
		NumVariable yVar = (NumVariable)data.getVariable("y");
		
		ValueEnumeration e = yVar.values();
		double sum = 0.0;
		int n = 0;
		while (e.hasMoreValues()) {
			double nextVal = e.nextDouble();
			if (!Double.isNaN(nextVal)) {
				sum += nextVal;
				n ++;
			}
		}
		return sum / n;
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		yMean = getYMean(data);
		
		ResidValueVariable linRes = new ResidValueVariable("Linear residual", data,
																									"time", "y", "linearLS", fitDecs);
		data.addVariable("linearResid", linRes);
		data.addVariable("linearResidPlus", new ScaledVariable("Lin res plus mean", linRes,
																							"linearResid", yMean, 1.0, fitDecs));
		data.addVariable("yOrLinRes", new WeightedMeanVariable("Actual or lin res",
																											data, "linearResidPlus", "y", 0.0));
		FittedValueVariable linFit = (FittedValueVariable)data.getVariable("linearFit");
		data.addVariable("linFitOrMean", new ScaledVariable("Lin fit or mean", linFit,
																										"linearFit", 0.0, 1.0, fitDecs));
		
		ResidValueVariable quadRes = new ResidValueVariable("Quadratic residual", data,
																									"time", "y", "quadraticLS", fitDecs);
		data.addVariable("quadraticResid", quadRes);
		data.addVariable("quadraticResidPlus", new ScaledVariable("Quad res plus mean", quadRes,
																								"quadraticResid", yMean, 1.0, fitDecs));
		data.addVariable("yOrQuadRes", new WeightedMeanVariable("Actual or quad res",
																											data, "quadraticResidPlus", "y", 0.0));
		FittedValueVariable quadFit = (FittedValueVariable)data.getVariable("quadraticFit");
		data.addVariable("quadFitOrMean", new ScaledVariable("Quad fit or mean", quadFit,
																											"quadraticFit", 0.0, 1.0, fitDecs));
		yNameText = new String[3];
		yNameText[0] = getParameter(VAR_NAME_PARAM);
		yNameText[1] = "";
		yNameText[2] = translate("Residual");
		
		return data;
	}
	
	protected VertAxis vertAxis(DataSet data) {
		MultiVertAxis localAxis = new MultiVertAxis(this, 3);
		
		String yAxisString = getParameter(AXIS_INFO_PARAM);
		localAxis.readNumLabels(yAxisString);
		localAxis.readExtraNumLabels("0.0 1.0 2.0 1.0");		//		no labels
		
		StringTokenizer st = new StringTokenizer(getParameter(RESID_LABEL_PARAM), ",");
		int minMaxDecimals = Integer.parseInt(st.nextToken());
		double lowResid = localAxis.minOnAxis - yMean;
		String lowResidString = (new NumValue(lowResid, minMaxDecimals)).toString();
		double highResid = localAxis.maxOnAxis - yMean;
		String highResidString = (new NumValue(highResid, minMaxDecimals)).toString();
		
		String residLabels = st.nextToken();
		String residAxisString = lowResidString + " " + highResidString + " " + residLabels;
//		System.out.println("+" + residAxisString + "+");
		localAxis.readExtraNumLabels(residAxisString);
		return localAxis;
	}
	
	protected XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		return thePanel;
	}
	
	protected XPanel lowerControlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		
			propnSlider = new XNoValueSlider(translate("Data"), translate("Residual"), null, 0, kMaxSlider, 0, this);
			
		String quadraticParam = getParameter(ALLOW_QUADRATIC_PARAM);
		if (quadraticParam == null || quadraticParam.equals("true")) {
			thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.HORIZONTAL,
																															ProportionLayout.TOTAL));
			
			thePanel.add(ProportionLayout.LEFT, super.controlPanel(data));
			thePanel.add(ProportionLayout.RIGHT, propnSlider);
		}
		else {
			thePanel.setLayout(new ProportionLayout(0.75, 0, ProportionLayout.HORIZONTAL,
																															ProportionLayout.TOTAL));
				XPanel leftPanel = new XPanel();
				leftPanel.setLayout(new ProportionLayout(0.3333, 0, ProportionLayout.HORIZONTAL,
																															ProportionLayout.TOTAL));
				leftPanel.add(ProportionLayout.LEFT, new XPanel());
				leftPanel.add(ProportionLayout.RIGHT, propnSlider);
			
			thePanel.add(ProportionLayout.LEFT, leftPanel);
			thePanel.add(ProportionLayout.RIGHT, new XPanel());
		}
			
		return thePanel;
	}
	
	protected String getCrossKey() {
		return "yOrLinRes";
	}
	
	protected String[] getLineKeys() {
		String keys[] = {"yOrLinRes", "linFitOrMean"};
		return keys;
	}
	
	protected void changeModelType(int newModelType) {
		String newActualKey = (newModelType == 0) ? "yOrLinRes" : "yOrQuadRes";
		String newFitKey = (newModelType == 0) ? "linFitOrMean" : "quadFitOrMean";
		getView().setActiveNumVariable(newActualKey);
		getView().setSmoothedVariable(newActualKey);
		getView().addSmoothedVariable(newFitKey);
		getView().repaint();
	}
	
	static final private int Y_DISPLAY = 0;
	static final private int NO_DISPLAY = 1;
	static final private int RESID_DISPLAY = 2;
	private int axisDisplay = Y_DISPLAY;

	
	private boolean localAction(Object target) {
		if (target == propnSlider) {
			int newSliderValue = propnSlider.getValue();
			double newP = newSliderValue / (double)kMaxSlider;
			
			WeightedMeanVariable linYRes = (WeightedMeanVariable)getData().getVariable("yOrLinRes");
			linYRes.setWeight(newP);
			WeightedMeanVariable quadYRes = (WeightedMeanVariable)getData().getVariable("yOrQuadRes");
			quadYRes.setWeight(newP);
			
			double c0 = yMean * newP;
			double c1 = (1.0 - newP);
			
			ScaledVariable linFitMean = (ScaledVariable)getData().getVariable("linFitOrMean");
			linFitMean.setScale(c0, c1, fitDecs);
			ScaledVariable quadFitMean = (ScaledVariable)getData().getVariable("quadFitOrMean");
			quadFitMean.setScale(c0, c1, fitDecs);
			
			getView().repaint();
			
			int newAxisDisplay = (newSliderValue == 0) ? Y_DISPLAY
														: (newSliderValue == propnSlider.getMaxValue()) ? RESID_DISPLAY
														: NO_DISPLAY;
			if (axisDisplay != newAxisDisplay) {
				axisDisplay = newAxisDisplay;
				((MultiVertAxis)theVertAxis).setAlternateLabels(axisDisplay);
				yVariateName.setText(yNameText[axisDisplay]);
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}