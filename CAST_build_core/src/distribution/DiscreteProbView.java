package distribution;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;


public class DiscreteProbView extends MarginalDataView {
	
	static final public int NO_DRAG = 0;
	static final public int DRAG_PROB = 1;
	static final public int DRAG_CUMULATIVE = 2;
	
	static final private double kProbForMaxHt = 0.8;
	static final private int kDragSlop = 150;
	static final private double kDisplayFactor = 1.1;
	
	static final protected Color kDistnColor = new Color(0x999999);
	static final private Color kHighlightColor = new Color(0x0000CC);
	static final protected Color kLabelColor = new Color(0xBBBBBB);
	
	private static final int kMinDensityWidth = 30;
	
	protected String distnKey, otherDistnKey;
	protected NumCatAxis pAxis;
	protected NumCatAxis countAxis;
	protected int dragType;
	
	private int minWidth = kMinDensityWidth;
	
	private double lambdaForMaxHt = 10;
	
	private Color distnColor = kDistnColor;
	private Color highlightColor = kHighlightColor;
	
	private String titleString = null;
	
	private boolean forceFixedMaxProb = false;
	private double fixedMaxProb = 1.0;
	private int extraBarWidth = 0;
	
	public DiscreteProbView(DataSet theData, XApplet applet, String distnKey, String otherDistnKey,
																																			NumCatAxis countAxis, int dragType) {
		this(theData, applet, distnKey, otherDistnKey, null, countAxis, dragType);
	}
	
	public DiscreteProbView(DataSet theData, XApplet applet, String distnKey, String otherDistnKey,
																			NumCatAxis pAxis, NumCatAxis countAxis, int dragType) {
																									//		pAxis only used for binomial distn
		super(theData, applet, new Insets(0, 0, 0, 0), countAxis);
		this.distnKey = distnKey;
		this.otherDistnKey = otherDistnKey;
		this.pAxis = pAxis;
		this.countAxis = countAxis;
		this.dragType = dragType;
	}
	
	public int minDisplayWidth() {
		return minWidth;
	}
	
	public void setMinDisplayWidth(int minWidth) {
		this.minWidth = minWidth;
	}
	
	public void setDensityColor(Color c) {
		distnColor = c;
	}
	
	public void setHighlightColor(Color c) {
		highlightColor = c;
	}
	
	public void setWiderBars(int extraBarWidth) {
		this.extraBarWidth = extraBarWidth;
	}
	
	public void setBaseLambda(double lambdaForMaxHt) {
		this.lambdaForMaxHt = lambdaForMaxHt;			//	For any lambda greater, probs are scaled to have the same sum as for this lambda
																							//	For any lambda lower, the max height is always the max allowed
	}
	
	public void setTitleString(String titleString, XApplet applet) {
		this.titleString = titleString;
		setFont(applet.getBigBoldFont());
	}
	
	protected void drawTitleString(Graphics g) {
		if (titleString != null) {
			g.setColor(kLabelColor);
			int ascent = g.getFontMetrics().getAscent();
			g.drawString(titleString, 4, ascent + 2);
			g.setColor(getForeground());
		}
	}
	
