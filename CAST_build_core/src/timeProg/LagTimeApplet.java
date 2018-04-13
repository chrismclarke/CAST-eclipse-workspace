package timeProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import scatter.*;
import corr.*;
import time.*;



public class LagTimeApplet extends CoreActualResidApplet {
	
	public void setupApplet() {
		super.setupApplet();
		
		setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL));
		
		add(ProportionLayout.TOP, timeSeriesPanel(data, "y", "fit", Y_AXIS_INFO_PARAM));
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(10, 0));
			
			bottomPanel.add("West", controlPanel(data));
			bottomPanel.add("Center", lagScatterPanel(data, "resid", "lagResid"));
		
		add(ProportionLayout.BOTTOM, bottomPanel);
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		NumVariable resid = (NumVariable)data.getVariable("resid");
		data.addVariable("lagResid", new LaggedVariable(translate("Previous Residual"), resid, 1));
		
		return data;
	}
	
	protected TimeView getTimeView(DataSet data, IndexTimeAxis horizAxis, VertAxis vertAxis) {
		return new LagTimeView(data, this, horizAxis, vertAxis, 1);
	}
	
	private XPanel lagScatterPanel(DataSet data, String yKey, String lagKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		Color yColor = BasicTimeApplet.kSmoothedColor;
		Color xColor = LagTimeView.kLagColor;
		
		VertAxis vertAxis = new VertAxis(this);
		vertAxis.readNumLabels(getParameter(RESID_AXIS_INFO_PARAM));
		vertAxis.setForeground(yColor);
		thePanel.add("Left", vertAxis);
		
		HorizAxis horizAxis = new HorizAxis(this);
		horizAxis.readNumLabels(getParameter(RESID_AXIS_INFO_PARAM));
		thePanel.add("Bottom", horizAxis);
		if (labelAxes)
			horizAxis.setAxisName(data.getVariable(lagKey).name);
		horizAxis.setForeground(xColor);
		thePanel.add("Bottom", horizAxis);
		
		ScatterArrowView theView = new ScatterArrowView(data, this, horizAxis, vertAxis, lagKey, yKey);
		theView.setDrawXZero(true);
		theView.setDrawYZero(true);
		theView.setRetainLastSelection(true);
		theView.setAxisColors(xColor, yColor);
		theView.lockBackground(Color.white);
		
		thePanel.add("Center", theView);
		
		thePanel = addYAxisName(thePanel, data, yKey, vertAxis, yColor);
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(30, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 30));
			
		thePanel.add(modelChoicePanel(true));		//	vertical
		
			CorrelationView rView = new CorrelationView(data, "lagResid", "resid", CorrelationView.NO_FORMULA, this);
			rView.setFont(getBigFont());
		thePanel.add(rView);
			
		return thePanel;
	}
}