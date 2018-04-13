package exerciseBivar;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import exercise2.*;


public class DragScatterView extends DataView implements StatusInterface {
//	static public final String DRAG_SCATTER_PLOT = "dragScatter";
	
	static final public Color kHorizAxisColor = Color.blue;
	static final public Color kVertAxisColor = new Color(0x009900);
	
	static final private int kCrossHiliteWidth = 12;
	static final private int kHitSlop = 12;
	static final private int kCorrectSlop = 5;
	static final private int kApproxSlop = 15;
	
	private String xKey, yKey;
	
	private HorizAxis xAxis;
	private VertAxis yAxis;
	
	private Color xColor, yColor;
	
	private boolean alreadyUsed[];
	private int xPos[];							//		screen coordinates
	protected int yPos[];
	private double xStartVal[];			//		when initialised from setStatus()
	private double yStartVal[];
	private boolean wasWrong[] = null;
	
	private boolean initialised = false;
	
	private int xOffset, yOffset;
	private int dragIndex;					//	for cross in screen coords
	private boolean doingDrag = false;
	
	private NumValue tempX = new NumValue(0, 0);
	private NumValue tempY = new NumValue(0, 0);
	
	public DragScatterView(DataSet theData, XApplet applet,
										HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey) {
		super(theData, applet, new Insets(5, 5, 5, 5));
		this.xKey = xKey;
		this.yKey = yKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		
		yColor = kVertAxisColor;
		xColor = kHorizAxisColor;
		
		setRetainLastSelection(true);
	}
	
	public String getStatus() {
		String s = "";
		Point p = null;
		for (int i=0 ; i<xPos.length ; i++) {
			p = translateFromScreen(xPos[i], yPos[i], p);
			NumValue xVal = getXValue(p.x, i);
			s += xVal + "*";
			
			NumValue yVal = getYValue(p.y, i);
			s += yVal + " ";
		}
		return s;
	}
	
	public void setStatus(String statusString) {
		xPos = yPos = null;
		StringTokenizer st = new StringTokenizer(statusString);
		int nPoints = st.countTokens();
		xStartVal = new double[nPoints];
		yStartVal = new double[nPoints];
		
		for (int i=0 ; i<nPoints ; i++) {
			StringTokenizer st2 = new StringTokenizer(st.nextToken(), "*");
			xStartVal[i] = Double.parseDouble(st2.nextToken());
			yStartVal[i] = Double.parseDouble(st2.nextToken());
		}
		initialised = false;
		repaint();
	}
	
	public void setAxisColors(Color xColor, Color yColor) {
		this.yColor = yColor;
		this.xColor = xColor;
	}
	
	public void setAlreadyUsed(boolean[] alreadyUsed) {
		this.alreadyUsed = alreadyUsed;
		initialised = false;
	}
	
	public void showCorrectCrosses() {
		showCrosses(0);
	}
	
	public void showCrosses(int numberToDrag) {
		NumVariable xVar = (NumVariable)getVariable(xKey);
		NumVariable yVar = (NumVariable)getVariable(yKey);
		Point p = null;
		wasWrong = null;
		for (int i=0 ; i<alreadyUsed.length ; i++) {
			double x = ((NumValue)xVar.valueAt(i)).toDouble();
			double y = ((NumValue)yVar.valueAt(i)).toDouble();
			if (i < yPos.length - numberToDrag) {
				int xOnAxis = xAxis.numValToRawPosition(x);
				int yOnAxis = yAxis.numValToRawPosition(y);
				p = translateToScreen(xOnAxis, yOnAxis, p);
				xPos[i] = p.x;
				yPos[i] = p.y;
			}
			else {
				int crossSize = 2 * getCrossPix() + 1;
				yPos[i] = crossSize;
				int crossCenter = (getSize().width - numberToDrag * (crossSize + 3) + crossSize) / 2;
				int dragindex = i - yPos.length + numberToDrag;
				crossCenter += dragindex * (crossSize + 3);
				xPos[i] = crossCenter;
			}
		}
	}
	
