package sampling;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;


public class NormExpRectView extends DataView {
//	static final public String NORM_EXP_RECT = "normExpRect";
	
	static final private double kStepFraction = 0.2;
	static final private double kMaxProbOnAxis = 0.6;
	
	static final public int NORMAL = 0;
	static final public int EXPONENTIAL = 1;
	static final public int RECTANGULAR = 2;
	static final public int TWO_VALUES = 3;
	
	private HorizAxis horizAxis;
	private VertAxis probAxis;
	private double mean;
	private int distnType;
	
	private NormalInfo normInfo = new NormalInfo();
	
	private int outlineX[];
	private int outlineY[];
	private int pointsUsed;
	
	public NormExpRectView(DataSet theData, XApplet applet, HorizAxis horizAxis, VertAxis probAxis,
																																double mean, int distnType) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.horizAxis = horizAxis;
		this.probAxis = probAxis;
		this.mean = mean;
		this.distnType = distnType;
	}
	
	public void setDistnType(int distnType) {
		this.distnType = distnType;
		repaint();
	}
	
	private void addPointToPoly(Point p) {
		outlineX[pointsUsed] = p.x;
		outlineY[pointsUsed ++] = p.y + 1;
	}
	
	private void drawBar(double x, double prob, Graphics g) {
		int probHt = probAxis.numValToRawPosition(probAxis.maxOnAxis * prob / kMaxProbOnAxis);
		try {
			int xPos = horizAxis.numValToPosition(x);
			Point topLeft = translateToScreen(xPos - 2, probHt, null);
			Point bottomRight = translateToScreen(xPos + 3, -1, null);
			g.fillRect(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
		} catch (AxisException e) {
		}
	}
	
	public void paintView(Graphics g) {
		Point tempPoint = null;
		g.setColor(Color.lightGray);
		
		if (distnType == TWO_VALUES) {
			drawBar(0.0, 0.5, g);
			drawBar(2.0 * mean, 0.5, g);
			return;
		}
		
		double z0[] = normInfo.getLowPoints().z;
		double d0[] = normInfo.getLowPoints().d;
		double z1[] = normInfo.getHighPoints().z;
		double d1[] = normInfo.getHighPoints().d;
		
		if (outlineX == null || outlineX.length != (z0.length + z1.length + 3)) {
			outlineX = new int[z0.length + z1.length + 3];
			outlineY = new int[z0.length + z1.length + 3];
		}
		pointsUsed = 0;
		switch (distnType) {
			case NORMAL:
				addPointToPoly(translateToScreen(0, 0, tempPoint));
				
				double sd = mean;
				
				int startX = 0;
				int startY = probAxis.numValToRawPosition(normInfo.lookup(Math.abs((horizAxis.minOnAxis - mean) / sd)) / sd);
				addPointToPoly(translateToScreen(startX, startY, tempPoint));
				for (int i=z0.length-1 ; i>=0 ; i--)
					try {
						int endX = horizAxis.numValToPosition(mean + z0[i] * sd);
						int endY = probAxis.numValToRawPosition(d0[i] / sd);
						addPointToPoly(translateToScreen(endX, endY, tempPoint));
					} catch (AxisException e) {
					}
				for (int i=1 ; i<z1.length ; i++)
					try {
						int endX = horizAxis.numValToPosition(mean + z1[i] * sd);
						int endY = probAxis.numValToRawPosition(d1[i] / sd);
						addPointToPoly(translateToScreen(endX, endY, tempPoint));
					} catch (AxisException e) {
					}
				startX = horizAxis.getAxisLength() - 1;
				startY = probAxis.numValToRawPosition(normInfo.lookup(Math.abs((horizAxis.maxOnAxis - mean) / sd)) / sd);
				addPointToPoly(translateToScreen(startX, startY, tempPoint));
				
				addPointToPoly(translateToScreen(startX, 0, tempPoint));
				break;
			case RECTANGULAR:
				double range = mean * Math.sqrt(12.0);
				double min = mean - range * 0.5;
				double max = mean + range * 0.5;
				double density = 1.0 / range;
				
				int minX = horizAxis.numValToRawPosition(min);
				int maxX = horizAxis.numValToRawPosition(max) + 1;		//		1 extra since max is not filled
				int ht = probAxis.numValToRawPosition(density);
				
				addPointToPoly(translateToScreen(minX, 0, tempPoint));
				addPointToPoly(translateToScreen(minX, ht, tempPoint));
				addPointToPoly(translateToScreen(maxX, ht, tempPoint));
				addPointToPoly(translateToScreen(maxX, 0, tempPoint));
				break;
			case EXPONENTIAL:
				double lambda = 1.0 / mean;
				int zeroX = horizAxis.numValToRawPosition(0.0);
				addPointToPoly(translateToScreen(zeroX, 0, tempPoint));
				int zeroHt = probAxis.numValToRawPosition(lambda);
				addPointToPoly(translateToScreen(zeroX, zeroHt, tempPoint));
				
				double step = mean * kStepFraction;
				double x = step;
				try {
					while (true) {
						int xPos = horizAxis.numValToPosition(x);
						int yPos = probAxis.numValToRawPosition(lambda * Math.exp(-lambda * x));
						addPointToPoly(translateToScreen(xPos, yPos, tempPoint));
						
						x += step;
					}
				} catch (AxisException e) {
				}
				int rightX = horizAxis.numValToRawPosition(horizAxis.maxOnAxis);
				int rightY = probAxis.numValToRawPosition(lambda * Math.exp(-lambda * horizAxis.maxOnAxis));
				addPointToPoly(translateToScreen(rightX, rightY, tempPoint));
				addPointToPoly(translateToScreen(rightX, 0, tempPoint));
				
				break;
		}
		
		g.fillPolygon(outlineX, outlineY, pointsUsed);
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
