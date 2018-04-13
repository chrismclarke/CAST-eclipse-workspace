package regnProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import valueList.OneValueView;
import models.*;
import coreGraphics.*;

import regnView.*;


public class FitResidApplet extends ScatterApplet {
	static final protected String MODEL_PARAM = "model";
	static final protected String DECIMALS_PARAM = "decimals";
	
//	private FitResidView theView;
	private LinearModel modelVariable;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		modelVariable = new LinearModel("model", data, "x", getParameter(MODEL_PARAM));
		data.addVariable("model", modelVariable);
		
		NumVariable y = (NumVariable)data.getVariable("y");
		int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
		data.addVariable("fit", new FittedValueVariable(translate("Fitted") + " " + y.name, data,
																							"x", "model", decimals));
		
		data.addVariable("resid", new ResidValueVariable(translate("Resid"), data, "x", "y", "model", decimals));
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 0, 0, 6);
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
		
		OneValueView yView = new OneValueView(data, "y", this);
		thePanel.add(yView);
		yView.setFont(theVertAxis.getFont());
		yView.setForeground(Color.blue);
		
		OneValueView fit = new OneValueView(data, "fit", this);
		thePanel.add(fit);
		fit.setFont(theVertAxis.getFont());
		fit.setForeground(FitResidView.darkGreen);
		
		OneValueView resid = new OneValueView(data, "resid", this);
		thePanel.add(resid);
		resid.setFont(theVertAxis.getFont());
		resid.setForeground(Color.red);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 3));
		
		thePanel.add(new OneValueView(data, "x", this));
		thePanel.add(new OneValueView(data, "label", this));
		return thePanel;
	}
	
	protected VertAxis createVertAxis(DataSet data) {
		VertAxis axis = super.createVertAxis(data);
		axis.setForeground(Color.blue);
		return axis;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		return new FitResidView(data, this, theHorizAxis, theVertAxis, "x", "y", "model");
	}
}