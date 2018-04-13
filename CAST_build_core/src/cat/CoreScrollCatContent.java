package cat;

import java.awt.*;

import dataView.*;
import valueList.*;


abstract public class CoreScrollCatContent extends ScrollValueContent {
	static final private Color kBackgroundColor = new Color(0xCCCCCC);		//		pale grey
	static final private Color kDullYellowColor = new Color(0xCCCC66);
	
	static final private Color kUsedValueColor = new Color(0x999999);
	static final private Color kSelCatValueColor = Color.red;
	static final private Color kSelOtherValueColor = new Color(0xCC6666);
	static final private Color kUnusedCatValueColor = Color.black;
	static final private Color kUnusedOtherValueColor = new Color(0x666666);
	
	private DataSet data;
	
	protected CatSelection clickSelection;
	
	private Font boldFont;
	
	public CoreScrollCatContent(DataSet data, XApplet applet, ScrollValueList listPanel) {
		super(data, applet, listPanel);
		lockBackground(kBackgroundColor);
		this.data = data;
		resetList();
		boldFont = applet.getStandardBoldFont();
	}
	
	public void resetList() {
		CatVariable catVar = data.getCatVariable();
		int nVals = catVar.noOfValues();
		if (clickSelection == null)
			clickSelection = new CatSelection(nVals);
		else
			clickSelection.resetList(nVals);
		resetColumns();
		repaint();
	}
	
	public void completeTable() {
		clickSelection.completeList();
		
		repaint();
	}
	
	public int numberCompleted() {
		return clickSelection.numberCompleted();
	}
	
	public CatSelection getValuesClicked() {
		return clickSelection;
	}
	
	protected Color getSelectedRowColor() {
		return kDullYellowColor;
	}
	
	protected Color getSelectedColColor() {
		return Color.white;
	}
	
	protected Color getSelectedRowColColor() {
		return Color.yellow;
	}
	
	protected boolean isSelectedRow(Flags selection, int dataIndex) {
		return dataIndex == clickSelection.selectedVal;
	}
	
	protected Font setValueFormat(Graphics g, int rowIndex, boolean selectedCol) {
		Font oldFont = null;
		if (isSelectedRow(null, rowIndex)) {
			if (selectedCol) {
				g.setColor(kSelCatValueColor);
				oldFont = g.getFont();
				g.setFont(boldFont);
			}
			else
				g.setColor(kSelOtherValueColor);
		}
		else if (clickSelection.valueClicked[rowIndex])
			g.setColor(kUsedValueColor);
		else {
			if (selectedCol)
				g.setColor(kUnusedCatValueColor);
			else
				g.setColor(kUnusedOtherValueColor);
		}
		return oldFont;
	}
	
	protected int extraLineSpacing() {
		return 5;
	}
}
