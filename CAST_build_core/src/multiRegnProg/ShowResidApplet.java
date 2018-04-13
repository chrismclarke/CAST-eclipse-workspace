package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import valueList.*;
import graphics3D.*;

import multiRegn.*;


public class ShowResidApplet extends CoreRegnPlaneApplet {
	static final private String PREDICT_MINMAX_PARAM = "predictMinMax";
	
	static final private Color kXAxisColor = D3Axis.axisColor[D3Axis.X_AXIS][D3Axis.FOREGROUND];
	static final private Color kYAxisColor = D3Axis.axisColor[D3Axis.Y_AXIS][D3Axis.FOREGROUND];
	static final private Color kZAxisColor = D3Axis.axisColor[D3Axis.Z_AXIS][D3Axis.FOREGROUND];
	
	private NumValue minPrediction, maxPrediction;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		StringTokenizer st = new StringTokenizer(getParameter(PREDICT_MINMAX_PARAM));
		minPrediction = new NumValue(st.nextToken());
		maxPrediction = new NumValue(st.nextToken());
		
		((NumVariable)data.getVariable("y")).setMaxDecimals();
		((NumVariable)data.getVariable("x")).setMaxDecimals();
		((NumVariable)data.getVariable("z")).setMaxDecimals();
							//		needed to make OneValueView always show same decimals
		
		return data;
	}
	
	protected Rotate3DView getRotatingView(DataSet data, D3Axis yAxis, D3Axis xAxis, D3Axis zAxis) {
		return new Model3ResidView(data, this, xAxis, yAxis, zAxis, "model", explanKey, "y");
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 0, 0, 5);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 6));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
				OneValueView xView = new OneValueView(data, explanKey[0], this);
				xView.setForeground(kXAxisColor);
			topPanel.add(xView);
			
				OneValueView zView = new OneValueView(data, explanKey[1], this);
				zView.setForeground(kZAxisColor);
			topPanel.add(zView);
			
				OneValueView yView = new OneValueView(data, "y", this);
				yView.setForeground(kYAxisColor);
			topPanel.add(yView);
		
		thePanel.add(topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				PredictionXZView predictionEqn = new PredictionXZView(data, this, getParameter(Y_VAR_NAME_PARAM),
																			explanKey, "model", minPrediction, maxPrediction);
				predictionEqn.setForeground(Color.black);
			bottomPanel.add(predictionEqn);
		
		thePanel.add(bottomPanel);
		return thePanel;
	}
}