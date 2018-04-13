package exerciseSD;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;
import exercise2.*;
import formula.*;


public class DragMeanMedStackedView extends StackedDotPlotView implements StatusInterface {
//	static public final String DRAG_MEAN_MED = "dragMeanMed";
	
	static final private int NO_DRAG = -1;
	static final private int MEAN_DRAG = 0;
	static final private int MEDIAN_DRAG = 1;
	
	static final private int kMinHitDist = 10;
	static final private int kArrowLength = 6;
	static final private int kArrowValueGap = 2;
	static final private int kTopBorder = 3;
	
	static final private Color kMeanColor = Color.red;
	static final private Color kMeanDimColor = new Color(0xFF9999);
	
	static final private Color kMedianColor = Color.blue;
	static final private Color kMedianDimColor = new Color(0x9999FF);
	
	private NumValue meanGuess, medianGuess;
	private NumValue meanCorrect, medianCorrect;
	
//	private boolean doingDrag = false;
	private int dragType, hitOffset;
	
	public DragMeanMedStackedView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																											String yKey, int meanMedDecimals) {
		super(theData, applet, theAxis);
		
		setActiveNumVariable(yKey);
		
		meanGuess = new NumValue(0.0, meanMedDecimals);
		medianGuess = new NumValue(0.0, meanMedDecimals);
		meanCorrect = new NumValue(0.0, meanMedDecimals);
		medianCorrect = new NumValue(0.0, meanMedDecimals);
		resetMeanMedian();
	}
	
	public void resetMeanMedian() {
		meanGuess.setValue((2 * axis.minOnAxis + axis.maxOnAxis) / 3);
		medianGuess.setValue((2 * axis.maxOnAxis + axis.minOnAxis) / 3);
		
		NumVariable yVar = getNumVariable();
		NumValue sortedY[] = yVar.getSortedData();
		
		double sy = 0.0;
		int n = sortedY.length;
		for (int i=0 ; i<n ; i++)
			sy += sortedY[i].toDouble();
		meanCorrect.setValue(sy / n);
		
		if (n % 2 == 0)
			medianCorrect.setValue((sortedY[n / 2 - 1].toDouble() + sortedY[n / 2].toDouble()) * 0.5);
		else
			medianCorrect.setValue(sortedY[n / 2].toDouble());
	}
	
	public NumValue getMeanGuess() {
		return meanGuess;
	}
	
	public NumValue getMedianGuess() {
		return medianGuess;
	}
	
	public NumValue getCorrectMean() {
		return meanCorrect;
	}
	
	public NumValue getCorrectMedian() {
		return medianCorrect;
	}
	
	public String getStatus() {
		return getMeanGuess() + " " + getMedianGuess();
	}
	
	public void setStatus(String status) {
		StringTokenizer st = new StringTokenizer(status);
		meanGuess.setValue(Double.parseDouble(st.nextToken()));
		medianGuess.setValue(Double.parseDouble(st.nextToken()));
		repaint();
	}
	
	public void setMeanMedianDecimals(int decimals) {
		meanGuess.decimals = decimals;
		medianGuess.decimals = decimals;
		meanCorrect.decimals = decimals;
		medianCorrect.decimals = decimals;
	}
	
	public void showCorrectMeanMedian() {
		meanGuess.setValue(meanCorrect.toDouble());
		medianGuess.setValue(medianCorrect.toDouble());
	}
	
	public double getColumnSlop() {
		int stackPixels = getCrossSize() * 2 + 3;
		double slop = 0;
		try {
			slop = axis.positionToNumVal(stackPixels) - axis.positionToNumVal(0);
		} catch (AxisException e) {
		}
		if (getCrossSize() == DataView.MEDIUM_CROSS)
			slop *= 1.5;
		return slop;
	}
	
	protected void paintBackground(Graphics g) {
		int ascent = g.getFontMetrics().getAscent();
		int meanBaseline = kTopBorder + ascent;
		int medianBaseline = meanBaseline + kArrowValueGap + 2 * kArrowLength + kTopBorder + ascent;
		drawDragArrow(g, meanBaseline, meanGuess, MEAN_DRAG, MText.expandText("x#bar# = "),
																																		kMeanColor, kMeanDimColor);
		drawDragArrow(g, medianBaseline, medianGuess, MEDIAN_DRAG, "Median = ",
																																		kMedianColor, kMedianDimColor);
	}
	
	private void drawDragArrow(Graphics g, int baseline, NumValue val, int drawType, String name,
																																	Color mainColor, Color dimColor) {
		int horiz = axis.numValToRawPosition(val.toDouble());
		int horizPos = translateToScreen(horiz, 0, null).x;
		
		int arrowVert = baseline + kArrowLength + kArrowValueGap;
		
		LabelValue label = new LabelValue(name + val.toString());
		
		if (dragType == drawType) {
			g.setColor(mainColor);
			
			label.drawCentred(g, horizPos, baseline);
			
			g.drawLine(horizPos, arrowVert, horizPos, getSize().height);
			
			g.drawLine(horizPos - kArrowLength, arrowVert, horizPos + kArrowLength, arrowVert);
			g.drawLine(horizPos - kArrowLength + 1, arrowVert - 1, horizPos + kArrowLength - 1, arrowVert - 1);
			g.drawLine(horizPos - kArrowLength + 1, arrowVert + 1, horizPos + kArrowLength - 1, arrowVert + 1);
			for (int i=2 ; i<4 ; i++) {
				g.drawLine(horizPos - kArrowLength + i, arrowVert - i, horizPos - kArrowLength + i, arrowVert + i);
				g.drawLine(horizPos + kArrowLength - i, arrowVert - i, horizPos + kArrowLength - i, arrowVert + i);
			}
		}
		else {
			if (dragType != NO_DRAG)
				g.setColor(dimColor);
			else
				g.setColor(mainColor);
			
			label.drawCentred(g, horizPos, baseline);
			
			g.drawLine(horizPos, arrowVert, horizPos, getSize().height);
			
			g.drawLine(horizPos - kArrowLength, arrowVert, horizPos + kArrowLength, arrowVert);
			g.drawLine(horizPos - kArrowLength, arrowVert, horizPos - kArrowLength + 3, arrowVert - 3);
			g.drawLine(horizPos - kArrowLength, arrowVert, horizPos - kArrowLength + 3, arrowVert + 3);
			g.drawLine(horizPos + kArrowLength, arrowVert, horizPos + kArrowLength - 3, arrowVert - 3);
			g.drawLine(horizPos + kArrowLength, arrowVert, horizPos + kArrowLength - 3, arrowVert + 3);
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
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		Point p = translateFromScreen(x, y, null);
		
		int meanPos = axis.numValToRawPosition(meanGuess.toDouble());
		int medianPos = axis.numValToRawPosition(medianGuess.toDouble());
		
		int meanOffset = meanPos - p.x;
		int medianOffset = medianPos - p.x;
		if (Math.abs(meanOffset) > kMinHitDist && Math.abs(medianOffset) > kMinHitDist)
			return null;
		else if (Math.abs(meanOffset) > Math.abs(medianOffset))
			return new HorizDragPosInfo(x, MEDIAN_DRAG, medianOffset);
		else
			return new HorizDragPosInfo(x, MEAN_DRAG, meanOffset);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		else
			return new HorizDragPosInfo(x);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		HorizDragPosInfo dragPos = (HorizDragPosInfo)startInfo;
		dragType = dragPos.index;
		hitOffset = dragPos.hitOffset;
		
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null) {
			HorizDragPosInfo dragPos = (HorizDragPosInfo)toPos;
			Point p = translateFromScreen(dragPos.x, 0, null);
			
			
			try {
				double dragVal = axis.positionToNumVal(p.x + hitOffset);
				if (dragType == MEAN_DRAG)
					meanGuess.setValue(dragVal);
				else 																//	MEDIAN_DRAG
					medianGuess.setValue(dragVal);
				repaint();
			} catch (AxisException e) {
			}
			
			((ExerciseApplet)getApplet()).noteChangedWorking();
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		dragType = NO_DRAG;
		repaint();
	}
}