package multiRegnProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;

import ssq.*;
import multiRegn.*;


public class OrthogAnovaApplet extends OrthogEqnsApplet {
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String COMPONENT_DECIMALS_PARAM = "componentDecimals";
	
	static final private Color kXZBackgroungColor = new Color(0xBBEEFF);
	static final private Color kZXBackgroungColor = new Color(0xFFDDDD);
	
	static final private int kR2Decimals = 3;
	
	private String kXZComponentNames[];
	private String kZXComponentNames[];
	
	private AnovaTableView xzTable, zxTable;
	
	private NumValue maxSsq;
	
	public void setupApplet() {
		kXZComponentNames = new String[4];
		kXZComponentNames[0] = translate("Total");
		kXZComponentNames[1] = "X";
		kXZComponentNames[2] = "Z " + translate("after") + " X";
		kXZComponentNames[3] = translate("Residual");
		
		kZXComponentNames = new String[4];
		kZXComponentNames[0] = translate("Total");
		kZXComponentNames[1] = "Z";
		kZXComponentNames[2] = "X " + translate("after") + " Z";
		kZXComponentNames[3] = translate("Residual");
		
		super.setupApplet();
	}
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		int componentDecimals = Integer.parseInt(getParameter(COMPONENT_DECIMALS_PARAM));
		SeqXZComponentVariable.addComponentsToData((CoreModelDataSet)data, "x", "z", "y", "lsX", "lsZ", "ls", componentDecimals);
		
		maxSsq = new NumValue(getParameter(MAX_SSQ_PARAM));
		
		return new AnovaSummaryData(data, "error",
										SeqXZComponentVariable.kAllComponentKeys, maxSsq.decimals, kR2Decimals);
	}
	
	protected XPanel leftSummaryPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 10);
		thePanel.setLayout(new BorderLayout(0, 0));
		
			xzTable = new AnovaTableView(summaryData, this, SeqXZComponentVariable.kXZComponentKey,
																			maxSsq, null, null, AnovaTableView.SSQ_AND_DF);
			xzTable.setHilite(1, Color.yellow);
			xzTable.setComponentNames(kXZComponentNames);
			
		thePanel.add("Center", xzTable);
		
		thePanel.lockBackground(kXZBackgroungColor);
		return thePanel;
	}
	
	protected XPanel rightSummaryPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 10);
		thePanel.setLayout(new BorderLayout(0, 0));
		
			NumValue maxSsq = new NumValue(getParameter(MAX_SSQ_PARAM));
			zxTable = new AnovaTableView(summaryData, this, SeqXZComponentVariable.kZXComponentKey,
																		maxSsq, null, null, AnovaTableView.SSQ_AND_DF);
			zxTable.setHilite(2, Color.yellow);
			zxTable.setComponentNames(kZXComponentNames);
			
		thePanel.add("Center", zxTable);
		
		thePanel.lockBackground(kZXBackgroungColor);
		return thePanel;
	}
	
	protected void changeMainVar(int newMainVar) {
		if (newMainVar == 0) {
			xzTable.setHilite(1, Color.yellow);
			xzTable.repaint();
			zxTable.setHilite(2, Color.yellow);
			zxTable.repaint();
			((Rotate3DCrossPlanesView)theView).setPlaneKeys("ls", "lsX");
			theView.repaint();
		}
		else {
			xzTable.setHilite(2, Color.yellow);
			xzTable.repaint();
			zxTable.setHilite(1, Color.yellow);
			zxTable.repaint();
			((Rotate3DCrossPlanesView)theView).setPlaneKeys("ls", "lsZ");
			theView.repaint();
		}
	}
}