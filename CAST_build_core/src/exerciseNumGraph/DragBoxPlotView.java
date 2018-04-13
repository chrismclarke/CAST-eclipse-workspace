package exerciseNumGraph;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import utils.*;
import exercise2.*;

//import boxPlot.*;


public class DragBoxPlotView extends MarginalDataView implements BoxPlotConstants, StatusInterface {
//	static public final String DRAG_BOX_PLOT = "dragBoxPlot";
	
	static final private int kQuartileHitSlop = 4;
	static final private int kMinDisplayWidth = 20;
	static final private int kValueTopBorder = 5;
	static final private int kValueGap = 2;
	static final private int kValueBottomBorder = 5;
	static final private int kArrowHead = 3;
	static final private int kArrowHeight = 7;
	
	private String yKey;
	
	private boolean initialised = false;
	private BoxInfo boxInfo = new BoxInfo();
	private int value1Baseline, value2Baseline, iqr1Baseline, iqr2Baseline, iqr3Baseline;
	
	private boolean showOutliers = true;
	private Color fillColor = null;
	
	private boolean[] correct = null;
	
	private int selectedQuartile = NO_SELECTED_QUART;
	private int hitOffset;
	
	public DragBoxPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis, String yKey) {
		super(theData, applet, new Insets(0, 5, 0, 5), theAxis);
		this.yKey = yKey;
	}
	
	public void setShowOutliers(boolean showOutliers) {
		this.showOutliers = showOutliers;
	}
	
	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

//-------------------------------------------------------------------------
	
	public String getStatus() {
		String s = "";
		for (int i=0 ; i<5 ; i++)
			s += boxInfo.boxVal[i] + " ";
		return s;
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		for (int i=0 ; i<5 ; i++)
			boxInfo.boxVal[i] = Double.parseDouble(st.nextToken());
		reset();
		repaint();
	}

//-------------------------------------------------------------------------
	
	protected void doInitialisation(Graphics g) {
		int ascent = g.getFontMetrics().getAscent();
		value1Baseline = kValueTopBorder + ascent;
		value2Baseline = value1Baseline + kValueGap + ascent;
		
		iqr1Baseline = getSize().height - kValueTopBorder;
		iqr2Baseline = iqr1Baseline - 2 * kArrowHeight - ascent;
		iqr3Baseline = iqr2Baseline - 2 * ascent;
		
		if (fillColor != null)
			boxInfo.setFillColor(fillColor);
		boxInfo.boxBottom = getBoxBottom();
		boxInfo.setBoxHeight(20);
		boxInfo.vertMidLine = boxInfo.boxBottom + boxInfo.getBoxHeight() / 2;
		
		boxInfo.setupBoxPositions(axis);
		if (showOutliers) {
			NumVariable yVar = (NumVariable)getVariable(yKey);
			boxInfo.countOutliers(yVar.getSortedData(), axis);
		}
		else
			boxInfo.clearOutliers();
	}
	
	
	final public void initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
		}
	}
	
	final public void reset() {
		initialised = false;
		correct = null;
	}
	
	private int getBoxBottom() {
		return (getSize().height - BoxInfo.kBoxHeight) / 2;
	}

