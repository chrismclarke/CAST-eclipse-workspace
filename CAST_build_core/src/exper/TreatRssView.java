package exper;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class TreatRssView extends DataView {
//	static public final String TREAT_RSS_DOTPLOT = "treatRssPlot";
	
	static final private Color kBaseColor = new Color(0xFF9999);
	static final private Color kMeanColor = new Color(0xBBBBBB);
	static final private Color kFitColor = new Color(0x0000CC);
	static final private Color kRssOutlineColor = new Color(0xFF0000);
	static final private Color kRssFillColor = new Color(0xFF6699);
	
	private static final int kMeanExtraPix = 40;
	private static final int kHalfBarWidth = 15;
	
	private NumCatAxis responseAxis, treatAxis;
	private String yKey, treatKey;
	
	private boolean showResiduals = false;
	
	public TreatRssView(DataSet theData, XApplet applet, NumCatAxis responseAxis, NumCatAxis treatAxis, String yKey,
								String treatKey, String modelKey) {
		super(theData, applet, new Insets(5, 5, 5, 5));
																//		5 pixels round for crosses to overlap into
		this.responseAxis = responseAxis;
		this.treatAxis = treatAxis;
		this.treatKey = treatKey;
		this.yKey = yKey;
	}
	
	public void setShowResiduals(boolean showResiduals) {
		this.showResiduals = showResiduals;
	}
	
	private Point getScreenPoint(int treatIndex, double value, Point p) {
		int vertPos = responseAxis.numValToRawPosition(value);
		int horizPos = treatAxis.catValToPosition(treatIndex);
		return translateToScreen(horizPos, vertPos, p);
	}
	
	public void paintView(Graphics g) {
		drawBars(g);
		drawMeans(g);
		
		if (showResiduals) {
			drawSquares(g, false);
			drawSquares(g, true);
		}
		drawFits(g);
		drawCrosses(g);
	}
	
	private void drawCrosses(Graphics g) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		CatVariable treatVar = (CatVariable)getVariable(treatKey);
		Point p = null;
		
		g.setColor(getForeground());
		ValueEnumeration ye = yVar.values();
		int index = 0;
		while (ye.hasMoreValues()) {
			double nextVal = ye.nextDouble();
			int treatIndex = treatVar.getItemCategory(index);
			p = getScreenPoint(treatIndex, nextVal, p);
			if (p != null)
				drawCross(g, p);
			index++;
		}
	}
	
	private void drawBars(Graphics g) {
		int noOfCategories = ((CatVariable)getVariable(treatKey)).noOfCategories();
		
		FactorsModel model = (FactorsModel)getVariable("model");
		double base = model.getConstant();
		double effects[] = model.getMainEffects(0);
		
		int basePos = responseAxis.numValToRawPosition(base);
		int baseVert = translateToScreen(0, basePos, null).y;
		
		Point p = null;
		for (int treat=0 ; treat<noOfCategories ; treat++) {
			double mean = base + effects[treat];
			p = getScreenPoint(treat, mean, p);
			
			g.setColor(TreatEffectSliderView.getShadedBarColor(treat, 0.4));
			int top = Math.min(p.y, baseVert);
			int bottom = Math.max(p.y, baseVert);
			g.fillRect(p.x - kHalfBarWidth, top, 2 * kHalfBarWidth, bottom - top);
		}
		
		g.setColor(kBaseColor);
		g.drawLine(0, baseVert, getSize().width, baseVert);
	}
	
	private void drawMeans(Graphics g) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		CatVariable treatVar = (CatVariable)getVariable(treatKey);
		int noOfCategories = treatVar.noOfCategories();
		double mean[] = new double[noOfCategories];
		int n[] = new int[noOfCategories];
		
		ValueEnumeration ye = yVar.values();
		int index = 0;
		while (ye.hasMoreValues()) {
			double nextVal = ye.nextDouble();
			int treatIndex = treatVar.getItemCategory(index);
			mean[treatIndex] += nextVal;
			n[treatIndex] ++;
			index++;
		}
		for (int i=0 ; i<noOfCategories ; i++)
			mean[i] /= n[i];
		
		Point p = null;
		g.setColor(kMeanColor);
		int offset = kMeanExtraPix + kHalfBarWidth;
		for (int treat=0 ; treat<noOfCategories ; treat++) {
			p = getScreenPoint(treat, mean[treat], p);
			g.drawLine(p.x - offset, p.y, p.x + offset, p.y);
		}
	}
	
	private void drawFits(Graphics g) {
		int noOfCategories = ((CatVariable)getVariable(treatKey)).noOfCategories();
		
		FactorsModel model = (FactorsModel)getVariable("model");
		double base = model.getConstant();
		double effects[] = model.getMainEffects(0);
		
		Point p = null;
		g.setColor(kFitColor);
		int offset = kMeanExtraPix + kHalfBarWidth;
		for (int treat=0 ; treat<noOfCategories ; treat++) {
			double mean = base + effects[treat];
			p = getScreenPoint(treat, mean, p);
			
			g.drawLine(p.x - offset, p.y, p.x + offset, p.y);
		}
	}
	
	private void drawSquares(Graphics g, boolean outlineNotFill) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		CatVariable treatVar = (CatVariable)getVariable(treatKey);
		FactorsModel model = (FactorsModel)getVariable("model");
		
		double base = model.getConstant();
		double effects[] = model.getMainEffects(0);
		Point pVal = null;
		Point pMean = null;
		
		g.setColor(outlineNotFill ? kRssOutlineColor : kRssFillColor);
		ValueEnumeration ye = yVar.values();
		int index = 0;
		while (ye.hasMoreValues()) {
			double nextVal = ye.nextDouble();
			int treatIndex = treatVar.getItemCategory(index);
			pVal = getScreenPoint(treatIndex, nextVal, pVal);
			pMean = getScreenPoint(treatIndex, base + effects[treatIndex], pMean);
			
			int top = Math.min(pVal.y, pMean.y);
			int bottom = Math.max(pVal.y, pMean.y);
			int ht = (bottom - top);
			
			boolean oddIndex = (index % 2) != 0;
			
			int left = oddIndex ? pVal.x - ht : pVal.x;
			if (outlineNotFill)
				g.drawRect(left, top, ht, ht);
			else
				g.fillRect(left, top, ht, ht);
			index++;
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