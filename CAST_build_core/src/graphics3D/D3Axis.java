package graphics3D;

import java.util.*;
import java.awt.*;
import dataView.*;
import axis.*;


public class D3Axis {
	static final public int X_AXIS = 0;
	static final public int Y_AXIS = 1;
	static final public int Z_AXIS = 2;
	static final public int X_Z_AXIS = 3;
	
	static final public int FOREGROUND = 0;
	static final public int BACKGROUND = 1;
	static final public int SHADED = 2;
	
	static final public Color[][] axisColor = setupColors();
	
	static final private int kMinNameDist = 30;
													//		min axis length for axis name to be drawn
	static final private int kMinLabelDist = 50;
													//		min axis length for axis labels to be drawn
	static final private int kLabelBorder = 1;
													//		minimum distance between text and edge

	private Font labelFont;
	
	static private Color[][] setupColors() {
		Color result[][] = new Color[3][];
		for (int i=0 ; i<3 ; i++)
			result[i] = new Color[3];
		result[X_AXIS][BACKGROUND] = new Color(0x000066);
		result[Y_AXIS][BACKGROUND] = new Color(0x003300);
		result[Z_AXIS][BACKGROUND] = new Color(0x660000);
		
		result[X_AXIS][FOREGROUND] = new Color(0x0033CC);
		result[Y_AXIS][FOREGROUND] = new Color(0x006633);
		result[Z_AXIS][FOREGROUND] = new Color(0xCC0000);
		
		result[X_AXIS][SHADED] = new Color(0x6699CC);
		result[Y_AXIS][SHADED] = new Color(0x66CC99);
		result[Z_AXIS][SHADED] = new Color(0xCC9999);
		return result;
	}
	
	protected Vector labels = new Vector(10);
	
	private double minOnAxis, maxOnAxis;
	private String labelName;
	private int orientation, tickAxisOrientation;
	
	private boolean showAxis = true;
	
	public D3Axis(String labelName, int orientation, int tickAxisOrientation, XApplet applet) {
		this.labelName = labelName;
		this.orientation = orientation;
		this.tickAxisOrientation = tickAxisOrientation;
		labelFont = applet.getSmallFont();
	}
	
	public void setShow(boolean showAxis) {
		this.showAxis = showAxis;
	}
	
	public void setLabelName(String labelName) {
		this.labelName = labelName;
	}
	
	public void setNumScale(double minOnAxis, double maxOnAxis, NumValue labelMin, NumValue labelStep) {
		this.minOnAxis = minOnAxis;
		this.maxOnAxis = maxOnAxis;
		
		initNumLabels(labels, minOnAxis, maxOnAxis, labelMin, labelStep);
	}
	
	public void setNumScale(String labelInfo) {
		StringTokenizer theLabels = new StringTokenizer(labelInfo);
		String minString = theLabels.nextToken();
		minOnAxis = Double.parseDouble(minString);
		String maxString = theLabels.nextToken();
		maxOnAxis = Double.parseDouble(maxString);
		NumValue labelMin = null;
		NumValue labelStep = null;
		if (theLabels.hasMoreTokens()) {
			String labelString = theLabels.nextToken();
			labelMin = new NumValue(labelString);
			String stepString = theLabels.nextToken();
			labelStep = new NumValue(stepString);
		}
		
		initNumLabels(labels, minOnAxis, maxOnAxis, labelMin, labelStep);
	}
	
	protected void initNumLabels(Vector labels, double minOnAxis, double maxOnAxis,
																				NumValue labelMin, NumValue labelStep) {
		labels.removeAllElements();
		if (labelMin != null) {
			double axisRange = maxOnAxis - minOnAxis;
				
			if (labelMin.toDouble() >= minOnAxis) {
				int decimals = Math.max(labelMin.decimals, labelStep.decimals);
				double label = labelMin.toDouble();
				double step = labelStep.toDouble();
				while (label <= maxOnAxis) {
					labels.addElement(new AxisLabel(new NumValue(label, decimals), (label - minOnAxis) / axisRange));
					label += step;
				}
			}
		}
	}
	
	public double getMinOnAxis() {
		return minOnAxis;
	}
	
	public double getMaxOnAxis() {
		return maxOnAxis;
	}
	
	public void setCatScale(CatVariableInterface v) {
		labels.removeAllElements();
		int noOfCats = v.noOfCategories();
		for (int i=0 ; i<noOfCats ; i++)
			labels.addElement(new AxisLabel(v.getLabel(i), (i + 0.5) / noOfCats));
	}
	
	public double numValToPosition(double theValue) {
		return (theValue - minOnAxis) / (maxOnAxis - minOnAxis);
	}
	
	public double catValToPosition(int catIndex, int noOfCats) {
		return (catIndex + 0.5) / noOfCats;
	}
	
	public double positionToNumVal(double thePropn) {
		return minOnAxis + thePropn * (maxOnAxis - minOnAxis);
	}
	
	private Point getAxisPoint(double propn, RotateMap map, DataView view, Point thePoint) {
		double x = 0.0;
		double y = 0.0;
		double z = 0.0;
		if (orientation == X_AXIS)
			x = propn;
		else if (orientation == Y_AXIS)
			y = propn;
		else
			z = propn;
		return view.translateToScreen(map.mapH3DGraph(y, x, z), map.mapV3DGraph(y, x, z), thePoint);
	}
	
