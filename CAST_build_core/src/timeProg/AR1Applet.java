package timeProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;
import coreVariables.*;

import time.*;


public class AR1Applet extends CoreAR1Applet {
	
	public void setupApplet() {
		super.setupApplet();
		
		setLayout(new ProportionLayout(0.55, 10, ProportionLayout.VERTICAL));
		
//		add(ProportionLayout.TOP, timeSeriesPanel(data, "y", "fit", Y_AXIS_INFO_PARAM));
//				seriesView[0].addSmoothedVariable("predict");
		
		add(ProportionLayout.TOP, timeSeriesPanel(data, "y", "predict", Y_AXIS_INFO_PARAM));
				seriesView[0].addSmoothedVariable("fit");
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(10, 0));
			
			bottomPanel.add("West", controlPanel(data));
			bottomPanel.add("Center", ar1ScatterPanel(data, "resid", "lagResid", "ar1Model", "smooth"));
		
		add(ProportionLayout.BOTTOM, bottomPanel);
	}
	
	protected TimeView getTimeView(DataSet data, IndexTimeAxis horizAxis, VertAxis vertAxis) {
		return new TimeARView(data, this, horizAxis, vertAxis);
	}
	
	protected String getActualKey() {
		return "resid";
	}
	
	protected String getLagKey() {
		return "lagResid";
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		LinearModel ar1Model = (LinearModel)data.getVariable("ar1Model");
		ar1Model.setIntercept(0.0);
		ar1Model.setSlope(0.0);
		
		NumVariable resid = (NumVariable)data.getVariable("resid");
		resid.name = "r(t) = residual";
		data.addVariable("lagResid", new LaggedVariable("r(t-1) = " + translate("lagged residual"), resid, 1));
		
		AR1Variable ar1Var = new AR1Variable(translate("Smoothed"), data, "resid", "ar1Model");
		ar1Var.setExtraDecimals(2);
		data.addVariable("smooth", ar1Var);
		
		data.addVariable("predict", new SumDiffVariable("Prediction", data, "fit", "smooth",
																																			SumDiffVariable.SUM));
		
//		data.addVariable("predict", new PredictionVariable("Prediction", data, "y", "time",
//																																			"model", "smooth"));
		
		return data;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 15));
		
		thePanel.add(modelChoicePanel(false));		//	horizontal
			
		thePanel.add(arButtonPanel(SHOW_ZERO_BUTTON, translate("Predict residual with") + ":", true));
			
		thePanel.add(getAREqn(data, "r(t)", "r(t-1)"));
		
		return thePanel;
	}
}