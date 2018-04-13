package propnVenn;

import java.awt.*;

import dataView.*;
import contin.*;


public class JointProbChoice extends DataView {
//	static public final String PROBCALC = "probCalc";
	
	static final private int kLeading = 3;
	static final private int kBoxBorder = 6;
//	static final private int kTimesXOffset = 25;
//	static final private int kTimesYOffset = 3;
	static final private int kTimesSize = 6;		//		7 x 7 pixels
	static final private int kTimesGap = 4;
	static final private int kProbDecimals = 3;
	static final private int kMinEqualsWidth = 16;
	static final private int kMaxEqualsWidth = 40;
	static final private int kEqualsSize = 6;
	static final private int kTimesImageOffset = 25;
	
	static final private int STANDARD = 0;
	static final private int SELECTED = 1;
	static final private int DIMMED = 2;
	
	static final public int TO_Y_MARGIN = 2000;
	static final public int TO_X_MARGIN = 2001;
	
	static final private NumValue kZeroValue = new NumValue(0.0, kProbDecimals);
	static final private String kPAndString = "P( and )";
	static final private String kPMarginString = "P()";
	static final private String kPConditString = "P( | )";
	
	private boolean currentYMargin;
	private AreaContin2View theView;
	private String yKey, xKey;
	
	private Rectangle itemBox[] = new Rectangle[3];
	private boolean doingTransition = false;
	private boolean initialised = false;
	
	private int ascent, descent;
	private int probWidth;
	private int maxJointWidth, maxYMarginWidth, maxXMarginWidth, maxConditWidth;
	private int displayHt, leftWidth, rightWidth1, rightWidth2, minWidth, timesOffset1, timesOffset2;
	
	
	static final public int LEFT = 0;
	static final public int RIGHT = 1;
	static final public int CENTER = 2;
	
	static final public int kDivGap = 2;
	
	static public void drawFraction(Graphics g, int baseX, int topBaseline, int numer,
																int denom, int direction) {
		NumValue numerVal = new NumValue(numer, 0);
		NumValue denomVal = new NumValue(denom, 0);
		int width = Math.max(numerVal.stringWidth(g), denomVal.stringWidth(g));
		if (direction == LEFT)
			baseX -= width / 2;
		else if (direction == RIGHT)
			baseX += width / 2;
		
		numerVal.drawCentred(g, baseX, topBaseline);
		denomVal.drawCentred(g, baseX, topBaseline + g.getFontMetrics().getAscent() + 2 * kDivGap);
		g.drawLine(baseX - (width+1) / 2, topBaseline + kDivGap,
															baseX + (width+1) / 2, topBaseline + kDivGap);
	}
	
	
	public JointProbChoice(DataSet data, boolean startYMargin,
					XApplet applet, AreaContin2View theView, String yKey, String xKey) {
		super(data, applet, new Insets(0, 0, 0, 0));
		
		setStickyDrag(true);
		
		this.currentYMargin = startYMargin;
		this.theView = theView;
		this.yKey = yKey;
		this.xKey = xKey;
		
		ContinImages.loadJointProbs(applet);
	}
	
	public void endTransition() {
		if (doingTransition) {
			doingTransition = false;
			repaint();
		}
	}
	
	public void startTransition() {
		if (!doingTransition) {
			doingTransition = true;
			repaint();
		}
	}
	
	private boolean initialise(Graphics g) {
		if (initialised)
			return false;
		else {
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			
			CatVariableInterface yVar = (CatVariableInterface)getVariable(yKey);
			int nYCats = yVar.noOfCategories();
			CatVariableInterface xVar = (CatVariableInterface)getVariable(xKey);
			int nXCats = xVar.noOfCategories();
			
			if (yVar instanceof CatVariable) {
				NumValue maxCount = new NumValue(((CatVariable)yVar).noOfValues(), 0);
				probWidth = maxCount.stringWidth(g);
			}
			else
				probWidth = kZeroValue.stringWidth(g);
			
			int maxYCatLength = 0;
			for (int i=0 ; i<nYCats ; i++)
				maxYCatLength = Math.max(maxYCatLength, yVar.getLabel(i).stringWidth(g));
			int maxXCatLength = 0;
			for (int i=0 ; i<nXCats ; i++)
				maxXCatLength = Math.max(maxXCatLength, xVar.getLabel(i).stringWidth(g));
			
			int marginStringWidth = fm.stringWidth(kPMarginString);
			maxYMarginWidth = marginStringWidth + maxYCatLength;
			maxXMarginWidth = marginStringWidth + maxXCatLength;
			maxConditWidth = fm.stringWidth(kPConditString) + maxYCatLength + maxXCatLength;
			maxJointWidth = fm.stringWidth(kPAndString) + maxYCatLength + maxXCatLength;
			
			leftWidth = Math.max(ContinImages.kJointWidth, maxJointWidth) + kBoxBorder;	//	no border on left
			timesOffset1 = Math.max(probWidth, maxYMarginWidth) + kTimesGap;
			rightWidth1 = timesOffset1 + kTimesGap + kTimesSize + 1
												+ Math.max(probWidth, maxConditWidth) + 2 * kBoxBorder;
			timesOffset2 = Math.max(probWidth, maxXMarginWidth) + kTimesGap;
			rightWidth2 = timesOffset2 + kTimesGap + kTimesSize + 1
												+ Math.max(probWidth, maxConditWidth) + 2 * kBoxBorder;
			minWidth = leftWidth + rightWidth1 + rightWidth2 + 2 * kMinEqualsWidth;
			
			displayHt = 2 * kBoxBorder + ContinImages.kJointHeight + 2 * (kLeading + ascent)
																											+ descent;
			if (yVar instanceof CatVariable)
				displayHt += ascent + 2 * kDivGap;
			
			for (int i=0 ; i<itemBox.length ; i++)
				itemBox[i] = null;
			
			initialised = true;
			return true;
		}
	}
	
