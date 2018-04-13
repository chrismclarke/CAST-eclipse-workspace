package glmAnovaProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import glmAnova.*;


public class InterblockAnovaApplet extends XApplet {
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String DENOM_INDEX_PARAM = "denomIndex";
	
	private IncompleteBlockDataSet data;
	private NumValue maxSsq, maxMsq, maxF;
	@SuppressWarnings("unused")
	private int maxDF;
	
	private AnovaCombineTableView anovaTable;
	
	private XCheckbox splitBlocksCheck, splitUnitsCheck;
	
	public void setupApplet() {
		readMaxSsqs();
		
		data = readData();
		
		setLayout(new BorderLayout(0, 10));
			
		add("Center", anovaPanel(data));
		
		add("South", controlPanel());
	}
	
	protected void readMaxSsqs() {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SSQ_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxDF = Integer.parseInt(st.nextToken());
		maxMsq = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
	}
	
	private IncompleteBlockDataSet readData() {
		IncompleteBlockDataSet data = new IncompleteBlockDataSet(this);
		data.addSsqComponents(this);
		return data;
	}
	
	private XPanel anovaPanel(IncompleteBlockDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			String componentKey[] = data.getComponentKeys();
			String[] componentName = data.getComponentNames();
			Color[] componentColor = data.getComponentColors();
			
			StringTokenizer st = new StringTokenizer(getParameter(DENOM_INDEX_PARAM));
			int denomIndex[] = new int[st.countTokens()];
			for (int i=0 ; i<denomIndex.length ; i++)
				denomIndex[i] = Integer.parseInt(st.nextToken());
			
			anovaTable = new AnovaNestedView(data, this, componentKey, maxSsq,
																							componentName, componentColor, null, denomIndex);
			anovaTable.setShowTests(true, maxMsq, maxF);
			anovaTable.setShowFArrows(true);
			anovaTable.setFont(getBigFont());
		
		thePanel.add("Center", anovaTable);
		
		return thePanel;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			splitBlocksCheck = new XCheckbox("Split blocks", this);
		thePanel.add(splitBlocksCheck);
			anovaTable.immediateGroup(1, 2, "Between blocks");
		
			splitUnitsCheck = new XCheckbox("Split units", this);
		thePanel.add(splitUnitsCheck);
			anovaTable.immediateGroup(3, 4, "Between units");
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == splitBlocksCheck) {
			if (splitBlocksCheck.getState())
				anovaTable.animateSplitting(1, 2, data.getComponentNames(), data.getComponentColors());
			else
				anovaTable.animateGrouping(1, 2, "Between blocks");
			return true;
		}
		else if (target == splitUnitsCheck) {
			if (splitUnitsCheck.getState())
				anovaTable.animateSplitting(3, 4, data.getComponentNames(), data.getComponentColors());
			else
				anovaTable.animateGrouping(3, 4, "Between units");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}

}