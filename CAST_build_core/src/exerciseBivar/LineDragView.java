package exerciseBivar;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import dataView.*;
import axis.*;
import models.*;
import utils.*;
import exercise2.*;

import exerciseBivarProg.*;


public class LineDragView extends DataView implements StatusInterface {
//	static public final String DRAG_LINE_PLOT = "dragLine";
	
	static final public Color kSelectedArrowColor = new Color(0xFF6666);
	static final public Color kDragArrowColor = Color.red;
	static final public Color kDimArrowColor = new Color(0xCCCCCC);
	
	static final private int kHitSlop = 12;
//	static final private int kCorrectSlop = 5;
//	static final private int kApproxSlop = 15;
	static final private int kArrowHead = 3;
	static final private int kHiliteRadius = 8;
	static final private int kCircleRadius = 3;
	
	static final private int kMaxExactError = 2;
	static final private int kMaxCloseError = 7;
	
	private String x01Key, y01Key, xKey, yKey, line01Key;
	
	private HorizAxis xAxis;
	private VertAxis yAxis;
	
	private int xOffset, yOffset;
	private int selectedIndex = -1;					//	0 or 1
	private boolean doingDrag = false;
	
	public LineDragView(DataSet theData, XApplet applet,
										HorizAxis xAxis, VertAxis yAxis, String x01Key, String y01Key, String xKey,
										String yKey, String line01Key) {
		super(theData, applet, new Insets(10, 10, 10, 10));
		this.x01Key = x01Key;
		this.y01Key = y01Key;
		this.xKey = xKey;
		this.yKey = yKey;
		this.line01Key = line01Key;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
	}
	
	public String getStatus() {
		NumVariable y01Var = (NumVariable)getVariable(y01Key);
		NumVariable x01Var = (NumVariable)getVariable(x01Key);
		String s = "";
		for (int i=0 ; i<2 ; i++) {
			NumValue yVal = (NumValue)(y01Var.valueAt(i));
			NumValue xVal = (NumValue)(x01Var.valueAt(i));
			s += xVal.toDouble() + "*" + yVal.toDouble() + " ";
		}
		return s;
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		NumVariable y01Var = (NumVariable)getVariable(y01Key);
		NumVariable x01Var = (NumVariable)getVariable(x01Key);
		
		for (int i=0 ; i<2 ; i++) {
			NumValue yVal = (NumValue)(y01Var.valueAt(i));
			NumValue xVal = (NumValue)(x01Var.valueAt(i));
			StringTokenizer st2 = new StringTokenizer(st.nextToken(), "*");
			xVal.setValue(Double.parseDouble(st2.nextToken()));
			yVal.setValue(Double.parseDouble(st2.nextToken()));
		}
		
		LinearModel line = (LinearModel)getVariable(line01Key);
		line.updateLSParams(y01Key);
		
		repaint();
	}
	
	public void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
	}
	
	public int checkLinePosition(double intercept, double slope) {
		NumVariable x01Var = (NumVariable)getVariable(x01Key);
		NumVariable y01Var = (NumVariable)getVariable(y01Key);
		
		double maxError = Math.max(onePointError(x01Var, y01Var, 0, intercept, slope),
																onePointError(x01Var, y01Var, 1, intercept, slope));
		
		double exactSlop = (yAxis.maxOnAxis - yAxis.minOnAxis) * kMaxExactError / 400.0;		//	for 400-pixel axis
		double closeSlop = (yAxis.maxOnAxis - yAxis.minOnAxis) * kMaxCloseError / 400.0;		//	for 400-pixel axis
		if (maxError <= exactSlop)
			return ExerciseConstants.ANS_CORRECT;
		else if (maxError <= closeSlop)
			return ExerciseConstants.ANS_CLOSE;
		else
			return SketchLineApplet.ANS_WRONG_LINE;
	}
	
	private double onePointError(NumVariable x01Var, NumVariable y01Var, int index,
																										double intercept, double slope) {
		double x = x01Var.doubleValueAt(index);
		double y = y01Var.doubleValueAt(index);
		double correctY = intercept + slope * x;
		return Math.abs(y - correctY);
	}
	
