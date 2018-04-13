package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import models.*;
import coreGraphics.*;
import formula.*;
import graphics3D.*;


import multivarProg.*;
import multiRegn.*;
import ssq.*;


public class ModelBandApplet extends RotateApplet {
	static final private String MAX_R_PARAM = "maxR";
	static final private String MAX_SLOPE_SD_PARAM = "maxSlopeSD";
	static final private String MAX_VIF_PARAM = "maxVIF";
	static final private String BX_AXIS_PARAM = "bxAxisInfo";
	static final private String BZ_AXIS_PARAM = "bzAxisInfo";
	
	static final private Color kXBackgroundColor = new Color(0xB0B3E9);
	static final private Color kZBackgroundColor = new Color(0xB7E2AE);
	
	static final private String kCoeffKey[] = {"b0", "b1", "b2"};
	
	private MultiRegnDataSet data;
	private SummaryDataSet summaryData;
	
	private R2Slider corrXZSlider;
	
	protected DataSet readData() {
		data = new AdjustXZCorrDataSet(this);
		
		summaryData = getSummaryData(data);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		SummaryDataSet summaryData = new SummaryDataSet(data, "error");
		for (int i=1 ; i<3 ; i++) {
			String biTheoryKey = kCoeffKey[i] + "Theory";
			String explanName = data.getVariable((i==1) ? "x" : "z").name;
			NormalDistnVariable biTheory = new NormalDistnVariable(translate("LS slope for") + " " + explanName);
			summaryData.addVariable(biTheoryKey, biTheory);
		}
		setCoeffDistns(data, summaryData);
		
		summaryData.addVariable("dummy", new NumVariable("Dummy"));
												//		To be used for JitterPllusNormalView
		
		return summaryData;
	}
	
	private void setCoeffDistns(DataSet data, SummaryDataSet summaryData) {
		MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
		
		double sigma = model.evaluateSD().toDouble();
		double variance = sigma * sigma;
		
		MultipleRegnModel ls = (MultipleRegnModel)data.getVariable("ls");
		double bVar[] = ls.getCoeffVariances("y", true, variance);
		
		
		for (int i=1 ; i<3 ; i++) {
			double biVar = bVar[(i + 1) * (i + 2) / 2 - 1];
			double biSD = Math.sqrt(biVar);
			NumValue biMean = model.getParameter(i);
			String biTheoryKey = kCoeffKey[i] + "Theory";
			
			NormalDistnVariable biTheory = (NormalDistnVariable)summaryData.getVariable(biTheoryKey);
			biTheory.setMean(biMean.toDouble());
			biTheory.setSD(biSD);
			biTheory.setDecimals(biMean.decimals, biMean.decimals + 2);
			
			summaryData.variableChanged(biTheoryKey);
		}
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		CoreVariable xVar = data.getVariable("x");
		D3Axis xAxis = new D3Axis(xVar == null ? "x" : xVar.name, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
		
		CoreVariable yVar = data.getVariable("y");
		D3Axis yAxis = new D3Axis(yVar == null ? "y" : yVar.name, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
		yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
		
		CoreVariable zVar = data.getVariable("z");
		D3Axis zAxis = new D3Axis(zVar == null ? "z" : zVar.name, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
		
		theView = new RotateModelBandView(data, this, xAxis, yAxis, zAxis, "model");
		
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 0));
		
			XPanel rotatePanel = new XPanel();
			rotatePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
			rotatePanel.add(RotateButton.createRotationPanel(theView, this));
				rotateButton = new XButton(translate("Spin"), this);
			rotatePanel.add(rotateButton);
		
		thePanel.add(rotatePanel);
		
			AdjustXZCorrDataSet data2 = (AdjustXZCorrDataSet)data;
			double initialR2 = data2.getInitialXZR2();
			String maxRString = getParameter(MAX_R_PARAM);
			double maxR = Double.parseDouble(maxRString);
			double maxR2 = maxR * maxR;
			corrXZSlider = new R2Slider(this, data, "z", "y", summaryData, translate("Correl(X, Z)"),
																											"0.0", maxRString, initialR2, maxR2);
		thePanel.add(corrXZSlider);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 5, 0, 0);
		thePanel.setLayout(new FixedSizeLayout(100, 200));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.HORIZONTAL,
																													ProportionLayout.TOTAL));
				StringTokenizer st = new StringTokenizer(getParameter(MAX_SLOPE_SD_PARAM));
		
			mainPanel.add(ProportionLayout.LEFT, distnAndVIF(summaryData, "b1Theory", data, "x",
																							st.nextToken(), BX_AXIS_PARAM, kXBackgroundColor));
			mainPanel.add(ProportionLayout.RIGHT, distnAndVIF(summaryData, "b2Theory", data, "z", 
																							st.nextToken(), BZ_AXIS_PARAM, kZBackgroundColor));
		thePanel.add(mainPanel);
		return thePanel;
	}
	
	private XPanel distnAndVIF(SummaryDataSet summaryData, String theoryKey, DataSet data,
															String explanKey, String maxSlopeSD, String axisParam,
															Color backgroundColor) {
		MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
		double sigma = model.evaluateSD().toDouble();
		
		NumVariable explan = (NumVariable)data.getVariable(explanKey);
		ValueEnumeration xe = explan.values();
		double sx = 0.0;
		double sxx = 0.0;
		int n = 0;
		while(xe.hasMoreValues()) {
			double x = xe.nextDouble();
			sx += x;
			sxx += x * x;
			n ++;
		}
		
		double minCoeffSD = sigma / Math.sqrt(sxx - sx * sx / n);
		
		NumValue maxSlopeSDValue = new NumValue(maxSlopeSD);
		NumValue minSlopeSDValue = new NumValue(minCoeffSD, maxSlopeSDValue.decimals);
		
		XPanel thePanel = new InsetPanel(0, 4, 0, 0);
		thePanel.setLayout(new BorderLayout(0, 5));
		
			XPanel sdPanel = new XPanel();
			sdPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				SlopeSDValueView slopeSD = new SlopeSDValueView(summaryData, this, theoryKey, maxSlopeSDValue);
			sdPanel.add(slopeSD);
		thePanel.add("North", sdPanel);
		
		thePanel.add("Center", distnPanel(summaryData, theoryKey, axisParam, backgroundColor));
		
			NumValue maxVIF = new NumValue(getParameter(MAX_VIF_PARAM));
			FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
			VIFPanel theVIFPanel = new VIFPanel(summaryData, theoryKey, minSlopeSDValue,
																											maxSlopeSDValue, maxVIF, stdContext);
		thePanel.add("South", theVIFPanel);
		
		thePanel.lockBackground(backgroundColor);
		return thePanel;
	}
	
	private XPanel distnPanel(SummaryDataSet summaryData, String theoryKey, String axisParam,
															Color backgroundColor) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis axis = new HorizAxis(this);
			axis.readNumLabels(getParameter(axisParam));
			NormalDistnVariable slopeTheory = (NormalDistnVariable)summaryData.getVariable(theoryKey);
			axis.setAxisName(slopeTheory.name);
			axis.lockBackground(backgroundColor);
			
		thePanel.add("Bottom", axis);
		
			JitterPlusNormalView distnView = new JitterPlusNormalView(summaryData, this,
													axis, theoryKey, 0.0, JitterPlusNormalView.STACK_ALGORITHM);
			distnView.setViewBorder(new Insets(5, 0, 0, 0));
			distnView.lockBackground(Color.white);
			
		thePanel.add("Center", distnView);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == corrXZSlider) {
									//	corrXZSlider already processed event to change X and Z
			setCoeffDistns(data, summaryData);
			
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