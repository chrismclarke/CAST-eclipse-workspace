package glmAnovaProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import glmAnova.*;


public class CombineSsqApplet extends AnovaTableSeqApplet {
	static final protected String COMPONENT_NAMES_PARAM = "componentNames";
	static final protected String COMBINE_PARAM = "combine";
	static final protected String SPLIT_PARAM = "split";
	
	static final private Color kComponentGroupColor[] = {new Color(0x660066), new Color(0x006600), new Color(0x993300), new Color(0x6633CC)};
	
	protected AnovaCombineTableView table;
	
	private XCheckbox combineCheck[];
	private int combineStart[];
	private int combineCount[];
	private String combinedComponentName[];
	
	private boolean combineNotSplit = true;		//	if false, checkboxes split instead of combine
	
	public void setupApplet() {
		readMaxSsqs();
		
		data = readData();
		
		setLayout(new BorderLayout(10, 0));
				
		add("Center", dataDisplayPanel(data));
		
		add("South", controlPanel());
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		StringTokenizer st = new StringTokenizer(getParameter(COMPONENT_NAMES_PARAM), "#");
		for (int i=1 ; i<componentName.length-1 ; i++)
			componentName[i] = st.nextToken();
		
		String combineSplitString = getParameter(COMBINE_PARAM);
		if (combineSplitString == null) {
			combineSplitString = getParameter(SPLIT_PARAM);
			combineNotSplit = false;
		}
		
		st = new StringTokenizer(combineSplitString, "#");
		int nCombines = st.countTokens();
		combineCheck = new XCheckbox[nCombines];
		combineStart = new int[nCombines];
		combineCount = new int[nCombines];
		combinedComponentName = new String[nCombines];
		for (int i=0 ; i<nCombines ; i++) {
			StringTokenizer st2 = new StringTokenizer(st.nextToken(), ":");
			combineStart[i] = Integer.parseInt(st2.nextToken());
			combineCount[i] = Integer.parseInt(st2.nextToken());
			combineCheck[i] = new XCheckbox(st2.nextToken(), this);
			combinedComponentName[i] = st2.nextToken();
//			combineCheck[i].setState(st2.hasMoreTokens() && st2.nextToken().equals("initCombined"));
			
			for (int j=0 ; j<combineCount[i] ; j++)
				componentColor[combineStart[i] + j] = kComponentGroupColor[i];
		}
		
		return data;
	}
	
	protected XPanel dataDisplayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 0));
		
			table = new AnovaCombineTableView(data, this, componentKeys, maxSsq, componentName,
																													componentColor, variableName);
			table.setFont(getBigFont());
			if (showTests)
				table.setShowTests(true, maxMsq, maxF);
			
		thePanel.add(table);
		return thePanel;
	}
	
	protected XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
		
		for (int i=0 ; i<combineCheck.length ; i++) {
			thePanel.add(combineCheck[i]);
			if (!combineNotSplit) {
				int lastIndex = combineStart[i] + combineCount[i] - 1;
				int firstIndex = combineStart[i];
				table.immediateGroup(firstIndex, lastIndex, combinedComponentName[i]);
			}
		}
		return thePanel;
	}
	
	private void combineSsq(int checkIndex, boolean isCombined) {
		int lastIndex = combineStart[checkIndex] + combineCount[checkIndex] - 1;
		int firstIndex = combineStart[checkIndex];
		if (isCombined)
			table.animateGrouping(firstIndex, lastIndex, combinedComponentName[checkIndex]);
		else
			table.animateSplitting(firstIndex, lastIndex, componentName, componentColor);
	}
	
	protected void frameChanged(DataView theView) {
		for (int i=0 ; i<combineCheck.length ; i++)
			combineCheck[i].setEnabled(table.getCurrentFrame() == 0);
	}
	
	private boolean localAction(Object target) {
		for (int i=0 ; i<combineCheck.length ; i++)
			if (target == combineCheck[i]) {
				combineSsq(i, combineCheck[i].getState() == combineNotSplit);
				return true;
			}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}