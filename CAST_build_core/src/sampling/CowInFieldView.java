package sampling;

import java.awt.*;

import dataView.*;
import images.*;


public class CowInFieldView extends DataView {
	static final private int kMaxWait = 30000;		//		30 seconds
	
	static final private int kCowWidth = 50;
	static final private int kCowHeight = 33;
	
	static final private int kLeftRightBorder = 25;
	static final private int kTopBottomBorder = 17;
//	static final private int kBigGridRadius = 4;
	
	static final private Color kFieldColor = new Color(0x66CC00);
	static final private Color kGridColor = new Color(0xCCCC99);
	
	private Color borderColor;
	
	private Image cowImage;
	private String xKey, yKey;
	
	private double[] xGrid = null;
	private double[] yGrid = null;
	private int gridRadius;
			
	public CowInFieldView(DataSet theData, XApplet applet, String xKey, String yKey) {
		super(theData, applet, new Insets(15, 0, 15, 0));
			cowImage = CoreImageReader.getImage("cow.png");
		MediaTracker tracker = new MediaTracker(applet);
		tracker.addImage(cowImage, 0);
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
		this.xKey = xKey;
		this.yKey = yKey;
		borderColor = applet.getBackground();
	}
	
	public void setGrid(double[] xGrid, double[] yGrid, int gridRadius) {
		this.xGrid = xGrid;
		this.yGrid = yGrid;
		this.gridRadius = gridRadius;
	}
	
	public void paintView(Graphics g) {
		g.setColor(borderColor);
		g.fillRect(0, 0, getSize().width, getSize().height);
		g.setColor(kFieldColor);
		g.fillRect(kLeftRightBorder, kTopBottomBorder, getSize().width - 2 * kLeftRightBorder,
																											getSize().height - 2 * kTopBottomBorder);
		
		double xFactor = getSize().width - 2 * kLeftRightBorder;
		double yFactor = getSize().height - 2 * kTopBottomBorder;
		int xOffset = kLeftRightBorder - kCowWidth / 2;
		int yOffset = kTopBottomBorder - kCowHeight / 2;
		
		if (xGrid != null && yGrid != null) {
			g.setColor(kGridColor);
			for (int i=0 ; i<xGrid.length ; i++) {
				double x = (i + 0.5) / xGrid.length;
				int xPos = kLeftRightBorder + (int)Math.round(x * xFactor);
				for (int j=0 ; j<yGrid.length ; j++) {
					double y = (j + 0.5) / yGrid.length;
					int yPos = kTopBottomBorder + (int)Math.round(y * yFactor);
					
					g.fillOval(xPos - gridRadius, yPos - gridRadius, 2 * gridRadius, 2 * gridRadius);
				}
			}
		}
		
		ValueEnumeration xe = ((NumVariable)getVariable(xKey)).values();
		ValueEnumeration ye = ((NumVariable)getVariable(yKey)).values();
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			double x = xe.nextDouble();
			double y = ye.nextDouble();
			
			int xPos = xOffset + (int)Math.round(x * xFactor);
			int yPos = yOffset + (int)Math.round(y * yFactor);
			
			g.drawImage(cowImage, xPos, yPos, kCowWidth, kCowHeight, this);
		}
	}
	
	//----------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
}
