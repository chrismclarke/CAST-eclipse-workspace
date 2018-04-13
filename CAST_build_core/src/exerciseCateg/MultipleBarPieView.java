package exerciseCateg;

import java.awt.*;

import dataView.*;
import axis.*;
import exercise2.*;


public class MultipleBarPieView extends CoreDragItemsView {
//	static public final String MULTIPLE_BAR_PIE = "multipleBarPie";
	
	static final public int BAR_CHART = TEXT_LABELS + 1;
	static final public int PIE_CHART = TEXT_LABELS + 2;
	
	static final private int kPieVertBorder = 2;
	static final private int kHalfBarWidth = 8;
	
	private String yKey[];
	private HorizAxis theAxis;
	private PieDrawer pieDrawer;		//	holds colour information for bar chart too
	
	public MultipleBarPieView(DataSet theData, XApplet applet, HorizAxis theAxis, String[] yKey,
														int[] order, int displayType, PieDrawer pieDrawer) {
		super(theData, applet, order, displayType, new Insets(0,5,0,5));
		
		this.yKey = yKey;
		this.theAxis = theAxis;
		this.pieDrawer = pieDrawer;
	}
	
	public void setYKeys(String[] yKey) {
		this.yKey = yKey;
	}

//----------------------------------------------------------------
	
	protected int noOfItems() {
		return yKey.length;
	}
	
	protected void drawBackground(Graphics g) {
	}
	
	protected String getItemName(int index) {
		return getVariable(yKey[index]).name;
	}
	
	protected void drawOneItem(Graphics g, int index, int baseline, int height) {
		if (displayType == BAR_CHART)
			drawOneBarChart(g, yKey[index], baseline, height);
		else				//	displayType == PIE_CHART
			drawOnePieChart(g, yKey[index], baseline, height);
	}

//----------------------------------------------------------------
	
	private void drawOneBarChart(Graphics g, String key, int baseline, int height) {
		baseline --;															//	so bottom 'axis' does not show
		
		CatVariable yVar = (CatVariable)getVariable(key);
//		int nCats = yVar.noOfCategories();
		int catCount[] = yVar.getCounts();
//		int totalCount = yVar.noOfValues();
		
		int maxCount = 0;
		for (int i=0 ; i<catCount.length ; i++)
			maxCount = Math.max(maxCount, catCount[i]);
		
		Point p0 = translateToScreen(0, baseline, null);		//	left end of axis
		g.drawLine(0, p0.y, getSize().width, p0.y);
		Point p1 = null;
		
		for (int i=0 ; i<catCount.length ; i++) {
			int xPos = theAxis.catValToPosition(i);
			int classTop = baseline + height * 9 * catCount[i] / 10 / maxCount;
			p0 = translateToScreen(xPos - kHalfBarWidth, classTop, p0);
			p1 = translateToScreen(xPos + kHalfBarWidth, baseline, p1);
			g.setColor(pieDrawer.getCatColor(i));
			g.fillRect(p0.x, p0.y, (p1.x - p0.x), (p1.y - p0.y));
			g.setColor(getForeground());
			g.drawRect(p0.x, p0.y, (p1.x - p0.x), (p1.y - p0.y));
		}
	}
	
	private void drawOnePieChart(Graphics g, String key, int baseline, int height) {
		CatVariable yVar = (CatVariable)getVariable(key);
		int nCats = yVar.noOfCategories();
		int catCount[] = yVar.getCounts();
		int totalCount = yVar.noOfValues();
		
		int radius = height / 2 - kPieVertBorder;
		int top = baseline + height - kPieVertBorder;
		int left = kPieVertBorder;
		Point topLeft = translateToScreen(left, top, null);
		
		int startAngle = 90;
		int cumCount = 0;
		for (int i=0 ; i<nCats ; i++) {
			cumCount += catCount[i];
			int endAngle = 90 - (cumCount * 360) / totalCount;
			if (endAngle < 0)
				endAngle += 360;
			int degrees = endAngle - startAngle;
			if (degrees > 0)
				degrees -= 360;
			if (cumCount == totalCount && (i == 0 || cumCount - catCount[i - 1] == 0))
				degrees = -360;
			if (degrees < 0) {
				g.setColor(pieDrawer.getCatColor(i));
				g.fillArc(topLeft.x, topLeft.y, 2 * radius, 2 * radius, startAngle, degrees);
			}
			startAngle = endAngle;
		}
	}
}