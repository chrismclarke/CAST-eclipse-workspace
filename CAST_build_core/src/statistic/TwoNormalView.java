package statistic;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import formula.*;


public class TwoNormalView extends DataView {
//	static final private Color kDistnColor = new Color(0xEEEEEE);
	static final private Color kMeanColor = Color.blue;
	static final private Color kSdColor = Color.red;
	static final private Color kSdLineColor = new Color(0xFF9999);
	static final private Color kGroupNameColor = new Color(0x666666);
	
	static final private Color k2SdShadeColor = new Color(0xFFEEEE);
	static final private Color k1SdShadeColor = new Color(0xFFDDDD);
	
	static final private int kArrowHead = 4;
	static final private int kMeanSdTopBottom = 5;
	static final private int kMeanSdGap = 2;
	
	private String y1Key, y2Key;
	private HorizAxis axis;
	
	private AccurateDistnArtist y1DistnArtist, y2DistnArtist;
	
	public TwoNormalView(DataSet theData, XApplet applet, HorizAxis axis,
																													String y1Key, String y2Key) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.y1Key = y1Key;
		this.y2Key = y2Key;
		this.axis = axis;
		
		y1DistnArtist = new AccurateDistnArtist("y1", getData());
		y2DistnArtist = new AccurateDistnArtist("y2", getData());
	}
	
	private void drawMeanSd(Graphics g, Insets border, String yKey) {
		DistnVariable yVar = (DistnVariable)getVariable(yKey);
		NumValue mean = yVar.getMean();
		NumValue sd = yVar.getSD();
		
		int meanPos = axis.numValToRawPosition(mean.toDouble());
		int meanPlusSdPos = axis.numValToRawPosition(mean.toDouble() + sd.toDouble());
		
		int top = border.top;
		int bottom = getSize().height - border.bottom;
		int ascent = g.getFontMetrics().getAscent();
		int sdBaseline = top - kMeanSdTopBottom;
		int meanBaseline = sdBaseline - Math.max(2 * kArrowHead, ascent) - kMeanSdGap;
		int sdArrowTop = meanBaseline - ascent;
		
		g.setColor(kMeanColor);
		g.drawLine(meanPos, meanBaseline + 2, meanPos, bottom - 1);
		
		LabelValue meanLabel = new LabelValue(MText.expandText("x#bar# = ") + mean);
		meanLabel.drawCentred(g, meanPos, meanBaseline);
		
		g.setColor(kSdLineColor);
		g.drawLine(meanPlusSdPos, sdArrowTop, meanPlusSdPos, bottom - 1);
		
		g.setColor(kSdColor);
		int sdArrowCenter = sdBaseline - ascent / 2;
		g.drawLine(meanPos + 1, sdArrowCenter, meanPlusSdPos, sdArrowCenter);
		g.drawLine(meanPlusSdPos, sdArrowCenter, meanPlusSdPos - kArrowHead, sdArrowCenter - kArrowHead);
		g.drawLine(meanPlusSdPos, sdArrowCenter, meanPlusSdPos - kArrowHead, sdArrowCenter + kArrowHead);
		
		g.drawString("s = " + sd, meanPlusSdPos + 3, sdBaseline);
		
		Font oldFont = g.getFont();
		Font nameFont = new Font(oldFont.getName(), Font.BOLD, oldFont.getSize() * 2);
		g.setFont(nameFont);
		g.setColor(kGroupNameColor);
		ascent = g.getFontMetrics().getAscent();
		int nameBaseline = top + ascent;
		g.drawString(yVar.name, 4, nameBaseline);
		
		g.setFont(oldFont);
	}
	
	private void shadeBackground(Graphics g, int bottomBorder, int topBorder, String yKey) {
		DistnVariable yVar = (DistnVariable)getVariable(yKey);
		NumValue mean = yVar.getMean();
		NumValue sd = yVar.getSD();
		
		int meanPlusPos = axis.numValToRawPosition(mean.toDouble() + 2 * sd.toDouble());
		int meanMinusPos = axis.numValToRawPosition(mean.toDouble() - 2 * sd.toDouble());
		
		g.setColor(k2SdShadeColor);
		g.fillRect(meanMinusPos, topBorder, meanPlusPos - meanMinusPos, getSize().height - topBorder - bottomBorder);
		
		meanPlusPos = axis.numValToRawPosition(mean.toDouble() + sd.toDouble());
		meanMinusPos = axis.numValToRawPosition(mean.toDouble() - sd.toDouble());
		
		g.setColor(k1SdShadeColor);
		g.fillRect(meanMinusPos, topBorder, meanPlusPos - meanMinusPos, getSize().height - topBorder - bottomBorder);
	}
	
	public void paintView(Graphics g) {
		int ascent = g.getFontMetrics().getAscent();
		int meanSdHeight = ascent + 2 * kMeanSdTopBottom + Math.max(2 * kArrowHead, ascent) + kMeanSdGap;
		
		int center = getSize().height / 2;
		
		Insets border1 = new Insets(meanSdHeight, 0, getSize().height - center, 0);
		Insets border2 = new Insets(meanSdHeight + center, 0, 0, 0);
		Insets oldBorder = getViewBorder();
		
		shadeBackground(g, border1.bottom, 0, "y1");
		setViewBorder(border1);
		y1DistnArtist.paintDistn(g, this, axis);
		drawMeanSd(g, border1, "y1");
		
		g.setColor(getForeground());
		g.drawLine(0, center, getSize().width, center);
		
		shadeBackground(g, 0, getSize().height - center, "y2");
		setViewBorder(border2);
		y2DistnArtist.paintDistn(g, this, axis);
		drawMeanSd(g, border2, "y2");
		
		setViewBorder(oldBorder);
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (y1Key.equals(key) || y2Key.equals(key)) {
			y1DistnArtist.resetDistn();
			y2DistnArtist.resetDistn();
			repaint();
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}