package exper;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class TreatEffectSliderView extends DataView {
//	static final public String TREAT_EFFECT_SLIDER = "treatEffectSlider";
	
	static final private Color kGrayColor = new Color(0x666666);
	static final private Color kBarColor[] = {new Color(0x9900FF), new Color(0x00CC00),
														new Color(0xFF6600), new Color(0x6699FF), new Color(0x993300)};
	
	static final private int kHalfWidth = 4;
	static final private int kArrowSize = 4;
	static final private int kHiliteArrowSize = 6;
	
	static final public Color getShadedBarColor(int i, double propn) {
		Color baseColor = (i >= 0) ? kBarColor[i % kBarColor.length] : kGrayColor;
		int red = baseColor.getRed();
		int green = baseColor.getGreen();
		int blue = baseColor.getBlue();
		red = (int)Math.rint(propn * red + (1.0 - propn) * 0xFF);
		green = (int)Math.rint(propn * green + (1.0 - propn) * 0xFF);
		blue = (int)Math.rint(propn * blue + (1.0 - propn) * 0xFF);
		return new Color(red, green, blue);
	}
	
	static final public Color getBaseBarColor(int i) {
		return (i >= 0) ? kBarColor[i % kBarColor.length] : kGrayColor;
	}
	
	private VertAxis effectAxis;
	private HorizAxis factorAxis;
	private String responseKey;
	private int factorIndex;
	
	private boolean doingDrag = false;
	private int highlightIndex = -1;
	
	private boolean useGrayColors;
	private boolean keepZeroMeanEffect = false;
	
	private double catToNum[];
	private double xMean;
	
	public TreatEffectSliderView(DataSet theData, XApplet applet, VertAxis effectAxis, HorizAxis factorAxis,
							String treatKey, String responseKey, int factorIndex, boolean useGrayColors, double catToNum[],
							double xMean) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.responseKey = responseKey;
		this.factorIndex = factorIndex;
		this.effectAxis = effectAxis;
		this.factorAxis = factorAxis;
		this.useGrayColors = useGrayColors;
		this.catToNum = catToNum;
		this.xMean = xMean;
	}
	
	public void setKeepZeroMeanEffect(boolean keepZeroMeanEffect) {
		this.keepZeroMeanEffect = keepZeroMeanEffect;
		if (keepZeroMeanEffect) {
			FactorsModel response = (FactorsModel)getVariable(responseKey);
			double effects[] = response.getMainEffects(factorIndex);
			if (effects.length > 1) {
				double sum = 0.0;
				for (int i=0 ; i<effects.length ; i++)
					sum += effects[i];
				double change = sum / effects.length;
				response.setConstant(response.getConstant() + change);
				for (int i=0 ; i<effects.length ; i++)
					 effects[i] -= change;
				response.setMainEffect(factorIndex, effects);
			}
		}
	}
	
	private void drawTriangle(Graphics g, int x, int y, int change, int arrowSize) {
		for (int i=0 ; i<arrowSize ; i++)
			g.drawLine(x + i * change, y - i, x + i * change, y + i);
	}
	
	public void paintView(Graphics g) {
		FactorsModel response = (FactorsModel)getVariable(responseKey);
		double effect[] = response.getMainEffects(factorIndex);
		boolean linearEffect = (catToNum != null) && effect.length == 1;
		Point p = null;
		
		g.setColor(Color.lightGray);
		int zeroPos = effectAxis.numValToRawPosition(0.0);
		p = translateToScreen(0, zeroPos, p);
		zeroPos = p.y;
		if (zeroPos < getSize().height - 1)
			g.drawLine(0, p.y, getSize().width, p.y);
		
		int nEffects = linearEffect ? catToNum.length : effect.length;
		for (int i=0 ; i<nEffects ; i++) {
			double effectI = linearEffect ? (catToNum[i] - xMean) * effect[0] : effect[i];
			int vert = effectAxis.numValToRawPosition(effectI);
			int horiz = (catToNum == null) ? factorAxis.catValToPosition(i)
																		: factorAxis.numValToRawPosition(catToNum[i]);
			p = translateToScreen(horiz, vert, p);
			g.setColor(useGrayColors ? kGrayColor : kBarColor[i]);
			if (p.y <= zeroPos)
				g.fillRect(p.x - kHalfWidth, p.y, 2 * kHalfWidth + 1, zeroPos - p.y + 1);
			else
				g.fillRect(p.x - kHalfWidth, zeroPos, 2 * kHalfWidth + 1, p.y - zeroPos + 1);
			
			g.setColor(Color.red);
			g.drawLine(p.x - kHalfWidth, p.y, p.x + kHalfWidth, p.y);
			if (doingDrag && highlightIndex == i) {
				g.drawLine(p.x - kHalfWidth, p.y - 1, p.x + kHalfWidth, p.y - 1);
				g.drawLine(p.x - kHalfWidth, p.y + 1, p.x + kHalfWidth, p.y + 1);
				drawTriangle(g, p.x + kHalfWidth, p.y, 1, kHiliteArrowSize);
				drawTriangle(g, p.x - kHalfWidth, p.y, -1, kHiliteArrowSize);
			}
			else if (!linearEffect || i == 0) {
				drawTriangle(g, p.x + kHalfWidth, p.y, 1, kArrowSize);
				drawTriangle(g, p.x - kHalfWidth, p.y, -1, kArrowSize);
			}
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}

	static final private int kMinHitDistance = 10;
	
	private int hitOffset;
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.y + hitOffset < 0)
			return new VertDragPosInfo(-hitOffset);
		else if (hitPos.y + hitOffset >= effectAxis.getAxisLength())
			return new VertDragPosInfo(-hitOffset + effectAxis.getAxisLength() - 1);
		else
			return new VertDragPosInfo(hitPos.y);
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		
		FactorsModel response = (FactorsModel)getVariable(responseKey);
		double effect[] = response.getMainEffects(factorIndex);
		boolean linearEffect = effect.length == 1;
		
		if (linearEffect) {
			double effect0 = effect[0] * (catToNum[0] - xMean);
			int vert = effectAxis.numValToRawPosition(effect0);
			int horiz = factorAxis.numValToRawPosition(catToNum[0]);
			if (Math.abs(horiz - hitPos.x) <= kMinHitDistance
																				&& Math.abs(vert - hitPos.y) <= kMinHitDistance)
				return new VertDragPosInfo(hitPos.y, 0, vert - hitPos.y);
		}
		else
			for (int i=0 ; i<effect.length ; i++) {
				int vert = effectAxis.numValToRawPosition(effect[i]);
				int horiz = (catToNum == null) ? factorAxis.catValToPosition(i)
																			: factorAxis.numValToRawPosition(catToNum[i]);
				if (Math.abs(horiz - hitPos.x) <= kMinHitDistance
																					&& Math.abs(vert - hitPos.y) <= kMinHitDistance)
					return new VertDragPosInfo(hitPos.y, i, vert - hitPos.y);
			}
		
		return null;
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		VertDragPosInfo dragPos = (VertDragPosInfo)startPos;
		
		FactorsModel response = (FactorsModel)getVariable(responseKey);
		double effect[] = response.getMainEffects(factorIndex);
		boolean linearEffect = effect.length == 1;
		
		if (!linearEffect || dragPos.index == 0) {
			hitOffset = dragPos.hitOffset;
			highlightIndex = dragPos.index;
			doingDrag = true;
			repaint();
			return true;
		}
		else
			return false;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos ==  null)
			return;
		
		FactorsModel response = (FactorsModel)getVariable(responseKey);
		double effects[] = response.getMainEffects(factorIndex);
		boolean linearEffect = effects.length == 1;
		
		VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
		int newVertPos = dragPos.y + hitOffset;
		try {
			double newEffect = effectAxis.positionToNumVal(newVertPos);
			
			if (linearEffect) {
				double sumX = 0.0;
				for (int i=0 ; i<catToNum.length ; i++)
					sumX += catToNum[i];
				
				double newSlope = -newEffect / (sumX / catToNum.length - catToNum[0]);
				
				effects[0] = newSlope;
			}
			else {
				effects[highlightIndex] = newEffect;
				if (keepZeroMeanEffect) {
					double sum = 0.0;
					for (int i=0 ; i<effects.length ; i++)
						sum += effects[i];
					double change = sum / (effects.length - 1);
					for (int i=0 ; i<effects.length ; i++)
						if (i != highlightIndex)
							effects[i] -= change;
				}
			}
			response.setMainEffect(factorIndex, effects);
			getData().variableChanged(responseKey);
		} catch (AxisException e) {
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doingDrag = false;
		repaint();
	}
}
	
