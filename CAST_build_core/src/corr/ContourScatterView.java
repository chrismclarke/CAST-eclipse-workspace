package corr;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;

//import utils.*;
//import scatter.*;


public class ContourScatterView extends ScatterView {
//	static public final String CONTOUR_SCATTER_PLOT = "contourScatterPlot";
	
	static final private int kNoOfContours = 12;
	static final private double kExtremeZ = 2.7;		//	needs to be more than 2.5 to shade into margin
	
	static final private Color blue1 = new Color(0xCCCCFF);
	static final private Color blue2 = new Color(0x9999FF);
	static final private Color blue3 = new Color(0x6666FF);
	static final private Color blue4 = new Color(0x3333FF);
	static final private Color blue5 = new Color(0x0000FF);
	static final private Color blue6 = new Color(0x000099);
	static final private Color red1 = new Color(0xFFCCCC);
	static final private Color red2 = new Color(0xFF9999);
	static final private Color red3 = new Color(0xFF6666);
	static final private Color red4 = new Color(0xFF3333);
	static final private Color red5 = new Color(0xFF0000);
	static final private Color red6 = new Color(0x990000);
	
	private double plotValue[][] = new double[kNoOfContours][];
	
	public ContourScatterView(DataSet theData, XApplet applet,
										HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		initialiseCountours();
	}
	
	private void initialiseCountours() {
		for (int i=0 ; i<kNoOfContours ; i++) {
			double k = (i+1) * 0.5;
			double rootK = Math.sqrt(k);
			double a = -2.0 * Math.log(kExtremeZ / rootK);
			int noOfLines = (int)Math.round(12 / Math.max(k, 1.0));
			plotValue[i] = new double[noOfLines + 1];
			double invNoOfLines = 1.0 / noOfLines;
			for (int j=0 ; j<=noOfLines ; j++) {
				double t = j * invNoOfLines;
				plotValue[i][j] = rootK * Math.exp(a * (t - 0.5));
			}
		}
	}
	
	private Point getScreenPoint(double xVal, double yVal, Point thePoint) {
							//		xVal and yVal may be outside main axis to allow shading into margin
		int vertPos = (int)Math.round((yAxis.getAxisLength() - 1) * (yVal + 2.5) / 5.0);
		int horizPos = (int)Math.round((axis.getAxisLength() - 1) * (xVal + 2.5) / 5.0);
		return translateToScreen(horizPos, vertPos, thePoint);
	}
	
	static final private boolean FILL = true;
	static final private boolean DRAW = false;
	
	private void drawContour(Graphics g, double[] contour, boolean negX, boolean negY,
																							boolean fillNotDraw) {
		int noOfPoints = contour.length;
		int xPos[] = new int[noOfPoints + 1];
		int yPos[] = new int[noOfPoints + 1];
		Point p = null;
		for (int ix=0, iy=noOfPoints-1 ; iy>=0 ; ix++, iy--) {
			double x = contour[ix];
			if (negX)
				x = -x;
			double y = contour[iy];
			if (negY)
				y = -y;
			p = getScreenPoint(x, y, p);
			xPos[ix] = p.x;
			yPos[ix] = p.y;
		}
		xPos[noOfPoints] = xPos[0];
		yPos[noOfPoints] = yPos[noOfPoints - 1];
		if (fillNotDraw)
			g.fillPolygon(xPos, yPos, noOfPoints + 1);
		else
			g.drawPolygon(xPos, yPos, noOfPoints + 1);
	}
	
	private void shadeBackground(Graphics g) {
		g.setColor(Color.lightGray);
		Point p1 = getScreenPoint(-kExtremeZ, 0.0, null);
		Point p2 = getScreenPoint(kExtremeZ, 0.0, null);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		p1 = getScreenPoint(0.0, -kExtremeZ, p1);
		p2 = getScreenPoint(0.0, kExtremeZ, p2);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		
		g.setColor(blue1);
		drawContour(g, plotValue[0], false, false, DRAW);
		drawContour(g, plotValue[1], false, false, FILL);
		drawContour(g, plotValue[0], true, true, DRAW);
		drawContour(g, plotValue[1], true, true, FILL);
		
		g.setColor(blue2);
		drawContour(g, plotValue[2], false, false, DRAW);
		drawContour(g, plotValue[3], false, false, FILL);
		drawContour(g, plotValue[2], true, true, DRAW);
		drawContour(g, plotValue[3], true, true, FILL);
		
		g.setColor(blue3);
		drawContour(g, plotValue[4], false, false, DRAW);
		drawContour(g, plotValue[5], false, false, FILL);
		drawContour(g, plotValue[4], true, true, DRAW);
		drawContour(g, plotValue[5], true, true, FILL);
		
		g.setColor(blue4);
		drawContour(g, plotValue[6], false, false, DRAW);
		drawContour(g, plotValue[7], false, false, FILL);
		drawContour(g, plotValue[6], true, true, DRAW);
		drawContour(g, plotValue[7], true, true, FILL);
		
		g.setColor(blue5);
		drawContour(g, plotValue[8], false, false, DRAW);
		drawContour(g, plotValue[9], false, false, FILL);
		drawContour(g, plotValue[8], true, true, DRAW);
		drawContour(g, plotValue[9], true, true, FILL);
		
		g.setColor(blue6);
		drawContour(g, plotValue[10], false, false, DRAW);
		drawContour(g, plotValue[11], false, false, DRAW);
		drawContour(g, plotValue[10], true, true, DRAW);
		drawContour(g, plotValue[11], true, true, DRAW);
		
		g.setColor(red1);
		drawContour(g, plotValue[0], true, false, DRAW);
		drawContour(g, plotValue[1], true, false, FILL);
		drawContour(g, plotValue[0], false, true, DRAW);
		drawContour(g, plotValue[1], false, true, FILL);
		
		g.setColor(red2);
		drawContour(g, plotValue[2], true, false, DRAW);
		drawContour(g, plotValue[3], true, false, FILL);
		drawContour(g, plotValue[2], false, true, DRAW);
		drawContour(g, plotValue[3], false, true, FILL);
		
		g.setColor(red3);
		drawContour(g, plotValue[4], true, false, DRAW);
		drawContour(g, plotValue[5], true, false, FILL);
		drawContour(g, plotValue[4], false, true, DRAW);
		drawContour(g, plotValue[5], false, true, FILL);
		
		g.setColor(red4);
		drawContour(g, plotValue[6], true, false, DRAW);
		drawContour(g, plotValue[7], true, false, FILL);
		drawContour(g, plotValue[6], false, true, DRAW);
		drawContour(g, plotValue[7], false, true, FILL);
		
		g.setColor(red5);
		drawContour(g, plotValue[8], true, false, DRAW);
		drawContour(g, plotValue[9], true, false, FILL);
		drawContour(g, plotValue[8], false, true, DRAW);
		drawContour(g, plotValue[9], false, true, FILL);
		
		g.setColor(red6);
		drawContour(g, plotValue[10], true, false, DRAW);
		drawContour(g, plotValue[11], true, false, DRAW);
		drawContour(g, plotValue[10], false, true, DRAW);
		drawContour(g, plotValue[11], false, true, DRAW);
		
		g.setColor(getForeground());
	}
	
	public void paintView(Graphics g) {
		shadeBackground(g);
		super.paintView(g);
	}
}
	
