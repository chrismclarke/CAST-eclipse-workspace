package exper2Prog;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import exper2.*;
import formula.*;


public class RandomiseLatinApplet extends XApplet {
	static final private String FACTOR_NAME_PARAM = "factorName";
	static final protected String ROW_NAMES_PARAM = "rowNames";
	static final private String COL_NAMES_PARAM = "colNames";
	static final protected String TREATMENT_NAMES_PARAM = "treatmentNames";
	static final private String BLOCK_VARS_PARAM = "blockVars";
	
	static final private Color kMessageColor = new Color(0x990000);
	
	private XPanel treatPanel, stepPanel;
	private CardLayout treatPanelLayout, stepPanelLayout;
	
	protected IncompleteDesignView designTable;
	private TreatmentPermView treatPermTable;
	
	private XButton randomiseRowsButton, randomiseColsButton, randomiseTreatsButton, randomiseUnitsButton;
	private XCheckbox randomiseCheck;
	
	private boolean rowsPermed = false, colsPermed = false, treatsPermed = false;
	
	public void setupApplet() {
		setLayout(new BorderLayout(0, 0));
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new BorderLayout(0, 30));
			dataPanel.add("Center", rowColPanel());
			dataPanel.add("North", treatPanel());
		
		add("Center", dataPanel);
		add("East", controlPanel());
	}
	
	
	protected String permRowsButtonName() {
		return translate("Permute") + " " + translate("rows");
	}
	
	protected String permColsButtonName() {
		return translate("Permute") + " " + translate("cols");
	}
	
	protected String permTreatsButtonName() {
		return translate("Permute") + " " + translate("letters");
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 12));
			
			stepPanel = new XPanel();
			stepPanelLayout = new CardLayout();
			stepPanel.setLayout(stepPanelLayout);
				
				XPanel treatPanel = new XPanel();
				treatPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 12));
				
					XLabel label = new XLabel(translate("Find treatments"), XLabel.LEFT, this);
					label.setFont(getBigBoldFont());
				treatPanel.add(label);
				
					randomiseRowsButton = new XButton(permRowsButtonName(), this);
				treatPanel.add(randomiseRowsButton);
					
				String permColsName = permColsButtonName();
				if (permColsName == null)
					colsPermed = true;
				else {
					randomiseColsButton = new XButton(permColsName, this);
					treatPanel.add(randomiseColsButton);
				}
					
					randomiseTreatsButton = new XButton(permTreatsButtonName(), this);
				treatPanel.add(randomiseTreatsButton);
				
			stepPanel.add("treats", treatPanel);
				
				XPanel unitsPanel = new XPanel();
				unitsPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 12));
				
					randomiseUnitsButton = new XButton(translate("Randomise"), this);
				unitsPanel.add(randomiseUnitsButton);
				
			stepPanel.add("units", unitsPanel);
			
		thePanel.add(stepPanel);
		
			randomiseCheck = new XCheckbox(translate("Allocate to units"), this);
			randomiseCheck.disable();
			
		thePanel.add(randomiseCheck);
		
		return thePanel;
	}
	
	protected LabelValue[] readLevels(String paramName) {
		String paramNameString = getParameter(paramName);
		if (paramNameString == null)
			return null;
		
		StringTokenizer st = new StringTokenizer(paramNameString, "*");
		LabelValue name[] = new LabelValue[st.countTokens()];
		for (int i=0 ; i<name.length ; i++)
			name[i] = new LabelValue(MText.expandText(st.nextToken()));
		return name;
	}
	
	protected XPanel rowColPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			LabelValue treatName[] = readLevels(TREATMENT_NAMES_PARAM);
			LabelValue rowName[] = readLevels(ROW_NAMES_PARAM);
			LabelValue colName[] = readLevels(COL_NAMES_PARAM);
			designTable = new IncompleteDesignView(rowName, colName, treatName, this);
			designTable.setFont(getBigBoldFont());
			
		thePanel.add("Center", designTable);
		
		return thePanel;
	}
	
	private XPanel treatPanel() {
		treatPanel = new XPanel();
		treatPanelLayout = new CardLayout();
		treatPanel.setLayout(treatPanelLayout);
			
			XPanel permPanel = new InsetPanel(50, 0, 0, 0);
			permPanel.setLayout(new BorderLayout(0, 0));
				LabelValue treatName[] = readLevels(TREATMENT_NAMES_PARAM);
				LabelValue factorName = new LabelValue(getParameter(FACTOR_NAME_PARAM));
				treatPermTable = new TreatmentPermView(factorName, treatName, designTable, this);
				treatPermTable.setFont(getBigBoldFont());
			permPanel.add("Center", treatPermTable);
			
		treatPanel.add("treats", permPanel);
		
			XPanel randomiseTitlePanel = new XPanel();
			randomiseTitlePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_BOTTOM, 0));
			
				XLabel title = new XLabel("Allocation of treatments", XLabel.LEFT, this);
				title.setFont(getBigBoldFont());
				title.setForeground(kMessageColor);
			randomiseTitlePanel.add(title);
				
				XLabel title2 = new XLabel("to experimental units", XLabel.LEFT, this);
				title2.setFont(getBigBoldFont());
				title2.setForeground(kMessageColor);
			randomiseTitlePanel.add(title2);
			
		treatPanel.add("units", randomiseTitlePanel);
		treatPanelLayout.show(treatPanel, "treats");
		
		return treatPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == randomiseRowsButton) {
			designTable.permuteRows();
			rowsPermed = true;
			if (colsPermed && treatsPermed)
				randomiseCheck.enable();
			return true;
		}
		else if (target == randomiseColsButton) {
			designTable.permuteCols();
			colsPermed = true;
			if (rowsPermed && treatsPermed)
				randomiseCheck.enable();
			return true;
		}
		else if (target == randomiseTreatsButton) {
			treatPermTable.permuteTreats();
			treatsPermed = true;
			if (rowsPermed && colsPermed)
				randomiseCheck.enable();
			return true;
		}
		else if (target == randomiseUnitsButton) {
			designTable.permuteUnits();
			return true;
		}
		else if (target == randomiseCheck) {
			if (randomiseCheck.getState()) {
				String blockString = getParameter(BLOCK_VARS_PARAM);
				boolean rowsAreBlocks = blockString.indexOf("rows") >= 0;
				boolean colsAreBlocks = blockString.indexOf("cols") >= 0;
				designTable.setDesignMode(IncompleteDesignView.ALLOCATE_UNITS, rowsAreBlocks, colsAreBlocks);
				treatPanelLayout.show(treatPanel, "units");
				stepPanelLayout.show(stepPanel, "units");
				if (rowsAreBlocks && colsAreBlocks)
					randomiseUnitsButton.disable();
			}
			else {
				designTable.setDesignMode(IncompleteDesignView.PICK_TREATMENTS, false, false);
				treatPanelLayout.show(treatPanel, "treats");
				stepPanelLayout.show(stepPanel, "treats");
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