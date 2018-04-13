package exper2Prog;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import coreVariables.*;

import exerciseSD.*;


public class GroupMeansApplet extends XApplet {
	static final private String Y_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String X_NAME_PARAM = "xVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String X_LABELS_PARAM = "xLabels";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	static final private String MEAN_DECIMALS_PARAM = "meanDecimals";
	
	private int noOfGroups;
	
	public void setupApplet() {
		DataSet data = getData();

		setLayout(new BorderLayout(0, 0));
			
		add("Center", dataPanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
			NumVariable yVar = new NumVariable(getParameter(Y_NAME_PARAM));
			yVar.readValues(getParameter(Y_VALUES_PARAM));
		data.addVariable("y", yVar);
			
			CatVariable xVar = new CatVariable(getParameter(X_NAME_PARAM));
			xVar.readLabels(getParameter(X_LABELS_PARAM));
			xVar.readValues(getParameter(X_VALUES_PARAM));
				
		data.addVariable("x", xVar);
		
		noOfGroups = xVar.noOfCategories();
		for (int i=0 ; i<noOfGroups ; i++) {
			FilterNumVariable conditVar = new FilterNumVariable("", data, "y", "x");
			conditVar.setFilterIndex(i);
			data.addVariable("y" + i, conditVar);
		}
		return data;
	}
	
	protected XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
			
				HorizAxis yAxis = new HorizAxis(this);
				yAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
				yAxis.setAxisName(getParameter(Y_NAME_PARAM));
			mainPanel.add("Bottom", yAxis);
			
				VertAxis xAxis = new VertAxis(this);
				CatVariable xVar = (CatVariable)data.getVariable("x");
				xAxis.setCatLabels(xVar);
			mainPanel.add("Left", xAxis);
				
				StackMeanSdView groupViews[] = new StackMeanSdView[noOfGroups];
				int meanDecimals = Integer.parseInt(getParameter(MEAN_DECIMALS_PARAM));
				for (int i=0 ; i<noOfGroups ; i++) {
					groupViews[noOfGroups - i - 1] = new StackMeanSdView(data, this, yAxis, "y" + i, meanDecimals);
					groupViews[noOfGroups - i - 1].setDrawSd(false);
					groupViews[noOfGroups - i - 1].setCrossSize(DataView.LARGE_CROSS);
				}
				
				MultipleDataView dataView = new MultipleDataView(data, this, groupViews);
				dataView.lockBackground(Color.white);
			mainPanel.add("Center", dataView);
		
		thePanel.add("Center", mainPanel);
		
			XLabel groupLabel = new XLabel(xVar.name, XLabel.LEFT, this);
			groupLabel.setFont(getStandardBoldFont());
		thePanel.add("North", groupLabel);
		
		return thePanel;
	}
}