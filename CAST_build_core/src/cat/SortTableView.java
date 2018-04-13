package cat;

import java.awt.*;

import dataView.*;


public class SortTableView extends TableValuesView {
//	static final public String SORT_TABLE_VIEW = "sortTableView";
	
	public static final int kEndFrame = 100;
	public static final int kFramesPerSec = 30;
	
	private int[] startRowIndex, endRowIndex;
	
	public SortTableView(DataSet theData, XApplet applet, String labelKey, String[] yKey,
																																int[] minShift, int[] maxShift) {
		super(theData, applet, labelKey, yKey, minShift, maxShift);
	}
	
	protected void doInitialisation(Graphics g) {
		super.doInitialisation(g);
		startRowIndex = new int[nRows];
		endRowIndex = new int[nRows];
		for (int i=0 ; i<nRows ; i++)
			startRowIndex[i] = endRowIndex[i] = i;
	}
	
	public void setSortIndex(int[] sortIndex, boolean biggestFirst) {
		int[] temp = startRowIndex;
		startRowIndex = endRowIndex;
		endRowIndex = temp;
		if (sortIndex == null)
			for (int i=0 ; i<nRows ; i++)
				endRowIndex[i] = i;
		else
			for (int i=0 ; i<nRows ; i++)
				endRowIndex[sortIndex[i]] = biggestFirst ? (nRows - i - 1) : i;
		animateFrames(0, kEndFrame, kFramesPerSec, null);
	}
	
	protected void drawLabels(Graphics g, Variable labelVar, int topBaseline, Font boldFont) {
		int lineHeight = NumImageValue.kDigitAscent + NumImageValue.kDigitDescent + kRowSpacing;
		for (int row=0 ; row<nRows ; row++) {
			double framePropn = getCurrentFrame() / (double)kEndFrame;
			int startBaseline = topBaseline + startRowIndex[row] * lineHeight;
			int endBaseline = topBaseline + endRowIndex[row] * lineHeight;
			int baseline = (int)Math.round(endBaseline * framePropn + startBaseline * (1 - framePropn));
			
			labelVar.valueAt(row).drawRight(g, 0, baseline);
		}
	}
	
	protected void drawValues(Graphics g, Variable yVar, int lineLeft, int lineRight, int topBaseline,
																																	int yValueRight, Font boldFont) {
		int lineHeight = NumImageValue.kDigitAscent + NumImageValue.kDigitDescent + kRowSpacing;
		for (int row=0 ; row<nRows ; row++) {
			double framePropn = getCurrentFrame() / (double)kEndFrame;
			int startBaseline = topBaseline + startRowIndex[row] * lineHeight;
			int endBaseline = topBaseline + endRowIndex[row] * lineHeight;
			int baseline = (int)Math.round(endBaseline * framePropn + startBaseline * (1 - framePropn));
			
			yVar.valueAt(row).drawLeft(g, yValueRight, baseline);
		}
	}
	
//-----------------------------------------------------------------------------------
	
	protected boolean canDrag() {
		return false;
	}
}