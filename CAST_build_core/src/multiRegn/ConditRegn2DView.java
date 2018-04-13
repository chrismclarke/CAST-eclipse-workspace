package multiRegn;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class ConditRegn2DView extends DataView {
//	static public final String CONDIT_REGN_2D = "conditRegn2D";
	
	private String modelKey;
	private VertAxis yAxis;
	private HorizAxis xAxis;
	
 	private boolean sliceZNotX;
	private double conditValue;
	private boolean showBand = false;
	
	public ConditRegn2DView(DataSet data, XApplet applet,
												VertAxis yAxis, HorizAxis xAxis, String modelKey, boolean sliceZNotX,
												double conditValue) {
		super(data, applet, new Insets(0, 0, 0, 0));
		this.modelKey = modelKey;
		this.sliceZNotX = sliceZNotX;
		this.conditValue = conditValue;
		this.yAxis = yAxis;
		this.xAxis = xAxis;
	}
	
	public void setShowBand(boolean showBand) {
		this.showBand = showBand;
		repaint();
	
	}
	
	public void setConditValue(double conditValue) {
		this.conditValue = conditValue;
		repaint();
	}
	
	private Point getScreenPoint(double x, double y, Point thePoint) {
		int horizPos = xAxis.numValToRawPosition(x);
		int vertPos = yAxis.numValToRawPosition(y);
		return translateToScreen(horizPos, vertPos, thePoint);
	}
	
	public void paintView(Graphics g) {
		double screenMin = xAxis.minOnAxis;
		double screenMax = xAxis.maxOnAxis;
		
		MultipleRegnModel model = (MultipleRegnModel)getData().getVariable(modelKey);
		double explan[] = new double[2];
		explan[sliceZNotX ? 1 : 0] = conditValue;
		
		explan[sliceZNotX ? 0 : 1] = screenMin;
		double y0Val = model.evaluateMean(explan);
		
		explan[sliceZNotX ? 0 : 1] = screenMax;
		double y1Val = model.evaluateMean(explan);
		
		Point p0 = null;
		Point p1 = null;
		
		if (showBand) {
			double twoSD = 2.0 * model.evaluateSD().toDouble();
			
			Polygon poly = new Polygon();
			p0 = getScreenPoint(screenMin, y0Val - twoSD, null);
			poly.addPoint(p0.x, p0.y);
			p1 = getScreenPoint(screenMin, y0Val + twoSD, null);
			poly.addPoint(p1.x, p1.y);
			p1 = getScreenPoint(screenMax, y1Val + twoSD, null);
			poly.addPoint(p1.x, p1.y);
			p1 = getScreenPoint(screenMax, y1Val - twoSD, null);
			poly.addPoint(p1.x, p1.y);
			poly.addPoint(p0.x, p0.y);
			
			g.setColor(ConditRegn3DView.kBandColor);
			g.fillPolygon(poly);
		}
		
		p0 = getScreenPoint(screenMin, y0Val, p0);
		p1 = getScreenPoint(screenMax, y1Val, p1);
		
		g.setColor(Color.red);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
		
		NumValue xSlope = model.getParameter(sliceZNotX ? 1 : 2);
		String slopeString = getApplet().translate("Slope") + " = " + xSlope.toString();
		FontMetrics fm = g.getFontMetrics();
		g.drawString(slopeString, getSize().width - fm.stringWidth(slopeString) - 4,
																										fm.getAscent() + 3);
		
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
	
}