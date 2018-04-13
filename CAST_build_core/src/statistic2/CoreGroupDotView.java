package statistic2;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


abstract public class CoreGroupDotView extends DotPlotView {
	static final protected int kMaxVertJitter = 30;
	static final private Color kOverallBackground = new Color(0xDDDDFF);
	
	protected NumCatAxis groupAxis;
	protected CatVariable groupingVariable;
	
	protected boolean drawingGroups;
	private NumValue sValue, meanValue;
	
	public CoreGroupDotView(DataSet theData, XApplet applet, String yKey, NumCatAxis numAxis,
																									NumCatAxis groupAxis, int sDecimals) {
		super(theData, applet, numAxis, 1.0);
		this.groupAxis = groupAxis;
		setActiveNumVariable(yKey);
		sValue = new NumValue(0.0, sDecimals);
		meanValue = new NumValue(0.0, sDecimals);
	}
	
	abstract protected int getNoOfAxisCats();
	abstract protected int getNoOfRealCats();
	
	protected int getMaxJitter() {
		int noOfGroups = getNoOfAxisCats();
		return Math.min(kMaxVertJitter,
							(getSize().height - getViewBorder().top - getViewBorder().bottom) / noOfGroups / 2);
	}
	
	protected void drawMeanSD(Graphics g, int n, double sy, double syy, int catIndex) {
		double mean = sy / n;
		double sd = Math.sqrt((syy - sy * mean) / (n - 1));
		
		int arrowCenter = groupAxis.catValToPosition(catIndex) + currentJitter / 2;
		
		g.setColor(Color.red);
		
		int meanPos = axis.numValToRawPosition(mean);
		Point meanPt = translateToScreen(meanPos, arrowCenter, null);
		int meanPlusSPos = axis.numValToRawPosition(mean + sd);
		Point meanPlusSPt = translateToScreen(meanPlusSPos, arrowCenter, null);
		g.drawLine(meanPt.x, meanPt.y - 5, meanPt.x, meanPt.y + 5);
		
		g.drawLine(meanPt.x, meanPt.y, meanPlusSPt.x, meanPt.y);
		g.drawLine(meanPt.x, meanPt.y - 1, meanPlusSPt.x - 1, meanPt.y - 1);
		g.drawLine(meanPt.x, meanPt.y + 1, meanPlusSPt.x - 1, meanPt.y + 1);
		
		for (int i=0 ; i<5 ; i++)
			g.drawLine(meanPlusSPt.x - i, meanPlusSPt.y - i, meanPlusSPt.x - i, meanPlusSPt.y + i);
		
		int ascent = g.getFontMetrics().getAscent();
		int baseline = meanPt.y + ascent / 2;
		sValue.setValue(sd);
		g.drawString("s = " + sValue.toString(), meanPlusSPt.x + 4, baseline);
		
		meanValue.setValue(mean);
		LabelValue meanLabel = new LabelValue(getApplet().translate("mean") + " = " + meanValue.toString());
		meanLabel.drawCentred(g, meanPt.x, meanPt.y - ascent / 2 - 2);
	}
	
	protected void shadeGroupBackground(Graphics g) {
		int nCats = getNoOfAxisCats();
		int overallBottom = (groupAxis.catValToPosition(nCats - 1)
																						+ groupAxis.catValToPosition(nCats - 2)) / 2;
		overallBottom = translateToScreen(0, overallBottom, null).y;
		
		g.setColor(kOverallBackground);
		g.fillRect(0, 0, getSize().width, overallBottom);
		
		for (int i=0 ; i<nCats-2 ; i++) {
			int groupTop = (groupAxis.catValToPosition(i)
																						+ groupAxis.catValToPosition(i + 1)) / 2;
			groupTop = translateToScreen(0, groupTop, null).y;
			g.fillRect(0, groupTop - 3, getSize().width, 3);
		}
	}
	
	protected void drawBackground(Graphics g) {
		shadeGroupBackground(g);
		
		int nCats = getNoOfRealCats() + 1;
		double sy[] = new double[nCats];
		double syy[] = new double[nCats];
		int n[] = new int[nCats];
		
		ValueEnumeration ye = getNumVariable().values();
		ValueEnumeration ce = groupingVariable.values();
		while (ye.hasMoreValues() && ce.hasMoreValues()) {
			double y = ye.nextDouble();
			int cat = groupingVariable.labelIndex(ce.nextValue());
			n[cat] ++;
			sy[cat] += y;
			syy[cat] += y * y;
			
			n[nCats - 1] ++;
			sy[nCats - 1] += y;
			syy[nCats - 1] += y * y;
		}
		
		for (int i=0 ; i<nCats ; i++)
			drawMeanSD(g, n[i], sy[i], syy[i], i);
	}
	
	public void paintView(Graphics g) {
		if (groupingVariable == null) {
			groupingVariable = getCatVariable();
			setJitter(0.5);
		}
		drawBackground(g);
		drawingGroups = true;
		super.paintView(g);
		drawingGroups = false;
		
		g.setColor(getForeground());
		super.paintView(g);
	}
}