package pairBlock;

import dataView.*;
import models.*;


public class RemoveBlockDataSet extends GroupsDataSet {
	static final private String BLOCK_VAR_NAME_PARAM = "blockVarName";
	static final private String BLOCK_VALUES_PARAM = "blockValues";
	static final private String BLOCK_LABELS_PARAM = "blockLabels";
	
	static private int treatToCenter;
	
	static XApplet sideEffect(XApplet applet, int paramTreatToCenter) {
									//	Messy messy way to get initialisation parameter to addAdjustedVariable()
		treatToCenter = paramTreatToCenter;
		return applet;
	}
	
	public RemoveBlockDataSet(XApplet applet, int treatToCenter) {
		super(sideEffect(applet, treatToCenter));
	}
	
	protected void addAdjustedVariable(String newYKey, double initialR2, String rawYKey,
																							XApplet applet) {
		addCatVariable("block", applet.getParameter(BLOCK_VAR_NAME_PARAM),
						applet.getParameter(BLOCK_VALUES_PARAM), applet.getParameter(BLOCK_LABELS_PARAM));
		
		NumVariable rawY = (NumVariable)getVariable(rawYKey);
		RemoveBlockVariable adjY = new RemoveBlockVariable(rawY.name, this, rawYKey,
																														"block", "x", treatToCenter);
																							// ignores initialR2
		addVariable(newYKey, adjY);
	}
}