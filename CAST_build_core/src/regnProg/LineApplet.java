package regnProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;
import coreGraphics.*;

import regn.*;
import regnView.*;

public class LineApplet extends ScatterApplet {
	static final protected String START_PARAMS_PARAM = "startParams";
	static final protected String MAX_PARAMS_PARAM = "maxParams";
	
	static final private Color kEquationBackground = new Color(0xDDDDFF);
	
	private LineView dataView;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		StringTokenizer st = new StringTokenizer(getParameter(START_PARAMS_PARAM));
		NumValue startIntercept = new NumValue(st.nextToken());
		NumValue startSlope = new NumValue(st.nextToken());
		LinearModel modelVariable = new LinearModel("model", data, "x", startIntercept, startSlope, new NumValue(0.0, 0));
		data.addVariable("model", modelVariable);
		
		return data;
	}
	
	protected LinearEquationView createEquationView(DataSet data) {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_PARAMS_PARAM));
		NumValue maxIntercept = new NumValue(st.nextToken());
		NumValue maxSlope = new NumValue(st.nextToken());
		return new LinearEquationView(data, this, "model", getParameter(Y_VAR_NAME_PARAM),
								getParameter(X_VAR_NAME_PARAM), maxIntercept, maxIntercept, maxSlope, maxSlope,
								Color.red, Color.blue);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel equationPanel = new InsetPanel(15, 3);
			equationPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
				LinearEquationView equationView = createEquationView(data);
				dataView.setLinkedEqn(equationView);
			equationPanel.add(equationView);
		
			equationPanel.lockBackground(kEquationBackground);
		thePanel.add(equationPanel);
		
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		dataView = new LineView(data, this, theHorizAxis, theVertAxis, "model");
		return dataView;
	}
}