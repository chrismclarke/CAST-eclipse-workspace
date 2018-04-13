package simulation;

import java.awt.*;

import dataView.*;
import axis.*;


public class EnvelopeGeneratorView extends DataView {
//	static public final String ENVELOPE_VIEW = "envelopeView";
	
	static final private Color kDarkGreen = new Color(0x009900);
	
	protected String xKey, zKey;
	protected HorizAxis horizAxis;
	protected VertAxis probAxis;
	
	protected int pixelHeight[];
	
	private boolean initialised = false;
	
	public EnvelopeGeneratorView(DataSet theData, XApplet applet,
					HorizAxis horizAxis, VertAxis probAxis, String xKey, String zKey) {
		super(theData, applet, new Insets(5, 5, 5, 5));
		
		this.xKey = xKey;
		this.zKey = zKey;
		this.horizAxis = horizAxis;
		this.probAxis = probAxis;
	}
	
	protected boolean initialise() {
		if (!initialised) {
			int noOfClasses = horizAxis.getAxisLength();
			
			if (pixelHeight == null || pixelHeight.length != noOfClasses + 1)
				pixelHeight = new int[noOfClasses + 1];
			
			EnvelopeVariable xVar = (EnvelopeVariable)getVariable(xKey);
			double noOfSDs = xVar.getNoOfSDs();
			
//			Point p = null;
			double invLength = 1.0 / pixelHeight.length;
			for (int i=0 ; i<pixelHeight.length ; i++) {
				double x = i * invLength;
				double d = Math.exp(-2.0 * noOfSDs * noOfSDs * (x - 0.5) * (x - 0.5));
				pixelHeight[i] = probAxis.numValToRawPosition(d);
			}
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	public void paintView(Graphics g) {
		initialise();
		
		EnvelopeVariable xVar = (EnvelopeVariable)getVariable(xKey);
		NumVariable zVar = (NumVariable)getVariable(zKey);
		
		g.setColor(Color.white);
		int x0Pos = horizAxis.numValToRawPosition(0.0);
		int x1Pos = horizAxis.numValToRawPosition(1.0);
		int z0Pos = probAxis.numValToRawPosition(0.0);
		int z1Pos = probAxis.numValToRawPosition(1.0);
		Point topLeft = translateToScreen(x0Pos, z1Pos, null);
		Point bottomRight = translateToScreen(x1Pos, z0Pos, null);
		g.fillRect(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
		
		g.setColor(Color.lightGray);
		Point thePoint = translateToScreen(0, 0, null);
		int lowVert = thePoint.y;
//		int lowHoriz = thePoint.x;
//		double invLength = 1.0 / pixelHeight.length;
		for (int i=0 ; i<pixelHeight.length ; i++) {
			thePoint = translateToScreen(i, pixelHeight[i], thePoint);
			g.drawLine(thePoint.x, thePoint.y, thePoint.x, lowVert);;
		}
		
		if (xVar.noOfValues() > 0) {
			g.setColor(xVar.lastValueRejected() ? Color.red : kDarkGreen);
			double x = xVar.doubleValueAt(xVar.noOfValues() - 1);
			double z = zVar.doubleValueAt(xVar.noOfValues() - 1);
			int xPos = horizAxis.numValToRawPosition(x);
			int zPos = probAxis.numValToRawPosition(z);
			thePoint = translateToScreen(xPos, zPos, thePoint);
			g.fillRect(thePoint.x - 4, thePoint.y - 4, 9, 9);
			
			g.drawLine(thePoint.x, thePoint.y, thePoint.x, getSize().height - 1);
			g.drawLine(thePoint.x - 1, thePoint.y, thePoint.x - 1, getSize().height - 2);
			g.drawLine(thePoint.x + 1, thePoint.y, thePoint.x + 1, getSize().height - 2);
			
			g.drawLine(thePoint.x, getSize().height, thePoint.x - 4, getSize().height - 4);
			g.drawLine(thePoint.x, getSize().height, thePoint.x + 4, getSize().height - 4);
			g.drawLine(thePoint.x, getSize().height - 1, thePoint.x - 3, getSize().height - 4);
			g.drawLine(thePoint.x, getSize().height - 1, thePoint.x + 3, getSize().height - 4);
		}
		
		g.setColor(getForeground());
		ValueEnumeration xe = xVar.values();
		ValueEnumeration ze = zVar.values();
		while (xe.hasMoreValues()) {
			double x = xe.nextDouble();
			double z = ze.nextDouble();
			int xPos = horizAxis.numValToRawPosition(x);
			int zPos = probAxis.numValToRawPosition(z);
			thePoint = translateToScreen(xPos, zPos, thePoint);
			if (thePoint != null)
				drawCross(g, thePoint);
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
	
