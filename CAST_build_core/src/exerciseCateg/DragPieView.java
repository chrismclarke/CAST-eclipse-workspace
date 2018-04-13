package exerciseCateg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import exercise2.*;


public class DragPieView extends DataView implements StatusInterface {
//	static final public String DRAG_PIECHART = "dragPieChart";
	
	static final private double kMinHitOffset = 0.01;		//	propn of circle
	static final private int kFractionHorizBorder = 5;
	static final private int kFractionVertBorder = 2;
	static final private int kFractionLineGap = 2;
	
	static final private Color kArrowColor = Color.black;

	protected String catKey;
	private boolean dragCumNotOne = true;
	
	protected int cumCount[] = null;
	protected int totalCount;
	
	private double[] dragCumCount;
	
	private int dragCat = -1;
	private double propnOffset;
	
	protected boolean initialised = false;
	private PieDrawer pieDrawer;
	
	private boolean[] wrongCats = null;
	
	public DragPieView(DataSet theData, XApplet applet, String catKey, PieDrawer pieDrawer) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.catKey = catKey;
		this.pieDrawer = pieDrawer;
	}
	
	public DragPieView(DataSet theData, XApplet applet, String catKey) {
		this(theData, applet, catKey, new PieDrawer());
	}
	
	public String getStatus() {
		String s = "";
		for (int i=0 ; i<cumCount.length ; i++)
			s += cumCount[i] + " ";
		return s;
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		cumCount = new int[st.countTokens()];
		for (int i=0 ; i<cumCount.length ; i++)
			cumCount[i] = Integer.parseInt(st.nextToken());
		
		clearWrongCats();
		repaint();
	}
	
	public void setDragCumNotOne(boolean dragCumNotOne) {
		this.dragCumNotOne = dragCumNotOne;
	}
	
	private int[] correctCounts() {
//		initialise();
		
		CatVariable yVar = (CatVariable)getVariable(catKey);
		return yVar.getCounts();
	}
	
	public boolean[] wrongCats() {
		int[] correctCount = correctCounts();
		boolean isWrong[] = new boolean[cumCount.length];
		int previousCum = 0;
		for (int i=0 ; i<cumCount.length ; i++) {
			isWrong[i] = (cumCount[i] - previousCum) != correctCount[i];
			previousCum = cumCount[i];
		}
		return isWrong;
	}
	
	public void setCorrectCounts() {
		int[] correctCount = correctCounts();
		cumCount[0] = correctCount[0];
		for (int i=1 ; i<cumCount.length ; i++)
			cumCount[i] = cumCount[i - 1] + correctCount[i];
	}
	

	public void setWrongCats(boolean[] wrongCats) {
		this.wrongCats = wrongCats;
	}
	
	public void clearWrongCats() {
		wrongCats = null;
	}

