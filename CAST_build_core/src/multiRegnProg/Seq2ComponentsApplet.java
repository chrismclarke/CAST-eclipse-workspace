package multiRegnProg;

import java.awt.*;

import dataView.*;
import utils.*;

import ssq.*;
import multiRegn.*;

public class Seq2ComponentsApplet extends SeqComponentsApplet {
//	static final private String kXComponentKeys[] = {"xOnly", "xAfterZ"};
//	static final private String kZComponentKeys[] = {"zOnly", "zAfterX"};
//	static final private Color kComponentColors[] = {Color.red, Color.red};
	static final private Color kComponent3DColors[] = {Color.yellow, Color.red, Color.red, Color.yellow};
	static final private Color kPaleYellow = new Color(0xFFFF99);
	static final private Color kMudRed = new Color(0x993333);
	
	static final private Color kFirstRedColor[] = {Color.black, Color.red, Color.black, Color.black};
	static final private Color kSecondRedColor[] = {Color.black, Color.black, Color.red, Color.black};
	static final private Color kFirstMudColor[] = {Color.black, kMudRed, Color.black, Color.black};
	static final private Color kSecondMudColor[] = {Color.black, Color.black, kMudRed, Color.black};
	
	private String kXZComponentNames[];
	private String kZXComponentNames[];
	
	private XChoice mainVarChoice, firstOrSecondChoice;
	private int currentMainVar = 0;
	private int currentFirstOrSecond = 0;
	
	private AnovaTableView xzTable, zxTable;
	
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
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 0, 0, 5);
		thePanel.setLayout(new BorderLayout(0, 0));
		
		thePanel.add("Center", new XPanel());
		
			XPanel mainVarPanel = new XPanel();
			mainVarPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
			
				XLabel mainVarLabel = new XLabel(translate("Sum of sqrs for") + ":", XLabel.LEFT, this);
				mainVarLabel.setFont(getStandardBoldFont());
				mainVarLabel.setForeground(kDarkRed);
			
			mainVarPanel.add(mainVarLabel);
				MultiRegnDataSet regnData = (MultiRegnDataSet)data;
				mainVarChoice = new XChoice(this);
				mainVarChoice.addItem(regnData.getXVarName());
				mainVarChoice.addItem(regnData.getZVarName());
			mainVarPanel.add(mainVarChoice);
			
		thePanel.add("West", mainVarPanel);
	
			XPanel firstOrSecondPanel = new XPanel();
			firstOrSecondPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
			
				XLabel firstSecondLabel = new XLabel(translate("Component") + ":", XLabel.LEFT, this);
				firstSecondLabel.setFont(getStandardBoldFont());
				firstSecondLabel.setForeground(kDarkRed);
			
			firstOrSecondPanel.add(firstSecondLabel);
			
				firstOrSecondChoice = new XChoice(this);
				firstOrSecondChoice.addItem(kExplainedString + "X");
				firstOrSecondChoice.addItem(kExplainedString + "X" + kAfterString + "Z");
			firstOrSecondPanel.add(firstOrSecondChoice);
			
		thePanel.add("East", firstOrSecondPanel);
		return thePanel;
	}
	
	protected XPanel dataPanel(DataSet data) {
		XPanel thePanel = super.dataPanel(data);
		theView.setComponentType(SeqComponentPlanesView.M0_TO_M1);
		theView.setModelColors(kModel1Color, kModel2Color, Color.yellow);		//	yellow should not be used
		
		return thePanel;
	}
	
	protected String[] componentKeys() {
		return SeqXZComponentVariable.kAllComponentKeys;
	}
	
	protected Color[] componentColors() {
		Color[] c = new Color[SeqXZComponentVariable.kAllComponentKeys.length];
		c[0] = SeqXZComponentVariable.kTotalColor;
		c[1] = c[3] = SeqXZComponentVariable.kXOnlyColor;
		c[2] = c[4] = SeqXZComponentVariable.kZAfterXColor;
		c[5] = SeqXZComponentVariable.kResidualColor;
		return c;
	}
	
