package coreGraphics;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import axis.*;


public class StackedDiscreteView extends DotPlotView implements BackgroundHiliteInterface {
//	static public final String STACKED_DISCRETE = "stackedDiscrete";
	
	static public final int CROSS_DISPLAY = 0;
	static public final int HISTO_DISPLAY = 1;
	static public final int NO_DISPLAY = 2;
	static public final int BAR_DISPLAY = 3;
	
	static final private int kSizeIncrement = 20;
	static final private double kSampleBarWidth = 0.3;
	
	static private final int kOffAxisPos = -100;		//	a value small enough to be off the axes after grouping
	
	static final private Color kStandardHistoFill = new Color(0xCCCCCC);
	static final private Color kHighlightHistoFill = new Color(0x999999);
	static final private Color kStandardHistoBorder = new Color(0x666666);
	static final private Color kHighlightHistoBorder = Color.black;
	
	private boolean initialised = false;
	private boolean hiliteBackground = false;
	
	private int noOfCrosses;
	
	private int vertPos[] = null;
	private int horizPos[] = null;
	private int minIndex[];
	protected int maxStackHeight;
	
	protected double step;
	
	private int displayType = CROSS_DISPLAY;
	
	protected double highlightVal = Double.NEGATIVE_INFINITY;
	protected int highlightSide = LOW_HIGHLIGHT;
	private Color highlightBackgroundColor = kPaleRedColor;
	
	private double halfBarWidth = 0.5;
	
