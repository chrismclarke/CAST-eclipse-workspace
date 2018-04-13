package exerciseCateg;

import dataView.*;
import valueList.*;

import cat.*;


public class ScrollCatValue2Content extends CoreScrollCatContent {
//	static public final String CAT_SCROLL2_LIST = "catScrollList2";
	
	public ScrollCatValue2Content(DataSet theData, XApplet applet, ScrollValueList listPanel) {
		super(theData, applet, listPanel);
	}
	
	public void resetList() {
		super.resetList();
		
		clickSelection.selectedVal = 0;
	}
	
	public void selectNextValue() {
		clickSelection.selectNext();
		repaint();
	}
	
	public void selectPreviousValue() {
		if (clickSelection.unselect())
			repaint();
	}
	
	public int getSelectedIndex() {
		return clickSelection.selectedVal;
	}

//------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
