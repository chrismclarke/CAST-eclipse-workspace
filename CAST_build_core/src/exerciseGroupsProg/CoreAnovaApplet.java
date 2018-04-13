package exerciseGroupsProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;
import exercise2.*;
import models.*;

import ssq.*;


abstract public class CoreAnovaApplet extends ExerciseApplet {
	
	static final private NumValue kZero = new NumValue(0, 0);
		
	protected AnovaSummaryData summaryData;
	
	private AnovaTableView tableView;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(data));
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
			bottomPanel.add(createMarkingPanel(NO_HINTS));
			
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add(messagePanel);
		
		add("South", bottomPanel);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		registerParameter("xVarName", "string");
		registerParameter("levelIndex", "int");		//	only used to pick xNames and xValues
		registerParameter("xNames", "string");
		registerParameter("xValues", "string");
		registerParameter("yVarName", "string");
		registerParameter("yModel", "string");
		registerParameter("componentNames", "string");		//	total, explained, resid (separated by *)
		registerParameter("maxSsq", "const");
		registerParameter("maxMss", "const");
		registerParameter("maxF", "const");
		registerParameter("maxRSquared", "const");
	}
	
	protected String getXVarName() {
		return getStringParam("xVarName");
	}
	
	protected String getXNames() {
		String s = getStringParam("xNames");
		s = "#" + s.replaceAll(" ", "# #") + "#";
		s = s.replaceAll("_", " ");
		return s;
	}
	
	protected String getXValues() {
		return getStringParam("xValues");
	}
	
	protected String getYVarName() {
		return getStringParam("yVarName");
	}
	
	protected String getYModel() {
		return getStringParam("yModel");		//	mean then sd for each group
	}
	
	protected String getComponentNames() {
		return getStringParam("componentNames");
	}
	
	protected NumValue getMaxSsq() {
		return getNumValueParam("maxSsq");
	}
	
	protected NumValue getMaxMss() {
		return getNumValueParam("maxMss");
	}
	
	protected NumValue getMaxF() {
		return getNumValueParam("maxF");
	}
	
	protected NumValue getMaxRSquared() {
		return getNumValueParam("maxRSquared");
	}
	
	
//-----------------------------------------------------------
	
	
	abstract protected Color getAnovaTableBackground();
	
	protected XPanel getAnovaTable(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel tablePanel = new InsetPanel(20, 5);
			tablePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				StringTokenizer st = new StringTokenizer(getComponentNames(), "*");
				String componentName[] = new String[3];
				for (int i=0 ; i<3 ; i++)
					componentName[i] = st.nextToken();
			
				tableView = new AnovaTableView(summaryData, this, BasicComponentVariable.kComponentKey,
											kZero, kZero, kZero, AnovaTableView.SSQ_F_PVALUE);
				tableView.setComponentNames(componentName);
				tableView.setFont(getBigFont());
			
			tablePanel.add(tableView);
			
			Color tableBackground = getAnovaTableBackground();
			if (tableBackground != null)
				tablePanel.lockBackground(tableBackground);
		thePanel.add(tablePanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		StringTokenizer st = new StringTokenizer(getComponentNames(), "*");
		String componentName[] = new String[3];
		for (int i=0 ; i<3 ; i++)
			componentName[i] = st.nextToken();
	
		tableView.setComponentNames(componentName);
		tableView.setMaxValues(getMaxSsq(), getMaxMss(), getMaxF());
		tableView.revalidate();
	}
	
	protected void setDataForQuestion() {
		CatVariable xVar = (CatVariable)data.getVariable("x");
			xVar.readLabels(getXNames());
			xVar.readValues(getXValues());
			int n = xVar.noOfValues();
		
		NumSampleVariable error = (NumSampleVariable)data.getVariable("error");
			error.setSampleSize(n);
			error.generateNextSample();
		
		GroupsModelVariable model = (GroupsModelVariable)data.getVariable("model");
			model.setParameters(getYModel());
			
		GroupsModelVariable lsFit = (GroupsModelVariable)data.getVariable("ls");
			lsFit.updateLSParams("y");
		
		summaryData.setSummaryDecimals(BasicComponentVariable.kComponentKey,
																					getMaxSsq().decimals, getMaxRSquared().decimals);
		summaryData.setSingleSummaryFromData();
	}
	
	
//-----------------------------------------------------------

	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			RandomNormal zGenerator = new RandomNormal(1, 0.0, 1.0, 3.0);
			NumSampleVariable error = new NumSampleVariable("error", zGenerator, 10);
			error.generateNextSample();
		data.addVariable("error", error);
		
			CatVariable xVar = new CatVariable("X");
			xVar.readLabels("G1");
			xVar.readValues("1@0");
		data.addVariable("x", xVar);
		
			GroupsModelVariable model = new GroupsModelVariable("YModel", data, "x");
			model.setParameters("0 1");
		data.addVariable("model", model);
			
			ResponseVariable yData = new ResponseVariable("Y", data, "x", "error", "model", 0);
		data.addVariable("y", yData);
		
			GroupsModelVariable lsFit = new GroupsModelVariable("least squares", data, "x");
		data.addVariable("ls", lsFit);
		
			BasicComponentVariable totalComp = new BasicComponentVariable("Total", data, "x", "y",
											"ls", BasicComponentVariable.TOTAL, 0);
		data.addVariable("total", totalComp);
			BasicComponentVariable explainedComp = new BasicComponentVariable("Explained", data, "x", "y",
											"ls", BasicComponentVariable.EXPLAINED, 0);
		data.addVariable("explained", explainedComp);
			BasicComponentVariable residComp = new BasicComponentVariable("Resid", data, "x", "y",
											"ls", BasicComponentVariable.RESIDUAL, 0);
		data.addVariable("resid", residComp);
		
		summaryData = new AnovaSummaryData(data, "error", BasicComponentVariable.kComponentKey,
																									0, 0);
		
		return data;
	}
	
}