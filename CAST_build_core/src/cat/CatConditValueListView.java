package cat;

import dataView.*;


public class CatConditValueListView extends CatValueListView {
//	static public final String CAT_CONDIT_LIST = "catConditList";
	
	private String conditKey;
	private Value conditLabel;
	
	public CatConditValueListView(DataSet theData, XApplet applet, String catKey,
											CoreCreateTableView freqTableView, int nCols, String conditKey, Value conditLabel) {
		super(theData, applet, catKey, freqTableView, nCols);
		this.conditKey = conditKey;
		this.conditLabel = conditLabel;
	}
	
	protected ValueEnumeration getEnumeration(CatVariable catVar) {
		CatVariable conditVar = (CatVariable)getVariable(conditKey);
		return new ConditValueEnumeration(catVar, conditVar, conditLabel);
	}
	
	protected int noOfValues(CatVariable catVar) {
		CatVariable conditVar = (CatVariable)getVariable(conditKey);
		int counts[] = conditVar.getCounts();
		int conditIndex = conditVar.labelIndex(conditLabel);
		return counts[conditIndex];
	}
	
/*
	protected int translateHitIndex(int hitIndex) {
		CatVariable conditVar = (CatVariable)getVariable(conditKey);
		ValueEnumeration e = conditVar.values();
		int index = 0;
		int conditIndex = 0;
		Value l = e.nextValue();
		while (l != conditLabel || hitIndex != conditIndex) {
			if (l == conditLabel)
				conditIndex ++;
			l = e.nextValue();
			index ++;
		}
		return index;
	}
*/

}