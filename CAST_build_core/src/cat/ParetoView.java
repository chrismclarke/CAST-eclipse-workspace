package cat;

import java.awt.*;

import dataView.*;
import axis.*;


public class ParetoView extends DataView {
//	static final public String PARETO_CHART = "paretoChart";
	
	static private final int kFinalFrame = 20;
	static private final int kFrameRate = 20;
	
	static final private int kHalfBarWidth = 7;
//	static final private int kHitSlop = 3;
	static final private int kArrowSize = 5;
	
	static final private int TO_EXPANDED = 0;
	static final private int TO_ZERO_ONE = 1;
	static final private int SELECT_CUMULATIVE = 2;
	static final private int NO_SELECTION = 3;
	
	static final private Color kHighlightColour = new Color(0xFFFFCC);
	static final private Color kFrameColor = new Color(0x333333);
	static final private Color kDimFrameColor = new Color(0xCCCCCC);
	
	private String catKey;
	private HorizAxis catAxis;
	private VertAxis propnAxis, cumAxis;
	private double maxProbScale;
	
	private boolean initialised = false;
	
	private int currentXCentre[], startXCentre[], endXCentre[];
	private double currentYBottom[], startYBottom[], endYBottom[];
	private double barHeight[];
	
	private int selectedCat = -1;
	private int animationType = SELECT_CUMULATIVE;
	
	public ParetoView(DataSet theData, XApplet applet, String catKey, HorizAxis catAxis,
																VertAxis propnAxis, VertAxis cumAxis, double maxProbScale) {
		super(theData, applet, new Insets(5, 0, 0, 0));
		this.catKey = catKey;
		this.catAxis = catAxis;
		this.propnAxis = propnAxis;
		this.cumAxis = cumAxis;
		this.maxProbScale = maxProbScale;
		setCrossSize(LARGE_CROSS);
	}
	
