package catProg;

import java.awt.*;

import dataView.*;
import utils.*;

import cat.*;


public class TableCombineApplet extends XApplet {
	
	private DataSet data;
	private TableCombineView theTable;
	private XCheckbox[] sortCheck, combineCheck, hideCheck;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 10));
		
		add("Center", tablePanel(data));
		add("East", controlPanel(data));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
			LabelVariable labelVar = new LabelVariable(getParameter(LABEL_NAME_PARAM));
			labelVar.readValues(getParameter(LABELS_PARAM));
		data.addVariable("label", labelVar);
		
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		data.addCatVariable("group", getParameter(CAT_NAME_PARAM), getParameter(CAT_VALUES_PARAM),
																															getParameter(CAT_LABELS_PARAM));
		
		return data;
	}

	private XPanel tablePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			theTable = new TableCombineView(data, this, "label", "y", "group");
			
			theTable.setFont(getBigFont());
			
		thePanel.add(theTable);
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 40));
		
		CatVariable groupVar = (CatVariable)data.getVariable("group");
		int nGroups = groupVar.noOfCategories();
		
		sortCheck = new XCheckbox[nGroups];
		combineCheck = new XCheckbox[nGroups];
		hideCheck = new XCheckbox[nGroups];
		
		for (int i=0 ; i<nGroups ; i++)
			thePanel.add(groupControlPanel(groupVar.getLabel(i), i));
		
		return thePanel;
	}
	
	private XPanel groupControlPanel(Value label, int group) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_TOP, 5));
		
			Color groupColor = TableCombineView.kGroupColour[group];
			
			XLabel groupLabel = new XLabel(label.toString(), XLabel.LEFT, this);
			groupLabel.setForeground(groupColor);
			groupLabel.setFont(getBigBoldFont());
			
		thePanel.add(groupLabel);
			
			sortCheck[group] = new XCheckbox(translate("Sort by frequency"), this);
//			sortCheck[group].setForeground(groupColor);
		thePanel.add(sortCheck[group]);
			
			combineCheck[group] = new XCheckbox(translate("Combine categories"), this);
//			combineCheck[group].setForeground(groupColor);
		thePanel.add(combineCheck[group]);
			
			hideCheck[group] = new XCheckbox(translate("Hide categories"), this);
//			hideCheck[group].setForeground(groupColor);
		thePanel.add(hideCheck[group]);
		
		return thePanel;
	}
	
	private void disableChecks() {
		for (int i=0 ; i<sortCheck.length ; i++) {
			sortCheck[i].disable();
			combineCheck[i].disable();
			hideCheck[i].disable();
		}
	}
	
	private void checkEnabling() {
		for (int i=0 ; i<sortCheck.length ; i++) {
			hideCheck[i].enable();
			if (hideCheck[i].getState()) {
				sortCheck[i].disable();
				combineCheck[i].disable();
			}
			else {
				combineCheck[i].enable();
				if (combineCheck[i].getState())
					sortCheck[i].disable();
				else
					sortCheck[i].enable();
			}
		}
	}
	
	protected void frameChanged(DataView theView) {
		if (theView.getCurrentFrame() == TableCombineView.kEndFrame)
			checkEnabling();
	}
	
	private boolean localAction(Object target) {
		for (int i=0 ;i<sortCheck.length ; i++) {
			if (target == sortCheck[i]) {
				if (sortCheck[i].getState())
					theTable.setGroupDisplay(i, TableCombineView.SORT);
				else
					theTable.setGroupDisplay(i, TableCombineView.NORMAL);
				
				theTable.repaint();
				disableChecks();
				return true;
			}
			else if (target == combineCheck[i]) {
				if (combineCheck[i].getState())
					theTable.setGroupDisplay(i, TableCombineView.COMBINE);
				else {
					boolean sorted = sortCheck[i].getState();
					theTable.setGroupDisplay(i, sorted ? TableCombineView.SORT
																							: TableCombineView.NORMAL);
				}
				
				theTable.repaint();
				disableChecks();
				return true;
			}
			else if (target == hideCheck[i]) {
				if (hideCheck[i].getState())
					theTable.setGroupDisplay(i, TableCombineView.HIDE);
				else {
					boolean sorted = sortCheck[i].getState();
					boolean combined = combineCheck[i].getState();
					theTable.setGroupDisplay(i, combined ? TableCombineView.COMBINE
																	: sorted ? TableCombineView.SORT : TableCombineView.NORMAL);
				}
				
				theTable.repaint();
				disableChecks();
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
	
}