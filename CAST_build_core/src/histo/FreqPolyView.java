package histo;

import java.awt.*;

import dataView.*;


public class FreqPolyView extends DataView {
//	static final public String FREQ_POLY_VIEW = "freqPolyView";
	
	static final private int kTopSpace = 20;
	static final public int kMaxFrames = 40;
	
	static final private int kFillShade = 0xCC;
	static final private int kVerticalShade = 0x66;
	static final private Color kFillColor = new Color(kFillShade, kFillShade, kFillShade);
	
	protected int classCounts[];		// first and last are assumed to be zero
	protected int maxCount, totalCount;
	
	protected Polygon poly = new Polygon();
	
	protected double scaleFactor = 1.0;
	
	public FreqPolyView(DataSet theData, XApplet applet, int[] counts) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		classCounts = counts;
		maxCount = 0;
		totalCount = 0;
		for (int i=0 ; i<counts.length ; i++) {
			maxCount = Math.max(maxCount, counts[i]);
			totalCount += counts[i];
		}
	}
	
	private int getTopPos(int count) {
		int ht = (int)Math.round(((getSize().height - kTopSpace) * count * scaleFactor) / maxCount);
		return getSize().height - ht + 1;
															//		+1 so zero height bar is not visible
	}
	
	private int getClassStart(int classNo) {
		return (getSize().width * classNo) / classCounts.length;
	}
	
	protected void setPolygonPoints(int counts[], Polygon p) {
		int noOfClasses = counts.length;
		int frame = getCurrentFrame();
		
		int lastY = getTopPos(0);
		int lastEndX = getClassStart(0);
//		int lastCentreX = lastEndX;
		
		if (p.xpoints.length != 3 * noOfClasses) {
			p.xpoints = new int[3 * noOfClasses];
			p.ypoints = new int[3 * noOfClasses];
		}
		p.npoints = 0;
		for (int i=0 ; i<noOfClasses ; i++) {
			int classEndX = getClassStart(i + 1);
			int classY = getTopPos(counts[i]);
			int centreX = (lastEndX + classEndX) / 2;
			
			if (i == 0) {
				p.xpoints[p.npoints] = centreX;
				p.ypoints[p.npoints++] = classY;
			}
			else {
				if (frame < kMaxFrames) {
					int offset = (classY - lastY) * frame / (2 * kMaxFrames);
					int y1 = lastY + offset;
					int y2 = classY - offset;
					p.xpoints[p.npoints] = lastEndX;
					p.ypoints[p.npoints++] = y1;
					p.xpoints[p.npoints] = lastEndX;
					p.ypoints[p.npoints++] = y2;
				}
				p.xpoints[p.npoints] = centreX;
				p.ypoints[p.npoints++] = classY;
			}
			
			lastY = classY;
			lastEndX = classEndX;
//			lastCentreX = centreX;
		}
	}
	
	public void paintView(Graphics g) {
		int noOfClasses = classCounts.length;
		int frame = getCurrentFrame();
		
		setPolygonPoints(classCounts, poly);
		
		g.setColor(kFillColor);
		g.fillPolygon(poly.xpoints, poly.ypoints, poly.npoints);
		
		if (frame < kMaxFrames) {
			int bottomY = getTopPos(0);
			int lineShade = kVerticalShade + (kFillShade - kVerticalShade)  * frame / kMaxFrames;
			Color lineColor = new Color(lineShade, lineShade, lineShade);
			g.setColor(lineColor);
			for (int i=1 ; i<noOfClasses ; i++) {
				int xPos = poly.xpoints[3 * i - 1];
				int yPos = poly.ypoints[3 * i - 1];
				g.drawLine(xPos, yPos, xPos, bottomY);
			}
		}
		
		if (frame > 0 && frame < kMaxFrames) {
			int redShade = Math.min(0xFF, kFillShade + Math.min(frame * 8 * 0xFF / kMaxFrames,
															(kMaxFrames - frame) * 8 * 0xFF / kMaxFrames));
			int greenBlueShade = Math.max(0x00, kFillShade - Math.min(frame * 8 * kFillShade / kMaxFrames,
													(kMaxFrames - frame) * 8 * kFillShade / kMaxFrames));
			Color redColor = new Color(redShade, greenBlueShade, greenBlueShade);
			g.setColor(redColor);
			Point p = new Point(0,0);
			for (int i=0 ; i<noOfClasses ; i++) {
				int xPos = poly.xpoints[3 * i];
				int yPos = poly.ypoints[3 * i];
				p.x = xPos;
				p.y = yPos;
				drawBlob(g, p);
			}
		}
		
		g.setColor(getForeground());
		g.drawPolygon(poly.xpoints, poly.ypoints, poly.npoints);
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		return new Dimension(50, 50);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}