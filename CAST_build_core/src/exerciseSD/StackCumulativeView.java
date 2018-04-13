package exerciseSD;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import utils.*;
import exercise2.*;


public class StackCumulativeView extends StackedDotPlotView implements StatusInterface {
//	static public final String STACK_CUMULATIVE = "StackCumulative";
	
	static final private Color kHiliteBackground = new Color(0xFFEEEE);
	static final private Color kUnderColor = new Color(0xDDBBBB);
	static final private Color kOverColor = new Color(0xCCCCCC);
	
	static final private int kHitSlop = 4;
	static final private int kRefTopBottomBorder = 3;
	static final private int kRefLeftRightBorder = 10;
	
	private NumValue cutoff;
	
	private int hitOffset;
	private boolean doingDrag = false;
	
	public StackCumulativeView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																											String yKey, int decimals) {
		super(theData, applet, theAxis, null, false);
		setActiveNumVariable(yKey);
		cutoff = new NumValue(0.0, decimals);
		setCutoff((theAxis.minOnAxis + theAxis.maxOnAxis) / 2);
	}
	
	public void setDecimals(int decimals) {
		cutoff.decimals = decimals;
	}

//-------------------------------------------------------------------

	public String getStatus() {
		return cutoff.toString();
	}
	
	public void setStatus(String status) {
		cutoff = new NumValue(status);
		getData().setSelection(getActiveNumKey(), Double.NEGATIVE_INFINITY, cutoff.toDouble());
		repaint();
	}

//-------------------------------------------------------------------
	
	public int countUnder(double cutoff) {
		ValueEnumeration ye = getNumVariable().values();
		int nUnder = 0;
		while (ye.hasMoreValues())
			if (ye.nextDouble() <= cutoff)
				nUnder ++;
		return nUnder;
	}
	
	public void setCutoff(double ref) {
		double factor = Math.pow(10.0, cutoff.decimals);
		double newRef = Math.rint(ref * factor) / factor;
		cutoff.setValue(newRef);
		getData().setSelection(getActiveNumKey(), Double.NEGATIVE_INFINITY, newRef);
	}
	
	protected void paintBackground(Graphics g) {
		int refHorizPos = axis.numValToRawPosition(cutoff.toDouble());
		int refHoriz = translateToScreen(refHorizPos, 0, null).x;
		int ascent = g.getFontMetrics().getAscent();
		
		g.setColor(kHiliteBackground);
		g.fillRect(0, 0, refHoriz, getSize().height);
		
		int lineTop = ascent + 2 * kRefTopBottomBorder;
		
		if (doingDrag) {
			g.setColor(getForeground());
			g.drawLine(refHoriz, lineTop, refHoriz, getSize().height);
			g.setColor(Color.red);
			g.drawLine(refHoriz - 1, lineTop, refHoriz - 1, getSize().height);
			g.drawLine(refHoriz + 1, lineTop, refHoriz + 1, getSize().height);
		}
		else {
			g.setColor(Color.red);
			g.drawLine(refHoriz, lineTop, refHoriz, getSize().height);
		}
		
		int refWidth = cutoff.stringWidth(g) + 2 * kRefLeftRightBorder;
		int refCenter = Math.max(refWidth / 2, Math.min(getSize().width - refWidth / 2, refHoriz));
//		g.setColor(Color.white);
//		g.fillRect(refCenter - refWidth / 2, 0, refWidth, ascent + 2 * kRefTopBottomBorder);
		
		g.setColor(getForeground());
		cutoff.drawCentred(g, refCenter, ascent + kRefTopBottomBorder);
		
		int nUnder = countUnder(cutoff.toDouble());
		int nTotal = getNumVariable().noOfValues();
		int nOver = nTotal - nUnder;
		
		Font stdFont = getApplet().getStandardFont();
		Font bigFont = new Font(stdFont.getName(), Font.BOLD, stdFont.getSize() * 3);
		g.setFont(bigFont);
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		int countBaseline = (getSize().height + lineTop + ascent) / 2;
		
		g.setColor(kUnderColor);
		String underString = String.valueOf(nUnder);
		g.drawString(underString, 5, countBaseline);
		
		g.setColor(kOverColor);
		String overString = String.valueOf(nOver);
		g.drawString(overString, getSize().width - 5 - fm.stringWidth(overString), countBaseline);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		double co = cutoff.toDouble();
		int refHorizPos = axis.numValToRawPosition(co);
		int refHoriz = translateToScreen(refHorizPos, 0, null).x;
		
		if (Math.abs(x - refHoriz) > kHitSlop)
			return null;
		else
			return new HorizDragPosInfo(x, 0, x - refHoriz);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.x - hitOffset < 0 || hitPos.x - hitOffset >= axis.getAxisLength())
			return null;
		else
			return new HorizDragPosInfo(x);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		HorizDragPosInfo dragPos = (HorizDragPosInfo)startInfo;
		hitOffset = dragPos.hitOffset;
		doingDrag = true;
		repaint();
		return true;
	}
	
/*
	private double round(double x, int decimals) {
		double factor = Math.pow(10.0, decimals);
		return Math.rint(x * factor) / factor;
	}
*/
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			doingDrag = false;
			repaint();
		}
		else {
			doingDrag = true;
			
			HorizDragPosInfo newPos = (HorizDragPosInfo)toPos;
			double newRef = 0.0;
			try {
				newRef = axis.positionToNumVal(newPos.x - hitOffset - getViewBorder().left);
			} catch (AxisException ex) {
				return;
			}
			setCutoff(newRef);
			repaint();
			
			((ExerciseApplet)getApplet()).noteChangedWorking();
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doingDrag = false;
		repaint();
	}
}