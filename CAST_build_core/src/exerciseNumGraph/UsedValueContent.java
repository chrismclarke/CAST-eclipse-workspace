package exerciseNumGraph;

import java.awt.*;

import dataView.*;
import valueList.*;


public class UsedValueContent extends ScrollValueContent {
	static final private Color kUnusedTextColor = Color.black;
	static final private Color kUsedTextColor = new Color(0x999999);
	static final private Color kSelectedTextColor = Color.red;
	
	private int numberToDrag = -1;
	private boolean[] alreadyUsed;
	
	public UsedValueContent(DataSet theData, XApplet applet, ScrollValueList listPanel) {
		super(theData, applet, listPanel);
	}
	
	public void setAlreadyUsed(boolean[] alreadyUsed) {
		this.alreadyUsed = alreadyUsed;
		resetColumns();
	}
	
	protected Color getRowTextColor(int dataIndex, boolean selectedRow) {
		if (selectedRow)
			return kSelectedTextColor;
		else if (numberToDrag >= 0) {
			int dragIndex = dataIndex - alreadyUsed.length + numberToDrag;
			if (dragIndex < 0)
				return kUsedTextColor;
			else
				return FinishStackedCrossView.getDragColor(dragIndex);
		}
		else if (alreadyUsed == null || !alreadyUsed[dataIndex])
			return kUnusedTextColor;
		else
			return kUsedTextColor;
	}
	
	public void setDragCount(int numberToDrag) {
		this.numberToDrag = numberToDrag;
	}
	
	protected void drawDataRow(Graphics g, boolean selected, int dataIndex, int baseline) {
		boolean drawBold = false;
		if (numberToDrag >= 0) {
			int dragIndex = dataIndex - alreadyUsed.length + numberToDrag;
			drawBold = dragIndex >= 0;
		}
		Font stdFont = g.getFont();
		if (drawBold)
			g.setFont(new Font(stdFont.getName(), Font.BOLD, stdFont.getSize()));
		drawDataRow(g, selected, dataIndex, baseline, getRowTextColor(dataIndex, selected));
		if (drawBold)
			g.setFont(stdFont);
	}
}
