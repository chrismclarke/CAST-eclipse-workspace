package randomisation;

import java.awt.*;

import dataView.*;
import axis.*;


public class TwoGroupDotView extends RandomisationView {
	static final private int kMaxVertJitter = 30;
	static final private int kMaxHorizJitter = 30;
	static final private Color kPaleBlueColor = new Color(0xEEEEFF);
	
//	static public final String TWO_GROUP_DOT = "twoGroupDot";
	
	private NumCatAxis groupAxis;
	
	private int oldGroups[];
	
	public TwoGroupDotView(DataSet theData, XApplet applet, NumCatAxis numAxis, NumCatAxis groupAxis,
																							String catKey, String actualRandKey) {
		super(theData, applet, numAxis, catKey, actualRandKey);
		this.groupAxis = groupAxis;
	}
	
	public void fixOldInfo() {
		CatVariable xVar = getCatVariable();
		if (oldGroups == null || oldGroups.length != xVar.noOfValues())
			oldGroups = new int[xVar.noOfValues()];
		for (int i=0 ; i<oldGroups.length ; i++)
			oldGroups[i] = xVar.getItemCategory(i);
		if (initialised)
			setFrame(0);
		else
			setInitialFrame(0);
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		Point newPoint = super.getScreenPoint(index, theVal, thePoint);
		if (newPoint != null) {
			int newGroupIndex = randVariable.getItemCategory(index);
			int groupCenter = (groupAxis.catValToPosition(0) + groupAxis.catValToPosition(1)) / 2;
			int frame = getCurrentFrame();
			if (frame < kEndFrame / 3) {
				int distFromStart = frame;
				int distFromMiddle = kEndFrame / 3 - distFromStart;
				groupCenter = (groupAxis.catValToPosition(oldGroups[index]) * distFromMiddle
										+ groupCenter * distFromStart) / (distFromStart + distFromMiddle);
			}
			else if (frame > kEndFrame * 2 / 3) {
				int distFromEnd = kEndFrame - frame;
				int distFromMiddle = kEndFrame / 3 - distFromEnd;
				groupCenter = (groupAxis.catValToPosition(newGroupIndex) * distFromMiddle
										+ groupCenter * distFromEnd) / (distFromMiddle + distFromEnd);
			}
			
			int offset = groupCenter - currentJitter / 2;
			if (!vertNotHoriz)
				newPoint.y -= offset;
			else
				newPoint.x += offset;
		}
		return newPoint;
	}
	
	protected int getMaxJitter() {
		int noOfGroups = getCatVariable().noOfCategories();
		if (!vertNotHoriz)
			return Math.min(kMaxVertJitter,
							(getSize().height - getViewBorder().top - getViewBorder().bottom) / noOfGroups / 2);
		else
			return Math.min(kMaxHorizJitter,
							(getSize().width - getViewBorder().left - getViewBorder().right) / noOfGroups / 2);
	}
	
	public double[] getMeans() {
		int count[] = new int[2];
		double mean[] = new double[2];
		
		ValueEnumeration ye = getNumVariable().values();
		CatVariable xVar = getCatVariable();
		ValueEnumeration xe = xVar.values();
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			int x = xVar.labelIndex(xe.nextValue());
			count[x] ++;
			mean[x] += y;
		}
		for (int i=0 ; i<2 ; i++)
			mean[i] /= count[i];
		return mean;
	}
	
	protected void drawBackground(Graphics g) {
		double mean[] = getMeans();
		
		try {
			int y0Pos = axis.numValToPosition(mean[0]);
			int y1Pos = axis.numValToPosition(mean[1]);
			
			int c0Pos = groupAxis.catValToPosition(0);
			int c1Pos = groupAxis.catValToPosition(1);
			
			Point p0 = translateToScreen(y0Pos, 0, null);
			Point p1 = translateToScreen(y0Pos, (c0Pos + 3 * c1Pos) / 4, null);
			
			Point p2 = translateToScreen(y1Pos, (3 * c0Pos + c1Pos) / 4, null);
			Point p3 = translateToScreen(y1Pos, getDisplayWidth(), null);
			
			g.setColor(kPaleBlueColor);
			
			int x1 = Math.min(p1.x, p2.x);
			int x2 = Math.max(p1.x, p2.x);
			int y1 = Math.min(p1.y, p2.y);
			int y2 = Math.max(p1.y, p2.y);
			g.fillRect(x1, y1, (x2 - x1), (y2 - y1));
			
			g.setColor(Color.blue);
			
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
			g.drawLine(p2.x, p2.y, p3.x, p3.y);
			
			g.setColor(Color.red);
			
			int xMid = (x1 + x2) / 2;
			drawArrow(g, p1.y, p2.y, xMid);
			
			g.setColor(getForeground());
		}
		catch (AxisException e) {
		}
	}
}