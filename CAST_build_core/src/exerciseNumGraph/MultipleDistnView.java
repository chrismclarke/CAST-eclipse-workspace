package exerciseNumGraph;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import exercise2.*;


public class MultipleDistnView extends CoreDragItemsView {
//	static public final String MULTIPLE_DISTN = "multipleDistn";
	
	static final public int DISTN_NAMES = TEXT_LABELS;
	static final public int STACKED_DOT_PLOT = TEXT_LABELS + 1;
	static final public int BOX_PLOT = TEXT_LABELS + 2;
	static final public int CUM_DISTN = TEXT_LABELS + 3;
	static final public int HISTOGRAM = TEXT_LABELS + 4;
	
	static final private Color kGridColor = new Color(0xDDDDDD);
	static final private Color kHistoFillColor = new Color(0xCCCCEE);
	
	protected String yKey[];
	protected HorizAxis theAxis;
	private double[] classLimits;
	
	private boolean drawGrid = true;
	
	public MultipleDistnView(DataSet theData, XApplet applet, HorizAxis theAxis, String[] yKey,
														int[] order, int displayType) {
		super(theData, applet, order, displayType, new Insets(0,5,0,5));
		
		this.yKey = yKey;
		this.theAxis = theAxis;
		
		if (displayType == DISTN_NAMES) {
			Font bigFont = applet.getBigFont();
			setFont(new Font(bigFont.getName(), Font.BOLD, bigFont.getSize() * 2));
		}
	}
	
	public void setYKeys(String[] yKey) {
		this.yKey = yKey;
	}
	
	public void setDrawGrid(boolean drawGrid) {
		this.drawGrid = drawGrid;
	}
	
	public void setClassLimits(double[] classLimits) {
		this.classLimits = classLimits;
	}

//----------------------------------------------------------------
	
	protected int noOfItems() {
		return yKey.length;
	}
	
	protected void drawBackground(Graphics g) {
		if (drawGrid) {
			g.setColor(kGridColor);
			
			Vector labels = theAxis.getLabels();
			Enumeration le = labels.elements();
			while (le.hasMoreElements()) {
				AxisLabel label = (AxisLabel)le.nextElement();
				double x = theAxis.minOnAxis + label.position * (theAxis.maxOnAxis - theAxis.minOnAxis);
				int xPos = getViewBorder().left + theAxis.numValToRawPosition(x);
				g.drawLine(xPos, 0, xPos, getSize().height);
			}
		}
	}
	
	protected String getItemName(int index) {
		return getVariable(yKey[index]).name;
	}
	
	protected void drawOneItem(Graphics g, int index, int baseline, int height) {
		if (displayType == STACKED_DOT_PLOT)
			drawOneStackedDotPlot(g, yKey[index], baseline, height);
		else if (displayType == BOX_PLOT)
			drawOneBoxPlot(g, yKey[index], baseline, height);
		else if (displayType == CUM_DISTN)
			drawOneCumulative(g, yKey[index], baseline, height);
		else				//	displayType == HISTOGRAM
			drawOneHistogram(g, yKey[index], baseline, height);
	}

//----------------------------------------------------------------
	
	private void drawOneStackedDotPlot(Graphics g, String key, int baseline, int height) {
		int stackWidth = getCrossSize() * 2 + 3;
		double stackStep = 0;
		try {
			stackStep = theAxis.positionToNumVal(stackWidth) - theAxis.positionToNumVal(0);
		} catch (AxisException e) {
		}
		
		int nStacks = (int)Math.round(Math.ceil((theAxis.maxOnAxis - theAxis.minOnAxis) / stackStep));
		int stackCount[] = new int[nStacks];
		
		NumVariable yVar = (NumVariable)getVariable(key);
		ValueEnumeration ye = yVar.values();
		
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			int stack = theAxis.numValToRawPosition(y) / stackWidth;
			stackCount[stack] ++;
		}
		
		int maxCount = 0;
		for (int i=0 ; i<stackCount.length ; i++)
			maxCount = Math.max(maxCount, stackCount[i]);
		int crossHeight = Math.min(height / maxCount, stackWidth);
		
