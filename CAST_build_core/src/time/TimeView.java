package time;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import axis.*;

import timeProg.*;


public class TimeView extends DataView {
//	static public final String TIME_PLOT = "timePlot";
	static protected final int kHalfHiliteWidth = 5;
	static private final int kMaxSmoothedLines = 4;
	
	static final private Color kPinkColor = new Color(0xFFCCCC);
	static final private Color kShadeColor = new Color(0x00FF00);
	
	private TimeAxis timeAxis;
	private VertAxis numAxis;
	
	protected String smoothedKey[] = new String[kMaxSmoothedLines];
	protected int noOfSmoothedLines = 0;
	private boolean sourceShading = false;
	
	private Color lineColor[] = {Color.blue, Color.red, Color.green};
	
	public TimeView(DataSet theData, XApplet applet, TimeAxis timeAxis, VertAxis numAxis) {
		super(theData, applet, new Insets(5, 5, 5, 5));
																//		5 pixels round for crosses to overlap into
		this.timeAxis = timeAxis;
		this.numAxis = numAxis;
	}
	
	public void setSmoothedVariable(String smoothedKey) {
		if (smoothedKey == null)
			noOfSmoothedLines = 0;
		else {
			this.smoothedKey[0] = smoothedKey;
			noOfSmoothedLines = 1;
		}
	}
	
	public void addSmoothedVariable(String smoothedKey) {
		if (noOfSmoothedLines < kMaxSmoothedLines) {
			this.smoothedKey[noOfSmoothedLines] = smoothedKey;
			noOfSmoothedLines ++;
		}
	}
	
	public void setLineColors(Color[] lineColor) {
		this.lineColor = lineColor;
	}
	
	protected String getSmoothedKey(int index) {
		return smoothedKey[index];
	}
	
	public void setSourceShading(boolean sourceShading) {
		this.sourceShading = sourceShading;
		repaint();
	}
	
	public boolean getSourceShading() {
		return sourceShading;
	}

//----------------------------------------------------------------------
	
	protected TimeAxis getTimeAxis() {
		return timeAxis;
	}
	
	protected VertAxis getVertAxis() {
		return numAxis;
	}
	
	protected Point getScreenPoint(int index, double theVal, Point thePoint) {
		if (Double.isNaN(theVal))
			return null;
		else
			try {
//				int vertPos = numAxis.numValToPosition(theVal);
				int vertPos = numAxis.numValToRawPosition(theVal);
				int horizPos = timeAxis.timePosition(index);
				return translateToScreen(horizPos, vertPos, thePoint);
			} catch (AxisException ex) {
				return null;
			}
	}
	
	protected Point getScreenBefore(int index, Point thePoint) {
		int horizPos;
		try {
			horizPos = timeAxis.timePositionBefore(index);
		} catch (AxisException ex) {
			if (ex.axisProblem == AxisException.TOO_LOW_ERROR)
				horizPos = 0;
			else
				horizPos = timeAxis.getAxisLength() - 1;
		}
		return translateToScreen(horizPos, 0, thePoint);
	}
	
	protected Color getHiliteColor() {
		return BasicTimeApplet.kActualColor;
	}
	
	protected Color getErrorColor() {
		return Color.red;
	}
	
	protected Color getSmoothedColor() {
		return BasicTimeApplet.kSmoothedColor;
	}
	
	protected Color getCrossColor() {
		return Color.black;
	}
	
	protected Color getLineColor(int keyIndex) {
		return (keyIndex < lineColor.length) ? lineColor[keyIndex]
															: lineColor[lineColor.length - 1];
	}
	
	private Color getShadeColor() {
		return kShadeColor;
	}
	
	private Color getUnknownSmoothedColor() {
		return kPinkColor;
	}
	
