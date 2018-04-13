package pairBlock;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;



public class PairedCovarView extends ScatterView {
	static final private int kTopBottomBorder = 3;
	static final private int kLeftRightBorder = 7;
	static final private int kHeadingTableGap = 5;
	static final private int kExtraLineSpacing = 4;
	static final private int kCrossLabelGap = 5;
	
	static final private Color kKeyBackground = new Color(0xEEDDFF);
	static final private Color kBandEvenColor = new Color(0xF7FFD1);
	static final private Color kBandOddColor = new Color(0xFFDDF1);
	
	private String factorKey;
	
	private boolean showPairBands = false;
	
	public PairedCovarView(DataSet theData, XApplet applet, String yKey, String covarKey,
																String factorKey, VertAxis yAxis, HorizAxis covarAxis) {
		super(theData, applet, covarAxis, yAxis, covarKey, yKey);
		this.factorKey = factorKey;
	}
	
	public void setShowPairBands(boolean showPairBands) {
		this.showPairBands = showPairBands;
	}
	
	protected int groupIndex(int itemIndex) {
		CatVariable factorVar = (CatVariable)getVariable(factorKey);
		return factorVar.getItemCategory(itemIndex);
	}
	
	private void drawFactorKey(Graphics g) {
		CatVariable factorVar = (CatVariable)getVariable(factorKey);
		int noOfCats = factorVar.noOfCategories();
		
		Font standardFont = g.getFont();
		Font boldFont = new Font(standardFont.getName(), Font.BOLD, standardFont.getSize());
		g.setFont(boldFont);
		
		FontMetrics fm = g.getFontMetrics();
		int boldAscent = fm.getAscent();
		int boldDescent = fm.getDescent();
		
		int catTitleWidth = fm.stringWidth(factorVar.name);
		
		g.setFont(standardFont);
		fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		
		int catNameWidth = 0;
		for (int i=0 ; i<noOfCats ; i++) {
			int nameWidth = factorVar.getLabel(i).stringWidth(g);
			if (nameWidth > catNameWidth)
				catNameWidth = nameWidth;
		}
		
		int tableTopBorder = boldAscent + boldDescent + kHeadingTableGap;
		int tableHeight = noOfCats * (ascent + descent) + (noOfCats - 1) * kExtraLineSpacing;
		
		int crossWidth = getCrossPix();
		
		g.setColor(kKeyBackground);
		g.fillRect(0, 0, 2 * kLeftRightBorder + Math.max(catTitleWidth, crossWidth + kCrossLabelGap + catNameWidth),
													2 * kTopBottomBorder + tableTopBorder + tableHeight);
		
		g.setColor(Color.black);
		g.setFont(boldFont);
		g.drawString(factorVar.name, kLeftRightBorder, kTopBottomBorder + ascent);
		
		g.setFont(standardFont);
		int baseline = kTopBottomBorder + tableTopBorder + ascent;
		Point p = new Point(kLeftRightBorder + crossWidth / 2, 0);
		for (int i=0 ; i<noOfCats ; i++) {
			g.setColor(getCrossColor(i));
			
			p.y = baseline - ascent / 3;
			drawMark(g, p, i);
			
			factorVar.getLabel(i).drawRight(g, kLeftRightBorder + crossWidth + kCrossLabelGap, baseline);
			baseline += (ascent + descent + kExtraLineSpacing);
		}
	}
	
	private void drawBands(Graphics g) {
		CatVariable factorVar = (CatVariable)getVariable(factorKey);
		int nCats = factorVar.noOfCategories();
		
		NumVariable covar = (NumVariable)getVariable(xKey);
		NumValue sortedX[] = covar.getSortedData();
		
		g.setColor(kBandEvenColor);
		g.fillRect(0, 0, getSize().width, getSize().height);
		
		Point p = new Point(0, 0);
		g.setColor(kBandOddColor);
		
		for (int i=nCats-1 ; i+nCats<sortedX.length ; i+=2*nCats) {
			double lowShadeX = (sortedX[i].toDouble() + sortedX[i + 1].toDouble()) / 2;
			double nextBandStart = (i + nCats + 1 < sortedX.length) ? sortedX[i + nCats + 1].toDouble()
																								: 2 * axis.maxOnAxis - axis.minOnAxis;
			double highShadeX = (sortedX[i + nCats].toDouble() + nextBandStart) / 2;
			
			int lowXPos = axis.numValToRawPosition(lowShadeX);
			int highXPos = axis.numValToRawPosition(highShadeX);
			
			int px0 = translateToScreen(lowXPos, 0, p).x;
			int px1 = translateToScreen(highXPos, 0, p).x;
			
			g.fillRect(px0, 0, (px1 - px0), getSize().height);
		}
	}
	
	public void paintView(Graphics g) {
		if (showPairBands)
			drawBands(g);
		
		drawFactorKey(g);
		
		super.paintView(g);
	}

}