	public Dimension getPreferredSize() {
		initialise(getGraphics());
		return new Dimension(minWidth, displayHt);
	}
	
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}
	
	private void drawEquals(Graphics g, int boxIndex) {
		g.setColor(Color.blue);
		int equalsXCenter = (itemBox[boxIndex].x + itemBox[boxIndex].width
																			+ itemBox[boxIndex + 1].x) / 2;
		int equalsYCenter = itemBox[boxIndex].y + kBoxBorder + ContinImages.kJointHeight / 2;
		
		int left = equalsXCenter - kEqualsSize / 2;
		g.drawLine(left, equalsYCenter + 1, left + kEqualsSize, equalsYCenter + 1);
		g.drawLine(left, equalsYCenter - 2, left + kEqualsSize, equalsYCenter - 2);
	}
	
	private void drawTimes(Graphics g, int left, int baseline) {
		g.drawLine(left, baseline - kTimesSize - 1, left + kTimesSize, baseline - 1);
		g.drawLine(left, baseline - 1, left + kTimesSize, baseline - kTimesSize - 1);
	}
	
	private void drawFormula(Graphics g, Image jointProgImage, CatVariableInterface marginVar,
							CatVariableInterface conditVar, int selectedMargin, int selectedCondit,
							Rectangle box, int status, int timesOffset, double marginProb,
							double conditProb, int nTotal) {
		int shadedHeight = (selectedMargin >= 0 && selectedCondit >= 0) ? box.height
											: box.y + ContinImages.kJointHeight + 2 * kBoxBorder;
		
		if (status != STANDARD) {
			g.setColor((status == SELECTED) ? Color.white : Color.lightGray);
			g.fillRect(box.x, box.y, box.width, shadedHeight);
		}
		g.setColor(Color.gray);
		g.drawRect(box.x, box.y, box.width - 1, shadedHeight - 1);
		
		int imageLeft = box.x + kBoxBorder + timesOffset - kTimesImageOffset;
		int top = box.y + kBoxBorder;
		g.drawImage(jointProgImage, imageLeft, top, ContinImages.kJointWidth,
																			ContinImages.kJointHeight, this);
		g.setColor(Color.blue);
		int timesLeft = box.x + kBoxBorder + timesOffset;
		int baseline = top + 9;
		drawTimes(g, timesLeft, baseline);
		
		if (selectedMargin >= 0 && selectedCondit >= 0) {
			baseline = top + ContinImages.kJointHeight + kLeading + ascent;
			g.setColor(getForeground());
			LabelValue leftP = new LabelValue("P(" + marginVar.getLabel(selectedMargin).toString() + ")");
			leftP.drawLeft(g, timesLeft - kTimesGap, baseline);
			LabelValue rightP = new LabelValue("P(" + conditVar.getLabel(selectedCondit).toString()
										+ " | " + marginVar.getLabel(selectedMargin).toString() + ")");
			rightP.drawRight(g, timesLeft + kTimesGap + kTimesSize + 1, baseline);
			
			drawTimes(g, timesLeft, baseline);
			
			baseline += ascent + descent + kLeading;
			g.setColor(Color.red);
			if (nTotal > 0) {
				int nMargin = (int)Math.round(nTotal * marginProb);
				int nJoint = (int)Math.round(nMargin * conditProb);
				drawFraction(g, timesLeft - kTimesGap, baseline, nMargin, nTotal, LEFT);
				drawFraction(g, timesLeft + kTimesGap + kTimesSize, baseline, nJoint, nMargin, RIGHT);
				baseline += ascent / 2;
			}
			else {
				NumValue leftVal = new NumValue(marginProb, kProbDecimals);
				leftVal.drawLeft(g, timesLeft - kTimesGap, baseline);
				NumValue rightVal = new NumValue(conditProb, kProbDecimals);
				rightVal.drawRight(g, timesLeft + kTimesGap + kTimesSize, baseline);
			}
			
			drawTimes(g, timesLeft, baseline);
		}
	}
	
	private void drawJointFormula(Graphics g, CatVariableInterface xVar,
							CatVariableInterface yVar, int selectedX, int selectedY,
							Rectangle box, double jointProb, int nTotal) {
		int horizCenter = box.x + box.width / 2;
		int top = box.y + kBoxBorder;
		g.drawImage(ContinImages.jointProb, horizCenter - ContinImages.kJointWidth / 2,
									top, ContinImages.kJointWidth, ContinImages.kJointHeight, this);
		if (selectedX >= 0 && selectedY >= 0) {
			top += ContinImages.kJointHeight + kLeading;
			LabelValue jointLabel = new LabelValue("P(" + xVar.getLabel(selectedX).toString()
											+ " and " + yVar.getLabel(selectedY).toString() + ")");
			g.setColor(Color.black);
			jointLabel.drawCentred(g, horizCenter, top + ascent);
			top += ascent + descent + kLeading;
			
			NumValue jointProbVal = new NumValue(jointProb, kProbDecimals);
			g.setColor(Color.red);
			if (nTotal > 0) {
				int nJoint = (int)Math.round(nTotal * jointProb);
				drawFraction(g, horizCenter, top + ascent, nJoint, nTotal, CENTER);
			}
			else 
				jointProbVal.drawCentred(g, horizCenter, top + ascent);
		}
	}
	
	public void paintView(Graphics g) {
		initialise(getGraphics());
		
		if (itemBox[0] == null) {
			int extraEqualsWidth = Math.min((getSize().width - minWidth) / 2,
																			kMaxEqualsWidth - kMinEqualsWidth);
			int boxTop = (getSize().height - displayHt) / 2;
			int boxLeft = (getSize().width - minWidth - 2 * extraEqualsWidth) / 2;
			
			itemBox[0] = new Rectangle(boxLeft, boxTop, leftWidth, displayHt);
			boxLeft += leftWidth + (kMinEqualsWidth + extraEqualsWidth);
			itemBox[1] = new Rectangle(boxLeft, boxTop, rightWidth1, displayHt);
			boxLeft += rightWidth1 + (kMinEqualsWidth + extraEqualsWidth);
			itemBox[2] = new Rectangle(boxLeft, boxTop, rightWidth2, displayHt);
		}
			
		CatVariableInterface yVar = (CatVariableInterface)getVariable(yKey);
		CatVariableInterface xVar = (CatVariableInterface)getVariable(xKey);
		
		int nTotal = (yVar instanceof CatVariable) ? ((CatVariable)yVar).noOfValues() : -1;
		
		int selectedX = theView.getSelectedX();
		int selectedY = theView.getSelectedY();
		
		drawJointFormula(g, xVar, yVar, selectedX, selectedY, itemBox[0],
				theView.getXMarginProb(selectedX) * theView.getYConditProb(selectedY,selectedX),
				nTotal);
		
		drawEquals(g, 0);
		drawFormula(g, ContinImages.jointProbY, yVar, xVar, selectedY, selectedX, itemBox[1],
									currentYMargin ? SELECTED : doingTransition ? DIMMED : STANDARD,
									timesOffset1, theView.getYMarginProb(selectedY),
									theView.getXConditProb(selectedX, selectedY), nTotal);
		
		drawEquals(g, 1);
		drawFormula(g, ContinImages.jointProbX, xVar, yVar, selectedX, selectedY, itemBox[2],
									!currentYMargin ? SELECTED : doingTransition ? DIMMED : STANDARD,
									timesOffset2, theView.getXMarginProb(selectedX),
									theView.getYConditProb(selectedY, selectedX), nTotal);
	}

//-----------------------------------------------------------------------------

	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return !doingTransition;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		for (int i=1 ; i<3 ; i++)
			if (((i==1) != currentYMargin) && itemBox[i] != null && itemBox[i].contains(x, y))
				return new IndexPosInfo(i);
		return null;
	}
	
	@SuppressWarnings("deprecation")
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo == null)
			return false;
		IndexPosInfo option = (IndexPosInfo)startInfo;
		currentYMargin = option.itemIndex == 1;
		int newMargin = currentYMargin ? TO_Y_MARGIN : TO_X_MARGIN;
		deliverEvent(new Event(this, newMargin, null));
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
	}
}
