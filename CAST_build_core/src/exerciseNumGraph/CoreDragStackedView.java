package exerciseNumGraph;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import exercise2.*;


abstract public class CoreDragStackedView extends DataView implements StatusInterface {

//	static final private int kMaxHitSlop = 4;
	
//	static final private double kEps = 0.000000001;				//	to make sure that cross on class boundary goes up
	
	static final private Color kSelectedSymbolColor = Color.red;
	static final private Color kSymbolColor = Color.black;
	
	protected String yKey;
	
	protected String axisInfo;
	protected double axisMin, axisMax, classWidth;
	
	protected boolean alreadyUsed[];
	protected int stackIndex[];
	protected int stackPosition[];
	
	private boolean initialised = false;
	protected int symbolScreenWidth, symbolScreenHeight, symbolAxisWidth, symbolAxisHeight;
	
	private int xOffset, yOffset;
	protected int dragX, dragY;					//	for topLeft of cross in screen coords
	private boolean doingDrag = false;
	
	public CoreDragStackedView(DataSet theData, XApplet applet, String yKey, String axisInfo) {
		super(theData, applet, null);
		this.yKey = yKey;
		changeAxis(axisInfo);
		setRetainLastSelection(true);
	}
	
	public String getStatus() {
		String s = getAxisStatus() + "*";
		for (int i=0 ; i<stackIndex.length ; i++)
			s += stackIndex[i] + " ";
		s += "*";
		for (int i=0 ; i<stackPosition.length ; i++)
			s += stackPosition[i] + " ";
		return s;
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString, "*");
		
		setAxisStatus(st.nextToken());
		
		StringTokenizer st2 = new StringTokenizer(st.nextToken());
		stackIndex = new int[st2.countTokens()];
		for (int i=0 ; i<stackIndex.length ; i++)
			stackIndex[i] = Integer.parseInt(st2.nextToken());
		
		st2 = new StringTokenizer(st.nextToken());
		stackPosition = new int[st2.countTokens()];
		for (int i=0 ; i<stackPosition.length ; i++)
			stackPosition[i] = Integer.parseInt(st2.nextToken());
		
		repaint();
	}
	
	protected String getAxisStatus() {
		return axisMin + " " + axisMax + " " + classWidth;
	}
	
	protected void setAxisStatus(String status) {
		StringTokenizer st = new StringTokenizer(status);
		axisMin = Double.parseDouble(st.nextToken());
		axisMax = Double.parseDouble(st.nextToken());
		classWidth = Double.parseDouble(st.nextToken());
	}
	
	public void setAlreadyUsed(boolean[] alreadyUsed) {
		this.alreadyUsed = alreadyUsed;
		initialised = false;
	}
	
	public boolean[] inWrongStack() {
		boolean wrongCross[] = new boolean[alreadyUsed.length];
		NumVariable yVar = (NumVariable)getVariable(yKey);
		ValueEnumeration ye = yVar.values();
		for (int i=0 ; i<alreadyUsed.length ; i++) {
			double y = ye.nextDouble();
//			int correctIndex = (int)Math.round(Math.floor((y - axisMin) / classWidth + kEps));
			int correctIndex = (int)Math.round(Math.floor((y - axisMin) / classWidth));
			wrongCross[i] = correctIndex != stackIndex[i];
		}
		return wrongCross;
	}
	
	public void showCorrectCrosses() {
		showCrosses(0);
	}
	
	public void showCrosses(int numberToDrag) {
		int noOfStacks = (int)Math.round(Math.ceil((axisMax - axisMin) / classWidth));
		int stackTotal[] = new int[noOfStacks];
		NumVariable yVar = (NumVariable)getVariable(yKey);
		NumValue sortedY[] = yVar.getSortedData();
		for (int si=0 ; si<sortedY.length ; si++) {
//			System.out.println("y(" + i + ") = " + sortedY[i]);
			double y = sortedY[si].toDouble();
			int i = yVar.rankToIndex(si);
			if (i < sortedY.length - numberToDrag) {
				int correctIndex = (int)Math.round(Math.floor((y - axisMin) / classWidth));
				stackIndex[i] = correctIndex;
				stackPosition[i] = (stackTotal[stackIndex[i]] ++);
			}
			else {
				stackIndex[i] = (noOfStacks - numberToDrag) / 2 + (i - sortedY.length + numberToDrag);
				stackPosition[i] = -1;
			}
		}
	}
	
	final public void initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
		}
	}
	
	abstract protected void readAxisInfo(Graphics g);
	abstract protected void setSymbolSize(Graphics g);
	
	protected void doInitialisation(Graphics g) {
		setSymbolSize(g);
		readAxisInfo(g);
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		int nValues = yVar.noOfValues();
		
		if (stackIndex == null || stackIndex.length != nValues)
			stackIndex = new int[nValues];
		if (stackPosition == null || stackPosition.length != nValues)
			stackPosition = new int[nValues];
	}
	
	public void changeAxis(String axisInfo) {
		this.axisInfo = axisInfo;
		initialised = false;
	}
	
