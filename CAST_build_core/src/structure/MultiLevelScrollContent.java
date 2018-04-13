package structure;

import java.awt.*;

import dataView.*;
import valueList.*;

public class MultiLevelScrollContent extends ScrollValueContent {
	static final private Color kPaleGrey = new Color(0xE2E2E2);
	
	public MultiLevelScrollContent(DataSet theData, XApplet applet, ScrollValueList listPanel) {
		super(theData, applet, listPanel);
		setRetainLastSelection(true);
	}
	
	protected void drawDataRow(Graphics g, boolean selected, int dataIndex, int baseline) {
		MultiLevelDataSet multiData = (MultiLevelDataSet)getData();
		int higherIndex[] = multiData.getHigherIndex();
		
		int highlightIndex = (higherIndex == null) ? dataIndex : higherIndex[dataIndex];
		
		if (highlightIndex % 2 == 1) {
			g.setColor(kPaleGrey);
			g.fillRect(2, baseline - baselineOffset, getSize().width - 3, lineHt);
			g.setColor(getForeground());
		}
		
		super.drawDataRow(g, selected, dataIndex, baseline);
	}
	

//-----------------------------------------------------------------------------------

	
	protected PositionInfo getPosition(int x, int y) {
		IndexPosInfo hitIndexPos = (IndexPosInfo)super.getPosition(x, y);
		
		if (hitIndexPos == null)
			return null;
		
		MultiLevelDataSet multiData = (MultiLevelDataSet)getData();
		int higherIndex[] = multiData.getHigherIndex();
		if (higherIndex == null)
			return hitIndexPos;
		
		int index = hitIndexPos.itemIndex;
		int highInd = higherIndex[index];
		
		while (index >= 0 && higherIndex[index] == highInd)
			index --;
		hitIndexPos.itemIndex = index + 1;
		
		return hitIndexPos;
	}
	
}