	protected boolean initialise(CatVariable variable, Graphics g) {
		if (!initialised) {
			int nCats = variable.noOfCategories();
			int count[] = variable.getCounts();
			int totalCount = variable.noOfValues();
			
			currentXCentre = new int[nCats];
			startXCentre = new int[nCats];
			endXCentre = new int[nCats];
			
			currentYBottom = new double[nCats];
			startYBottom = new double[nCats];
			endYBottom = new double[nCats];
			
			barHeight = new double[nCats];
			for (int i=0 ; i<nCats ; i++)
				barHeight[i] = ((double)count[i]) / totalCount;
			
			for (int i=0 ; i<nCats ; i++)
				currentXCentre[i] = startXCentre[i] = endXCentre[i] = catAxis.catValToPosition(i);
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	private void drawHighlight(CatVariable variable, Graphics g) {
		if (selectedCat >= 0) {
			int xCentre = catAxis.catValToPosition(selectedCat);
			int width = catAxis.catValToPosition(1) - catAxis.catValToPosition(0) - 2;
			Point p = translateToScreen(xCentre - width / 2, 0, null);
			g.setColor(kHighlightColour);
			g.fillRect(p.x, 0, width, getSize().height);
		}
	}
	
	private void drawDimBars(CatVariable variable, Graphics g) {
		int nCats = variable.noOfCategories();
		int count[] = variable.getCounts();
		double totalDouble = variable.noOfValues();
		Point p1 = null;
		Point p2 = null;
		for (int i=0 ; i<nCats ; i++) {
			int yBottom = propnAxis.numValToRawPosition(0.0);
			int yTop = propnAxis.numValToRawPosition(count[i] / totalDouble);
			int xCentre = catAxis.catValToPosition(i);
			
			p1 = translateToScreen(xCentre - kHalfBarWidth, yTop, p1);
			p2 = translateToScreen(0, yBottom, p2);
			
			g.setColor(CatDataView.getColor(i, false));
			g.fillRect(p1.x, p1.y, 2 * kHalfBarWidth + 1, p2.y - p1.y + 1);
			g.setColor(kDimFrameColor);
			g.drawRect(p1.x, p1.y, 2 * kHalfBarWidth, p2.y - p1.y);
		}
	}
	
	private void drawBoldBars(CatVariable variable, Graphics g) {
		int currentFrame = kFinalFrame;
		double propn = 1.0;
		if (animationType == SELECT_CUMULATIVE && animationType != NO_SELECTION) {
			currentFrame = getCurrentFrame();
			propn = currentFrame / (double)kFinalFrame;
		}
		
		int nCats = variable.noOfCategories();
		Point p1 = null;
		Point p2 = null;
		for (int i=0 ; i<nCats ; i++) {
			currentYBottom[i] = (1.0 - propn) * startYBottom[i] + propn * endYBottom[i];
			int yBottomPos = propnAxis.numValToRawPosition(currentYBottom[i]);
			int yTopPos = propnAxis.numValToRawPosition(currentYBottom[i] + barHeight[i]);
			currentXCentre[i] = ((kFinalFrame - currentFrame) * startXCentre[i]
																								+ currentFrame * endXCentre[i]) / kFinalFrame;
			
			p1 = translateToScreen(currentXCentre[i] - kHalfBarWidth, yTopPos, p1);
			p2 = translateToScreen(0, yBottomPos, p2);
			
			g.setColor(CatDataView.getColor(i, true));
			g.fillRect(p1.x, p1.y, 2 * kHalfBarWidth + 1, p2.y - p1.y + 1);
			g.setColor(kFrameColor);
			g.drawRect(p1.x, p1.y, 2 * kHalfBarWidth, p2.y - p1.y);
		}
	}
	
	private void drawCumulative(CatVariable variable, Graphics g) {
		int nCats = variable.noOfCategories();
		int count[] = variable.getCounts();
		double totalDouble = variable.noOfValues();
		Point previousP = null;
		Point currentP = null;
		double cumProb = 0.0;
		
		g.setColor(Color.black);
		for (int i=0 ; i<nCats ; i++) {
			int xCentre = catAxis.catValToPosition(i);
			cumProb += (count[i] / totalDouble);
			int cumPos = cumAxis.numValToRawPosition(cumProb);
			
			Point temp = previousP;
			previousP = currentP;
			currentP = translateToScreen(xCentre, cumPos, temp);
			
			if (previousP != null)
				g.drawLine(previousP.x, previousP.y, currentP.x, currentP.y);
			drawBlob(g, currentP);
		}
		if (selectedCat >= 0) {
			g.setColor(Color.red);
			int xCentre = catAxis.catValToPosition(selectedCat);
			cumProb = 0.0;
			for (int i=0 ; i<=selectedCat ; i++)
				cumProb += (count[i] / totalDouble);
				int cumPos = cumAxis.numValToRawPosition(cumProb);
				currentP = translateToScreen(xCentre, cumPos, currentP);
				int rightPos = getSize().width - 1;
				g.drawLine(currentP.x, currentP.y, rightPos, currentP.y);
				g.drawLine(rightPos - 1, currentP.y, rightPos - kArrowSize, currentP.y - kArrowSize);
				g.drawLine(rightPos, currentP.y, rightPos - kArrowSize, currentP.y + kArrowSize);
		}
	}
	
	public void paintView(Graphics g) {
		CatVariable variable = (CatVariable)getVariable(catKey);
		initialise(variable, g);
		
		if (animationType != SELECT_CUMULATIVE && animationType != NO_SELECTION) {
			int currentFrame = getCurrentFrame();
			if (animationType == TO_ZERO_ONE)
				currentFrame = kFinalFrame - currentFrame;
			double maxProb = 1.0 - currentFrame * (1.0 - maxProbScale) / kFinalFrame;
			
			if (propnAxis.maxOnAxis != maxProb) {
				propnAxis.maxOnAxis = maxProb;
				propnAxis.setPower(1.0);
			}
		}
		
		drawHighlight(variable, g);
		drawDimBars(variable, g);
		drawBoldBars(variable, g);
		drawCumulative(variable, g);
		
		if (animationType != SELECT_CUMULATIVE && animationType != NO_SELECTION
																									&& getCurrentFrame() == kFinalFrame)
			animationType = (propnAxis.maxOnAxis == cumAxis.maxOnAxis) ? SELECT_CUMULATIVE
																												: NO_SELECTION;
	}
	
	public void animateChange(boolean expandedProbScale) {
		selectedCat = -1;
		for (int i=0 ; i<endXCentre.length ; i++) {
			endXCentre[i] = catAxis.catValToPosition(i);
			endYBottom[i] = 0.0;
		}
		
		animationType = expandedProbScale ? TO_EXPANDED : TO_ZERO_ONE;
		animateFrames(0, kFinalFrame, kFrameRate, null);
	}

//-----------------------------------------------------------------------------------
	
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return animationType == SELECT_CUMULATIVE;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		int nearestCat = catAxis.positionToCatVal(hitPos.x);
		int nearestX = catAxis.catValToPosition(nearestCat);
		
		return new CatPosInfo(nearestCat, hitPos.x >= nearestX);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		int newSel = (startInfo == null) ? -1 : ((CatPosInfo)startInfo).catIndex;
		if (newSel <= 0 && selectedCat <= 0) {
			selectedCat = newSel;
			repaint();
		}
		else {
			selectedCat = newSel;
			
			int tempX[] = startXCentre;
			startXCentre = currentXCentre;
			currentXCentre = tempX;
			for (int i=0 ; i<currentXCentre.length ; i++)
				endXCentre[i] = catAxis.catValToPosition(Math.max(i, selectedCat));
			
			double tempY[] = startYBottom;
			startYBottom = currentYBottom;
			currentYBottom = tempY;
			double cum = 0.0;
			for (int i=0 ; i<=selectedCat ; i++) {
				endYBottom[i] = cum;
				cum += barHeight[i];
			}
			for (int i=selectedCat+1 ; i<endYBottom.length ; i++)
				endYBottom[i] = 0.0;
			
			animateFrames(0, kFinalFrame, kFrameRate, null);
		}
		
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		startDrag(endPos);
	}
}