		Point p = translateToScreen(0, baseline - 1, null);
		g.drawLine(0, p.y, getSize().width, p.y);
		for (int j=0 ; j<nStacks ; j++) {
			p = translateToScreen(j * stackWidth + stackWidth / 2, baseline + stackWidth / 2, p);
			for (int k=0 ; k<stackCount[j] ; k++)  {
				drawCross(g, p);
				p.y -= crossHeight;
			}
		}
	}
	
	private void drawOneHistogram(Graphics g, String key, int baseline, int height) {
		baseline --;															//	so bottom 'axis' does not show
		
		int classCount[] = new int[classLimits.length - 1];
		int maxCount = 0;
		
		NumVariable yVar = (NumVariable)getVariable(key);
		NumValue sortedY[] = yVar.getSortedData();
		int n = sortedY.length;
		int yIndex = 0;
		for (int i=0 ; i<classCount.length ; i++) {
			while (yIndex < n && sortedY[yIndex].toDouble() < classLimits[i + 1]) {
				classCount[i] ++;
				yIndex ++;
			}
			maxCount = Math.max(maxCount, classCount[i]);
		}
		
		Point p0 = translateToScreen(0, baseline, null);		//	left end of axis
		g.drawLine(0, p0.y, getSize().width, p0.y);
		Point p1 = null;
		
		for (int i=0 ; i<classCount.length ; i++) {
			int lowX = theAxis.numValToRawPosition(classLimits[i]);
			int highX = theAxis.numValToRawPosition(classLimits[i + 1]);
			int classTop = baseline + height * 9 * classCount[i] / 10 / maxCount;
			p0 = translateToScreen(lowX, classTop, p0);
			p1 = translateToScreen(highX, baseline, p1);
			g.setColor(kHistoFillColor);
			g.fillRect(p0.x, p0.y, (p1.x - p0.x), (p1.y - p0.y));
			g.setColor(getForeground());
			g.drawRect(p0.x, p0.y, (p1.x - p0.x), (p1.y - p0.y));
		}
	}
	
	private void drawOneBoxPlot(Graphics g, String key, int baseline, int height) {
		NumVariable yVar = (NumVariable)getVariable(key);
		NumValue sortedY[] = yVar.getSortedData();
		
		BoxInfo boxInfo = new BoxInfo();
		boxInfo.initialiseBox(sortedY, false, theAxis);
		boxInfo.setFillColor(Color.lightGray);
		boxInfo.vertMidLine = baseline + height / 3;
		boxInfo.boxBottom = boxInfo.vertMidLine - boxInfo.getBoxHeight() / 2;
		
		boxInfo.drawBoxPlot(g, this, sortedY, theAxis);
	}
	
	private void drawOneCumulative(Graphics g, String key, int baseline, int height) {
		baseline --;															//	so bottom 'axis' does not show
		
		NumVariable yVar = (NumVariable)getVariable(key);
		NumValue sortedY[] = yVar.getSortedData();
		int n = sortedY.length;
		
		int heightUsed = height * 9 / 10;
		int cumTop = translateToScreen(0, baseline + heightUsed, null).y;
		g.setColor(Color.lightGray);
		g.drawLine(0, cumTop, getSize().width, cumTop);
		
		Point lastP = translateToScreen(0, baseline, null);		//	left end of axis
		lastP.x = 0;
		g.drawLine(0, lastP.y, getSize().width, lastP.y);
		Point nextP = null;
		g.setColor(getForeground());
		
		if (classLimits == null)
			for (int i=0 ; i<n ; i++) {
				int nextY = baseline + (i + 1) * heightUsed / n;
				int nextX = theAxis.numValToRawPosition(sortedY[i].toDouble());
				nextP = translateToScreen(nextX, nextY, nextP);
				g.drawLine(lastP.x, lastP.y, nextP.x, lastP.y);
				g.drawLine(nextP.x, lastP.y, nextP.x, nextP.y);
				Point tempP = lastP;
				lastP = nextP;
				nextP = tempP;
			}
		else {
			int cum = 0;
			for (int i=0 ; i<classLimits.length ; i++) {
				while (cum < n && sortedY[cum].toDouble() <= classLimits[i])
					cum ++;
				int nextY = baseline + cum * heightUsed / n;
				int nextX = theAxis.numValToRawPosition(classLimits[i]);
				nextP = translateToScreen(nextX, nextY, nextP);
				g.drawLine(lastP.x, lastP.y, nextP.x, nextP.y);
				Point tempP = lastP;
				lastP = nextP;
				nextP = tempP;
			}
		}
		g.drawLine(lastP.x, lastP.y, getSize().width, lastP.y);
	}
}