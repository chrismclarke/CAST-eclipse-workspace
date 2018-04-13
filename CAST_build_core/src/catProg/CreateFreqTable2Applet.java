package catProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import valueList.*;

import cat.*;


public class CreateFreqTable2Applet extends CreateFreqTableApplet {
	static final private String VARIABLES_PARAM = "variables";
	static final private String LIST_TYPE_PARAM = "listType";
	
	static final private int SIMPLE = 0;
	static final private int DATA_MATRIX = 1;
	static final private int BOTH = 2;
	
	private int noOfVariables, mainCatIndex;
	
	private CatValueScrollList theScrollList;
	
	private int displayTypesAllowed = DATA_MATRIX;
	private XChoice displayTypeChoice = null;
	private int selectedDisplayType = 0;
	
	private XPanel displayPanel;
	private CardLayout displayCardLayout;
	
	public void setupApplet() {
		String listTypeString = getParameter(LIST_TYPE_PARAM);
		if (listTypeString != null)
			displayTypesAllowed = listTypeString.equals("simple") ? SIMPLE
														: listTypeString.equals("dataMatrix") ? DATA_MATRIX : BOTH;
		
		super.setupApplet();
		
		if (displayTypesAllowed == BOTH) {
			displayTypeChoice = new XChoice(translate("Data format") + ":", XChoice.HORIZONTAL, this);
			displayTypeChoice.addItem(translate("Simple"));
			displayTypeChoice.addItem(translate("Data matrix"));
			
			XPanel topPanel = new InsetPanel(0, 0, 0, 5);
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
			topPanel.add(displayTypeChoice);
			
			add("North", topPanel);
		}
	}
	
	protected DataSet readData() {
		StringTokenizer st = new StringTokenizer(getParameter(VARIABLES_PARAM));
		noOfVariables = Integer.parseInt(st.nextToken());
		mainCatIndex = Integer.parseInt(st.nextToken());
		
		DataSet data = new DataSet();
		for (int i=1 ; i<=noOfVariables ; i++) {
			String labelsString = getParameter(CAT_LABELS_PARAM + i);
			if (labelsString == null)
				data.addNumVariable("y" + i, getParameter(VAR_NAME_PARAM + i), getParameter(VALUES_PARAM + i));
			else
				data.addCatVariable("y" + i, getParameter(VAR_NAME_PARAM + i), getParameter(VALUES_PARAM + i), labelsString);
		}
		return data;
	}
	
	protected String getMainCatVariableKey() {
		return "y" + (mainCatIndex + 1);
	}
	
	protected XPanel valueListPanel(DataSet data, CoreCreateTableView freqTable) {
		if (displayTypesAllowed == SIMPLE)
			return super.valueListPanel(data, freqTable);
		else {
			theScrollList = new CatValueScrollList(data, this, ScrollValueList.HEADING, freqTable);
			
			for (int i=1 ; i<=noOfVariables ; i++)
				theScrollList.addVariableToList("y" + i, ScrollValueList.RAW_VALUE);
			theScrollList.setSelectedCols(mainCatIndex, -1);
			
			if (displayTypesAllowed == BOTH) {
				displayPanel = new XPanel();
				displayCardLayout = new CardLayout();
				displayPanel.setLayout(displayCardLayout);
				
					XPanel valuePanel = new XPanel();
					valuePanel.setLayout(new BorderLayout(0, 0));
					valuePanel.add("Center", super.valueListPanel(data, freqTable));
				
				displayPanel.add("std", valuePanel);
				displayPanel.add("dataMatrix", theScrollList);
				
				theList.setCommonValuesClicked(theScrollList.getValuesClicked());
				
				return displayPanel;
			}
			else
				return theScrollList;
		}
	}
	
	protected int numberCompleted() {
		return (theScrollList != null) ? theScrollList.numberCompleted()
																: super.numberCompleted();
	}
	
	protected void doReset() {
		if (theScrollList != null)
			theScrollList.resetList();
		super.doReset();
	}
	
	protected void doCompleteTable() {
		if (theScrollList != null)
			theScrollList.completeTable();
		super.doCompleteTable();
	}
	
	private boolean localAction(Object target) {
		if (target == displayTypeChoice) {
			int newChoice = displayTypeChoice.getSelectedIndex();
			if (newChoice != selectedDisplayType) {
				selectedDisplayType = newChoice;
				displayCardLayout.show(displayPanel, (newChoice == 0) ? "std" : "dataMatrix");
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean  action(Event  evt, Object  what) {
		if (super.action(evt, what))
			return true;
		else 
			return localAction(evt.target);
	}
}