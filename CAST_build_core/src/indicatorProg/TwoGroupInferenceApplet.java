package indicatorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import curveInteract.*;


public class TwoGroupInferenceApplet extends CoreTwoGroupApplet {
	static final private String MAX_TABLE_ENTRIES_PARAM = "maxTableEntries";
	
	static final private Color kTableBackground = new Color(0xD6E1FF);
	
	static final private int kInteractionHierarchy[][] = {null, null, null, {1,2}};
	
	
	protected XPanel bottomPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel innerPanel = new InsetPanel(20, 7);
			innerPanel.setLayout(new BorderLayout(0, 0));

				StringTokenizer st = new StringTokenizer(getParameter(MAX_TABLE_ENTRIES_PARAM));
				NumValue maxParam = new NumValue(st.nextToken());
				NumValue maxSE = new NumValue(st.nextToken());
				NumValue maxT = new NumValue(st.nextToken());
				
				ParamTestsView testTable = new ParamTestsRemoveView(data, this, "ls", "y", paramName,
																			maxParam, maxSE, maxT, summaryData, getHierarchy());
				testTable.setShowT(true);
				testTable.setShowPValue(true);
				
			innerPanel.add("Center", testTable);
		
			innerPanel.lockBackground(kTableBackground);
		thePanel.add(innerPanel);
		
		return thePanel;
	}
	
	private int[][] getHierarchy() {
		if (hasInteraction)
			return kInteractionHierarchy;
		else
			return null;
	}
}