package ssq;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import distn.*;


public class GroupMeanDotView extends DotPlotView {
//	static final public String GROUP_MEAN_DOTPLOT = "groupMeanDotPlot";
	
	static final public int MEAN_ONLY = 0;
	static final public int CI_BANDS = 1;
	static final public int CI_POOLED_BANDS = 2;
	
	static final private int kMaxHorizJitter = 30;
	
	static final private Color kMeanColor = new Color(0x3399FF);		//	mid blue
	static final private Color kCiColor = new Color(0x99CCFF);		//	pale blue
	
	private NumCatAxis groupAxis;
	private CatVariable groupingVariable;
	
	private int meanDisplay = MEAN_ONLY;
	
	private boolean initialised = false;
	
	private double mean[];
	private double sd[];
	private int n[];
	
	public GroupMeanDotView(DataSet theData, XApplet applet, NumCatAxis numAxis, NumCatAxis groupAxis,
								String yKey, String xKey, double jitterPropn) {
		super(theData, applet, numAxis, jitterPropn);
		this.groupAxis = groupAxis;
		setActiveNumVariable(yKey);
		setActiveCatVariable(xKey);
		groupingVariable = (CatVariable)theData.getVariable(xKey);
	}
	
	public void setMeanDisplay(int meanDisplay) {
		this.meanDisplay = meanDisplay;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		Point newPoint = super.getScreenPoint(index, theVal, thePoint);
		if (newPoint != null) {
			int groupIndex = groupingVariable.getItemCategory(index);
			int offset = groupAxis.catValToPosition(groupIndex) - currentJitter / 2;
			if (groupAxis instanceof VertAxis)
				newPoint.y -= offset;
			else
				newPoint.x += offset;
		}
		return newPoint;
	}
	
	protected int getMaxJitter() {
		int noOfGroups = groupingVariable.noOfCategories();
		return Math.min(kMaxHorizJitter,
							(getSize().width - getViewBorder().left - getViewBorder().right) / noOfGroups / 2);
	}
	
	protected boolean initialise() {
		if (initialised)
			return false;
		
		int noOfGroups = groupingVariable.noOfCategories();
		mean = new double[noOfGroups];
		sd = new double[noOfGroups];
		n = new int[noOfGroups];
		
		ValueEnumeration ye = getNumVariable().values();
		ValueEnumeration xe = groupingVariable.values();
		while (ye.hasMoreValues() && xe.hasMoreValues()) {
			double y = ye.nextDouble();
			int group = groupingVariable.labelIndex(xe.nextValue());
			mean[group] += y;
			sd[group] += y * y;
			n[group] ++;
		}
		
		for (int i=0 ; i<noOfGroups ; i++) {
			sd[i] = (n[i] > 1) ? Math.sqrt((sd[i] - mean[i] * mean[i] / n[i]) / (n[i] - 1)) : Double.NaN;
			mean[i] /= n[i];
		}
		
		initialised = true;
		return true;
	}
	
/*
	private void drawBand(Graphics g, double mean, double sd, double z, int xCenter,
														int offset, Color fillColor, Point topLeftPoint,
														Point bottomRightPoint) {
		int yTopPos = axis.numValToRawPosition(mean + z * sd);
		int yBottomPos = axis.numValToRawPosition(mean - z * sd);
		topLeftPoint = translateToScreen(yTopPos, xCenter - offset,
																							topLeftPoint);
		bottomRightPoint = translateToScreen(yBottomPos, xCenter + offset,
																						bottomRightPoint);
		g.setColor(fillColor);
		g.fillRect(topLeftPoint.x, topLeftPoint.y, 2 * offset + 1,
															bottomRightPoint.y - topLeftPoint.y + 1);
	}
*/
	
	public int getPooledDf() {
		initialise();
		int pooledDf = 0;
		for (int i=0 ; i<n.length ; i++)
			pooledDf += (n[i] - 1);
		return pooledDf;
	}
	
	public double getPooledSd(int pooledDf) {
		initialise();
		double ssq = 0.0;
		for (int i=0 ; i<n.length ; i++)
			ssq += sd[i] * sd[i] * (n[i] - 1);
		return Math.sqrt(ssq / pooledDf);
	}
	
	private void drawBackground(Graphics g) {
		int offset = (currentJitter * 15) / 20;	//	50% more than currentJitter / 2
		Point p1 = null;
		Point p2 = null;
		
		int nX = groupingVariable.noOfCategories();
		
		int pooledDf = 0;
		double sPooled = 0.0;
		if (meanDisplay == CI_POOLED_BANDS) {
			pooledDf = getPooledDf();
			sPooled = getPooledSd(pooledDf);
		}
		
		for (int i=0 ; i<nX ; i++) {
			int xCenter = groupAxis.catValToPosition(i);
			
			if (meanDisplay != MEAN_ONLY) {
				g.setColor(kCiColor);
				double si = (meanDisplay == CI_BANDS) ? sd[i] : sPooled;
				double se = si / Math.sqrt(n[i]);
				int df = (meanDisplay == CI_BANDS) ? (n[i] - 1) : pooledDf;
				double t = TTable.quantile(0.975, df);
				
				int lowPos = axis.numValToRawPosition(mean[i] - t * se);
				p1 = translateToScreen(lowPos, xCenter - offset, p1);
				int highPos = axis.numValToRawPosition(mean[i] + t * se);
				p2 = translateToScreen(highPos, xCenter + offset, p2);
				
				fillRect(g, p1, p2);
			}
			
			g.setColor(kMeanColor);
			int meanPos = axis.numValToRawPosition(mean[i]);
			p1 = translateToScreen(meanPos, xCenter - offset, p1);
			p2 = translateToScreen(meanPos + 1, xCenter + offset, p2);
			fillRect(g, p1, p2);
		}
		
		g.setColor(getForeground());
	}
	
	public void paintView(Graphics g) {
		initialise();
		
		drawBackground(g);
		
		super.paintView(g);
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(getActiveNumKey()) || key.equals(getActiveCatKey())) {
			initialised = false;
			repaint();
		}
	}
}