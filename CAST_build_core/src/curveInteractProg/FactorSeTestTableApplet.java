package curveInteractProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;

import curveInteract.*;


public class FactorSeTestTableApplet extends EstSeTableApplet {
	static final private String FACTOR_NAME_PARAM = "factorVarName";
	static final private String FACTOR_LABELS_PARAM = "factorLabels";
	static final private String FACTOR_VALUES_PARAM = "factorValues";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	
	static final protected Color kTableBackground = new Color(0xDDDDEE);
	
	public void setupApplet() {
		data = readData();
		summaryData = createSummaryData(data);
		summaryData.setSingleSummaryFromData();
		
		setLayout(new BorderLayout(10, 0));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new BorderLayout(10, 0));
			mainPanel.add("Center", dataDisplayPanel(data));
			mainPanel.add("East", ssqPanel(summaryData));
			
		add("Center", mainPanel);
		
		add("South", tablePanel(data, summaryData));
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		CatVariable factor = new CatVariable(getParameter(FACTOR_NAME_PARAM));
		factor.readLabels(getParameter(FACTOR_LABELS_PARAM));
		factor.readValues(getParameter(FACTOR_VALUES_PARAM));
		data.addVariable("factor", factor);
		
		return data;
	}
	
	protected XPanel dataDisplayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 2));
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new AxisLayout());
			
				HorizAxis xAxis = new HorizAxis(this);
				CatVariable xFactorVar = (CatVariable)data.getVariable("factor");
				xAxis.setCatLabels(xFactorVar);
				xAxis.setAxisName(xFactorVar.name);
			innerPanel.add("Bottom", xAxis);
			
				VertAxis yAxis = new VertAxis(this);
				yAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
			innerPanel.add("Left", yAxis);
			
				FactorLevelMeanView dataView = new FactorLevelMeanView(data, this, xAxis, yAxis, "y", "factor", "ls");
				dataView.lockBackground(Color.white);
			innerPanel.add("Center", dataView);
		
		thePanel.add("Center", innerPanel);
		
			XLabel yNameLabel = new XLabel(data.getVariable("y").name, XLabel.LEFT, this);
			yNameLabel.setFont(yAxis.getFont());
		thePanel.add("North", yNameLabel);
		
		return thePanel;
	}
	
	protected XPanel tablePanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new InsetPanel(20, 7);
		thePanel.setLayout(new BorderLayout(0, 0));
		
			StringTokenizer st = new StringTokenizer(getParameter(MAX_TABLE_ENTRIES_PARAM));
			NumValue maxParam = new NumValue(st.nextToken());
			NumValue maxSE = new NumValue(st.nextToken());
			NumValue maxT = new NumValue(st.nextToken());
//			NumValue maxVIF = new NumValue(st.nextToken());
			testTable = new ParamTestsRemoveView(data, this, "ls", "y", paramName, maxParam, maxSE, maxT, summaryData);
//			testTable.setShowT(false);
//			testTable.setShowPValue(false);
		thePanel.add("Center", testTable);
		
		thePanel.lockBackground(kTableBackground);
		return thePanel;
	}
}