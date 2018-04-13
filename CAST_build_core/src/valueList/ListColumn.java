package valueList;

import java.awt.*;
import dataView.*;


public class ListColumn {
	static final private String kRankString = "rank";
	
	protected DataSet data;
	protected String varKey;
	private int displayType;
	protected int columnWidth, columnOffset, titleWidth, maxDecimals;
	private boolean drawLeft;
	
	public ListColumn(DataSet data, String varKey, int displayType) {
		this.data = data;
		this.varKey = varKey;
		this.displayType = displayType;
	}
	
	protected int findWidth(Graphics g) {
		Variable v = (Variable)data.getVariable(varKey);
		FontMetrics fm = g.getFontMetrics();
		if (displayType == ScrollValueList.RAW_VALUE) {
			drawLeft = v instanceof NumVariable;
			if (drawLeft) {
				NumVariable y = (NumVariable)v;
				maxDecimals = y.getMaxDecimals();
				columnWidth = y.getMaxAlignedWidth(g, maxDecimals);
			}
			else
				columnWidth = v.getMaxWidth(g);
			
			titleWidth = fm.stringWidth(v.name);
		}
		else {
			int noOfCases = v.noOfValues();
			String maxRank = String.valueOf(noOfCases);
			columnWidth = g.getFontMetrics().stringWidth(maxRank);
			titleWidth = fm.stringWidth(kRankString);
			drawLeft = true;
		}
		
		if (titleWidth > columnWidth) {
			columnOffset = (titleWidth - columnWidth) / 2;
			columnWidth = titleWidth;
		}
		else
			columnOffset = 0;
		return columnWidth;
	}
	
	protected int getColumnWidth() {
		return columnWidth;
	}
	
	protected int getColumnOffset() {
		return columnOffset;
	}
	
	protected String getVarKey() {
		return varKey;
	}
	
	protected void drawValue(Graphics g, int index, int baseline, int columnLeft) {
		Variable v = (Variable)data.getVariable(varKey);
		if (displayType == ScrollValueList.RAW_VALUE) {
			if (v instanceof NumVariable) {
				int valueRight = columnLeft + columnWidth - columnOffset;
				NumValue val = (NumValue)v.valueAt(index);
				val.drawLeft(g, maxDecimals, valueRight, baseline);
			}
			else
				v.valueAt(index).drawRight(g, columnLeft + columnOffset, baseline);
		}
		else {
			NumVariable vy = ((NumVariable)v);
			int sortedIndex[] = vy.getSortedIndex();
			for (int i=0 ; i<sortedIndex.length ; i++)
				if (sortedIndex[i] == index) {
					int rankInt = (displayType == ScrollValueList.RANK) ? i + 1 : vy.noOfValues() - i;
					String rank = String.valueOf(rankInt);
					int rankWidth = g.getFontMetrics().stringWidth(rank);
					g.drawString(rank, columnLeft + columnWidth - columnOffset
																								- rankWidth, baseline);
					break;
				}
		}
	}
	
	protected void drawHeading(Graphics g, int baseline, int columnLeft) {
		if (displayType == ScrollValueList.RAW_VALUE) {
			Variable v = (Variable)data.getVariable(varKey);
			g.drawString(v.name, columnLeft + (columnWidth - titleWidth) / 2, baseline);
		}
		else
			g.drawString(kRankString, columnLeft + (columnWidth - titleWidth) / 2, baseline);
	}
	
	protected void drawTotal(Graphics g, int baseline, int columnLeft) {
		NumVariable v = (NumVariable)data.getVariable(varKey);
		ValueEnumeration ve = v.values();
		double total = 0.0;
		while (ve.hasMoreValues())
			total += ve.nextDouble();
		
		int valueRight = columnLeft + columnWidth - columnOffset;
		int decimals = v.getMaxDecimals();
		NumValue totalVal = new NumValue(total, decimals);
		totalVal.drawLeft(g, maxDecimals, valueRight, baseline);
	}
}