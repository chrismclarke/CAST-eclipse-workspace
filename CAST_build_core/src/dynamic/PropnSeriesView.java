package dynamic;

import java.awt.*;

import dataView.*;
import axis.*;


public class PropnSeriesView extends PieSizeView {
//	static final private int kHalfBarWidth = 8;
	
	private HorizAxis yearAxis;
	private VertAxis freqAxis;
	private double startYear, yearStep;
	
	public PropnSeriesView(DataSet theData, XApplet applet,
													String yKey, Color[] catColors, HorizAxis yearAxis, VertAxis freqAxis,
													double startYear, double yearStep) {
		super(theData, applet, yKey, catColors);
		this.yearAxis = yearAxis;
		this.freqAxis = freqAxis;
		this.startYear = startYear;
		this.yearStep = yearStep;
	}
	
	private Point getScreenPoint(int yearIndex, double y, double[] total, Point thePoint) {
		int horizPos = yearAxis.numValToRawPosition(startYear + yearIndex * yearStep);
		int vertPos = freqAxis.numValToRawPosition(total == null ? y : (y / total[yearIndex] * 100.0));
		return translateToScreen(horizPos, vertPos, thePoint);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		NumSeriesVariable yVar = (NumSeriesVariable)getVariable(yKey);
		int nCats = yVar.noOfValues();
		int nYears = yVar.seriesLength();
		int xCoord[] = new int[2 * nYears + 1];
		int yCoord[] = new int[2 * nYears + 1];
		
		double total[] = null;
		if (showOnlyProportions) {
			total = new double[nYears];
			for (int i=0 ; i<nCats ; i++) {
				NumSeriesValue y = (NumSeriesValue)yVar.valueAt(i);
				for (int yearIndex=0 ; yearIndex<nYears ; yearIndex++)
					total[yearIndex] += y.toDouble(yearIndex);
			}
		}
		
		Point p = null;
		double cum[] = new double[nYears];
		
		for (int i=0 ; i<nYears ; i++) {
			p = getScreenPoint(i, 0.0, total, p);
			xCoord[i] = p.x;
			yCoord[i] = p.y;
		}
		
		for (int i=0 ; i<nCats ; i++) {
			for (int j=0 ; j<nYears ; j++) {
				xCoord[2 * nYears - j - 1] = xCoord[j];
				yCoord[2 * nYears - j - 1] = yCoord[j];
			}
			
			NumSeriesValue y = (NumSeriesValue)yVar.valueAt(i);
			for (int yearIndex=0 ; yearIndex<nYears ; yearIndex++) {
				cum[yearIndex] += y.toDouble(yearIndex);
				
				p = getScreenPoint(yearIndex, cum[yearIndex], total, p);
				xCoord[yearIndex] = p.x;
				yCoord[yearIndex] = p.y;
			}
			xCoord[2 * nYears] = xCoord[0];
			yCoord[2 * nYears] = yCoord[0];
			
			g.setColor(catColors[i]);
			g.fillPolygon(xCoord, yCoord, 2 * nYears + 1);
			g.drawPolygon(xCoord, yCoord, 2 * nYears + 1);
//			if (i < nCats - 1) {
//				g.setColor(getForeground());
//				g.drawPolyline(xCoord, yCoord, nYears);
//			}
		}
		g.setColor(Color.red);
		double yearIndex = yVar.getSeriesIndex();
		int horizPos = yearAxis.numValToRawPosition(startYear + yearIndex * yearStep);
		p = translateToScreen(horizPos, 0, p);
		g.drawLine(p.x, 0, p.x, getSize().height);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
}
	
