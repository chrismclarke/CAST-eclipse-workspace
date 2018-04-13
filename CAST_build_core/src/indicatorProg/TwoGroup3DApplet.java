package indicatorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import multivarProg.*;
import indicator.*;


public class TwoGroup3DApplet extends RotateApplet {
	static final private String PARAM_DECIMALS_PARAM = "paramDecimals";
	static final private String Z_LABELS_PARAM = "zLabels";
	
	static final protected String kExplanKey[] = {"x", "z"};
	
	private DataSet data;
	
	protected void addModel(DataSet data, String[] xKey, String yKey, String modelKey) {
		
			MultipleRegnModel model = new MultipleRegnModel("Model", data, kExplanKey);
			StringTokenizer st = new StringTokenizer(getParameter(PARAM_DECIMALS_PARAM));
			int paramDecimals[] = new int[3];
			for (int i=0 ;i<3 ; i++)
				paramDecimals[i] = Integer.parseInt(st.nextToken());
			model.setLSParams(yKey, paramDecimals, 9);
			
		data.addVariable("model", model);
	}
	
	protected void addDataValues(DataSet data) {
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
		data.addCatVariable("z", getParameter(Z_VAR_NAME_PARAM), getParameter(Z_VALUES_PARAM),
																													getParameter(Z_LABELS_PARAM));
	}
	
	protected DataSet readData() {
		data = new DataSet();
		
		addDataValues(data);
		addModel(data, kExplanKey, "y", "model");
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}

	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			D3Axis xAxis = new D3Axis(data.getVariable("x").name, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
			D3Axis yAxis = new D3Axis(data.getVariable("y").name, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			D3Axis zAxis = new D3Axis(data.getVariable("z").name, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
			
			theView = new Groups3DView(data, this, xAxis, yAxis, zAxis, "model", kExplanKey, "y");
			theView.lockBackground(Color.white);
		
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			
		thePanel.add(new RotateButton(RotateButton.YX_ROTATE, theView, this));
		thePanel.add(new RotateButton(RotateButton.XYZ_ROTATE, theView, this));
		
			rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			XLabel zLabel = new XLabel(data.getVariable("z").name, XLabel.LEFT, this);
			zLabel.setFont(getBigFont());
		thePanel.add(zLabel);
			
			CatKey zKey = new CatKey(data, "z", this, CatKey.VERT);
			zKey.setFont(getBigFont());
		thePanel.add(zKey);
		
		return thePanel;
	}
	
}