//-------------------------------------------------------------------------
	
	public void setCorrectBox() {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		NumValue sortedY[] = yVar.getSortedData();
		
		boxInfo.initialiseBox(sortedY, showOutliers, axis);
		correct = null;
	}
	
	public boolean[] correctQuartiles() {
		boolean correctQuartile[] = new boolean[5];
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		NumValue sortedY[] = yVar.getSortedData();
		int noOfVals = sortedY.length;
		
		double slop1Pix = (axis.maxOnAxis - axis.minOnAxis) / 400;		//	1 pixel if axis is 400 pixels long
		double slop2Pix = slop1Pix * 2;		//	2 pixels if axis is 400 pixels long
		
		double median = ((noOfVals & 0x1) == 1) ? sortedY[noOfVals / 2].toDouble()
						: (sortedY[noOfVals / 2 - 1].toDouble() + sortedY[noOfVals / 2].toDouble()) * 0.5;
		correctQuartile[MEDIAN] = Math.abs(median - boxInfo.boxVal[MEDIAN]) < slop2Pix;
		
		int noBeyondQuartile = noOfVals / 4;
		double lowQuartile0 = sortedY[noBeyondQuartile - 1].toDouble();
		double lowQuartile1 = sortedY[noBeyondQuartile].toDouble();
		correctQuartile[LOW_QUART] = boxInfo.boxVal[LOW_QUART] > lowQuartile0 - slop1Pix
																							&& boxInfo.boxVal[LOW_QUART] < lowQuartile1 + slop1Pix;
		
		double highQuartile0 = sortedY[noOfVals - noBeyondQuartile - 1].toDouble();
		double highQuartile1 = sortedY[noOfVals - noBeyondQuartile].toDouble();
		correctQuartile[HIGH_QUART] = boxInfo.boxVal[HIGH_QUART] > highQuartile0 - slop1Pix
																							&& boxInfo.boxVal[HIGH_QUART] < highQuartile1 + slop1Pix;
		if (showOutliers) {
			double guessIQR = boxInfo.boxVal[HIGH_QUART] - boxInfo.boxVal[LOW_QUART];
			
			double highWhisker = boxInfo.boxVal[HIGH_QUART] + 1.5 * guessIQR;
			for (int i=noOfVals-1 ; i>=0 ; i--)
				if (sortedY[i].toDouble() <= highWhisker) {
					highWhisker = sortedY[i].toDouble();
					break;
				}
			correctQuartile[HIGH_EXT] = Math.abs(boxInfo.boxVal[HIGH_EXT] - highWhisker) < slop2Pix;
			
			double lowWhisker = boxInfo.boxVal[LOW_QUART] - 1.5 * guessIQR;
			for (int i=0 ; i<noOfVals ; i++)
				if (sortedY[i].toDouble() >= lowWhisker) {
					lowWhisker = sortedY[i].toDouble();
					break;
				}
			correctQuartile[LOW_EXT] = Math.abs(boxInfo.boxVal[LOW_EXT] - lowWhisker) < slop2Pix;
		}
		else {
			double lowExt = sortedY[0].toDouble();
			correctQuartile[LOW_EXT] = Math.abs(lowExt - boxInfo.boxVal[LOW_EXT]) < slop2Pix;
			double highExt = sortedY[noOfVals - 1].toDouble();
			correctQuartile[HIGH_EXT] = Math.abs(highExt - boxInfo.boxVal[HIGH_EXT]) < slop2Pix;
		}
		return correctQuartile;
	}
	
	public boolean whiskersTooLong() {
		double guessIQR = boxInfo.boxVal[HIGH_QUART] - boxInfo.boxVal[LOW_QUART];
		double highWhisker = boxInfo.boxVal[HIGH_EXT] - boxInfo.boxVal[HIGH_QUART];
		double lowWhisker = boxInfo.boxVal[LOW_QUART] - boxInfo.boxVal[LOW_EXT];
		return (lowWhisker > 1.5 * guessIQR) || (highWhisker > 1.5 * guessIQR);
	}
	
	public void showWrongQuartiles(boolean[] correct) {
		this.correct = correct;
	}
	
	public void setDefaultBox() {
		double axisRange = axis.maxOnAxis - axis.minOnAxis;
		for (int i=LOW_EXT ; i<=HIGH_EXT ; i++)
			boxInfo.boxVal[i] = axis.minOnAxis + axisRange * (2 * i + 1) * 0.1;
	}
	
	private void drawBackground(Graphics g) {
		Point p = new Point(0,0);
		NumVariable yVar = (NumVariable)getVariable(yKey);
		int decimals = yVar.getMaxDecimals();
		NumValue val = new NumValue(0, decimals);
		
		int lowExtPos = drawOneValue(g, val, LOW_EXT, value1Baseline, p);
//		int medianPos = drawOneValue(g, val, MEDIAN, value1Baseline, p);
		int highExtPos = drawOneValue(g, val, HIGH_EXT, value1Baseline, p);
		
		int lowQuartPos = drawOneValue(g, val, LOW_QUART, value2Baseline, p);
		int highQuartPos = drawOneValue(g, val, HIGH_QUART, value2Baseline, p);
		
		if (showOutliers) {
			g.setColor(Color.red);
			drawRange(g, val, LOW_QUART, HIGH_QUART, lowQuartPos, highQuartPos, iqr1Baseline, true, p);
			
			g.setColor(Color.blue);
			drawRange(g, val, LOW_EXT, LOW_QUART, lowExtPos, lowQuartPos, iqr2Baseline, false, p);
			drawRange(g, val, HIGH_QUART, HIGH_EXT, highQuartPos, highExtPos, iqr2Baseline, false, p);
			
			g.setColor(Color.gray);
			int center = (lowQuartPos + highQuartPos) / 2;
			double range = 1.5 * (boxInfo.boxVal[HIGH_QUART] - boxInfo.boxVal[LOW_QUART]);
			val.setValue(range);
			LabelValue l = new LabelValue("(1.5 " + getApplet().translate("IQR") + " = " + val + ")");
			l.drawCentred(g, center, iqr3Baseline);
		}
	}
	
	private void drawRange(Graphics g, NumValue val, int lowIndex, int highIndex, 
														int lowPos, int highPos, int baseline, boolean arrowAbove, Point p) {
		int center = (lowPos + highPos) / 2;
		double range = boxInfo.boxVal[highIndex] - boxInfo.boxVal[lowIndex];
		val.setValue(range);
		val.drawCentred(g, center, baseline);
		
		int lineVert = arrowAbove ? baseline - g.getFontMetrics().getAscent() - 3 : baseline + 3;
		g.drawLine(lowPos + 1, lineVert, highPos - 1, lineVert);
		g.fillRect(lowPos + 2, lineVert - 1, highPos - lowPos - 3, 3);
		
		int arrowLength = Math.min(kArrowHead, (highPos - lowPos) / 4);
		g.drawLine(lowPos + 1, lineVert, lowPos + 1 + arrowLength, lineVert + arrowLength);
		g.drawLine(lowPos + 1, lineVert, lowPos + 1 + arrowLength, lineVert - arrowLength);
		
		g.drawLine(highPos - 1, lineVert, highPos - 1 - arrowLength, lineVert + arrowLength);
		g.drawLine(highPos - 1, lineVert, highPos - 1 - arrowLength, lineVert - arrowLength);
	}
	
	private int drawOneValue(Graphics g, NumValue val, int valIndex, int baseline, Point p) {
		boolean isSelected = valIndex == selectedQuartile;
		boolean isWrong = (correct !=  null) && !correct[valIndex];
		int lineTop = baseline + kValueBottomBorder;
		p = translateToScreen(boxInfo.boxPos[valIndex], 0, p);
		
		if (isSelected) {
			g.setColor(Color.yellow);
			g.fillRect(p.x - 2, lineTop, 5, getSize().height - lineTop);
		}
		
		g.setColor(isWrong ? Color.red : isSelected ? getForeground() : Color.lightGray);
		g.drawLine(p.x, lineTop, p.x, getSize().height);
		
		val.setValue(boxInfo.boxVal[valIndex]);
		int valWidth = val.stringWidth(g);
		int center = Math.max(valWidth / 2, Math.min(getSize().width - valWidth / 2, p.x));
		val.drawCentred(g, center, baseline);
		
		return p.x;
	}
	
	public void paintView(Graphics g) {
		if (!initialised)
			initialise(g);
		
		drawBackground(g);
		
		g.setColor(getForeground());
		NumVariable yVar = (NumVariable)getVariable(yKey);
		boxInfo.drawBoxPlot(g, this, yVar.getSortedData(), axis);
	}

