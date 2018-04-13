package multivar;

import java.awt.*;

import dataView.*;
import axis.*;


public class ZSymbolScatterView extends DataView {
//	static final public String Z_SYMBOL_PLOT = "zSymbolPlot";
	
	static final public int CIRCLES = 0;
	static final public int ANGLES = 1;
	static final public int COLOURS = 2;
	
	static final private int kMaxDiameter = 11;
	static final private int kNoOfAngles = 8;
	static final private int kAngleOffset[][] = {{0, 1, 2, 3, 4, 5, 5, 5}, {0, 0, 0, 1, 2, 3, 4, 5}};
	
	private String xKey, yKey, zKey;
	private HorizAxis xAxis;
	private VertAxis yAxis;
	private int zSymbol;
	
	public ZSymbolScatterView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis,
							String xKey, String yKey, String zKey, int zSymbol) {
		super(theData, applet, new Insets(5, 5, 5, 5));
		this.xKey = xKey;
		this.yKey = yKey;
		this.zKey = zKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.zSymbol = zSymbol;
	}
	
	public void setZSymbol(int newSymbol) {
		if (zSymbol != newSymbol) {
			zSymbol = newSymbol;
			repaint();
		}
	}
	
	private Point getScreenPoint(NumValue xVal, NumValue yVal, Point thePoint) {
		try {
			int vertPos = yAxis.numValToPosition(yVal.toDouble());
			int horizPos = xAxis.numValToPosition(xVal.toDouble());
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	public void drawSymbol(Graphics g, Point thePoint, double zProportion) {
											//	public so that SymbolKey can use it
		switch (zSymbol) {
			case CIRCLES:
				int diameter = 1 + (int)(kMaxDiameter * zProportion);
				if (diameter > kMaxDiameter)
					diameter = kMaxDiameter;
				if (diameter == 1)
					g.drawLine(thePoint.x, thePoint.y, thePoint.x, thePoint.y);
				else
					g.drawOval(thePoint.x - diameter / 2, thePoint.y - diameter / 2,
															diameter - 1, diameter - 1);
				break;
			case ANGLES:
				int angleIndex = (int)(kNoOfAngles * zProportion);
				if (angleIndex >= kNoOfAngles)
					angleIndex = kNoOfAngles -1;
				g.drawLine(thePoint.x + kAngleOffset[0][angleIndex],
							thePoint.y - kAngleOffset[1][kNoOfAngles - 1 - angleIndex],
							thePoint.x - kAngleOffset[0][angleIndex],
							thePoint.y + kAngleOffset[1][kNoOfAngles - 1 - angleIndex]);
				break;
			case COLOURS:
				g.setColor(new Color((float)zProportion, 0.0f, (float)(1.0 - zProportion)));
				g.fillOval(thePoint.x - 2, thePoint.y - 2, 5, 5);
				break;
		}
	}
	
	static public int maxSymbolSize() {
		return kMaxDiameter;
	}
	
	public void paintView(Graphics g) {
		NumVariable xVariable = (NumVariable)getVariable(xKey);
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		NumVariable zVariable = (NumVariable)getVariable(zKey);
		NumValue sortedZ[] = zVariable.getSortedData();
		double zMin = sortedZ[0].toDouble();
		double zMax = sortedZ[zVariable.noOfValues() - 1].toDouble();
		Point thePoint = null;
		
		ValueEnumeration xe = xVariable.values();
		ValueEnumeration ye = yVariable.values();
		ValueEnumeration ze = zVariable.values();
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			NumValue xVal = (NumValue)xe.nextValue();
			NumValue yVal = (NumValue)ye.nextValue();
			NumValue zVal = (NumValue)ze.nextValue();
			thePoint = getScreenPoint(xVal, yVal, thePoint);
			if (thePoint != null)
				drawSymbol(g, thePoint, (zVal.toDouble() - zMin) / (zMax - zMin));
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
