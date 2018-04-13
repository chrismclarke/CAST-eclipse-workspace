package test;

import java.awt.*;

import dataView.*;
import qnUtils.*;
import imageGroups.*;


public class PValueScalesView extends DataView {
	
	static final private Color kBeamColor = new Color(0xff6666);
	
	static final private int kMinCrossScaleGap = 100;
	static final private int kHalfBeamWidth = 80;
	static final private int kMinTopBorder = ScalesImages.kPostHeight
																			- 2 * ScalesImages.kPostCentreOffset;
	static final private int kMinBottomBorder = 0;
	static final private int kCrossFromRight = 5;
	static final private int kLeftBorder = 10;
	static final private int kBeamOverhang = 8;
	
	private PValueAxis axis;
	private HypothesisTest test;
	private int hypothDrawType;
	
	private boolean initialised = false;
	
	public PValueScalesView(DataSet theData, XApplet applet,
								PValueAxis axis, HypothesisTest test, int hypothDrawType) {
		super(theData, applet, null);
		
		this.test = test;
		this.axis = axis;
		this.hypothDrawType = hypothDrawType;
	}
	
	private boolean initialise(Graphics g) {
		if (!initialised) {
			initialised = true;
			return true;
		}
		return false;
	}
	
	public int getPValuePos() {
		double pValue = test.evaluatePValue();
		
		int onePos = getTopBorder();
		int zeroPos = getSize().height - getBottomBorder();
		return zeroPos - (int)Math.round(pValue * (zeroPos - onePos));
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		int pValueY = getPValuePos();
		int pValueX = getSize().width - kCrossFromRight;
		
		int postLeft = kLeftBorder + kHalfBeamWidth + (ScalesImages.kTrayWidth - ScalesImages.kPostWidth) / 2;
		int postTop = kMinTopBorder + (getSize().height - getMinimumSize().height) / 2;
		
		int pivotY = postTop + ScalesImages.kPostCentreOffset;
		int pivotX = postLeft + ScalesImages.kPostWidth / 2;
		
		g.setColor(Color.red);
		g.drawLine(pivotX, pivotY, pValueX, pValueY);
		
		drawCross(g, new Point(pValueX, pValueY));
		g.setColor(getForeground());
		
		Point pivotShift = getShift(pivotX, pivotY, pValueX, pValueY, kHalfBeamWidth);
		
		drawBeam(g, pivotX, pivotY, pValueX, pValueY);
		
		g.drawImage(ScalesImages.post, postLeft, postTop, this);
		
		g.drawImage(ScalesImages.tray, pivotX + pivotShift.x - ScalesImages.kTrayWidth / 2,
												pivotY + pivotShift.y - ScalesImages.kTrayCentreOffset, this);
		drawHypothesis(g, pivotX + pivotShift.x, pivotY + pivotShift.y - ScalesImages.kTrayCentreOffset
																	+ ScalesImages.kTrayContentsOffset, false);
		g.drawImage(ScalesImages.tray, pivotX - pivotShift.x - ScalesImages.kTrayWidth / 2,
												pivotY - pivotShift.y - ScalesImages.kTrayCentreOffset, this);
		drawHypothesis(g, pivotX - pivotShift.x, pivotY - pivotShift.y - ScalesImages.kTrayCentreOffset
																	+ ScalesImages.kTrayContentsOffset, true);
	}
	
	private Point getShift(int pivotX, int pivotY, int pValueX, int pValueY,
																								int halfBreamWidth) {
		double p = halfBreamWidth / Math.sqrt((pValueX - pivotX) * (pValueX - pivotX)
																		+ (pValueY - pivotY) * (pValueY - pivotY));
		int xShift = (int)Math.round(p * (pValueX - pivotX));
		int yShift = (int)Math.round(p * (pValueY - pivotY));
		return new Point(xShift, yShift);
	}
	
	private void drawHypothesis(Graphics g, int horizCenter, int baseline, boolean isNull) {
		Dimension size = test.getSize(g, hypothDrawType);
		test.paintBlue(g, horizCenter - size.width / 2, baseline, isNull, hypothDrawType, this);
	}
	
	private void drawBeam(Graphics g, int pivotX, int pivotY, int pValueX, int pValueY) {
		Point beamShift = getShift(pivotX, pivotY, pValueX, pValueY, kHalfBeamWidth + kBeamOverhang);
		
		g.setColor(Color.white);
		g.drawLine(pivotX - beamShift.x, pivotY - beamShift.y,
															pivotX + beamShift.x, pivotY + beamShift.y);
		g.setColor(kBeamColor);
		g.drawLine(pivotX - beamShift.x, pivotY - beamShift.y - 1,
															pivotX + beamShift.x, pivotY + beamShift.y - 1);
		g.drawLine(pivotX - beamShift.x, pivotY - beamShift.y + 1,
															pivotX + beamShift.x, pivotY + beamShift.y + 1);
		
		g.setColor(Color.black);
		g.drawLine(pivotX - beamShift.x, pivotY - beamShift.y - 2,
															pivotX + beamShift.x, pivotY + beamShift.y - 2);
		g.drawLine(pivotX - beamShift.x, pivotY - beamShift.y + 2,
															pivotX + beamShift.x, pivotY + beamShift.y + 2);
	}
	
	public int getTopBorder() {
		if (!initialised)						//	avoid getGraphics() if already initialised
			initialise(getGraphics());
		int minTopBorder = axis.getMinTopBorder();
		return minTopBorder;
	}
	
	public int getBottomBorder() {
		if (!initialised)						//	avoid getGraphics() if already initialised
			initialise(getGraphics());
		int minBottomBorder = axis.getMinBottomBorder();
		return minBottomBorder;
	}
	
	public Dimension getMinimumSize() {
		int height = ScalesImages.kPostHeight + kMinTopBorder + kMinBottomBorder;
		int width = kLeftBorder + ScalesImages.kTrayWidth + 2 * kHalfBeamWidth + kMinCrossScaleGap;
		
		return new Dimension(width, height);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
