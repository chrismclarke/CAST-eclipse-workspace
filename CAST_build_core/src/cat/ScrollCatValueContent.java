package cat;

import dataView.*;
import valueList.*;


public class ScrollCatValueContent extends CoreScrollCatContent {
//	static public final String CAT_SCROLL_LIST = "catScrollList";
	
	private CoreCreateTableView freqTableView;
	
	public ScrollCatValueContent(DataSet theData, XApplet applet, ScrollValueList listPanel) {
		super(theData, applet, listPanel);
	}
	
	public void setLinkedFreqTable(CoreCreateTableView freqTableView) {
		this.freqTableView = freqTableView;
	}
	
	public void resetList() {
		super.resetList();
		
		if (freqTableView != null)
			freqTableView.clearCounts();
	}
	
	public void completeTable() {
		super.completeTable();
		
		freqTableView.completeCounts();
	}
	
	private void selectValue(int i) {
		clickSelection.selectIndex(i);
		repaint();
		
		freqTableView.addCatValue(i);
	}

//------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		IndexPosInfo indexPos = (IndexPosInfo)super.getPosition(x, y);
		if (indexPos == null || clickSelection.valueClicked[indexPos.itemIndex])
			return null;
		else
			return indexPos;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		IndexPosInfo indexPos = (IndexPosInfo)startInfo;
		selectValue(indexPos.itemIndex);
		
		getApplet().notifyDataChange(this);
		
		return false;
	}
}
