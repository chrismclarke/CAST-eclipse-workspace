package glmAnovaProg;

import java.util.*;

import dataView.*;
import utils.*;

import glmAnova.*;


public class CombineNestedApplet extends CombineSsqApplet {
	static final private String DENOM_INDEX_PARAM = "denomIndex";
													//		space-separated list of indices
													//		each is index of denominator for F test of component
													//		0 = total (never use), nVars + 1 = residual
		
	protected XPanel dataDisplayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 0));
			
			StringTokenizer st = new StringTokenizer(getParameter(DENOM_INDEX_PARAM));
			int denomIndex[] = new int[st.countTokens()];
			for (int i=0 ; i<denomIndex.length ; i++)
				denomIndex[i] = Integer.parseInt(st.nextToken());
			
			table = new AnovaNestedView(data, this, componentKeys, maxSsq, componentName,
																										componentColor, variableName, denomIndex);
			table.setShowFArrows(true);
			table.setFont(getBigFont());
			if (showTests)
				table.setShowTests(true, maxMsq, maxF);
			
		thePanel.add(table);
		return thePanel;
	}
}