//-------------------------------------------------------------------
	
	abstract protected void drawAxis(Graphics g);
	
	abstract protected void setSymbolRect(int axisPix, int offAxisPix, Rectangle r);
	
	abstract protected void setDefaultSymbolRect(Rectangle r);
	
	abstract protected int getStackIndex(int dragX, int dragY, boolean center);
	abstract protected int getHighIndexOffset(int lowDragStackIndex);
	abstract protected int getDragStackPosition();
	
	protected Color getSymbolColor(int index) {
		return kSymbolColor;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		drawAxis(g);
		
		Flags selection = getSelection();
		g.setColor(Color.yellow);
		Rectangle r = new Rectangle();
		for (int i=0 ; i<alreadyUsed.length ; i++)
			if (selection.valueAt(i)) {
				if (doingDrag)
					g.fillRect(dragX, dragY, symbolScreenWidth, symbolScreenHeight);
				else if (alreadyUsed[i]) {
					int left = stackIndex[i] * symbolAxisWidth;
					int top = (stackPosition[i] + 1) * symbolAxisHeight;
					setSymbolRect(left, top, r);
					g.fillRect(r.x, r.y, r.width, r.height);
				}
				else {
					setDefaultSymbolRect(r);
					g.fillRect(r.x, r.y, r.width, r.height);
				}
			}
		
		int lowDragStackIndex = getStackIndex(dragX, dragY, false);
		int highIndexOffset = getHighIndexOffset(lowDragStackIndex);
		int newDragStackPosition = getDragStackPosition();
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		
		for (int i=0 ; i<alreadyUsed.length ; i++) {
			Color crossColor = selection.valueAt(i) ? kSelectedSymbolColor : getSymbolColor(i);
			NumValue y = (NumValue)yVar.valueAt(i);
			if (selection.valueAt(i) && doingDrag) {
				r.x = dragX;
				r.y = dragY;
				r.width = symbolScreenWidth;
				r.height = symbolScreenHeight;
				drawSymbol(g, r, crossColor, y, i);
			}
			else if (alreadyUsed[i]) {
				int stackInd = stackIndex[i];
				int stackPos = stackPosition[i];
				int left = stackInd * symbolAxisWidth;
				int top = (stackPos + 1) * symbolAxisHeight;
				
				if (doingDrag && stackInd == lowDragStackIndex && stackPos >= newDragStackPosition)
					top += (symbolAxisHeight - highIndexOffset);
				else if (doingDrag && stackInd == lowDragStackIndex + 1 && stackPos >= newDragStackPosition)
					top += highIndexOffset;
				
				setSymbolRect(left, top, r);
				drawSymbol(g, r, crossColor, y, i);
			}
			else if (selection.valueAt(i)) {
				setDefaultSymbolRect(r);
				drawSymbol(g, r, crossColor, y, i);
			}
		}
	}
	
	abstract protected void drawSymbol(Graphics g, Rectangle r, Color crossColor, NumValue y, int index);
	

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		int hitIndex = -1;
		int hitLeft = 0;
		int hitTop = 0;
		Rectangle r = new Rectangle();
		for (int i=0 ; i<alreadyUsed.length ; i++)
			if (alreadyUsed[i]) {
				hitLeft = stackIndex[i] * symbolAxisWidth;
				hitTop = (stackPosition[i] + 1) * symbolAxisHeight;
				setSymbolRect(hitLeft, hitTop, r);
				if (x >= r.x && x < r.x + r.width && y >= r.y && y < r.y + r.height) {
					hitIndex = i;
					break;
				}
			}
			else if (getSelection().valueAt(i)) {
				setDefaultSymbolRect(r);
				if (x >= r.x && x < r.x + r.width && y >= r.y && y < r.y + r.height) {
					hitIndex = i;
					break;
				}
			}
		
		xOffset = x - r.x;
		yOffset = y - r.y;
		dragX = r.x;
		dragY = r.y;
		
		return (hitIndex >= 0) ? new IndexPosInfo(hitIndex) : null;
	}
	
	abstract protected PositionInfo getPosition(int x, int y);
	
	protected boolean startDrag(PositionInfo startInfo) {
		IndexPosInfo hitPos = (IndexPosInfo)startInfo;
		int hitIndex = hitPos.itemIndex;
		if (alreadyUsed[hitIndex]) {		//	drop other crosses on old stack
			int hitStackIndex = stackIndex[hitIndex];
			int hitStackPosition = stackPosition[hitIndex];
			for (int i=0 ; i<stackIndex.length ; i++)
				if (stackIndex[i] == hitStackIndex && hitStackPosition >= 0
																						&& stackPosition[i] > hitStackPosition)
					stackPosition[i] --;
		}
		
		getData().setSelection(hitIndex);
		doingDrag = true;
		
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null) {
			DragPosInfo dragPos = (DragPosInfo)toPos;
			dragX = dragPos.x - xOffset;
			dragY = dragPos.y - yOffset;
			repaint();
			
			((ExerciseApplet)getApplet()).noteChangedWorking();
		}
	}
	
	protected void setDoneDrag(int index) {
		alreadyUsed[index] = true;
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doingDrag = false;
		int hitIndex = getSelection().findSingleSetFlag();
		setDoneDrag(hitIndex);
		int columnIndex = getStackIndex(dragX, dragY, true);
		int columnPosition = getDragStackPosition();
		int noInStack = 0;
		for (int i=0 ; i<alreadyUsed.length ; i++)
			if (i != hitIndex && alreadyUsed[i] && stackIndex[i] == columnIndex && stackPosition[i] >= 0)
				noInStack ++;
		stackIndex[hitIndex] = columnIndex;
		
		if (columnPosition >= noInStack)
			stackPosition[hitIndex] = noInStack;
		else {
			for (int i=0 ; i<alreadyUsed.length ; i++)
				if (alreadyUsed[i] && stackIndex[i] == columnIndex && stackPosition[i] >= columnPosition)
					stackPosition[i] ++;
			stackPosition[hitIndex] = columnPosition;
		}
		
		repaint();
	}
	
}
	
