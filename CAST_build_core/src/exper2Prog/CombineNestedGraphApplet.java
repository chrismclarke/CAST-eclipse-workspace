package exper2Prog;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;

import exper2.*;

import glmAnovaProg.*;


public class CombineNestedGraphApplet extends CombineNestedApplet {
	static final private String BLOCK_NAME_PARAM = "blockName";
	static final private String BLOCK_VALUES_PARAM = "blockValues";
	static final private String BLOCK_LABELS_PARAM = "blockLabels";
	static final private String FACTOR_NAME_PARAM = "factorName";
	static final private String FACTOR_VALUES_PARAM = "factorValues";
	static final private String FACTOR_LABELS_PARAM = "factorLabels";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	
	static final private String[] kFactorKeys = {"factor"};
	static final private String[] kBlockKeys = {"block"};
	
//	static final private Color kTableBackgroundColor = new Color(0xDAE4FF);
	static final private Color kComponentColors[] = {new Color(0x009900), Color.red, Color.blue, new Color(0x660033)};
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
			CatVariable blockVar = new CatVariable(getParameter(BLOCK_NAME_PARAM));
			blockVar.readLabels(getParameter(BLOCK_LABELS_PARAM));
			blockVar.readValues(getParameter(BLOCK_VALUES_PARAM));
//			int nBlocks = blockVar.noOfCategories();
		data.addVariable("block", blockVar);
		
			CatVariable factorVar = new CatVariable(getParameter(FACTOR_NAME_PARAM));
			factorVar.readLabels(getParameter(FACTOR_LABELS_PARAM));
			factorVar.readValues(getParameter(FACTOR_VALUES_PARAM));
//			int nLevels = factorVar.noOfCategories();
		data.addVariable("factor", factorVar);
		
			MultipleRegnModel lsFactor = new MultipleRegnModel("LS_factor", data, kFactorKeys);
			lsFactor.setParameterDecimals(9);
			lsFactor.updateLSParams("y");
		data.addVariable("lsFactor", lsFactor);
			
			MultipleRegnModel lsBlock = new MultipleRegnModel("LS_block", data, kBlockKeys);
			lsBlock.setParameterDecimals(9);
			lsBlock.updateLSParams("y");
		data.addVariable("lsBlock", lsBlock);
		
		return data;
	}
		
	protected XPanel dataDisplayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 20));
		
		thePanel.add("South", super.dataDisplayPanel(data));
			
		thePanel.add(scatterplotPanel(data));
		return thePanel;
	}
		
	private XPanel scatterplotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
			
				VertAxis yAxis = new VertAxis(this);
				yAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
			mainPanel.add("Left", yAxis);
			
				NestedFactorAxis blockAxis = new NestedFactorAxis(this);
				CatVariable blockVar = (CatVariable)data.getVariable("block");
				CatVariable factorVar = (CatVariable)data.getVariable("factor");
				blockAxis.setCatLabels(blockVar, factorVar, kComponentColors[1]);
				blockAxis.setForeground(kComponentColors[2]);
			mainPanel.add("Bottom", blockAxis);
				
				NestedFactorView theView = new NestedFactorView(data, this, yAxis, blockAxis, "y", "block",
																											"factor", "lsBlock", "lsFactor", kComponentColors);
				theView.setShadeComponents(true);
				theView.lockBackground(Color.white);
					
			mainPanel.add("Center", theView);
		
		thePanel.add("Center", mainPanel);
		
			XLabel yNameLabel = new XLabel(data.getVariable("y").name, XLabel.LEFT, this);
		thePanel.add("North", yNameLabel);
		
		return thePanel;
	}
}