	public StackedDiscreteView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																															String yKey, double step) {
		super(theData, applet, theAxis, 0.0);
		setActiveNumVariable(yKey);
		this.step = step;
		addComponentListener(this);
	}
	
	public StackedDiscreteView(DataSet theData, XApplet applet, NumCatAxis theAxis, String yKey) {
		this(theData, applet, theAxis, yKey, 1.0);
	}
	
	public void setDrawTheory(boolean drawTheory) {
	}
	
	public void setDisplayType(int displayType) {
		this.displayType = displayType;
		if (displayType == BAR_DISPLAY)
			halfBarWidth = kSampleBarWidth / 2;
		else if (displayType == HISTO_DISPLAY)
			halfBarWidth = 0.5;
	}
	
	public void setCrossHighlight(double highlightVal, int highlightSide) {
		this.highlightVal = highlightVal;
		this.highlightSide = highlightSide;
	}
	
	public void setHighlightBackground(boolean hiliteBackground) {
		this.hiliteBackground = hiliteBackground;
	}
	
	public void setHighlightColor(Color highlightBackgroundColor) {
		this.highlightBackgroundColor = highlightBackgroundColor;
	}
	
	protected boolean initialise() {
		if (initialised)
			return false;
		
		NumVariable y = getNumVariable();
		int noOfValues = y.noOfValues();
		
		if (horizPos == null || horizPos.length < noOfValues)
			horizPos = new int[noOfValues];
		if (vertPos == null || vertPos.length < noOfValues)
			vertPos = new int[noOfValues];
		
		int iMinOnAxis = (int)Math.ceil(axis.minOnAxis / step - 0.00001);
		int iMaxOnAxis = (int)Math.floor(axis.maxOnAxis / step + 0.00001);
		minIndex = new int[iMaxOnAxis - iMinOnAxis + 1];
		
		addPositions(y, 0);
		
		initialised = true;
		return true;
	}
	
	private int[] resizeArray(int[] array, int newSize) {
		if (array.length < newSize) {
			int[] temp = array;
			array = new int[newSize + kSizeIncrement];
			System.arraycopy(temp, 0, array, 0, temp.length);
		}
		return array;
	}
	
	private void addPositions(NumVariable yVar, int startIndex) {
		int groupSize = getCrossSize() * 2 + 3;
		noOfCrosses = yVar.noOfValues();
		
		int iMinOnAxis = (int)Math.ceil(axis.minOnAxis / step - 0.00001);
		int iMaxOnAxis = (int)Math.floor(axis.maxOnAxis / step + 0.00001);
	
		for (int i=startIndex ; i<noOfCrosses ; i++)
			try {
				double y = ((NumValue)yVar.valueAt(i)).toDouble();
				horizPos[i] = axis.numValToPosition(y);
				int iy = (int)Math.round(y / step);
				int groupIndex = iy - iMinOnAxis;
				int vertIndex = minIndex[groupIndex];
				vertPos[i] = vertIndex * groupSize;
				minIndex[groupIndex] ++;
				
			} catch (AxisException ex) {
				horizPos[i] = kOffAxisPos;
			}
		
		int maxIndex = 0;
		for (int i=0 ; i<=iMaxOnAxis-iMinOnAxis ; i++)
			if (minIndex[i] > maxIndex)
				maxIndex = minIndex[i];
		maxStackHeight = (maxIndex + 1) * groupSize;
	}
	
	private void addCrossInfo() {
		NumVariable yVar = getNumVariable();
		int noOfValues = yVar.noOfValues();
		
		if (horizPos == null || noOfCrosses >= noOfValues) {
			initialised = false;
			initialise();
			return;
		}
		
		horizPos = resizeArray(horizPos, noOfValues);
		vertPos = resizeArray(vertPos, noOfValues);
		
		addPositions(yVar, noOfCrosses);
	}
	
	protected void checkJittering() {
	}
	
	protected int getMaxStackHeight() {
		return maxStackHeight;
	}
	
	public void setCrossSize(int crossSize) {
		super.setCrossSize(crossSize);
		initialised = false;
		repaint();
	}
	
	protected int translateHt(int rawHt) {
		int displayHeight = getDisplayWidth();
		if (maxStackHeight > displayHeight)
			rawHt = rawHt * displayHeight / maxStackHeight;
		return rawHt;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		if (horizPos == null || horizPos[index] < 0)
			return null;
		
		return translateToScreen(horizPos[index], translateHt(vertPos[index]), thePoint);
	}
	
	protected int groupIndex(int itemIndex) {
		NumVariable variable = getNumVariable();
		double value = variable.doubleValueAt(itemIndex);
		
		return ((value <= highlightVal) == (highlightSide == LOW_HIGHLIGHT)) ? 2 : 0;
	}
	
	protected void drawBackground(Graphics g) {
		initialise();
		
		if (hiliteBackground) {
//			double hiliteBorder = highlightVal + (highlightSide == LOW_HIGHLIGHT ? 0.5 : -0.5);
//			int borderPos = axis.numValToRawPosition(hiliteBorder);
			int borderPos = axis.numValToRawPosition(highlightVal);
			Point p = translateToScreen(borderPos, 0, null);
			
			g.setColor(highlightBackgroundColor);
			if (highlightSide == LOW_HIGHLIGHT)
				g.fillRect(0, 0, p.x, getSize().height);
			else
				g.fillRect(p.x, 0, getSize().width, getSize().height);
			g.setColor(Color.red);
			g.drawLine(p.x, 0, p.x, getSize().height);
		}
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		
		if (displayType == CROSS_DISPLAY)
			super.paintView(g);
		else if (displayType == HISTO_DISPLAY || displayType == BAR_DISPLAY) {
			int groupSize = getCrossSize() * 2 + 3;
			
			int iMinOnAxis = (int)Math.ceil(axis.minOnAxis / step - 0.00001);
			int iMaxOnAxis = (int)Math.floor(axis.maxOnAxis / step + 0.00001);
			
			Point p0 = null;
			Point p1 = null;
			
			for (int i=iMinOnAxis ; i<=iMaxOnAxis ; i++) {
				int classLowPos = axis.numValToRawPosition(i - halfBarWidth);
				int classHighPos = axis.numValToRawPosition(i + halfBarWidth);
				if (minIndex[i - iMinOnAxis] > 0) {
					int classVertPos = translateHt(minIndex[i - iMinOnAxis] * groupSize);
					
					p0 = translateToScreen(classLowPos, classVertPos, p0);
					p1 = translateToScreen(classHighPos, 0, p1);
					int topPos = p0.y + getSize().height - p1.y;		//	to make bottom flush with axis
					g.setColor(((i <= highlightVal) == (highlightSide == LOW_HIGHLIGHT))
													? kHighlightHistoFill : kStandardHistoFill);
					g.fillRect(p0.x, topPos, (p1.x - p0.x), (p1.y - p0.y));
					g.setColor(((i <= highlightVal) == (highlightSide == LOW_HIGHLIGHT))
													? kHighlightHistoBorder : kStandardHistoBorder);
					g.drawRect(p0.x, topPos, (p1.x - p0.x), (p1.y - p0.y));
				}
			}
		}
	}

//-----------------------------------------------------------------------------------

	public void componentResized(ComponentEvent e) {
		initialised = false;
	}
	
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(getActiveNumKey()))
			initialised = false;
		super.doChangeVariable(g, key);
	}
	
	protected void doAddValues(Graphics g, int noOfValues) {
		addCrossInfo();
		repaint();
	}
	
	
	protected boolean canDrag() {
		return (displayType == CROSS_DISPLAY);
	}
}