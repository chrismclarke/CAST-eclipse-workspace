package multiRegnProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import regn.*;
import multiRegn.*;


public class DragResidSquareApplet extends DragParamsApplet {
	static private final String MAX_RSS_PARAM = "maxRss";
	
	static final private String[] xKey = {"x", "z"};
	
	private XButton lsButton;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		String yValues = getParameter(Y_VALUES_PARAM);
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), yValues);
		String xValues = getParameter(X_VALUES_PARAM);
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), xValues);
		String zValues = getParameter(Z_VALUES_PARAM);
		data.addNumVariable("z", getParameter(Z_VAR_NAME_PARAM), zValues);
		
		data.addVariable("resid", new BasicComponentVariable(translate("Residual"), data, xKey, "y",
																		"model", BasicComponentVariable.RESIDUAL, 9));
		
		return data;
	}
	
	protected Rotate3DView getDataView(DataSet data, D3Axis xAxis, D3Axis yAxis,
																																	D3Axis zAxis, String modelKey) {
		return new DragResidSquareView(data, this, xAxis, yAxis, zAxis, "model", "y", "x", "z", equationView);
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
			leftPanel.add(getEquationView(data));
		
		thePanel.add("Center", leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 7));
				
				NumValue maxRss = new NumValue(getParameter(MAX_RSS_PARAM));
				ResidSsq2View rss = new ResidSsq2View(data, this, "resid", maxRss, "xEquals/residualSsq.png", 14);
			rightPanel.add(rss);
			
				lsButton = new XButton(translate("Least squares"), this);
			rightPanel.add(lsButton);
		
		thePanel.add("East", rightPanel);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == lsButton) {
			MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
			model.updateLSParams("y");
			data.variableChanged("model");	
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