	public void paintView(Graphics g) {
		drawTitleString(g);
		
		Point topLeft = null;
		DiscreteDistnVariable y = (DiscreteDistnVariable)getVariable(distnKey);
		int maxY = (int)Math.round(Math.floor(countAxis.maxOnAxis));
		if (y instanceof BinomialDistnVariable)
			maxY = ((BinomialDistnVariable)y).getCount();
		
		double maxProb = getMaxProb();
		
		int barSpacing = 0;
		try {
			int x0Pos = (pAxis != null) ? pAxis.numValToPosition(0.0) : countAxis.numValToPosition(0.0);
			int x1Pos = (pAxis != null) ? pAxis.numValToPosition(1.0 / maxY) : countAxis.numValToPosition(1.0);
			barSpacing = x1Pos - x0Pos;
		} catch (AxisException e) {
			int x0Pos = (pAxis != null) ? pAxis.numValToRawPosition(pAxis.minOnAxis) : countAxis.numValToRawPosition(countAxis.minOnAxis);
			int x1Pos = (pAxis != null) ? pAxis.numValToRawPosition(pAxis.minOnAxis + 1.0 / maxY) : countAxis.numValToRawPosition(countAxis.minOnAxis + 1.0);
			barSpacing = x1Pos - x0Pos;
		}
		
		int halfBarWidth = (barSpacing >= 20) ? 2
								: (barSpacing >= 10) ? 1
								: 0;
		halfBarWidth += extraBarWidth;		//	sometimes wider bars are needed to be seen if crosses are drawn on top later
		
		for (int i=0 ; i<=maxY ; i++)
			try {
				int x = (pAxis != null) ? pAxis.numValToPosition(((double)i) / maxY) : countAxis.numValToPosition(i);
				double prob = y.getScaledProb(i) * y.getProbFactor();
				int ht = (int)Math.round(getSize().height * prob / maxProb);
				topLeft = translateToScreen(x, ht, topLeft);
				
				if (i < y.getMinSelection() || i > y.getMaxSelection())
					g.setColor(distnColor);
				else
					g.setColor(highlightColor);
				
				g.fillRect(topLeft.x - halfBarWidth, topLeft.y + 1, 2 * halfBarWidth + 1,
																						getSize().height - topLeft.y - 1);
			} catch (AxisException e) {
			}
	}
	
	public double getMaxProb() {
		if (forceFixedMaxProb)
			return fixedMaxProb;
		
		DiscreteDistnVariable y = (DiscreteDistnVariable)getVariable(distnKey);
		double maxProbMax = 0.0;
		
		if (y instanceof BinomialDistnVariable) {
			BinomialDistnVariable yBinom = (BinomialDistnVariable)y;
			double p = yBinom.getProb();
			yBinom.setProb(kProbForMaxHt);
			maxProbMax = yBinom.getMaxScaledProb() * yBinom.getProbFactor();
			yBinom.setProb(p);
		}
		else if (y instanceof PoissonDistnVariable) {
			PoissonDistnVariable yPoisson = (PoissonDistnVariable)y;
			NumValue oldLambda = yPoisson.getMean();
			NumValue newLambda = new NumValue(lambdaForMaxHt, oldLambda.decimals);
			yPoisson.setLambda(newLambda);
			maxProbMax = yPoisson.getMaxScaledProb() * yPoisson.getProbFactor();
			yPoisson.setLambda(oldLambda);
		}
		double currentMaxProb = y.getMaxScaledProb() * y.getProbFactor();
		return Math.max(maxProbMax, currentMaxProb) * kDisplayFactor;
	}
	
	public void setForceZeroOneAxis(boolean forceZeroOneAxis) {
		forceFixedMaxProb = forceZeroOneAxis;
		fixedMaxProb = 1.0;
		repaint();
	}
	
	public void setFixedMaxProb(double fixedMaxProb) {
		forceFixedMaxProb = true;
		this.fixedMaxProb = fixedMaxProb;
		repaint();
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (distnKey.equals(key))
			repaint();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return dragType != NO_DRAG;
	}
	
	private int startDragValue = -1;
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < -kDragSlop || y < -kDragSlop || x >= getSize().width + kDragSlop || y >= getSize().height + kDragSlop)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		int hitVal;
		try {
			hitVal = (int)Math.round(countAxis.positionToNumVal(hitPos.x));
		} catch (AxisException e) {
			hitVal = (e.axisProblem == AxisException.TOO_LOW_ERROR) ? 0 : (int)Math.round(countAxis.maxOnAxis);
		}
		return new DiscreteDragInfo(hitVal);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo == null) {
			getData().setSelection(distnKey, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
			if (otherDistnKey != null)
				getData().setSelection(otherDistnKey, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		}
		else {
			startDragValue = ((DiscreteDragInfo)startInfo).xValue;
			double lowVal =  (dragType == DRAG_PROB) ? startDragValue - 0.5 : Double.NEGATIVE_INFINITY;
			getData().setSelection(distnKey, lowVal, startDragValue + 0.5);
			if (otherDistnKey != null)
				getData().setSelection(otherDistnKey, lowVal, startDragValue + 0.5);
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null)
			startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
//		getData().setSelection(distnKey, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
//		if (otherDistnKey != null)
//			getData().setSelection(otherDistnKey, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}
}