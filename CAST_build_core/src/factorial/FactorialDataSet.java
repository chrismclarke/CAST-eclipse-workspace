package factorial;

import java.util.*;

import dataView.*;
import coreVariables.*;

public class FactorialDataSet extends DataSet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String X_KEYS_PARAM = "xKeys";
	static final private String CENTRE_POINTS_PARAM = "centrePoints";
	
	static final private String kXVarNameSuffix = "VarName";
	static final private String kXValuesSuffix = "Values";
	static final private String kXLabelsSuffix = "Labels";
	
	private String[][] termKeys;
	
	public FactorialDataSet(int maxInteraction, int[][] startModel, XApplet applet) {
		String yValueString = applet.getParameter(Y_VALUES_PARAM);
		if (yValueString != null)
			addNumVariable("y", applet.getParameter(Y_VAR_NAME_PARAM), yValueString);
		
		
		StringTokenizer st = new StringTokenizer(applet.getParameter(X_KEYS_PARAM));
		String[] mainEffectKeys = new String[st.countTokens()];
		int nMainEffects = 0;
		while (st.hasMoreTokens()) {
			String xKey = st.nextToken();
			addCatVariable(xKey, applet.getParameter(xKey + kXVarNameSuffix),
																						applet.getParameter(xKey + kXValuesSuffix),
																						applet.getParameter(xKey + kXLabelsSuffix));
			mainEffectKeys[nMainEffects ++] = xKey;
		}
		
		if (maxInteraction < 0)
			maxInteraction = mainEffectKeys.length;
		termKeys = new String[maxInteraction][];
		termKeys[0] = mainEffectKeys;
		
//		printStringArray("Main effect keys: ", termKeys[0]);
		
		for (int interactLevel=1 ; interactLevel<maxInteraction ; interactLevel++) {
			int nLowerLevel = termKeys[interactLevel - 1].length;
			termKeys[interactLevel] = new String[(nLowerLevel * (nMainEffects - interactLevel)) / (interactLevel + 1)];
//			System.out.println("No of interaction terms at level " + interactLevel + " is " + termKeys[interactLevel].length);
			
			
			int[] mainEffectIndex = new int[interactLevel + 1];
			int lastTermIndex = 0;
			for (int i=interactLevel ; i<nMainEffects ; i++) {
				mainEffectIndex[interactLevel] = i;
				String xiName = getVariable(termKeys[0][i]).name;
				lastTermIndex = addInteractionTerms(termKeys[0], mainEffectIndex,
																			interactLevel - 1, xiName,
																			termKeys[interactLevel], lastTermIndex);
//				printStringArray("InteractLevel " + interactLevel + ", highestIndex = " + i + ": ", termKeys[interactLevel]);
			}
		}
		if (startModel != null) {
			MultiFactorModel model;
			String centrePointsString = applet.getParameter(CENTRE_POINTS_PARAM);
			if (centrePointsString == null)
				model = new MultiFactorModel("Model", this, termKeys, startModel);
			else {
				st = new StringTokenizer(centrePointsString);
				int n = st.countTokens();
				double centreY[] = new double[n];
				for (int i=0 ; i<n ; i++)
					centreY[i] = Double.parseDouble(st.nextToken());
				model = new CentrePointFactorialModel("Model", this, termKeys, startModel, centreY);
			}
			addVariable("model", model);
		}
	}

/*	
	private void printStringArray(String title, String[] keys) {
		System.out.print(title);
		for (int i=0 ; i<keys.length ; i++)
			System.out.print(" " + keys[i]);
		System.out.print("\n");
	}
*/
	
	private int addInteractionTerms(String[] allMainEffectKeys, int[] mainEffectIndex,
												int interactLevel, String interactKeySuffix, String[] termKeys,
												int lastTermIndex) {
						//	assumes mainEffectIndex[] from (interactLevel + 1) up is fixed
						//	finds all combinations at level interactLevel and lower
		int highestFixedTerm = mainEffectIndex[interactLevel + 1];
		for (int i=interactLevel ; i<highestFixedTerm ; i++) {
			mainEffectIndex[interactLevel] = i;
			String xiName = getVariable(allMainEffectKeys[i]).name;
			if (interactLevel == 0) {
				String interactKey = xiName + " * " + interactKeySuffix;
//				printIntArray("Interactions for term " + interactKey + ": ", mainEffectIndex);
				boolean[] activeKey = new boolean[allMainEffectKeys.length];
				for (int j=0 ; j<mainEffectIndex.length ; j++)
					activeKey[mainEffectIndex[j]] = true;
				InteractionVariable interact = new InteractionVariable(interactKey, this,
																																allMainEffectKeys, activeKey);
				addVariable(interactKey, interact);
				termKeys[lastTermIndex ++] = interactKey;
			}
			else
				lastTermIndex = addInteractionTerms(allMainEffectKeys, mainEffectIndex,
															interactLevel - 1, xiName + " * " + interactKeySuffix, termKeys, lastTermIndex);
		}
		return lastTermIndex;
	}
	
	public String getMainEffectKey(int index) {
		return termKeys[0][index];
	}
}
