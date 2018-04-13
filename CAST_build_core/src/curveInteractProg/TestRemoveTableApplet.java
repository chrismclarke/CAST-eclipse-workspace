package curveInteractProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import curveInteract.*;


public class TestRemoveTableApplet extends EstSeTableApplet {
	static final private String SHOW_VIF_PARAM = "showVIF";
	static final private String SHOW_TYPE3_SSQ_PARAM = "showType3Ssq";
	static final private String SHOW_P_TO_ADD_PARAM = "showPToAdd";
	static final private String ONLY_DELETE_ONE_PARAM = "onlyDeleteOne";
	
	protected boolean onlyDeleteOne;
	protected XButton removeAllButton, addAllButton;
	
	protected XPanel tablePanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new InsetPanel(20, 7);
		thePanel.setLayout(new BorderLayout(0, 0));

			String showVIFString = getParameter(SHOW_VIF_PARAM);
			boolean showVIF = (showVIFString != null) & showVIFString.equals("true");
			
			String showType3SsqString = getParameter(SHOW_TYPE3_SSQ_PARAM);
			boolean showType3Ssq = (showType3SsqString != null) && showType3SsqString.equals("true");
			
			String showPToAddString = getParameter(SHOW_P_TO_ADD_PARAM);
			boolean showPToAdd = (showPToAddString != null) && showPToAddString.equals("true");
			
			String onlyDeleteOneString = getParameter(ONLY_DELETE_ONE_PARAM);
			onlyDeleteOne = (onlyDeleteOneString != null) && onlyDeleteOneString.equals("true");
			
			StringTokenizer st = new StringTokenizer(getParameter(MAX_TABLE_ENTRIES_PARAM));
			NumValue maxParam = new NumValue(st.nextToken());
			NumValue maxSE = new NumValue(st.nextToken());
			NumValue maxT = new NumValue(st.nextToken());
			NumValue maxVIF = null;
			if (showVIF)
				maxVIF = new NumValue(st.nextToken());
			NumValue maxType3Ssq = null;
			if (showType3Ssq)
				maxType3Ssq = new NumValue(st.nextToken());
			
			testTable = new ParamTestsRemoveView(data, this, "ls", "y", paramName, maxParam, maxSE, maxT, summaryData);
			testTable.setShowT(!showType3Ssq);
			testTable.setShowPValue(true);
			if (showType3Ssq)
				testTable.setShowType3Ssq(true, maxType3Ssq);
			if (showVIF)
				testTable.setShowVIF(true, maxVIF);
			if (showPToAdd)
				testTable.setDisplayPValueToAdd(true);
			if (onlyDeleteOne)
				testTable.setCanOnlyDeleteOne(true);
			
		thePanel.add("Center", testTable);
		
//		thePanel.lockBackground(kTableBackground);
		return thePanel;
	}
	
	protected XPanel rightPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 40));
		
		if (!onlyDeleteOne) {
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 6));
			
				removeAllButton = new XButton(translate("Remove all variables"), this);
			buttonPanel.add(removeAllButton);
		
				addAllButton = new XButton(translate("Add all variables"), this);
			buttonPanel.add(addAllButton);
			
			thePanel.add(buttonPanel);
		}
		
		thePanel.add(ssqPanel(summaryData));
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == removeAllButton) {
			for (int i=0 ; i<xKeys.length ; i++)
				testTable.setConstraint(i + 1, true);
			testTable.repaint();
			return true;
		}
		else if (target == addAllButton) {
			for (int i=0 ; i<xKeys.length ; i++)
				testTable.setConstraint(i + 1, false);
			testTable.repaint();
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