//-----------------------------------------------------------------------------------

	public int minDisplayWidth() {
		return kMinDisplayWidth;
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(getActiveNumKey())) {
			initialised = false;
			super.doChangeVariable(g, key);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		int hitIndex = NO_SELECTED_QUART;
		int hitOffset = kQuartileHitSlop;
		int absOffset = kQuartileHitSlop;
		for (int i=LOW_EXT ; i<=HIGH_EXT ; i++) {
			int thisHitOffset = hitPos.x - boxInfo.boxPos[i];
			int thisAbsOffset = Math.abs(thisHitOffset);
			if (thisAbsOffset < absOffset) {
				hitIndex = i;
				hitOffset = thisHitOffset;
				absOffset = thisAbsOffset;
			}
		}
		if (hitIndex == NO_SELECTED_QUART)
			return null;
		else
			return new HorizDragPosInfo(hitPos.x, hitIndex, hitOffset);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.x - hitOffset < 0
														|| hitPos.x - hitOffset >= axis.getAxisLength())
			return null;
		else
			return new HorizDragPosInfo(hitPos.x);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo == null) {
			if (selectedQuartile != NO_SELECTED_QUART) {
				selectedQuartile = NO_SELECTED_QUART;
				repaint();
			}
			return false;
		}
		else {
			HorizDragPosInfo dragPos = (HorizDragPosInfo)startInfo;
			hitOffset = dragPos.hitOffset;
			selectedQuartile = dragPos.index;
			correct = null;
			repaint();
			return true;
		}
	}
	
	private void setSelectedQuartile(int newPos) {
		if (newPos < 0 || newPos >= axis.getAxisLength())
			return;
		int newRank = 0;
		for (int i=LOW_EXT ; i<=HIGH_EXT ; i++)
			if (i != selectedQuartile && newPos > boxInfo.boxPos[i])
				newRank++;
		if (newRank > selectedQuartile) {
			for (int i=selectedQuartile ; i<newRank ; i++) {
				boxInfo.boxPos[i] = boxInfo.boxPos[i+1];
				boxInfo.boxVal[i] = boxInfo.boxVal[i+1];
			}
			selectedQuartile = newRank;
		}
		else if (newRank < selectedQuartile) {
			for (int i=selectedQuartile ; i>newRank ; i--) {
				boxInfo.boxPos[i] = boxInfo.boxPos[i-1];
				boxInfo.boxVal[i] = boxInfo.boxVal[i-1];
			}
			selectedQuartile = newRank;
		}
		
		boxInfo.boxPos[selectedQuartile] = newPos;
		try {
			BoxInfo correct = new BoxInfo();
			NumVariable yVar = (NumVariable)getVariable(yKey);
			NumValue sortedY[] = yVar.getSortedData();
			
			correct.initialiseBox(sortedY, showOutliers, axis);
			boolean doingSnap = false;
			for (int i=0 ; i<5 ; i++)
				if (correct.boxPos[i] == newPos) {
					boxInfo.boxVal[selectedQuartile] = correct.boxVal[i];
					doingSnap = true;
					break;
				}
			if (!doingSnap && showOutliers) {
				double minY = sortedY[0].toDouble();
				int minPos = axis.numValToRawPosition(minY);
				if (minPos == newPos) {
					boxInfo.boxVal[selectedQuartile] = minY;
					doingSnap = true;
				}
				else {
					double maxY = sortedY[sortedY.length - 1].toDouble();
					int maxPos = axis.numValToRawPosition(maxY);
					if (maxPos == newPos) {
						boxInfo.boxVal[selectedQuartile] = maxY;
						doingSnap = true;
					}
				}
			}
			if (!doingSnap)
				boxInfo.boxVal[selectedQuartile] = axis.positionToNumVal(newPos);
				
		} catch (AxisException e) {
		}
		
		if (showOutliers) {
			NumVariable yVar = (NumVariable)getVariable(yKey);
			boxInfo.countOutliers(yVar.getSortedData(), axis);
		}
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null) {
			HorizDragPosInfo dragPos = (HorizDragPosInfo)toPos;
			setSelectedQuartile(dragPos.x - hitOffset);
			repaint();
		
			((ExerciseApplet)getApplet()).noteChangedWorking();
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
	}
	

//-----------------------------------------------------------------------------------

	public void mousePressed(MouseEvent e) {
		requestFocus();
		super.mousePressed(e);
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		
		if (key == KeyEvent.VK_LEFT) {
			if (selectedQuartile != NO_SELECTED_QUART) {
				setSelectedQuartile(boxInfo.boxPos[selectedQuartile] - 1);
				repaint();
				
				((ExerciseApplet)getApplet()).noteChangedWorking();
			}
		}
		else if (key == KeyEvent.VK_RIGHT) {
			if (selectedQuartile != NO_SELECTED_QUART) {
				setSelectedQuartile(boxInfo.boxPos[selectedQuartile] + 1);
				repaint();
				
				((ExerciseApplet)getApplet()).noteChangedWorking();
			}
		}
	}
	
	public void focusLost(FocusEvent e) {
		if (selectedQuartile != NO_SELECTED_QUART) {
			selectedQuartile = NO_SELECTED_QUART;
			repaint();
		}
	}
	
}
	
