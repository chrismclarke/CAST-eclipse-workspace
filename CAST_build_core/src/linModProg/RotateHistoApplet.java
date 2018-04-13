package linModProg;

import java.awt.*;

import dataView.*;
import utils.*;
import graphics3D.*;

import multivarProg.*;
import linMod.*;


public class RotateHistoApplet extends RotateApplet {
	static final private String CLASS_INFO_PARAM = "classInfo";
	static final protected String SORTED_X_PARAM = "sortedX";
	static final private String JITTERING_PARAM = "jittering";
	static final protected String X_CAT_NAMES_PARAM = "xCatNames";
									//	Overrides numbers on axis (to make numerical x look cat)
									//	Numerical x axis should be -0.5 to n - 0.5 to match
									//	Messy hack to get applet to work with multiple groups
	
	static final protected String kDefaultZAxis = "0.0 1.0 2.0 1.0";
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		D3Axis xAxis = new D3Axis(getParameter(X_VAR_NAME_PARAM), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
		String xCatLabels = getParameter(X_CAT_NAMES_PARAM);
		if (xCatLabels != null) {
			CatVariable xTemp = new CatVariable("");
			xTemp.readLabels(xCatLabels);
			xAxis.setCatScale(xTemp);		//	replaces values by strings
		}
		D3Axis yAxis = new D3Axis(getParameter(Y_VAR_NAME_PARAM), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
		D3Axis zAxis = new D3Axis("", D3Axis.Y_AXIS, D3Axis.X_AXIS, this);
		zAxis.setNumScale(kDefaultZAxis);
		
		theView = getView(data, xAxis, yAxis, zAxis);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected Rotate3DView getView(DataSet data, D3Axis xAxis, D3Axis yAxis, D3Axis densityAxis) {
		RotateHistoView theView = new RotateHistoView(data, this, xAxis, yAxis, densityAxis,"x", "y",
																						getParameter(CLASS_INFO_PARAM), getParameter(SORTED_X_PARAM));
		theView.setBigHitRadius();
		String jitterString = getParameter(JITTERING_PARAM);
		if (jitterString != null)
			theView.setJitterFraction(Double.parseDouble(jitterString));
		return theView;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}
	
	protected int rotationPanelOrientation() {
		return RotateButton.VERTICAL;
	}
	
	protected XPanel rotationPanel() {
		XPanel thePanel = RotateButton.create2DRotationPanel(theView, this, rotationPanelOrientation());
		rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		return rotationPanel();
	}
}