	protected void drawDotPlot(Graphics g, NumVariable variable) {
		if (variable.noOfValues() > 300)
			setCrossSize(DOT_CROSS);
		else if (variable.noOfValues() > 150)
			setCrossSize(SMALL_CROSS);
		else
			setCrossSize(MEDIUM_CROSS);
		
		Point thePoint = null;
		Point smoothPoint = null;
		
		int selectedIndex = getSelection().findSingleSetFlag();
		
		if (selectedIndex >= 0 && selectedIndex < variable.noOfValues()) {
			NumValue theVal = (NumValue)variable.valueAt(selectedIndex);
			thePoint = getScreenPoint(selectedIndex, theVal.toDouble(), thePoint);
			if (noOfSmoothedLines == 1) {
				NumVariable smoothedVariable = (NumVariable)getVariable(smoothedKey[0]);
				double smoothedVal = smoothedVariable.doubleValueAt(selectedIndex);
				smoothPoint = getScreenPoint(selectedIndex, smoothedVal, smoothPoint);
				if (thePoint != null && smoothPoint != null) {
					g.setColor(getErrorColor());
					int top = Math.min(thePoint.y, smoothPoint.y);
					int bottom = Math.max(thePoint.y, smoothPoint.y);
					g.drawLine(thePoint.x, top, thePoint.x, bottom - 1);
				}
				if (smoothPoint != null) {
					g.setColor(getSmoothedColor());
					drawBlob(g, smoothPoint);
				}
			}
		}
		g.setColor(getCrossColor());
		ValueEnumeration e = variable.values();
		int index = 0;
		while (e.hasMoreValues()) {
			double nextVal = e.nextDouble();
			thePoint = getScreenPoint(index, nextVal, thePoint);
			if (thePoint != null)
				drawSquare(g, thePoint);
			index++;
		}
	}
	
	protected void drawSmoothed(Graphics g, int keyIndex) {
		NumVariable smoothedVariable = (NumVariable)getVariable(smoothedKey[keyIndex]);
		g.setColor(getLineColor(keyIndex));
		Point lastPoint = null;
		Point thisPoint = null;
		int index = 0;
		ValueEnumeration e = smoothedVariable.values();
		while (e.hasMoreValues()) {
			thisPoint = getScreenPoint(index, e.nextDouble(), thisPoint);
			if (thisPoint != null && lastPoint != null)
				g.drawLine(lastPoint.x, lastPoint.y, thisPoint.x, thisPoint.y);
			index++;
			Point temp = lastPoint;
			lastPoint = thisPoint;
			thisPoint = temp;
		}
	}
	
	private int getMinSmoothed(String key) {
		int minWithSmoothed = -1;
		NumVariable smoothedVariable = (NumVariable)getVariable(key);
//		if (smoothedVariable instanceof StoredFunctVariable) {
			for (int i=0 ; i<smoothedVariable.noOfValues() ; i++)
				if (!Double.isNaN(smoothedVariable.doubleValueAt(i))) {
					minWithSmoothed = i;
					break;
				}
//		}
		return minWithSmoothed;
	}
	
	private int getMaxSmoothed(String key) {
		NumVariable smoothedVariable = (NumVariable)getVariable(key);
		int maxWithSmoothed = smoothedVariable.noOfValues();
//		if (smoothedVariable instanceof StoredFunctVariable) {
			for (int i=maxWithSmoothed-1 ; i>=0 ; i--)
				if (!Double.isNaN(smoothedVariable.doubleValueAt(i))) {
					maxWithSmoothed = i;
					break;
				}
//		}
		return maxWithSmoothed;
	}
	
	protected int firstSmoothedForShading() {
					//		will allow SeasonalEffectApplet to exclude data
		return 0;
	}
	
