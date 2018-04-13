package responseSurfaceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;


public class SurfaceChoiceApplet extends QuadSurfaceApplet {
	static final private String NO_OF_MODELS_PARAM = "noOfModels";
	static final private String MODEL_POINTS_PARAM = "modelPoints";
	
	private double[][] modelPoints;
	
	private XChoice surfaceChoice;
	private int currentSurfaceIndex = 0;
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = super.topPanel(data);
		
		boolean[] showParameter = {true, true, true, true, true, true};
		theEqn.setDrawParameters(showParameter);
		
		return thePanel;
	}

	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			D3Axis xAxis = new D3Axis(xName, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
			D3Axis yAxis = new D3Axis(yName, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			D3Axis zAxis = new D3Axis(zName, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
			
			double startY = (yAxis.getMaxOnAxis() + yAxis.getMinOnAxis()) / 2.0;
			DragResponseSurfaceView localView = new DragResponseSurfaceView(data, this, xAxis, yAxis, zAxis, "model",
																											explanKey, "y");
			localView.resetModel(startY);
			localView.lockBackground(Color.white);
			localView.setColourMap(colourMap);
			for (int i=3 ; i<6 ; i++)
				localView.setAllowTerm(i, true);
			theView = localView;
		
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		
			XLabel modelLabel = new XLabel(translate("Select model") + ":", XLabel.LEFT, this);
			modelLabel.setFont(getStandardBoldFont());
		thePanel.add(modelLabel);
		
			surfaceChoice = new XChoice(this);
			surfaceChoice.addItem(translate("Custom"));
			int nModels = Integer.parseInt(getParameter(NO_OF_MODELS_PARAM));
			modelPoints = new double[nModels][6];
			for (int i=0 ; i<nModels ; i++) {
				String modelString = getParameter(MODEL_POINTS_PARAM + i);
				int hashIndex = modelString.indexOf('#');
				String modelName = modelString.substring(0, hashIndex);
				surfaceChoice.addItem(modelName);
				StringTokenizer st = new StringTokenizer(modelString.substring(hashIndex + 1));
				for (int j=0 ; j<6 ; j++)
					modelPoints[i][j] = Double.parseDouble(st.nextToken());
			}
		thePanel.add(surfaceChoice);
		selectModel(1);
			
		return thePanel;
	}
	
	public void notifyDataChange(DataView theView) {
													//	called by theView when model is changed by dragging
		surfaceChoice.select(0);
	}
	
	private void selectModel(int choiceIndex) {
		surfaceChoice.select(choiceIndex);
		
		NumVariable yVar = (NumVariable)data.getVariable("y");
		for (int i=0 ; i<6 ; i++)
			((NumValue)yVar.valueAt(i)).setValue(modelPoints[choiceIndex - 1][i]);
		
		ResponseSurfaceModel model = (ResponseSurfaceModel)data.getVariable("model");
		model.updateLSParams("y");
		
		data.variableChanged("model");
	}

	
	private boolean localAction(Object target) {
		if (target == surfaceChoice) {
			int newChoice = surfaceChoice.getSelectedIndex();
			if (newChoice != currentSurfaceIndex) {
				currentSurfaceIndex = newChoice;
				
				if (newChoice > 0)
					selectModel(newChoice);
			}
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