	private Point getOffAxisPoint(double propn, double offPropn,
																RotateMap map, DataView view, Point thePoint) {
		double x = 0.0;
		double y = 0.0;
		double z = 0.0;
		if (orientation == X_AXIS)
			x = propn;
		else if (orientation == Y_AXIS)
			y = propn;
		else
			z = propn;
		if (tickAxisOrientation == X_Z_AXIS)
			x = z = offPropn * 0.6;
		else if (tickAxisOrientation == X_AXIS)
			x = offPropn;
		else if (tickAxisOrientation == Y_AXIS)
			y = offPropn;
		else
			z = offPropn;
		return view.translateToScreen(map.mapH3DGraph(y, x, z), map.mapV3DGraph(y, x, z), thePoint);
	}
	
	static final private double kRadToDeg = 180.0 / Math.PI;
	
	private int getAngle(RotateMap map, DataView view, double axisChange, double offAxisChange) {
		Point start = getAxisPoint(0.0, map, view, null);
		Point end = getOffAxisPoint(axisChange, offAxisChange, map, view, null);
		int dx = end.x - start.x;
		int dy = end.y - start.y;
		if (dx == 0 && dy == 0)
			return 45;
		int angle = -(int)Math.round(kRadToDeg * Math.atan2(dy, dx));
		angle += 135;
		if (angle < 0)
			angle += 360;
		return angle;
	}
	
	private void drawAtAngle(Graphics g, int width, int height, String valueString,
																					Point anchor, int angle) {
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		int labelWidth = fm.stringWidth(valueString);
		int left, baseline;
		if (angle < 90) {				//		top edge
			left = anchor.x - (90 - angle) * labelWidth / 90;
			baseline = anchor.y + ascent;
		}
		else if (angle < 180) {		//		left edge
			left = anchor.x;
			baseline = anchor.y + ascent - (angle - 90) * (ascent + descent) / 90;
		}
		else if (angle < 270) {		//		bottom edge
			left = anchor.x - (angle - 180) * labelWidth / 90;
			baseline = anchor.y - descent;
		}
		else {							//		right edge
			left = anchor.x - labelWidth;
			baseline = anchor.y - descent + (angle - 270) * (ascent + descent) / 90;
		}
		left = Math.max(kLabelBorder, Math.min(width - labelWidth - kLabelBorder, left));
		baseline = Math.max(ascent + kLabelBorder, Math.min(height - descent - kLabelBorder,
																											baseline));
		g.drawString(valueString, left, baseline);
	}
	
	public Enumeration getLabelEnumeration() {
		return labels.elements();
	}
	
	public void draw(Graphics g, RotateMap map, DataView view, int shadePosition,
																							boolean drawLabels) {
		if (!showAxis)
			return;
		
		Color oldColor = g.getColor();
		g.setColor(axisColor[orientation][shadePosition]);
		
		int displayWidth = view.getSize().width;
		int displayHeight = view.getSize().height;
		
		Point origin = getAxisPoint(0.0, map, view, null);
		Point axisEnd = getAxisPoint(1.0, map, view, null);
		g.drawLine(origin.x, origin.y, axisEnd.x, axisEnd.y);
		
//		boolean toRight = (axisEnd.x > origin.x);
		int xDist = Math.abs(axisEnd.x - origin.x);
//		boolean toTop = (axisEnd.y < origin.y);
		int yDist = Math.abs(axisEnd.y - origin.y);
		if (Math.max(xDist, yDist) > kMinNameDist) {
			int axisAngle = getAngle(map, view, 0.5, 0.0);
			Point nameAnchor = getAxisPoint(1.05, map, view, null);
			drawAtAngle(g, displayWidth, displayHeight, labelName, nameAnchor, axisAngle);
			
			
//			int symbolY, symbolX;
//			if (yDist > xDist) {
//				symbolY = axisEnd.y + (toTop ? -4 : 4);
//				symbolX = axisEnd.x + (axisEnd.x - origin.x) * (symbolY - axisEnd.y) / (axisEnd.y - origin.y);
//			}
//			else {
//				symbolX = axisEnd.x + (toRight ? 4 : -4);
//				symbolY = axisEnd.y + (axisEnd.y - origin.y) * (symbolX - axisEnd.x) / (axisEnd.x - origin.x);
//			}
//			switch (orientation) {
//				case X_AXIS:
//					RotateButton.drawX(g, symbolX - 2, symbolY - 3);
//					break;
//				case Y_AXIS:
//					RotateButton.drawY(g, symbolX - 2, symbolY - 3);
//					break;
//				case Z_AXIS:
//					RotateButton.drawZ(g, symbolX - 2, symbolY - 3);
//					break;
//			}
		}
		if (drawLabels) {
			boolean drawLabelText = Math.max(xDist, yDist) > kMinLabelDist;
			Font oldFont = g.getFont();
			g.setFont(labelFont);
			
			Enumeration e = getLabelEnumeration();
			Point tickStart = null;
			Point tickEnd = null;
			Point labelAnchor = null;
			int angle = getAngle(map, view, 0.0, -0.5);
			while (e.hasMoreElements()) {
				AxisLabel theLabel = (AxisLabel)e.nextElement();
				tickStart = getAxisPoint(theLabel.position, map, view, tickStart);
				tickEnd = getOffAxisPoint(theLabel.position, -0.05, map, view, tickEnd);
				g.drawLine(tickStart.x, tickStart.y, tickEnd.x, tickEnd.y);
				
				if (drawLabelText) {
					labelAnchor = getOffAxisPoint(theLabel.position, -0.08, map, view, labelAnchor);
					String valueString = theLabel.label.toString();
					drawAtAngle(g, displayWidth, displayHeight, valueString, labelAnchor, angle);
				}
			}
			g.setFont(oldFont);
		}
		g.setColor(oldColor);
	}
}