//	protected String[] componentKeys() {
//		return (mainVarChoice == null || mainVarChoice.getSelectedIndex() == 0)
//																							? kXComponentKeys : kZComponentKeys;
//	}
//	
//	protected Color[] componentColors() {
//		return kComponentColors;
//	}
	
	protected Color[] component3DColors() {
		return kComponent3DColors;
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		thePanel.add("Center", super.rightPanel(data));
			componentPlot.getView().setShowSD(true);
			componentPlot.setComponent(1);
			
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new InsetPanel(0, 20, 0, 0);
		thePanel.setLayout(new ProportionLayout(0.5, 10));
		
			xzTable = new AnovaTableView(summaryData, this, SeqXZComponentVariable.kXZComponentKey,
																						maxSsq, null, null, AnovaTableView.SSQ_AND_DF);
			xzTable.setComponentColors(kFirstRedColor);
			xzTable.setHilite(1, Color.yellow);
			xzTable.setComponentNames(kXZComponentNames);
			
		thePanel.add(ProportionLayout.LEFT, xzTable);
		
			zxTable = new AnovaTableView(summaryData, this, SeqXZComponentVariable.kZXComponentKey,
																						maxSsq, null, null, AnovaTableView.SSQ_AND_DF);
			zxTable.setComponentColors(kSecondMudColor);
			zxTable.setHilite(2, kPaleYellow);
			zxTable.setComponentNames(kZXComponentNames);
			
		thePanel.add(ProportionLayout.RIGHT, zxTable);
		
		return thePanel;
	}
	
	private void changeSelectedVariable(int newOrderIndex, int newMainVar) {
		if (newOrderIndex == 0) {		//	X or Z
			theView.setModelColors(kModel1Color, kModel2Color, Color.yellow);		//	yellow should not be used
			theView.setComponentType(SeqComponentPlanesView.M0_TO_M1);
			if (newMainVar == 0) {		//	X
				componentPlot.setComponent(1);
				theView.setModelKeys("ls0", "lsX", "ls");
				xzTable.setComponentColors(kFirstRedColor);
				xzTable.setHilite(1, Color.yellow);
				zxTable.setComponentColors(kSecondMudColor);
				zxTable.setHilite(2, kPaleYellow);
			}
			else {										//	Z
				componentPlot.setComponent(3);
				theView.setModelKeys("ls0", "lsZ", "ls");
				xzTable.setComponentColors(kSecondMudColor);
				xzTable.setHilite(2, kPaleYellow);
				zxTable.setComponentColors(kFirstRedColor);
				zxTable.setHilite(1, Color.yellow);
			}
			xzTable.repaint();
			zxTable.repaint();
		}
		else {								//	X after Z  or  Z after X
			theView.setModelColors(Color.yellow, kModel1Color, kModel2Color);		//	yellow should not be used
			theView.setComponentType(SeqComponentPlanesView.M1_TO_M2);
			if (newMainVar == 0) {								//	X after Z
				componentPlot.setComponent(4);
				theView.setModelKeys("ls0", "lsZ", "ls");
				xzTable.setComponentColors(kFirstMudColor);
				xzTable.setHilite(1, kPaleYellow);
				zxTable.setComponentColors(kSecondRedColor);
				zxTable.setHilite(2, Color.yellow);
			}
			else {																//	Z after X
				componentPlot.setComponent(2);
				theView.setModelKeys("ls0", "lsX", "ls");
				xzTable.setComponentColors(kSecondRedColor);
				xzTable.setHilite(2, Color.yellow);
				zxTable.setComponentColors(kFirstMudColor);
				zxTable.setHilite(1, kPaleYellow);
			}
			xzTable.repaint();
			zxTable.repaint();
		}
	}

	
	private boolean localAction(Object target) {
		if (target == firstOrSecondChoice) {
			if (firstOrSecondChoice.getSelectedIndex() != currentFirstOrSecond) {
				currentFirstOrSecond = firstOrSecondChoice.getSelectedIndex();
				changeSelectedVariable(currentFirstOrSecond, mainVarChoice.getSelectedIndex());
			}
			return true;
		}
		else if (target == mainVarChoice) {
			if (mainVarChoice.getSelectedIndex() != currentMainVar) {
				currentMainVar = mainVarChoice.getSelectedIndex();
				firstOrSecondChoice.changeItem(0, kExplainedString + ((currentMainVar == 0) ? "X" : "Z"));
				firstOrSecondChoice.changeItem(1, kExplainedString + ((currentMainVar == 0) ? ("X" + kAfterString + "Z") : ("Z" + kAfterString + "X")));
				changeSelectedVariable(firstOrSecondChoice.getSelectedIndex(), currentMainVar);
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