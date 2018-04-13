package indicatorProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import axis.*;
import coreVariables.*;

import indicator.*;


public class CatCatInteractionApplet extends NumCatInteractionApplet {
	static final private String X_LABELS_PARAM = "xLabels";
	
	static final private String[] kXKeys = {"x", "z", "xz"};
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM),
																												getParameter(Y_VALUES_PARAM));
		
		CatVariable xVar = new CatVariable(getParameter(X_VAR_NAME_PARAM));
		xVar.readLabels(getParameter(X_LABELS_PARAM));
		xVar.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("x", xVar);
		
		CatVariable zVar = new CatVariable(getParameter(Z_VAR_NAME_PARAM));
		zVar.readLabels(getParameter(Z_LABELS_PARAM));
		zVar.readValues(getParameter(Z_VALUES_PARAM));
		data.addVariable("z", zVar);
		
		xKeys = kXKeys;
		
		data.addVariable(xKeys[2], new CatCatInteractionVariable("Interaction", data, xKeys[0], xKeys[1]));
		
		setupParams(xVar, zVar);
		
		nxPerTerm = new int[3];
		for (int i=0 ; i<3 ; i++)
			nxPerTerm[i] = 1;
		
		int nx = xVar.noOfCategories();
		int nz = zVar.noOfCategories();
		paramDecimals = new int[nx * nz];
		paramDecimals[0] = maxParam[0].decimals;
		for (int i=1 ; i<nx ; i++)
			paramDecimals[i] = maxParam[1].decimals;
		for (int i=1 ; i<nz ; i++)
			paramDecimals[nx + i - 1] = maxParam[2].decimals;
		for (int i=0 ; i<(nx-1)*(nz-1) ; i++)
			paramDecimals[nx + nz - 1 + i] = maxParam[3].decimals;
		
			MultipleRegnModel lsModel = new MultipleRegnModel("ls", data, xKeys);
			lsModel.setLSParams("y", paramDecimals, 9);
		data.addVariable("ls", lsModel);
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0,0));
				
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new AxisLayout());
			
				HorizAxis xAxis = new HorizAxis(this);
				CatVariable xVar = (CatVariable)data.getVariable(xKeys[0]);
				xAxis.setCatLabels(xVar);
				xAxis.setAxisName(xVar.name);
			dataPanel.add("Bottom", xAxis);
			
				VertAxis yAxis = new VertAxis(this);
				yAxis.readNumLabels(getParameter(Y_AXIS_PARAM));
			dataPanel.add("Left", yAxis);
			
				DragCatCatLinesView theView = new DragCatCatLinesView(data, this, xAxis, yAxis,
																											xKeys, "y", null, null, "ls", paramDecimals);
				theView.setCanDragHandles(false);
				theView.lockBackground(Color.white);
				
			dataPanel.add("Center", theView);
		
		thePanel.add("Center", dataPanel);
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
				XLabel yLabel = new XLabel(data.getVariable("y").name, XLabel.LEFT, this);
				yLabel.setFont(yAxis.getFont());
			topPanel.add(yLabel);
		
		thePanel.add("North", topPanel);
		
		return thePanel;
	}
}