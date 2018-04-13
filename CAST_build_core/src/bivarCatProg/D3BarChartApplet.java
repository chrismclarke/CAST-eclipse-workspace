package bivarCatProg;

import java.awt.*;

import dataView.*;
import graphics3D.*;

import bivarCat.*;
import multivarProg.RotateApplet;


public class D3BarChartApplet extends RotateApplet {
	static final protected String COUNT_AXIS_INFO_PARAM = "countAxis";
	static final protected String X_LABELS_PARAM = "xLabels";
	static final protected String Z_LABELS_PARAM = "zLabels";
	
	private CatVariable xVariable, zVariable;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		xVariable = new CatVariable(getParameter(X_VAR_NAME_PARAM), Variable.USES_REPEATS);
		xVariable.readLabels(getParameter(X_LABELS_PARAM));
		xVariable.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("x", xVariable);
		
		zVariable = new CatVariable(getParameter(Z_VAR_NAME_PARAM), Variable.USES_REPEATS);
		zVariable.readLabels(getParameter(Z_LABELS_PARAM));
		zVariable.readValues(getParameter(Z_VALUES_PARAM));
		data.addVariable("z", zVariable);
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		D3Axis xAxis = new D3Axis(xVariable.name, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		xAxis.setCatScale(xVariable);
		D3Axis zAxis = new D3Axis(zVariable.name, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		zAxis.setCatScale(zVariable);
		
		D3Axis countAxis = new D3Axis("Freq", D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
		countAxis.setNumScale(getParameter(COUNT_AXIS_INFO_PARAM));
		
		theView = new D3BarChartView(data, this, xAxis, countAxis, zAxis,"x", "z");
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected String getYAxisName() {
		return "Freq";
	}
}