	public boolean[] getErrors(boolean exact) {
		double xSlop = (xAxis.maxOnAxis - xAxis.minOnAxis) * (exact ? kCorrectSlop : kApproxSlop) / 400.0;	//	for 400 pixel axis
		double ySlop = (yAxis.maxOnAxis - yAxis.minOnAxis) * (exact ? kCorrectSlop : kApproxSlop) / 400.0;	//	for 400 pixel axis
		NumVariable xVar = (NumVariable)getVariable(xKey);
		NumVariable yVar = (NumVariable)getVariable(yKey);
		int nValues = yVar.noOfValues();
		
		Point p = null;
		wasWrong = new boolean[nValues];
		boolean hasError = false;
		for (int i=0 ; i<nValues ; i++) {
			double x = ((NumValue)xVar.valueAt(i)).toDouble();
			double y = ((NumValue)yVar.valueAt(i)).toDouble();
			
			double attemptX, attemptY;
			if (xStartVal == null || yStartVal == null) {
				p = translateFromScreen(xPos[i], yPos[i], p);
				attemptX = getXValue(p.x, i).toDouble();
				attemptY = getYValue(p.y, i).toDouble();
			}
			else {
				attemptX = xStartVal[i];
				attemptY = yStartVal[i];
			}
			
			double xError = Math.abs(x - attemptX);
			double yError = Math.abs(y - attemptY);
			if (xError > xSlop || yError > ySlop) {
				wasWrong[i] = true;
				hasError = true;
			}
		}
		return hasError ? wasWrong : null;
	}
	
//-----------------------------------------------------------------
	
	final public void initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
		}
	}
	
	protected void doInitialisation(Graphics g) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		int nValues = yVar.noOfValues();
		
		wasWrong = null;
		
		if (xPos == null || yPos == null || xPos.length != nValues || yPos.length != nValues) {
			xPos = new int[nValues];				//	if values are set by setStatus(), find their pixel positions
			yPos = new int[nValues];
			Point p = null;
			if (xStartVal != null && yStartVal != null) {
				for (int i=0 ; i<nValues ; i++) {
					int xOnAxis = xAxis.numValToRawPosition(xStartVal[i]);
					int yOnAxis = yAxis.numValToRawPosition(yStartVal[i]);
					p = translateToScreen(xOnAxis, yOnAxis, p);
					xPos[i] = p.x;
					yPos[i] = p.y;
				}
				
				xStartVal = null;
				yStartVal = null;
			}
		}
		
