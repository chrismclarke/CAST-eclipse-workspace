package pairBlockProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;

import pairBlock.*;


public class MeanBlockCisApplet extends XApplet {
	static final protected String JITTER_PARAM = "jitter";
	
	static final private String kXZKeys[] = {"x", "z"};
	
	protected TwoTreatDataSet data;
	
	private XCheckbox showCiCheck;
	private XChoice blockDisplayChoice;
	private int currentBlockDisplay = 0;
	
	protected TreatBlockCIView dataView;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 10));
	
		add("North", dataCiPanel());
		add("Center", displayPanel(data));
		add("South", controlPanel());
	}
	
	protected TwoTreatDataSet readData() {
		TwoTreatDataSet data = new TwoTreatDataSet(this);
		
			BasicComponentVariable residXZComp = new BasicComponentVariable("ResidXZ", data, kXZKeys,
																			"y", "ls", BasicComponentVariable.RESIDUAL, 9);
		data.addVariable("residXZ", residXZComp);
		
			GroupsModelVariable lsX = new GroupsModelVariable("X only", data, "x");
			lsX.updateLSParams("y");
		data.addVariable("lsX", lsX);
					
			BasicComponentVariable residXComp = new BasicComponentVariable("ResidX", data, "x", "y",
																			"lsX", BasicComponentVariable.RESIDUAL, 9);
		data.addVariable("residX", residXComp);
		
		return data;
	}
	
	protected XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
	
			blockDisplayChoice = new XChoice(this);
			blockDisplayChoice.addItem(translate("Analysis ignoring blocks"));
			blockDisplayChoice.addItem(translate("Correct analysis taking account of blocks"));
			
		thePanel.add(blockDisplayChoice);
		
		return thePanel;
	}
	
	private XPanel dataCiPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			showCiCheck = new XCheckbox(translate("Show 95% confidence intervals for means"), this);
		thePanel.add(showCiCheck);
		
		return thePanel;
	}
	
	protected XPanel titlePanel(TwoTreatDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XLabel titleLabel = new XLabel(data.getVariable("x").name, XLabel.LEFT, this);
			titleLabel.setFont(getStandardBoldFont());
		
		thePanel.add("West", titleLabel);
		thePanel.add("Center", new XPanel());
		
		return thePanel;
	}
	
	protected XPanel displayPanel(TwoTreatDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
		thePanel.add("North", titlePanel(data));
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new AxisLayout());
			
				NumCatAxis treatAxis = new VertAxis(this);
				CatVariable xVar = (CatVariable)data.getVariable("x");
				treatAxis.setCatLabels(xVar);
			
			dataPanel.add("Left", treatAxis);
			
				HorizAxis numAxis = new HorizAxis(this);
				numAxis.readNumLabels(data.getYAxisInfo());
				numAxis.setAxisName(data.getYVarName());
			
			dataPanel.add("Bottom", numAxis);
			
				double jitter = Double.parseDouble(getParameter(JITTER_PARAM));
				dataView = new TreatBlockCIView(data, this, "x", "z", "lsX", numAxis, treatAxis, jitter);
				dataView.setActiveNumVariable("y");
				dataView.setCrossSize(DataView.LARGE_CROSS);
				dataView.setShowBlocks(false);
				dataView.setCIType(TreatBlockCIView.NO_CIS);
				dataView.lockBackground(Color.white);
				
			dataPanel.add("Center", dataView);
			
			addMarginView(dataPanel, numAxis);
		
		thePanel.add("Center", dataPanel);
		
		return thePanel;
	}
	
	protected void addMarginView(XPanel dataPanel, NumCatAxis numAxis) {
								//		to allow MeanComparisonApplet to add slider for multiple comparisons
	}
	
	protected void changeBlockDisplay(boolean useBlocks) {
		dataView.setShowBlocks(useBlocks);
		if (showCiCheck.getState())
			dataView.setCIType(useBlocks ? TreatBlockCIView.TREAT_IN_BLOCK_CIS
																					: TreatBlockCIView.TREAT_CIS);
		dataView.repaint();
	}

	
	private boolean localAction(Object target) {
		if (target == showCiCheck) {
			if (showCiCheck.getState()) {
				dataView.setCIType((currentBlockDisplay == 0) ? TreatBlockCIView.TREAT_CIS
																									: TreatBlockCIView.TREAT_IN_BLOCK_CIS);
				dataView.repaint();
			}
			else {
				dataView.setCIType(TreatBlockCIView.NO_CIS);
				dataView.repaint();
			}
			return true;
		}
		else if (target == blockDisplayChoice) {
			int newChoice = blockDisplayChoice.getSelectedIndex();
			if (newChoice != currentBlockDisplay) {
				currentBlockDisplay = newChoice;
				changeBlockDisplay(newChoice != 0);
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}