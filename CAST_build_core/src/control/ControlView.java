package control;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;


public class ControlView extends DataView {
//	static public final String CONTROL_PLOT = "controlPlot";
	
	private TimeAxis timeAxis;
	private ControlLimitAxis numAxis;
	private int problemFlags;
	private boolean zoneDisplay = false;
	
	public ControlView(DataSet theData, XApplet applet,
										TimeAxis timeAxis, ControlLimitAxis numAxis, int problemFlags) {
		super(theData, applet, new Insets(5, 5, 5, 5));
																//		5 pixels round for crosses to overlap into
		this.timeAxis = timeAxis;
		this.numAxis = numAxis;
		this.problemFlags = problemFlags;
	}
	
	protected TimeAxis getTimeAxis() {
		return timeAxis;
	}
	
	protected ControlLimitAxis getNumAxis() {
		return numAxis;
	}
	
	public int getProblemFlags() {
		return problemFlags;
	}
	
	public void setZoneDisplay(boolean showNotHide) {
		zoneDisplay = showNotHide;
		repaint();
	}
	
	protected Point getScreenPoint(int index, double theVal, Point thePoint) {
		try {
			int vertPos = numAxis.numValToPosition(theVal);
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
	
//----------------------------------------------------------------------------------
	
	protected void drawNextFrame() {		//		this draws next frame in animation
		if (getCurrentFrame() > 0)
			getData().setSelection(getCurrentFrame() - 1);
		else
			getData().clearSelection();
	}
	
//----------------------------------------------------------------------------------
	
	static final protected Color kHiliteColor = Color.yellow;
	static final protected Color kBlobColor = new Color(0xCC0000);
	static final protected Color kCrossColor = Color.black;
	static final protected Color kLineColor = Color.blue;
	static final protected Color kShadeColor = Color.lightGray;
	static final protected Color kCentreLineColor = Color.gray;
	static final protected Color kZoneLineColor = Color.lightGray;
	
	protected void drawValues(Graphics g, NumVariable variable) {
		g.setColor(kHiliteColor);
		ControlledEnumeration e = new ControlledEnumeration(variable, numAxis, problemFlags, getApplet());
		FlagEnumeration fe = getSelection().getEnumeration();
		int index = 0;
		Point thePoint = null;
		while (e.hasMoreValues()) {
			if (index >= getCurrentFrame())
				break;
			double nextVal = e.nextDouble();
			boolean nextSel = fe.nextFlag();
			if (nextSel) {
				thePoint = getScreenPoint(index, nextVal, thePoint);
				if (thePoint != null)
					drawCrossBackground(g, thePoint);
			}
			index++;
		}
		g.setColor(kCrossColor);
		e = new ControlledEnumeration(variable, numAxis, problemFlags, getApplet());
		index = 0;
		while (e.hasMoreValues()) {
			if (index >= getCurrentFrame())
				break;
			double nextVal = e.nextDouble();
			thePoint = getScreenPoint(index, nextVal, thePoint);
			if (thePoint != null) {
				if (e.getControlProblem() != null) {
					g.setColor(kBlobColor);
					drawBlob(g, thePoint);
					g.setColor(kCrossColor);
				}
				else
					drawSquare(g, thePoint);
			}
			index++;
		}
	}
	
	protected void joinValues(Graphics g, NumVariable variable) {
		g.setColor(kLineColor);
		ControlledEnumeration e = new ControlledEnumeration(variable, numAxis, problemFlags, getApplet());
		Point lastPoint = getScreenPoint(0, e.nextDouble(), null);
		Point thisPoint = null;
		int index = 1;
		while (e.hasMoreValues()) {
			if (index >= getCurrentFrame())
				break;
			thisPoint = getScreenPoint(index, e.nextDouble(), thisPoint);
			if (thisPoint != null && lastPoint != null)
				g.drawLine(lastPoint.x, lastPoint.y, thisPoint.x, thisPoint.y);
			index++;
			Point temp = lastPoint;
			lastPoint = thisPoint;
			thisPoint = temp;
		}
	}
	
	protected void drawBackground(Graphics g, NumVariable variable) {
		g.setColor(kShadeColor);
		
		ControlledEnumeration e = new ControlledEnumeration(variable, numAxis, problemFlags, getApplet());
		FlagEnumeration fe = getSelection().getEnumeration();
		Point startPos = null;
		Point endPos = null;
		int index = 0;
		while (e.hasMoreValues()) {
			if (index >= getCurrentFrame())
				break;
			@SuppressWarnings("unused")
			NumValue nextVal = (NumValue)e.nextValue();
			boolean nextSel = fe.nextFlag();
			if (nextSel) {
				ControlProblem theProblem = e.getControlProblem();
				if (theProblem != null) {
					endPos = getScreenBefore(index + 1, endPos);
					startPos = getScreenBefore(index - theProblem.getPreviousValues(), startPos);
					g.fillRect(startPos.x, 0, (endPos.x - startPos.x), getSize().height);
				}
			}
			index++;
		}
		
		g.setColor(kCentreLineColor);
		int centre = getScreenPoint(0, numAxis.getCentre(), null).y;
		int lowLimit = getScreenPoint(0, numAxis.getLowerLimit(), null).y;
		int highLimit = getScreenPoint(0, numAxis.getUpperLimit(), null).y;
		g.drawLine(0, centre, getSize().width - 1, centre);
		g.drawLine(0, lowLimit, getSize().width - 1, lowLimit);
		g.drawLine(0, highLimit, getSize().width - 1, highLimit);
		
		if (zoneDisplay) {
			g.setColor(kZoneLineColor);
			int lowAB = getScreenPoint(0, numAxis.getLowerABLimit(), null).y;
			int highAB = getScreenPoint(0, numAxis.getUpperABLimit(), null).y;
			int lowBC = getScreenPoint(0, numAxis.getLowerBCLimit(), null).y;
			int highBC = getScreenPoint(0, numAxis.getUpperBCLimit(), null).y;
			g.drawLine(0, lowAB, getSize().width - 1, lowAB);
			g.drawLine(0, highAB, getSize().width - 1, highAB);
			g.drawLine(0, lowBC, getSize().width - 1, lowBC);
			g.drawLine(0, highBC, getSize().width - 1, highBC);
			
			FontMetrics fm = g.getFontMetrics();
			int ascent = fm.getAscent();
			g.drawString("C", 1, (centre + lowBC + ascent) / 2);
			g.drawString("C", 1, (centre + highBC + ascent) / 2);
			g.drawString("B", 1, (lowAB + lowBC + ascent) / 2);
			g.drawString("B", 1, (highAB + highBC + ascent) / 2);
			g.drawString("A", 1, (lowAB + lowLimit + ascent) / 2);
			g.drawString("A", 1, (highAB + highLimit + ascent) / 2);
		}
	}
	
	public void paintView(Graphics g) {
		NumVariable variable = getNumVariable();
		
		drawBackground(g, variable);
		joinValues(g, variable);
		drawValues(g, variable);
	}

//-----------------------------------------------------------------------------------
	
	public void doAnimation(XSlider controller) {
		NumVariable variable = getNumVariable();
		if (getCurrentFrame() < variable.noOfValues())
			animateFrames(getCurrentFrame() + 1, variable.noOfValues(), 3, controller);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		try {
			Point hitPos = translateFromScreen(x, y, null);
			int index = timeAxis.positionToIndex(hitPos.x);
			if (index >= getCurrentFrame())
				return null;
			else
				return new IndexPosInfo(index);
		} catch (AxisException e) {
			return null;
		}
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo == null)
			getData().clearSelection();
		else
			getData().setSelection(((IndexPosInfo)startInfo).itemIndex);
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
	}
}
	
