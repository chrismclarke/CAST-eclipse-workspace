package exerciseNumGraph;

import java.awt.*;

import dataView.*;
import valueList.*;


public class ScrollStemLeafContent extends UsedValueContent {
//	static public final String SCROLL_STEM_LEAF = "scrollStemLeaf";
	
	static final private Color kLeafEdge = new Color(0xFFAAFF);
	static final private Color kLeafBackground = new Color(0xFFCCFF);
	
	private int decimals, stemPower;
	
	public ScrollStemLeafContent(DataSet theData, XApplet applet, ScrollValueList listPanel) {
		super(theData, applet, listPanel);
	}
	
	public void setLeafPosition(int decimals, int stemPower) {
		this.decimals = decimals;
		this.stemPower = stemPower;
	}
	
	protected void drawValues(Graphics g, Flags selection, int[] sortedIndex) {
		int valueRight = getColumnValueRight(0);
		
		StringBuffer sb = new StringBuffer();
		for (int i=0 ; i<decimals ; i++)
			sb.append('0');
		if (stemPower <= 0) {
			for (int i=0 ; i<-stemPower ; i++)
				sb.deleteCharAt(0);
		}
		else {
			sb.insert(0, '.');
			for (int i=0 ; i<stemPower ; i++)
				sb.insert(0, '0');
		}
		
		FontMetrics fm = g.getFontMetrics();
		int leafLeft = valueRight - fm.stringWidth(sb.toString());
		
		sb.deleteCharAt(0);
		int leafRight = valueRight - fm.stringWidth(sb.toString());
		
		g.setColor(kLeafBackground);
		g.fillRect(leafLeft - 1, 0, (leafRight - leafLeft + 1), getSize().height);
		g.setColor(kLeafEdge);
		g.drawLine(leafLeft - 1, 0, leafLeft - 1, getSize().height);
		g.drawLine(leafRight, 0, leafRight, getSize().height);
		
		super.drawValues(g, selection, sortedIndex);
	}
}
