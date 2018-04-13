package catProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import cat.*;


public class TableSortApplet extends XApplet {
	static final private String DISPLAY_KEY_PARAM = "displayKey";
	static final private String SORT_KEY_PARAM = "sortKey";
	static final private String SORT_ORDER_PARAM = "sortOrder";
	
	static final private String kNameSuffix = "Name";
	static final private String kValueSuffix = "Values";
	static final private String kUnitsSuffix = "Units";
	
	static final private Color kDarkBlue =new Color(0x000099);
	
	private String[] displayKeys;
	private String[] sortKeys;
	private boolean[] biggestFirst;
	
	private int[] minShift;
	private int[] maxShift;
	
	private DataSet data;
	private SortTableView theTable;
	private XChoice sortChoice;
	private int currentSortIndex;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 10));
		
		add("Center", tablePanel(data));
		add("South", controlPanel(data));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
			LabelVariable labelVar = new LabelVariable(getParameter(LABEL_NAME_PARAM));
			labelVar.readValues(getParameter(LABELS_PARAM));
		data.addVariable("label", labelVar);
		
		StringTokenizer st = new StringTokenizer(getParameter(DISPLAY_KEY_PARAM));
		int nDisplay = st.countTokens();
		displayKeys = new String[nDisplay];
		minShift = maxShift = new int[nDisplay];		//	all zeros
		
		for (int i=0 ; i<nDisplay ; i++) {
			String key = st.nextToken();
			displayKeys[i] = key;
			data.addNumVariable(key, getParameter(key + kNameSuffix), getParameter(key + kValueSuffix));
		}
		
		st = new StringTokenizer(getParameter(SORT_KEY_PARAM));
		StringTokenizer st2 = new StringTokenizer(getParameter(SORT_ORDER_PARAM));
		int nSort = st.countTokens();
		sortKeys = new String[nSort];
		biggestFirst = new boolean[nSort];
		
		for (int i=0 ; i<nSort ; i++) {
			String key = st.nextToken();
			sortKeys[i] = key;
			biggestFirst[i] = Boolean.valueOf(st2.nextToken()).booleanValue();
			if (data.getVariable(key) == null)
				data.addNumVariable(key, getParameter(key + kNameSuffix), getParameter(key + kValueSuffix));
		}
		
		return data;
	}

	private XPanel tablePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			theTable = new SortTableView(data, this, "label", displayKeys, minShift, maxShift);
			theTable.setForeground(kDarkBlue);
			for (int i=0 ; i<displayKeys.length ; i++)
				theTable.setCustomUnits(i, getParameter(displayKeys[i] + kUnitsSuffix));
			theTable.setHasTotalRow(false);
			theTable.setFont(getBigFont());
			
		thePanel.add(theTable);
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
			
			XLabel sortLabel = new XLabel(translate("Sort rows by") + ":", XLabel.LEFT, this);
			sortLabel.setFont(getStandardBoldFont());
			
		thePanel.add(sortLabel);
			
			sortChoice = new XChoice(this);
			sortChoice.addItem(translate("Alphabetic order"));
			for (int i=0 ; i<sortKeys.length ; i++)
				sortChoice.addItem(data.getVariable(sortKeys[i]).name);
		thePanel.add(sortChoice);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == sortChoice) {
			int newChoice = sortChoice.getSelectedIndex();
			if (newChoice != currentSortIndex) {
				currentSortIndex = newChoice;
				
				if (newChoice == 0)
					theTable.setSortIndex(null, false);
				else {
					NumVariable sortVar = (NumVariable)data.getVariable(sortKeys[newChoice - 1]);
					int[] sortIndex = sortVar.getSortedIndex();
					theTable.setSortIndex(sortIndex, biggestFirst[newChoice - 1]);
				}
				theTable.repaint();
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