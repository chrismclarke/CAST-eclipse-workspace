package glmAnova;

import java.awt.*;
import java.util.*;

import dataView.*;
import models.*;


public class IncompleteBlockDataSet extends DataSet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String BLOCK_VAR_NAME_PARAM = "blockVarName";
	static final private String BLOCK_VALUES_PARAM = "blockValues";
	static final private String BLOCK_LABELS_PARAM = "blockLabels";
	static final private String FACTOR_VAR_NAME_PARAM = "factorVarName";
	static final private String FACTOR_VALUES_PARAM = "factorValues";
	static final private String FACTOR_LABELS_PARAM = "factorLabels";
	
	static final private String DECIMALS_PARAM = "decimals";
	static final private String COMPONENT_NAMES_PARAM = "componentNames";
	
	static final private String componentKeys[] = {"totalComp", "interComp", "blockComp", "intraComp", "residComp"};
	static final private String componentName[] = {"Total", null, null, null, "Residual"};
	static final private Color componentColor[] = {Color.black, Color.blue, Color.blue, Color.red, Color.red};
	
	static final private String[] kFactorKeys = {"factor"};
	static final private String[] kBlockKeys = {"block"};
	static final public String[] kIntraKeys = {"block", "factor"};
	
	private String[] interKeys;
	
	public IncompleteBlockDataSet(XApplet applet) {
		addNumVariable("y", applet.getParameter(Y_VAR_NAME_PARAM), applet.getParameter(Y_VALUES_PARAM));
		
		addCatVariable("block", applet.getParameter(BLOCK_VAR_NAME_PARAM),
												applet.getParameter(BLOCK_VALUES_PARAM), applet.getParameter(BLOCK_LABELS_PARAM));
		
			CatVariable factor = new CatVariable(applet.getParameter(FACTOR_VAR_NAME_PARAM));
			factor.readLabels(applet.getParameter(FACTOR_LABELS_PARAM));
			factor.readValues(applet.getParameter(FACTOR_VALUES_PARAM));
		addVariable("factor", factor);
		
		InterBlockDummyVariable.addDummyVariables(this, "factor", "block");
		
			int decimals = Integer.parseInt(applet.getParameter(DECIMALS_PARAM));
			
			int nLevels = factor.noOfCategories();
			interKeys = new String[nLevels - 1];
			for (int i=1 ; i<nLevels ; i++)
				interKeys[i - 1] = "interDummy" + i;
		addLsAndFit("inter", "Inter-block", interKeys, decimals);
		
		addLsAndFit("block", "Block", kBlockKeys, decimals);
		
		addLsAndFit("factor", "Factor", kFactorKeys, decimals);
		
		addLsAndFit("intra", "Intra-block", kIntraKeys, decimals);
	}
	
	public void addSsqComponents(XApplet applet) {
			StringTokenizer st = new StringTokenizer(applet.getParameter(COMPONENT_NAMES_PARAM), "#");
			for (int i=1 ; i<4 ; i++)
				componentName[i] = st.nextToken();
		
		addVariable(componentKeys[0], new BasicComponentVariable(componentName[0], this,
														kBlockKeys, "y", "blockLS", BasicComponentVariable.TOTAL, 9));
		
		addVariable(componentKeys[4], new BasicComponentVariable(componentName[4], this,
														kIntraKeys, "y", "intraLS", BasicComponentVariable.RESIDUAL, 9));
		
		addVariable(componentKeys[1], new BasicComponentVariable(componentName[1], this,
														interKeys, "y", "interLS", BasicComponentVariable.EXPLAINED, 9));
		
			SeqComponentVariable interComp = new SeqComponentVariable(componentName[2], this,
																													"blockFit", "interFit", 9);
		addVariable(componentKeys[2], interComp);
		
		addVariable(componentKeys[3], new SeqComponentVariable(componentName[3], this,
																													"intraFit", "blockFit", 9));
	}
	
	private void addLsAndFit(String prefix, String name, String[] xKeys, int decimals) {
			MultipleRegnModel ls = new MultipleRegnModel(name + " LS", this, xKeys);
			ls.setParameterDecimals(decimals);
			ls.updateLSParams("y");
		addVariable(prefix + "LS", ls);
		
			FittedValueVariable fit = new FittedValueVariable(name + " fit", this, xKeys,
																																prefix + "LS", 9);
		addVariable(prefix + "Fit", fit);
	}
	
	public String[] getComponentKeys() {
		return componentKeys;
	}
	
	public String[] getComponentNames() {
		return componentName;
	}
	
	public Color[] getComponentColors() {
		return componentColor;
	}

}