/*
		boolean alreadyUsedOK = true;
		if (alreadyUsed == null || alreadyUsed.length != nValues)
			alreadyUsedOK = false;
		else
			for (int i=0 ; i<nValues ; i++)
				if (alreadyUsed[i])
					alreadyUsedOK = false;
		if (!alreadyUsedOK)			
			throw new RuntimeException("DragScatterView: array alreadyUsed[] is incorrectly initialised.");
*/
	}
	
	private NumValue getXValue(int xPos, int crossIndex) {		//	xPos is in axis coordinates
		try {
			NumVariable xVar = (NumVariable)getVariable(xKey);
			double correctX = ((NumValue)xVar.valueAt(crossIndex)).toDouble();
			int correctXPos = xAxis.numValToRawPosition(correctX);
			
			double displayX = (xPos == correctXPos) ? correctX : xAxis.positionToNumVal(xPos);
			tempX.setValue(displayX);
		} catch (AxisException e) {
			if (e.axisProblem == AxisException.TOO_LOW_ERROR)
				tempX.setValue(xAxis.minOnAxis);
			else if (e.axisProblem == AxisException.TOO_HIGH_ERROR)
				tempX.setValue(xAxis.maxOnAxis);
		}
		return tempX;
	}
	
	private NumValue getYValue(int yPos, int crossIndex) {		//	yPos is in axis coordinates
		try {
			NumVariable yVar = (NumVariable)getVariable(yKey);
			double correctY = ((NumValue)yVar.valueAt(crossIndex)).toDouble();
			int correctYPos = yAxis.numValToRawPosition(correctY);
			
			double displayY = (yPos == correctYPos) ? correctY : yAxis.positionToNumVal(yPos);
			tempY.setValue(displayY);
		} catch (AxisException e) {
			if (e.axisProblem == AxisException.TOO_LOW_ERROR)
				tempY.setValue(yAxis.minOnAxis);
			else if (e.axisProblem == AxisException.TOO_HIGH_ERROR)
				tempY.setValue(yAxis.maxOnAxis);
		}
		return tempY;
	}
	
	private void drawCrossHilite(Graphics g, int xPos, int yPos, int crossIndex, boolean showArrows) {
		int halfWidth = kCrossHiliteWidth / 2;
		g.setColor(Color.yellow);
		g.fillRect(xPos - halfWidth, yPos - halfWidth, kCrossHiliteWidth + 1, kCrossHiliteWidth + 1);
		
		if (showArrows) {
			Point p = translateFromScreen(xPos, yPos, null);
			g.setColor(xColor);
			g.drawLine(xPos, yPos - 1, xPos, getSize().height - 1);
			if (doingDrag) {
				g.drawLine(xPos - 1, yPos - 1, xPos - 1, getSize().height - 2);
				g.drawLine(xPos + 1, yPos - 1, xPos + 1, getSize().height - 2);
				for (int i=2 ; i<=4 ; i++)
					g.drawLine(xPos - i, getSize().height - i - 1, xPos + i, getSize().height - i - 1);
			}
			else {
				g.drawLine(xPos, getSize().height - 1, xPos - 4, getSize().height - 5);
				g.drawLine(xPos, getSize().height - 1, xPos + 4, getSize().height - 5);
			}
			NumValue xVal = getXValue(p.x, crossIndex);
			int valueLeft = xPos + 3;
			if (valueLeft + xVal.stringWidth(g) > getSize().width)
				xVal.drawLeft(g, xPos - 3, getSize().height - 6);
			else
				xVal.drawRight(g, xPos + 3, getSize().height - 6);
			
			g.setColor(yColor);
			g.drawLine(0, yPos, xPos + 1, yPos);
			if (doingDrag) {
				g.drawLine(1, yPos - 1, xPos + 1, yPos - 1);
				g.drawLine(1, yPos + 1, xPos + 1, yPos + 1);
				for (int i=2 ; i<=4 ; i++)
					g.drawLine(i, yPos - i, i, yPos + i);
			}
			else {
				g.drawLine(0, yPos, 4, yPos - 4);
				g.drawLine(0, yPos, 4, yPos + 4);
			}
			NumValue yVal = getYValue(p.y, crossIndex);
			int ascent = g.getFontMetrics().getAscent();
			int baseline = yPos - 3;
			if (baseline < ascent)
				baseline = yPos + ascent + 3;
			yVal.drawRight(g, 5, baseline);
		}
			
		g.setColor(getForeground());
	}
	
	protected Color getSymbolColor(int index) {
		return getForeground();
	}
	
	protected void drawSymbol(Graphics g, Point p, int index) {
		drawCross(g, p);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		int defaultXPos = getSize().width / 2;
		int defaultYPos = getSize().height / 2;
		
		Flags selection = getSelection();
		int singleSelectedIndex = selection.findSingleSetFlag();
		if (singleSelectedIndex >= 0) {
			tempX.decimals = ((NumVariable)getVariable(xKey)).getMaxDecimals();
			tempY.decimals = ((NumVariable)getVariable(yKey)).getMaxDecimals();
			if (alreadyUsed[singleSelectedIndex])
				drawCrossHilite(g, xPos[singleSelectedIndex], yPos[singleSelectedIndex],
																														singleSelectedIndex, true);
			else
				drawCrossHilite(g, defaultXPos, defaultYPos, singleSelectedIndex, true);
		}
		else
			for (int i=0 ; i<alreadyUsed.length ; i++)
				if (selection.valueAt(i)) {
					if (alreadyUsed[i])
						drawCrossHilite(g, xPos[i], yPos[i], -1, false);
					else
						drawCrossHilite(g, defaultXPos, defaultYPos, -1, false);
				}
			
		Point p = new Point(0,0);
		for (int i=0 ; i<alreadyUsed.length ; i++) {
			if (alreadyUsed[i]) {
				p.x = xPos[i];
				p.y = yPos[i];
				g.setColor(selection.valueAt(i) || (wasWrong != null && wasWrong[i]) ? Color.red : getSymbolColor(i));
				drawSymbol(g, p, i);
			}
			else if (selection.valueAt(i)) {
				p.x = defaultXPos;
				p.y = defaultYPos;
				g.setColor(Color.red);
				drawSymbol(g, p, i);
			}
		}
	}
	
