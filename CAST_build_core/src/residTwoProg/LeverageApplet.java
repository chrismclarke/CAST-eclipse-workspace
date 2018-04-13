package residTwoProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;
import distn.*;

import multiRegn.*;
import residTwo.*;


public class LeverageApplet extends LeverageGraphApplet {
	static final private String B0_AXIS_PARAM = "b0AxisInfo";
	static final private String B1_AXIS_PARAM = "bxAxisInfo";
	static final private String B2_AXIS_PARAM = "bzAxisInfo";
	
	static final private String kCoeffKey[] = {"b0", "b1", "b2"};
	
	
	protected SummaryDataSet getSummaryData(MultiRegnDataSet data) {
		SummaryDataSet summaryData = new SummaryDataSet(data, "error");
		for (int i=0 ; i<3 ; i++) {
			String biTheoryKey = kCoeffKey[i] + "Theory";
			String explanName = (i==0) ? translate("LS intercept") : 
														(translate("LS slope for") + " " + data.getVariable((i==1) ? "x" : "z").name);
			NormalDistnVariable biTheory = new NormalDistnVariable(explanName);
			summaryData.addVariable(biTheoryKey, biTheory);
		}
		setCoeffDistns(data, summaryData);
		
		for (int i=0 ; i<3 ; i++) {
			String biTheoryKey = kCoeffKey[i] + "Theory";
			NormalDistnVariable biTheory = (NormalDistnVariable)summaryData.getVariable(biTheoryKey);
			summaryData.addVariable(biTheoryKey + "Full", new NormalDistnVariable(biTheory));
		}
		
		summaryData.addVariable("dummy", new NumVariable("Dummy"));
												//		To be used for JitterPlusNormalView
		
		return summaryData;
	}
	
	public void setCoeffDistns(DataSet data, SummaryDataSet summaryData) {
		MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
		
		double sigma = model.evaluateSD().toDouble();
		double variance = sigma * sigma;
		
		MultipleRegnModel ls = (MultipleRegnModel)data.getVariable("ls");
		double bVar[] = ls.getCoeffVariances("y", true, variance);
		
		
		for (int i=0 ; i<3 ; i++) {
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
	
	protected XPanel rightPanel(MultiRegnDataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.3333, 0, ProportionLayout.VERTICAL,
																													ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.TOP, oneDistnPanel(summaryData, "b0Theory", B0_AXIS_PARAM));
		
			XPanel slopePanel = new XPanel();
			slopePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL,
																													ProportionLayout.TOTAL));
		
			slopePanel.add(ProportionLayout.TOP, oneDistnPanel(summaryData, "b1Theory",
																																						B1_AXIS_PARAM));
			slopePanel.add(ProportionLayout.BOTTOM, oneDistnPanel(summaryData, "b2Theory",
																																						B2_AXIS_PARAM));
		
		thePanel.add(ProportionLayout.BOTTOM, slopePanel);
		return thePanel;
	}
	
	private XPanel oneDistnPanel(SummaryDataSet summaryData, String theoryKey,
																																				String axisParam) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis axis = new HorizAxis(this);
			axis.readNumLabels(getParameter(axisParam));
			NormalDistnVariable paramTheory = (NormalDistnVariable)summaryData.getVariable(theoryKey);
			axis.setAxisName(paramTheory.name);
			
		thePanel.add("Bottom", axis);
		
			TwoNormalView distnView = new TwoNormalView(summaryData, this, axis, theoryKey, theoryKey + "Full");
			distnView.lockBackground(Color.white);
			
		thePanel.add("Center", distnView);
		
		return thePanel;
	}
}

