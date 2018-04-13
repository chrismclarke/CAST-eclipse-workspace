package sampling;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;


public class HistoAndNormalView extends HistoDragView {
//	static final public String HISTO_AND_NORMAL = "histoAndNormal";
	
	static final private int kMeanLineHeight = 16;
	static final private int kSDLineHeight = kMeanLineHeight / 2;
	static final private int kArrowHead = 4;
	
	static final public boolean SHOW_MEANSD = true;
	static final public boolean NO_SHOW_MEANSD = false;
	
	static final public boolean FILL_DENSITY = true;
	static final public boolean OUTLINE_DENSITY = false;
	
	private String distnKey;
	private boolean showMeanSD, fillNotOutline;
	private NormalInfo normInfo = new NormalInfo();
	
	private int outlineX[];
	private int outlineY[];
	private int pointsUsed;
	
	public HistoAndNormalView(DataSet theData, XApplet applet,
								HorizAxis horizAxis, VertAxis probAxis, String distnKey, String yKey,
								double class0Start, double classWidth, boolean showMeanSD, 
								boolean fillNotOutline) {
		super(theData, applet, horizAxis, probAxis, yKey, class0Start, classWidth, NO_SHOW_VALUES);
		this.distnKey = distnKey;
		this.showMeanSD = showMeanSD;
		this.fillNotOutline = fillNotOutline;
	}
	
	private void addPointToPoly(Point p) {
		outlineX[pointsUsed] = p.x;
		outlineY[pointsUsed ++] = p.y + 1;
	}
	
	private void drawThePolygon(Graphics g) {
//		String versionString = System.getProperty("java.version");
//		StringTokenizer st = new StringTokenizer(versionString, ".");
//		int mainVersion = Integer.parseInt(st.nextToken());
//		int subVersion = Integer.parseInt(st.nextToken());
//		
//		if (mainVersion > 1 || (mainVersion == 1 && subVersion >= 1))
//			g.drawPolyline(outlineX, outlineY, pointsUsed);
												//		Netscape 4.5 (Mac) can't find drawPolyline()
//		else
			for (int i=1 ; i<pointsUsed ; i++)
				g.drawLine(outlineX[i-1], outlineY[i-1], outlineX[i], outlineY[i]);
	}
	
	public void paintView(Graphics g) {
		super.paintView(g);
		
		double z0[] = normInfo.getLowPoints().z;
		double d0[] = normInfo.getLowPoints().d;
		double z1[] = normInfo.getHighPoints().z;
		double d1[] = normInfo.getHighPoints().d;
		
		if (outlineX == null || outlineX.length != (z0.length + z1.length + 3)) {
			outlineX = new int[z0.length + z1.length + 3];
			outlineY = new int[z0.length + z1.length + 3];
		}
		pointsUsed = 0;
		Point tempPoint = null;
		if (fillNotOutline)
			addPointToPoly(translateToScreen(0, 0, tempPoint));
		
		NormalDistnVariable y = (NormalDistnVariable)getVariable(distnKey);
		double mean = y.getMean().toDouble();
		double sd = y.getSD().toDouble();
		
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
		if (fillNotOutline)
			addPointToPoly(translateToScreen(startX, 0, tempPoint));
		
		if (fillNotOutline) {
			g.setColor(Color.lightGray);
			g.fillPolygon(outlineX, outlineY, pointsUsed);
		}
		else {
			g.setColor(Color.black);
			drawThePolygon(g);
		}
		
		if (showMeanSD) {
			int meanPos = horizAxis.numValToRawPosition(mean);
			int meanSDPos = horizAxis.numValToRawPosition(mean + sd);
			Point meanOnAxis = translateToScreen(meanPos, 0, null);
			Point meanSDLineEnd = translateToScreen(meanSDPos, kSDLineHeight, null);
			
			g.setColor(Color.red);
			g.drawLine(meanOnAxis.x, meanSDLineEnd.y, meanSDLineEnd.x, meanSDLineEnd.y);
			g.drawLine(meanSDLineEnd.x, meanSDLineEnd.y, meanSDLineEnd.x - kArrowHead, meanSDLineEnd.y - kArrowHead);
			g.drawLine(meanSDLineEnd.x, meanSDLineEnd.y, meanSDLineEnd.x - kArrowHead, meanSDLineEnd.y + kArrowHead);
			
			g.setColor(Color.blue);
			g.drawLine(meanOnAxis.x, meanOnAxis.y, meanOnAxis.x, meanOnAxis.y - kMeanLineHeight);
			g.drawLine(meanOnAxis.x, meanOnAxis.y, meanOnAxis.x - kArrowHead, meanOnAxis.y - kArrowHead);
			g.drawLine(meanOnAxis.x, meanOnAxis.y, meanOnAxis.x + kArrowHead, meanOnAxis.y - kArrowHead);
		}
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
