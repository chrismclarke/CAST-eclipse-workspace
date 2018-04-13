package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import valueList.*;
import models.*;
import graphics3D.*;
import multivarProg.*;
import multiRegn.*;


public class Predict3DApplet extends RotateApplet {
	static final protected String MODEL_PARAM_PARAM = "modelParams";
	static final private String PREDICT_MINMAX_PARAM = "predictMinMax";
	
	static final private Color kYAxisColor = D3Axis.axisColor[D3Axis.Y_AXIS][D3Axis.BACKGROUND];
	
	@SuppressWarnings("unused")
	private NumValue minPrediction, maxPrediction, minX, maxX, minZ, maxZ;
//	private PredictionXZView predictionEqn;
	
	private String explanName[];
	private String[] explanKey = {"x", "z"};
	private String yVarName;
	
	private void addVariable1(DataSet data, int index) {
		NumVariable var = new NumVariable(explanName[index]);
		var.readValues("0.0");
		data.addVariable(explanKey[index], var);
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		explanName = new String[2];
		explanName[0] = getParameter(X_VAR_NAME_PARAM);
		explanName[1] = getParameter(Z_VAR_NAME_PARAM);
		yVarName = getParameter(Y_VAR_NAME_PARAM);
		
		addVariable1(data, 0);
		addVariable1(data, 1);
		
		data.addVariable("model", new MultipleRegnModel("Model", data, explanKey, getParameter(MODEL_PARAM_PARAM)));
		
		StringTokenizer st = new StringTokenizer(getParameter(PREDICT_MINMAX_PARAM));
		minPrediction = new NumValue(st.nextToken());
		maxPrediction = new NumValue(st.nextToken());
		minX = new NumValue(st.nextToken());
		maxX = new NumValue(st.nextToken());
		minZ = new NumValue(st.nextToken());
		maxZ = new NumValue(st.nextToken());
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 0, 0, 5);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
				OneValueView xView = new OneValueView(data, explanKey[0], this, maxX);
				xView.setForeground(Color.blue);
			topPanel.add(xView);
			
				OneValueView zView = new OneValueView(data, explanKey[1], this, maxZ);
				zView.setForeground(Color.red);
			topPanel.add(zView);
		
		thePanel.add(topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				PredictionXZView predictionEqn = new PredictionXZView(data, this, yVarName, explanKey,
								"model", minPrediction, maxPrediction);
				predictionEqn.setForeground(kYAxisColor);
			bottomPanel.add(predictionEqn);
		
		thePanel.add(bottomPanel);
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		D3Axis xAxis = new D3Axis(explanName[0], D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
		D3Axis yAxis = new D3Axis(yVarName, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
		yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
		D3Axis zAxis = new D3Axis(explanName[1], D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
		
		theView = new PredictDrag3View(data, this, xAxis, yAxis, zAxis, "model", explanKey);
		((ModelDot3View)theView).setDrawPlaneOutline(true);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
}