//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		int hitIndex = -1;
		int minHitDist = Integer.MAX_VALUE;
		for (int i=0 ; i<alreadyUsed.length ; i++) {
			int xOffset = Integer.MAX_VALUE;
			int yOffset = Integer.MAX_VALUE;
			boolean gotPoint = false;
			if (alreadyUsed[i]) {
				xOffset = x - xPos[i];
				yOffset = y - yPos[i];
				gotPoint = true;
			}
			else if (getSelection().valueAt(i)) {
				int defaultXPos = getSize().width / 2;
				int defaultYPos = getSize().height / 2;
				xOffset = x - defaultXPos;
				yOffset = y - defaultYPos;
				gotPoint = true;
			}
			if (gotPoint) {
				int dist = Math.abs(xOffset) + Math.abs(yOffset);
				if (dist < minHitDist) {
					hitIndex = i;
					minHitDist = dist;
				}
			}
		}
		
		if (minHitDist < kHitSlop)
			return new ScatterPosInfo(x, y, hitIndex, xOffset, yOffset);
		else
			return new ScatterPosInfo(0, 0, -1, 0, 0);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < getViewBorder().left || x >= getSize().width - getViewBorder().right
																|| y < getViewBorder().top || y >= getSize().height - getViewBorder().bottom)
			return null;
		else
			return new ScatterPosInfo(x, y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		ScatterPosInfo hitPos = (ScatterPosInfo)startInfo;
		dragIndex = hitPos.index;
		
		if (dragIndex < 0) {
			getData().clearSelection();
			return false;
		}
		else {
			xOffset = hitPos.xOffset;
			yOffset = hitPos.yOffset;
			
			if (!alreadyUsed[dragIndex]) {
				alreadyUsed[dragIndex] = true;
				xPos[dragIndex] = hitPos.x - xOffset;
				yPos[dragIndex] = hitPos.y - yOffset;
			}
			
			getData().setSelection(dragIndex);
			doingDrag = true;
			
			((ExerciseApplet)getApplet()).noteChangedWorking();
			
			repaint();
			return true;
		}
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null) {
			ScatterPosInfo dragPos = (ScatterPosInfo)toPos;
			xPos[dragIndex] = dragPos.x - xOffset;
			yPos[dragIndex] = dragPos.y - yOffset;
			repaint();
		}
	}
	
	protected void setDoneDrag(int index) {
		alreadyUsed[index] = true;
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doingDrag = false;
		int hitIndex = getSelection().findSingleSetFlag();
		setDoneDrag(hitIndex);
		dragIndex = -1;
		repaint();
	}

//-----------------------------------------------------------------------------------

	public void mousePressed(MouseEvent e) {
		requestFocus();				//	needed in order for arrow keys to be recognised
		super.mousePressed(e);
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		Flags selection = getSelection();
		int singleSelectedIndex = selection.findSingleSetFlag();
		if (singleSelectedIndex >= 0) {
			if (key == KeyEvent.VK_LEFT && xPos[singleSelectedIndex] > getViewBorder().left)
				xPos[singleSelectedIndex] --;
			else if (key == KeyEvent.VK_RIGHT && xPos[singleSelectedIndex] < getSize().width - getViewBorder().right - 1)
				xPos[singleSelectedIndex] ++;
			else if (key == KeyEvent.VK_UP && yPos[singleSelectedIndex] > getViewBorder().top)
				yPos[singleSelectedIndex] --;
			else if (key == KeyEvent.VK_DOWN && yPos[singleSelectedIndex] < getSize().height - getViewBorder().bottom - 1)
				yPos[singleSelectedIndex] ++;
			
			repaint();
			((ExerciseApplet)getApplet()).noteChangedWorking();
		}
	}
	
}
	
