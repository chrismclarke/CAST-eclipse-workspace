package test;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class DotCumulativeView extends DotPlotView {
//	private static final int kMaxJitter = 50;
	private static final int kLeftBorder = 30;
	private static final int kVerticalGap = 30;
	private static final int kHeadingGap = 3;
	private static final int kTickLength = 5;
	private static final int kTickValueGap = 2;
	
//	static public final String DOT_CUMULATIVE_PLOT = "dotCumulative";
	
	private boolean showCumulative;
	
	public DotCumulativeView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																															boolean showCumulative) {
		super(theData, applet, theAxis, 1.0);
		getViewBorder().left = kLeftBorder;
		this.showCumulative = showCumulative;
	}
	
	public void paintView(Graphics g) {
		g.setColor(Color.white);
		
		int ascent = g.getFontMetrics().getAscent();
		int descent = g.getFontMetrics().getDescent();
		
		int topBorder = ascent + descent + kHeadingGap + (ascent + descent) / 2;
		int jitteredHeight = currentJitter + 2 * getViewBorder().bottom;
		int cumulativeHeight = getSize().height - jitteredHeight - kVerticalGap - topBorder;
		
		g.fillRect(kLeftBorder - 5, topBorder + cumulativeHeight + kVerticalGap, getSize().width - kLeftBorder + 5, jitteredHeight);
		if (showCumulative) {
			g.fillRect(kLeftBorder, topBorder, getSize().width - kLeftBorder, cumulativeHeight);
			
			g.setColor(Color.black);
			int zeroVert = topBorder + cumulativeHeight - 1;
			int oneVert = topBorder;
			g.drawLine(kLeftBorder - 1, oneVert, kLeftBorder - 1, zeroVert);
			g.drawLine(kLeftBorder - kTickLength, oneVert, kLeftBorder - 1, oneVert);
			g.drawLine(kLeftBorder - kTickLength, zeroVert, kLeftBorder - 1, zeroVert);
			
			int digitLeft = kLeftBorder - kTickLength - kTickValueGap - g.getFontMetrics().stringWidth("0");
			g.drawString("1", digitLeft, oneVert + (ascent - descent) / 2);
			g.drawString("0", digitLeft, zeroVert + (ascent - descent) / 2);
			
			g.setColor(Color.lightGray);
			int zeroHoriz = axis.numValToRawPosition(0.0) + getViewBorder().left;
			int oneHoriz = axis.numValToRawPosition(1.0) + getViewBorder().left;
			g.drawLine(zeroHoriz, zeroVert, oneHoriz, oneVert);
			
			g.setColor(Color.red);
			g.drawString(getApplet().translate("Proportion less than p"), 0, ascent);
			
			NumValue[] sorted = getNumVariable().getSortedData();
			if (sorted != null && sorted.length > 0) {
//				int cum = 0;
				int horizPos = zeroHoriz;
				int vertPos = zeroVert;
				for (int i=0 ; i<sorted.length ; i++) {
					int nextHoriz = axis.numValToRawPosition(sorted[i].toDouble()) + getViewBorder().left;
					int nextVert = zeroVert + (i + 1) * (oneVert - zeroVert) / sorted.length;
					g.drawLine(horizPos, vertPos, nextHoriz, vertPos);
					g.drawLine(nextHoriz, vertPos, nextHoriz, nextVert);
					horizPos = nextHoriz;
					vertPos = nextVert;
				}
				g.drawLine(horizPos, oneVert, oneHoriz, oneVert);
			}
		}
		g.setColor(getForeground());
		
		super.paintView(g);
	}
}
	
