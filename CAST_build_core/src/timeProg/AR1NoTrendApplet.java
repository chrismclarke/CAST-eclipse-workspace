package timeProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import valueList.*;

import time.*;


public class AR1NoTrendApplet extends CoreAR1Applet {
	
	public void setupApplet() {
		super.setupApplet();
		
		setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL));
		
		add(ProportionLayout.TOP, timeSeriesPanel(data, "y", "smooth", Y_AXIS_INFO_PARAM));
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(10, 0));
			
			bottomPanel.add("West", controlPanel(data));
			bottomPanel.add("Center", ar1ScatterPanel(data, "y", "lagY", "ar1Model", "smooth"));
		
		add(ProportionLayout.BOTTOM, bottomPanel);
	}
	
	protected TimeView getTimeView(DataSet data, IndexTimeAxis horizAxis, VertAxis vertAxis) {
		return new TimeARView(data, this, horizAxis, vertAxis);
	}
	
	protected String getActualKey() {
		return "y";
	}
	
	protected String getLagKey() {
		return "lagY";
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		NumVariable yVar = (NumVariable)data.getVariable("y");
//		String rawName = yVar.name;
		yVar.name = "y(t) = " + yVar.name;
		data.addVariable("lagY", new LaggedVariable("y(t-1) = " + translate("previous value"), yVar, 1));
		
		AR1Variable ar1Var = new AR1Variable(translate("Smoothed"), data, "y", "ar1Model");
		ar1Var.setExtraDecimals(2);
		data.addVariable("smooth", ar1Var);
		
		return data;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 0));
			
		thePanel.add(arButtonPanel(!SHOW_ZERO_BUTTON));
		
		thePanel.add(getAREqn(data, "y(t)", "y(t-1)"));
		
			XPanel forecastPanel = new XPanel();
			forecastPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
			
				OneValueView lag = new OneValueView(data, "lagY", this);
				lag.setLabel(translate("Previous") + " =");
				lag.setFont(getBigFont());
				
			forecastPanel.add(lag);
			
				OneValueView pred = new OneValueView(data, "smooth", this);
				pred.setForeground(TimeARView.kPredictColor);
				pred.setLabel(translate("Forecast") + " =");
				pred.setFont(getBigFont());
			forecastPanel.add(pred);
			
		thePanel.add(forecastPanel);
			
		return thePanel;
	}
}