	protected void drawBackground(Graphics g, NumVariable variable) {
		int firstShadeIndex = firstSmoothedForShading();
		if (noOfSmoothedLines > firstShadeIndex) {
			g.setColor(getUnknownSmoothedColor());
			
			int firstSmoothedMin = getMinSmoothed(smoothedKey[firstShadeIndex]);
			for (int i=firstShadeIndex+1 ; i<noOfSmoothedLines ; i++)
				if (getMinSmoothed(smoothedKey[i]) != firstSmoothedMin)
					firstSmoothedMin = -1;
			
			int minRaw = getMinSmoothed(getActiveNumKey());
			Point thePoint = null;
			if (firstSmoothedMin > minRaw) {
				thePoint = getScreenBefore(firstSmoothedMin, thePoint);
				g.fillRect(0, 0, thePoint.x, getSize().height);
			}
				
			int firstSmoothedMax = getMaxSmoothed(smoothedKey[firstShadeIndex]);
			int nVals = ((NumVariable)getVariable(smoothedKey[firstShadeIndex])).noOfValues();
			for (int i=firstShadeIndex+1 ; i<noOfSmoothedLines ; i++)
				if (getMaxSmoothed(smoothedKey[i]) != firstSmoothedMax)
					firstSmoothedMax = nVals;
					
			int maxRaw = getMaxSmoothed(getActiveNumKey());
			if (firstSmoothedMax < maxRaw) {
				thePoint = getScreenBefore(firstSmoothedMax + 1, thePoint);
				g.fillRect(thePoint.x, 0, getSize().width - thePoint.x, getSize().height);
			}
		}
				
		if (!sourceShading) {
			int selectedIndex = getSelection().findSingleSetFlag();
			if (selectedIndex >= 0) {
				g.setColor(getShadeColor());
				try {
					int horizPos = timeAxis.timePosition(selectedIndex);
					Point thePoint = translateToScreen(horizPos, 0, null);
					g.drawLine(thePoint.x, 0, thePoint.x, getSize().height);
				} catch (AxisException ex) {
				}
			}
		}
		else if (noOfSmoothedLines == 1) {
			NumVariable smoothedVariable = (NumVariable)getVariable(smoothedKey[0]);
			if (smoothedVariable instanceof StoredFunctVariable) {
				StoredFunctVariable smoothed = (StoredFunctVariable)smoothedVariable;
				int selectedIndex = getSelection().findSingleSetFlag();
				
				if (selectedIndex >= 0) {
					g.setColor(getShadeColor());
					double smoothedVal = smoothed.doubleValueAt(selectedIndex);
					if (Double.isNaN(smoothedVal)) {
						Point thePoint = getScreenPoint(selectedIndex,
																variable.doubleValueAt(selectedIndex), null);
						if (thePoint != null)
							g.drawLine(thePoint.x, 0, thePoint.x, getSize().height);
					}
					else
						shadeInfluence(g, smoothed, selectedIndex);
				}
			}
		}
	}
	
	protected void shadeInfluence(Graphics g, StoredFunctVariable smoothed, int selectedIndex) {
		Point thePoint = getScreenBefore(smoothed.getMinInfluence(selectedIndex), null);
		int lowGrayPos = thePoint.x;
		thePoint = getScreenBefore(smoothed.getMaxInfluence(selectedIndex) + 1, thePoint);
		int highGrayPos = thePoint.x;
		g.fillRect(lowGrayPos, 0, (highGrayPos - lowGrayPos), getSize().height);
	}
	
	public void paintView(Graphics g) {
		NumVariable variable = getNumVariable();
		
		drawBackground(g, variable);
		for (int i=0 ; i<noOfSmoothedLines ; i++)
			drawSmoothed(g, i);
		drawDotPlot(g, variable);
	}

//-----------------------------------------------------------------------------------
	
	protected void doTransformView(Graphics g, NumCatAxis theAxis) {
		if (theAxis == numAxis)
			repaint();
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		boolean needsRepaint = key.equals(getActiveNumKey());
		for (int i=0 ; i<noOfSmoothedLines ; i++)
			needsRepaint = needsRepaint || key.equals(smoothedKey[i]);
		if (needsRepaint)
			repaint();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		try {
			Point hitPos = translateFromScreen(x, y, null);
			return new IndexPosInfo(timeAxis.positionToIndex(hitPos.x));
		} catch (AxisException e) {
			return null;
		}
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo == null)
			getData().clearSelection();
		else
			getData().setSelection(((IndexPosInfo)startInfo).itemIndex);
		requestFocus();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
	}

//-----------------------------------------------------------------------------------

	
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		NumVariable variable = getNumVariable();
		int maxIndex = variable.noOfValues() - 1;
		int currentSelectedIndex = getSelection().findSingleSetFlag();
		
		if (key == KeyEvent.VK_LEFT && currentSelectedIndex > 0)
			getData().setSelection(currentSelectedIndex - 1);
		else if (key == KeyEvent.VK_RIGHT && currentSelectedIndex < maxIndex)
			getData().setSelection(currentSelectedIndex + 1);
	}

}
	
