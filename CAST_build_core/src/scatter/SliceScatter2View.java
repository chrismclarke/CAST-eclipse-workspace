package scatter;

import java.awt.*;

import dataView.*;
import axis.*;


public class SliceScatter2View extends SliceScatterView {
//	static public final String SLICE2_SCATTER = "slice2Scatter";
	
	static final private int kProbBarWidth = 10;
	static final private int kProbBarBorder = 5;
	static final private int kArrowHeadSize = 7;
	static final private int kArrowWidth = 14;
	static final private int kArrowBodyHeight = 6;
	
	static final private int arrowX[] = {0, kArrowHeadSize, kArrowHeadSize, kArrowWidth,
															kArrowWidth, kArrowHeadSize, kArrowHeadSize, 0};
	private int arrowY[] = new int[8];
	
	private VertAxis probAxis;
	
	public SliceScatter2View(DataSet theData, XApplet applet,
									HorizAxis xAxis, VertAxis yAxis, VertAxis probAxis, String xKey,
									String yKey, double selectRange) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, selectRange,
											kArrowWidth + 5, kProbBarWidth + kProbBarBorder * 2);
		this.probAxis = probAxis;
	}
	
	protected void drawScatterBorders(Graphics g, NumVariable yVariable, NumVariable xVariable) {
		ValueEnumeration ye = yVariable.values();
		FlagEnumeration fe = getSelection().getEnumeration();
		double sum = 0.0;
		int n = 0;
		int zero = 0;
		while (ye.hasMoreValues()) {
			double nextY = ye.nextDouble();
			boolean nextSel = fe.nextFlag();
			if (!selected || nextSel) {
				if (nextY > 0.0)
					sum += nextY;
				else
					zero ++;
				n ++;
			}
		}
		
		if (n - zero > 0)
			try {
				int meanY = yAxis.numValToPosition(sum / (n - zero));
				Point meanPos = translateToScreen(0, meanY, null);
				
				arrowY[0] = arrowY[7] = meanPos.y;
				arrowY[1] = meanPos.y - kArrowHeadSize;
				arrowY[6] = meanPos.y + kArrowHeadSize;
				arrowY[2] = arrowY[3] = meanPos.y - kArrowBodyHeight / 2;
				arrowY[4] = arrowY[5] = meanPos.y + kArrowBodyHeight / 2;
				
				g.setColor(selected ? Color.red : kPaleGray);
				g.fillPolygon(arrowX, arrowY, 8);
				g.drawPolygon(arrowX, arrowY, 8);
			} catch (AxisException ex) {
			}
		
		if (n > 0)
			try {
				int zeroY = probAxis.numValToPosition(0.0);
				Point zeroPos = translateToScreen(0, zeroY, null);
				int oneY = probAxis.numValToPosition(1.0);
				Point onePos = translateToScreen(0, oneY, null);
				
				int probY = probAxis.numValToPosition(zero / (double)n);
				Point probPos = translateToScreen(0, probY, null);
				if (selected) {
					g.setColor(Color.red);
					g.fillRect(getSize().width - kProbBarWidth - kProbBarBorder, onePos.y, kProbBarWidth, probPos.y - onePos.y);
				}
				g.setColor(selected ? Color.blue : kPaleGray);
				g.fillRect(getSize().width - kProbBarWidth - kProbBarBorder, probPos.y, kProbBarWidth, zeroPos.y - probPos.y);
			} catch (AxisException ex) {
			}
	}
}
	
