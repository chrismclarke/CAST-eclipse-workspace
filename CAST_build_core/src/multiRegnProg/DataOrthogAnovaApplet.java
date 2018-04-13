package multiRegnProg;

import java.awt.*;

import dataView.*;
import utils.*;

import ssq.*;
import multiRegn.*;


public class DataOrthogAnovaApplet extends DataSeqAnovaApplet {
	
	private AnovaTableView anovaTable;
	
	protected void addTTestVariables(SummaryDataSet summaryData, int[] decimals, int n) {
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
		
			XLabel dataSetLabel = new XLabel(translate("Data set") + ":", XLabel.LEFT, this);
			dataSetLabel.setFont(getStandardBoldFont());
		thePanel.add(dataSetLabel);
		
			dataSetChoice = ((MultiRegnDataSet)data).dataSetChoice(this);
		thePanel.add(dataSetChoice);
		
		return thePanel;
	}
	
	private String[] getExplanNames(DataSet data) {
		MultiRegnDataSet regnData = (MultiRegnDataSet)data;
		String xVarName[] = new String[2];
		xVarName[0] = regnData.getXVarName();
		xVarName[1] = regnData.getZVarName();
		return xVarName;
	}
	
	protected XPanel fullEquationPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 5, 0, 0);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			MultiRegnDataSet regnData = (MultiRegnDataSet)data;
			String yVarName = regnData.getYVarName();
			String xVarName[] = getExplanNames(data);
			
			fullEquationView = new MultiLinearEqnView(data, this, "ls", yVarName, xVarName, maxCoeff, maxCoeff);
		thePanel.add(fullEquationView);
		
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
		thePanel.add("West", leftPanel(data));
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new BorderLayout(0, 10));
			
			rightPanel.add("Center", view3DPanel(data));
		
		thePanel.add("Center", rightPanel);
		
		return thePanel;
	}
	
	protected boolean canChangeOrder() {
		return false;
	}
	
	private String[] getSsqNames(DataSet data) {
		MultiRegnDataSet regnData = (MultiRegnDataSet)data;
		String xVarName[] = new String[4];
		xVarName[0] = translate("Total");
		xVarName[1] = regnData.getXVarName();
		xVarName[2] = regnData.getZVarName();
		xVarName[3] = translate("Residual");
		return xVarName;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 7, 0, 0);
		thePanel.setLayout(new BorderLayout(0, 7));
		
		thePanel.add("North", fullEquationPanel(data));
		
			XPanel anovaPanel = new InsetPanel(0, 7, 0, 2);
			anovaPanel.setLayout(new BorderLayout(0, 0));
				anovaTable = new AnovaTableView(summaryData, this,
																	SeqXZComponentVariable.kXZComponentKey, maxSsq, maxMsq, maxF,
																	AnovaTableView.SSQ_F_PVALUE);
				anovaTable.setComponentNames(getSsqNames(data));
		
			anovaPanel.add("Center", anovaTable);
		
			anovaPanel.lockBackground(kTestBackgroundColor);
		thePanel.add("Center", anovaPanel);
		
		thePanel.add("South", conclusionPanel(data));
		
		return thePanel;
	}
	
	protected void changeDisplaysForNewData(MultiRegnDataSet data) {
		super.changeDisplaysForNewData(data);
		
		anovaTable.setComponentNames(getSsqNames(data));
			MultiRegnDataSet regnData = (MultiRegnDataSet)data;
		fullEquationView.setYName(regnData.getYVarName());
		fullEquationView.setXNames(getExplanNames(data));
	}
}