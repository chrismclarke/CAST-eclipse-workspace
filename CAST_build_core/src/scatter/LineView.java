package scatter;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;
import coreVariables.*;
import images.*;


public class LineView extends DataView {
	
	static final private int kArrowSize = 5;
	static final private int kWhiteBand = kArrowSize + 1;
	static final private int kEqnLineGap = 10;
	static final private int kMaxWait = 30000;		//		30 seconds
	
	private HorizAxis xAxis;
	private VertAxis yAxis;
	private PowerVariable invX;
	private ScaledVariable yVar;
	private String xKey;
	
	private Image equation;
	
	public LineView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis,
													String powerKey, String scaleKey, String xKey, String eqnFileName) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		invX = (PowerVariable)getVariable("inv");
		yVar = (ScaledVariable)getVariable("y");
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.xKey = xKey;
		
		MediaTracker tracker = new MediaTracker(applet);
			equation = CoreImageReader.getImage(eqnFileName);
		tracker.addImage(equation, 0);
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
	}
	
	private double transform(double x) {
		double power = invX.getPower();
		double const0 = yVar.getParam(0);
		double const1 = yVar.getParam(1);
		
		double xp = (power == 1.0) ? x
					: (power == 0.0) ? Math.log(x)
					: (power < 0.0) ? -Math.pow(x, power)
					:  Math.pow(x, power);
		return const0 + const1 * xp;
	}
	
	
	private void drawSegment(double x0, double y0, double x1, double y1,
												Graphics g, PositionFinder finder, Point p0, Point p1) {
		if (finder.sameX(x0, x1) && (finder.notFinite(y0) || finder.notFinite(y1)))
			return;
		
		double xMid = (x0 + x1) * 0.5;
		double yMid = transform(xMid);
		
		if (finder.nearlyLinear(y0, yMid, y1) || finder.sameX(x0, x1)) {
			p0 = finder.findPoint(x0, y0, p0);
			p1 = finder.findPoint(x1, y1, p1);
			if (p0.y >= -100 && p0.y <= 1000 && p1.y >= -100 && p1.y <= 1000)
																//		ad hoc check for way-out points
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
		}
		else {
			drawSegment(x0, y0, xMid, yMid, g, finder, p0, p1);
			drawSegment(xMid, yMid, x1, y1, g, finder, p0, p1);
		}
	}
	
	private void drawEquation(Graphics g, double xLow, double xHigh, Point p0) {
		double xMid = (xLow + xHigh) * 0.5;
		double yMid = transform(xMid);
		
		try {
			int xPos = xAxis.numValToPosition(xMid);
			int yPos = yAxis.numValToPosition(yMid);
			p0 = translateToScreen(xPos, yPos, p0);
			
			int eqnHeight = equation.getHeight(this);
			int eqnWidth = equation.getWidth(this);
			if (p0.x + kEqnLineGap + eqnWidth <= getSize().width) {
				g.drawImage(equation, p0.x + kEqnLineGap, p0.y - eqnHeight - kEqnLineGap, this);
				return;
			}
		} catch (AxisException e) {
			return;
		}
		
		drawEquation(g, xLow, xMid, p0);
	}
	
	public void paintView(Graphics g) {
		Point p0 = new Point(0,0);
		Point p1 = new Point(0,0);
		
		int selIndex = getSelection().findSingleSetFlag();
		if (selIndex >= 0) {
			NumVariable xVar = (NumVariable)getVariable(xKey);
			double x = xVar.doubleValueAt(selIndex);
			double y = transform(x);
			try {
				int xPos = xAxis.numValToPosition(x);
				int yPos = yAxis.numValToPosition(y);
				p0 = translateToScreen(xPos, yPos, p0);
				
				g.setColor(Color.white);
				g.fillRect(0, p0.y - kWhiteBand, p0.x + kWhiteBand + 1, 2 * kWhiteBand + 1);
				g.fillRect(p0.x - kWhiteBand, p0.y, 2 * kWhiteBand + 1, getSize().height - p0.y);
				
				g.setColor(Color.red);
				g.drawLine(0, p0.y, p0.x, p0.y);
				g.drawLine(p0.x, p0.y, p0.x, getSize().height);
				g.drawLine(0, p0.y, kArrowSize, p0.y + kArrowSize);
				g.drawLine(0, p0.y, kArrowSize, p0.y - kArrowSize);
			} catch (AxisException e) {
			}
		}
		
		double x0 = xAxis.minOnAxis;
		double x1 = xAxis.maxOnAxis;
		drawEquation(g, x0, x1, p0);
		
		double y0 = transform(x0);
		double y1 = transform(x1);
		
		PositionFinder finder = new PositionFinder(this, xAxis, yAxis);
		
		g.setColor(getForeground());
		drawSegment(x0, y0, x1, y1, g, finder, p0, p1);
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
