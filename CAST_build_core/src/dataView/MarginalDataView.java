package dataView;

import java.awt.*;
import java.awt.geom.*;

import axis.*;

abstract public class MarginalDataView extends DataView {
	protected NumCatAxis axis;
	protected boolean vertNotHoriz;
	
	public MarginalDataView(DataSet initData, XApplet applet, Insets border, NumCatAxis axis) {
		super(initData, applet, border);
		this.axis = axis;
		vertNotHoriz = axis instanceof VertAxis;
	}
	
	abstract public int minDisplayWidth();		//	used for layout and later for actual width
	
	protected int getDisplayWidth() {
		if (vertNotHoriz)
			return getSize().width - getViewBorder().left - getViewBorder().right;
		else
			return getSize().height - getViewBorder().top - getViewBorder().bottom;
	}
	
	protected int getDisplayHeight() {
		if (vertNotHoriz)
			return getSize().height - getViewBorder().top - getViewBorder().bottom;
		else
			return getSize().width - getViewBorder().left - getViewBorder().right;
	}
	
	protected int getDisplayBorderNearAxis() {
		if (vertNotHoriz)
			return getViewBorder().left;
		else
			return getViewBorder().bottom;
	}
	
	protected int getDisplayBorderAwayAxis() {
		if (vertNotHoriz)
			return getViewBorder().right;
		else
			return getViewBorder().top;
	}
	
	public Point translateToScreen(int horiz, int vert, Point thePoint) {
		if (vertNotHoriz)
			return super.translateToScreen(vert, horiz, thePoint);
		else
			return super.translateToScreen(horiz, vert, thePoint);
	}
	
	public Point2D.Double translateToScreenD2(double horiz, double vert, Point2D.Double thePoint) {
		if (vertNotHoriz)
			return super.translateToScreenD2(vert, horiz, thePoint);
		else
			return super.translateToScreenD2(horiz, vert, thePoint);
	}
	
	protected Point2D.Double convertToScreen(double x, double y, NumCatAxis xAxis, NumCatAxis yAxis,
																					 Point2D.Double thePoint) {							//	can be off-axis
		int displayWd = vertNotHoriz ? getSize().height : getSize().width;
		int displayHt = vertNotHoriz ? getSize().width : getSize().height;
		double xPos = (xAxis == null) ? (x * displayWd) : xAxis.numValToRawDoublePos(x);
		double yPos = (yAxis == null) ? (y * displayHt) : yAxis.numValToRawDoublePos(y);
		if (Double.isInfinite(xPos) || Double.isInfinite(yPos))
			return null;
		else
			return translateToScreenD2(xPos, yPos, thePoint);
	}
	
	protected Point translateFromScreen(int x, int y, Point thePoint) {
		Point result = super.translateFromScreen(x, y, thePoint);
		if (vertNotHoriz) {
			int temp = result.x;
			result.x = result.y;
			result.y = temp;
		}
		return result;
	}
	
	protected void doTransformView(Graphics g, NumCatAxis theAxis) {
		if (axis == theAxis) {
			reinitialiseAfterTransform();
			repaint();
		}
	}
	
	protected void fillRect(Graphics g, Point p1, Point p2) {
		int xLow, width, yLow, height;
		if (p1.x < p2.x) {
			xLow = p1.x;
			width = p2.x - p1.x;
		}
		else {
			xLow = p2.x;
			width = p1.x - p2.x;
		}
		if (p1.y < p2.y) {
			yLow = p1.y;
			height = p2.y - p1.y;
		}
		else {
			yLow = p2.y;
			height = p1.y - p2.y;
		}
		g.fillRect(xLow, yLow, width, height);
	}
}