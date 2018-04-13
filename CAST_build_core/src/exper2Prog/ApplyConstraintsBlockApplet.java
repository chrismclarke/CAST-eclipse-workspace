package exper2Prog;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;

import exper2.*;


public class ApplyConstraintsBlockApplet extends ApplyConstraintsApplet {
	static final protected String BLOCK_VAR_NAME_PARAM = "blockVarName";
	static final protected String BLOCK_VALUES_PARAM = "blockValues";
	static final protected String BLOCK_LABELS_PARAM = "blockLabels";
	
	protected String[] getXKeys(DataSet data) {
			CatVariable blockVar = new CatVariable(getParameter(BLOCK_VAR_NAME_PARAM));
			blockVar.readLabels(getParameter(BLOCK_LABELS_PARAM));
			blockVar.readValues(getParameter(BLOCK_VALUES_PARAM));
		data.addVariable("block", blockVar);
		
		String[] xKey = {"xCat", "block"};
		return xKey;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			XPanel scatterPanel = new XPanel();
			scatterPanel.setLayout(new AxisLayout());
				
					NumVariable yVar = (NumVariable)data.getVariable("y");
					CatVariable xVar = (CatVariable)data.getVariable("xCat");
					
					HorizAxis xAxis = new HorizAxis(this);
					xAxis.setCatLabels(xVar);
					xAxis.setAxisName(xVar.name);
				
				scatterPanel.add("Bottom", xAxis);
				
					VertAxis yAxis = new VertAxis(this);
					String labelInfo = getParameter(Y_AXIS_INFO_PARAM);
					yAxis.readNumLabels(labelInfo);
				
				scatterPanel.add("Left", yAxis);
					
					BlockFactorMeansView theView = new BlockFactorMeansView(data, this, xAxis, yAxis,
																																				"xCat", "block", "y", "ls");
					theView.setCrossSize(DataView.LARGE_CROSS);
					theView.lockBackground(Color.white);
					theView.setShowResiduals(true);
				
				scatterPanel.add("Center", theView);
				
		thePanel.add("Center", scatterPanel);
			
			XLabel yVariateName = new XLabel(yVar.name, XLabel.LEFT, this);
			yVariateName.setFont(yAxis.getFont());
			
		thePanel.add("North", yVariateName);
		
		return thePanel;
	}
}