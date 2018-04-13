package ssq;

import dataView.*;
import models.*;


public class AnovaTableView extends CoreAnovaTableView {
//	static final public String ANOVA_TABLE = "anovaTable";
	
	private String componentKey[];		//	first is total, last is residual
	
	public AnovaTableView(DataSet theData, XApplet applet,
							String[] componentKey, NumValue maxSsq, NumValue maxMsq, NumValue maxF,
							int tableDisplayType) {
		super(theData, applet, maxSsq, maxMsq, maxF, tableDisplayType);
		this.componentKey = componentKey;
	}

//-----------------------------------------------------------------------------------
	
	protected int countAnovaComponents() {
		return componentKey.length;
	}
	
	protected boolean canDrawTable() {
		int selectedIndex = getData().getSelection().findSingleSetFlag();
		return selectedIndex >= 0;
	}
	
	protected void findComponentSsqs(NumValue[] ssq, NumValue[] df) {
																								//	first is total, last is residual
		int selectedIndex = getData().getSelection().findSingleSetFlag();
	
		for (int i=0 ; i<componentKey.length ; i++) {
			SsqVariable comp = (SsqVariable)getVariable(componentKey[i]);
			ssq[i] = (NumValue)comp.valueAt(selectedIndex);
			if (df != null)
				df[i] = new NumValue(comp.getDF(), 0);
		}
	}

}