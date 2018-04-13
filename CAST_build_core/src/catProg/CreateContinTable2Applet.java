package catProg;

import java.awt.*;

import dataView.*;
import utils.*;

import cat.*;


public class CreateContinTable2Applet extends CreateContinTableApplet {
	static final private String NO_OF_COLS_PARAM = "noOfCols";
	static final private String LIST_TYPE_PARAM = "listType";
	
	static final private int SIMPLE = 0;
	static final private int DATA_MATRIX = 1;
	static final private int BOTH = 2;
	
	private CatConditValueListView groupList[];
	
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
	
	protected XPanel valueListPanel(DataSet data, CoreCreateTableView freqTable) {
		if (displayTypesAllowed == DATA_MATRIX)
			return super.valueListPanel(data, freqTable);
		else {
			String colKey = "y" + (rowCatIndex + 1);		//	swap rows and cols
			String rowKey = "y" + (colCatIndex + 1);		//	swap rows and cols
			CatVariable rowVar = (CatVariable)data.getVariable(rowKey);
			int noOfRows = rowVar.noOfCategories();
			int nListCols = Integer.parseInt(getParameter(NO_OF_COLS_PARAM));
			groupList = new CatConditValueListView[noOfRows];
			
			XPanel thePanel = new XPanel();
			thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 20));
			
			int counts[] = rowVar.getCounts();
			int cumTotal = 0;
			for (int i=0 ; i<noOfRows ; i++) {
				XPanel rowPanel = new XPanel();
				rowPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 4));
					Value rowCategory = rowVar.getLabel(i);
					XLabel rowCatLabel = new XLabel(rowCategory.toString(), XLabel.LEFT, this);
					rowCatLabel.setFont(getStandardBoldFont());
				rowPanel.add(rowCatLabel);
				
					groupList[i] = new CatConditValueListView(data, this, colKey, freqTable, nListCols, rowKey, rowCategory);
					groupList[i].setConditOffset(cumTotal, counts[i]);
					cumTotal += counts[i];
					groupList[i].lockBackground(Color.white);
				rowPanel.add(groupList[i]);
				
				thePanel.add(rowPanel);
			}
				
			if (displayTypesAllowed == BOTH) {
				displayPanel = new XPanel();
				displayCardLayout = new CardLayout();
				displayPanel.setLayout(displayCardLayout);
				
					XPanel valuePanel = new XPanel();
					valuePanel.setLayout(new BorderLayout(0, 0));
					valuePanel.add("Center", super.valueListPanel(data, freqTable));
				
				displayPanel.add("std", thePanel);
				displayPanel.add("dataMatrix", valuePanel);
				
				CatSelection clickSelection = theScrollList.getValuesClicked();
				for (int i=0 ; i<groupList.length ; i++)
					groupList[i].setCommonValuesClicked(clickSelection);
				
				return displayPanel;
			}
			else
				return thePanel;
		}
	}
	
	protected int numberCompleted() {
		if (groupList == null)
			return super.numberCompleted();
		else {
			int n = 0;
			for (int i=0 ; i<groupList.length ; i++)
				n += groupList[i].numberCompleted();
			return n;
		}
	}
	
	protected void doReset() {
		if (groupList != null)
			for (int i=0 ; i<groupList.length ; i++)
				groupList[i].resetList();
		
		super.doReset();
	}
	
	protected void doCompleteTable() {
		if (groupList != null)
			for (int i=0 ; i<groupList.length ; i++)
				groupList[i].completeTable();
		
		super.doCompleteTable();
	}


	public void notifyDataChange(DataView theView) {
		super.notifyDataChange(theView);
		if (groupList != null)
			for (int i=0 ; i<groupList.length ; i++)
				if (groupList[i] != theView)
					groupList[i].repaint();
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