//-------------------------------------------------------------------
	
	public void setDefaultCounts() {
		CatVariable variable = (CatVariable)getVariable(catKey);
		
		totalCount = variable.noOfValues();
		int nCats = variable.noOfCategories();
		int averageCount = totalCount / nCats;
		cumCount = new int[nCats];
		for (int i=0 ; i<nCats-1 ; i++)
			cumCount[i] = averageCount * (i + 1);
		cumCount[nCats - 1] = totalCount;
		
		dragCumCount = new double[nCats];
	}
	
	public void paintView(Graphics g) {
		pieDrawer.setRadius(Math.min(getSize().height, getSize().width) / 2 - 1, this);
		
		if (dragCat < 0)
			for (int i=0 ; i<cumCount.length ; i++)
				dragCumCount[i] = cumCount[i];							//	change from int[] into double[] for pieDrawer
		
		pieDrawer.fillPieSegments(g, dragCumCount, dragCat, dragCat);
		
		if (dragCat >= 0) {
			int lowIndex = dragCumNotOne ? 0 : dragCat;
			g.setColor(kArrowColor);
			pieDrawer.drawArc(g, dragCumCount, lowIndex, dragCat);
			
			@SuppressWarnings("unused")
			double thisCount = dragCumCount[dragCat];
			if (!dragCumNotOne && dragCat > 0)
				thisCount -= dragCumCount[dragCat - 1];
			
			double displayCount = dragCumCount[dragCat];
			if (dragCat > 0 && !dragCumNotOne)
				displayCount -= dragCumCount[dragCat - 1];
			
			
			Point midPoint = pieDrawer.findMidSegment(dragCumCount, lowIndex, dragCat);
			drawFraction(g, displayCount, totalCount, midPoint);
		}
		else if (wrongCats != null)
			pieDrawer.markWrongCats(g, dragCumCount, wrongCats);
	}
	
	private void drawFraction(Graphics g, double numerCount, int denomCount, Point midPoint) {
		NumValue numer = new NumValue(numerCount, 0);
		NumValue denom = new NumValue(denomCount, 0);
		
		double midPropn = pieDrawer.findPropn(midPoint.x, midPoint.y);
		boolean isTop = midPropn <= 0.25 || midPropn >= 0.75;
		boolean isLeft = midPropn > 0.5;
		
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int halfWidth = denom.stringWidth(g) / 2 + 1 + kFractionHorizBorder;
		int center = isLeft ? halfWidth : getSize().width - halfWidth ;
		int boxHeight = 2 * ascent + 1 + 2 * kFractionLineGap + 2 * kFractionVertBorder;
		int top = isTop ? 0 : getSize().height - boxHeight;
		int lineX = isLeft ? 2 * halfWidth : getSize().width - 2 * halfWidth;
		int lineY = isTop ? boxHeight : top;
		
		g.setColor(Color.yellow);
		g.fillRect(center - halfWidth, top, 2 * halfWidth, boxHeight);

		g.setColor(kArrowColor);
		
		int baseline = top + kFractionVertBorder + ascent;
		numer.drawCentred(g, center, baseline);
		
		halfWidth -= kFractionHorizBorder;
		g.drawLine(center - halfWidth, baseline + kFractionLineGap, center + halfWidth, baseline + kFractionLineGap);
		g.drawLine(center - halfWidth, baseline + kFractionLineGap + 1, center + halfWidth, baseline + kFractionLineGap + 1);
		
		baseline += ascent + 1 + 2 * kFractionLineGap;
		denom.drawCentred(g, center, baseline);
		
		g.drawLine(midPoint.x, midPoint.y, lineX, lineY);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		double hitPropn = pieDrawer.findPropn(x, y);
		
		int nearestCat = -1;
		double nearestPropn = -1;
		for (int i=0 ; i<cumCount.length-1 ; i++) {		//		ignore last category
			double thisPropn = cumCount[i] / (double)totalCount;
			if (Math.abs(thisPropn - hitPropn) <= Math.abs(nearestPropn - hitPropn)) {
				nearestCat = i;
				nearestPropn = thisPropn;
			}
		}
		
		if (Math.abs(nearestPropn - hitPropn) < kMinHitOffset)
			return new PropnPosInfo(nearestCat, hitPropn - nearestPropn);
		else
			return null;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		double hitPropn = pieDrawer.findPropn(x, y);
		return new PropnPosInfo(hitPropn);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		PropnPosInfo start = (PropnPosInfo)startInfo;
		dragCat = start.catIndex;
		propnOffset = start.propnOffset;
		
		int nCats = cumCount.length;
		dragCumCount = new double[nCats];
		for (int i=0 ; i<nCats ; i++)
			dragCumCount[i] = cumCount[i];
		
		repaint();
		
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		double newPropn = ((PropnPosInfo)toPos).propn - propnOffset;
		double newCum = newPropn * totalCount;
		
		int nCats = cumCount.length;
		newCum = Math.min(newCum, totalCount);
		
		int previousCum = (dragCat == 0) ? 0 : cumCount[dragCat - 1];
		newCum = Math.max(newCum, previousCum);
		
		dragCumCount[dragCat] = newCum;
		for (int i=dragCat+1 ; i<nCats-1 ; i++) {
			int nRemainingCats = nCats - i;
			double remainder = totalCount - dragCumCount[i - 1];
			 
			double minPerRemainingCat = Math.min(totalCount / 180 + 1, remainder / nRemainingCats);
																																		//		2 degrees or equal spacing of remainder
			
			dragCumCount[i] = Math.max(cumCount[i], dragCumCount[i - 1] + minPerRemainingCat);
		}
		dragCumCount[nCats - 1] = totalCount;
		
		repaint();
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (dragCat != -1) {
			int newCum = (int)Math.round(dragCumCount[dragCat]);
			
			int nCats = cumCount.length;
//			int nRemainingCats = nCats - dragCat - 1;
			int minPerRemainingCat = totalCount / 360 + 1;
			
			cumCount[dragCat] = newCum;
			for (int i=dragCat+1 ; i<nCats ; i++)
				cumCount[i] = Math.max(cumCount[i], cumCount[i - 1] + minPerRemainingCat);
			
			dragCat = -1;
			repaint();
			
			((ExerciseApplet)getApplet()).noteChangedWorking();
		}
	}
}