package stdError;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class Stacked2SdBoundsView extends StackedDotPlotView {
	static final private Color kTwoSeColor = Color.blue;
	static final private Color kBiasColor = new Color(0x009900);
	static final private Color kTwoSeExtremeShade = new Color(0xEEEEEE);
	
//	static final private int kExtraTopBorder = 20;
	static final private int kArrowHead = 4;
	
	static final private LabelValue kTwoSeLabel = new LabelValue("2 s.e.");
	
	protected boolean showBounds = false;
	
	public Stacked2SdBoundsView(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		super(theData, applet, theAxis, null, false);
//		Insets border = getViewBorder();
//		border.top += kExtraTopBorder;
	}
	
	public void setShowBounds(boolean showBounds) {
		this.showBounds = showBounds;
	}
	
	public double findMean() {
		ValueEnumeration ye = getNumVariable().values();
		int n = 0;
		double sy = 0.0;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			sy += y;
			n ++;
		}
		
		return sy / n;
	}
	
	public double findSd() {
		ValueEnumeration ye = getNumVariable().values();
		int n = 0;
		double sy = 0.0;
		double syy = 0.0;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			sy += y;
			syy += y * y;
			n ++;
		}
		
		return Math.sqrt((syy - sy * sy / n) / (n - 1));
	}
	
	protected void paintBackground(Graphics g) {
		if (showBounds) {
			double bias = findMean();
			double se = findSd();
			
			int midPos = axis.numValToRawPosition(bias);
			int lowPos = axis.numValToRawPosition(bias - 2 * se);
			int highPos = axis.numValToRawPosition(bias + 2 * se);
			int midScreen = translateToScreen(midPos, 0, null).x;
			int lowScreen = translateToScreen(lowPos, 0, null).x;
			int highScreen = translateToScreen(highPos, 0, null).x;
			
			g.setColor(kBiasColor);
			int ascent = g.getFontMetrics().getAscent();
			int biasBaseline = ascent + 3;
			
			LabelValue kBiasLabel = new LabelValue(getApplet().translate("Bias"));
			kBiasLabel.drawCentred(g, midScreen, biasBaseline);
			g.drawLine(midScreen, biasBaseline + 2, midScreen, getSize().height);
			g.drawLine(midScreen, getSize().height - 1, midScreen - kArrowHead, getSize().height - kArrowHead);
			g.drawLine(midScreen, getSize().height - 1, midScreen + kArrowHead, getSize().height - kArrowHead);
			
			g.setColor(kTwoSeColor);
			int arrowVertPos = biasBaseline + 6;
			int seBaseline = arrowVertPos + ascent + 2;
			
			kTwoSeLabel.drawCentred(g, (midScreen + highScreen) / 2, seBaseline);
			g.drawLine(midScreen + 2, arrowVertPos, highScreen - 1, arrowVertPos);
			g.drawLine(highScreen - 1, arrowVertPos, highScreen - kArrowHead - 1, arrowVertPos - kArrowHead);
			g.drawLine(highScreen - 1, arrowVertPos, highScreen - kArrowHead - 1, arrowVertPos + kArrowHead);
			
			kTwoSeLabel.drawCentred(g, (midScreen + lowScreen) / 2, seBaseline);
			g.drawLine(midScreen - 2, arrowVertPos, lowScreen + 1, arrowVertPos);
			g.drawLine(lowScreen + 1, arrowVertPos, lowScreen + kArrowHead + 1, arrowVertPos - kArrowHead);
			g.drawLine(lowScreen + 1, arrowVertPos, lowScreen + kArrowHead + 1, arrowVertPos + kArrowHead);
			
			
			g.setColor(kTwoSeExtremeShade);
			g.fillRect(0, 0, lowScreen, getSize().height);
			g.fillRect(highScreen, 0, getSize().width - highScreen, getSize().height);
			
			g.setColor(getForeground());
		}
	}
}