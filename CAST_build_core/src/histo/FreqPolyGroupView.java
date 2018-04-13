package histo;

import java.awt.*;

import dataView.*;


public class FreqPolyGroupView extends FreqPolyView {
//	static final public String FREQ_POLY2_VIEW = "freqPoly2View";
	
	static final private Color kGroup1Color = Color.blue;
	static final private Color kGroup2Color = Color.red;
	
	private int class2Counts[];		// first and last are assumed to be zero
	private int totalCount2;
	
	private Polygon poly2 = new Polygon();
	
	public FreqPolyGroupView(DataSet theData, XApplet applet, int[] counts1, int[] counts2) {
		super(theData, applet, counts1);
		class2Counts = counts2;
		totalCount2 = 0;
		for (int i=0 ; i<class2Counts.length ; i++) {
			maxCount = Math.max(maxCount, class2Counts[i]);
			totalCount2 += class2Counts[i];
		}
	}
	
	public void paintView(Graphics g) {
//		int noOfClasses = classCounts.length;
//		int frame = getCurrentFrame();
		
		scaleFactor = (totalCount > totalCount2) ? 1.0 : totalCount / (double)totalCount2;
		setPolygonPoints(classCounts, poly);
		scaleFactor = (totalCount < totalCount2) ? 1.0 : totalCount2 / (double)totalCount;
		setPolygonPoints(class2Counts, poly2);
		
		g.setColor(kGroup1Color);
		g.drawPolygon(poly.xpoints, poly.ypoints, poly.npoints);
		
		g.setColor(kGroup2Color);
		g.drawPolygon(poly2.xpoints, poly2.ypoints, poly2.npoints);
	}
}