//-----------------------------------------------------------------
	
	private Point getScreenPoint(int index, Point thePoint) {
		NumVariable y01Var = (NumVariable)getVariable(y01Key);
		NumVariable x01Var = (NumVariable)getVariable(x01Key);
		NumValue y01Val = (NumValue)(y01Var.valueAt(index));
		NumValue x01Val = (NumValue)(x01Var.valueAt(index));
		
		int vertPos = yAxis.numValToRawPosition(y01Val.toDouble());
		int horizPos = xAxis.numValToRawPosition(x01Val.toDouble());
		
		return translateToScreen(horizPos, vertPos, thePoint);
	}
	
	public void paintView(Graphics g) {
		Point p = null;
		NumVariable xVar = (NumVariable)getVariable(xKey);
		NumVariable yVar = (NumVariable)getVariable(yKey);
		int ascent = g.getFontMetrics().getAscent();
		
		for (int i=0 ; i<2 ; i++) {
			p = getScreenPoint(i, p);
			if (selectedIndex == i) {
				g.setColor(Color.yellow);
				g.fillOval(p.x - kHiliteRadius, p.y - kHiliteRadius, 2 * kHiliteRadius + 1, 2 * kHiliteRadius + 1);
			}
			
			g.setColor((selectedIndex == i) ? (doingDrag ? kDragArrowColor : kSelectedArrowColor) : kDimArrowColor);
			g.drawLine(p.x, p.y, p.x, getSize().height);
			g.drawLine(p.x, getSize().height - 1, p.x - kArrowHead, getSize().height - kArrowHead - 1);
			g.drawLine(p.x, getSize().height - 1, p.x + kArrowHead, getSize().height - kArrowHead - 1);
			
			NumValue x = (NumValue)(xVar.valueAt(i));
			if (i == 0)
				x.drawRight(g, p.x + kArrowHead, getSize().height - kArrowHead - 2);
			else
				x.drawLeft(g, p.x - kArrowHead, getSize().height - kArrowHead - 2);
			
			g.drawLine(p.x, p.y, 0, p.y);
			g.drawLine(0, p.y, kArrowHead, p.y - kArrowHead);
			g.drawLine(0, p.y, kArrowHead, p.y + kArrowHead);
			
			NumValue y = (NumValue)(yVar.valueAt(i));
			int baseline = p.y - kArrowHead - 1;
			if (baseline < ascent + 2)
				baseline = p.y + kArrowHead + ascent;
			y.drawRight(g, kArrowHead + 1, baseline);
			
			g.setColor(Color.red);
			g.fillOval(p.x - kCircleRadius, p.y - kCircleRadius, 2 * kCircleRadius + 1, 2 * kCircleRadius + 1);
//			drawBlob(g, p);
		}
		
		g.setColor(Color.blue);
		LinearModel line = (LinearModel)getVariable(line01Key);
		line.drawMean(g, this, xAxis, yAxis);
	}
	

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}

	protected PositionInfo getInitialPosition(int x, int y) {
		Point p = null;
		
		int hitIndex = -1;
		int minHitDist = Integer.MAX_VALUE;
		int hitXOffset = 0;
		int hitYOffset = 0;
		
		for (int i=0 ; i<2 ; i++) {
			p = getScreenPoint(i, p);
			
			int thisXOffset = x - p.x;
			int thisYOffset = y - p.y;
			int dist = Math.abs(thisXOffset) + Math.abs(thisYOffset);
			
			if (dist < minHitDist) {
				hitIndex = i;
				hitXOffset = thisXOffset;
				hitYOffset = thisYOffset;
				minHitDist = dist;
			}
		}
		
		if (minHitDist < kHitSlop)
			return new ScatterPosInfo(x, y, hitIndex, hitXOffset, hitYOffset);
		else
			return null;
	}
	
	private int xAllowed(int x) {
		int leftAllowed = (selectedIndex == 0) ? getViewBorder().left : getSize().width * 2 / 3;
		int rightAllowed = (selectedIndex == 0) ? getSize().width / 3 : getSize().width - getViewBorder().right - 1;
		return Math.min(rightAllowed, Math.max(leftAllowed, x));
	}
	
	private int yAllowed(int y) {
		return Math.min(getSize().height - getViewBorder().bottom, Math.max(getViewBorder().top, y));
	}
	
	protected PositionInfo getPosition(int x, int y) {
		x -= xOffset;
		y -= yOffset;
		
		x = xAllowed(x);
		y = yAllowed(y);
		
		return new ScatterPosInfo(x, y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		ScatterPosInfo hitPos = (ScatterPosInfo)startInfo;
		selectedIndex = hitPos.index;
		doingDrag = true;
		
		xOffset = hitPos.xOffset;
		yOffset = hitPos.yOffset;
		
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null) {
			ScatterPosInfo dragPos = (ScatterPosInfo)toPos;
			int xPos = dragPos.x;
			int yPos = dragPos.y;
			
			Point p = translateFromScreen(xPos, yPos, null);
			try {
				double x = xAxis.positionToNumVal(p.x);
				double y = yAxis.positionToNumVal(p.y);
				
				NumVariable y01Var = (NumVariable)getVariable(y01Key);
				NumVariable x01Var = (NumVariable)getVariable(x01Key);
				NumValue y01Val = (NumValue)(y01Var.valueAt(selectedIndex));
				NumValue x01Val = (NumValue)(x01Var.valueAt(selectedIndex));
				
				x01Val.setValue(x);
				y01Val.setValue(y);
				
				LinearModel line = (LinearModel)getVariable(line01Key);
				line.updateLSParams(y01Key);
			} catch (AxisException e) {
			}
			
			repaint();
			
			((ExerciseApplet)getApplet()).noteChangedWorking();
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doingDrag = false;
		repaint();
	}

//-----------------------------------------------------------------------------------

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		
		if (selectedIndex >= 0) {
			if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) {
				Point p = getScreenPoint(selectedIndex, null);
				if (key == KeyEvent.VK_LEFT)
					p.x --;
				else
					p.x ++;
				p = translateFromScreen(xAllowed(p.x), p.y, p);
				try {
					double x = xAxis.positionToNumVal(p.x);
					NumVariable x01Var = (NumVariable)getVariable(x01Key);
					NumValue x01Val = (NumValue)(x01Var.valueAt(selectedIndex));
					x01Val.setValue(x);
				} catch (AxisException ex) {
				}
			}
			else if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
				Point p = getScreenPoint(selectedIndex, null);
				if (key == KeyEvent.VK_UP)
					p.y --;
				else
					p.y ++;
				p = translateFromScreen(p.x, yAllowed(p.y), p);
				try {
					double y = yAxis.positionToNumVal(p.y);
					NumVariable y01Var = (NumVariable)getVariable(y01Key);
					NumValue y01Val = (NumValue)(y01Var.valueAt(selectedIndex));
					y01Val.setValue(y);
				} catch (AxisException ex) {
				}
			}
					
			LinearModel line = (LinearModel)getVariable(line01Key);
			line.updateLSParams(y01Key);
			
			repaint();
			((ExerciseApplet)getApplet()).noteChangedWorking();
		}
	}
	
}
	
