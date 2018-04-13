package graphics;

import java.awt.*;
import java.util.*;

import dataView.*;


public class SortingBarView extends DataView {
//	static final public String SORTING_BAR_VIEW = "sortingBarView";
	
	static final private Color kGridColor = new Color(0xDDDDDD);
	static final private Color kBarColor = new Color(0x000099);
	
	static final private int kTickLength = 4;
	static final private int kTickValueGap = 2;
	
	static final public int kEndFrame = 40;
	static final public int kFramesPerSec = 10;
	
	private String yKey, labelKey;
	
	private double yMin, yMax, yPrintMin;
	private NumValue yPrintStep;
	
	private boolean initialised = false;
	
//	private boolean sorted;
	private int[] yRank;
	
	private int ascent, descent, leftBorder, bottomBorder;
	private int nBars, halfBarWidth;
	
	public SortingBarView(DataSet theData, XApplet applet,
															String yKey, String labelKey, String yAxisInfo) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.yKey = yKey;
		this.labelKey = labelKey;
		
		StringTokenizer st = new StringTokenizer(yAxisInfo);
		yMin = Double.parseDouble(st.nextToken());
		yMax = Double.parseDouble(st.nextToken());
		yPrintMin = Double.parseDouble(st.nextToken());
		yPrintStep = new NumValue(st.nextToken());
	}
	
	public void setSort(boolean sorted) {
//		this.sorted = sorted;
		if (sorted)
			animateFrames(0, kEndFrame, kFramesPerSec, null);
		else
			animateFrames(kEndFrame, -kEndFrame, kFramesPerSec, null);
	}
	
	protected void initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
		}
	}
	
	protected void doInitialisation(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		descent = fm.getDescent();
		
		LabelVariable labelVar = (LabelVariable)getVariable(labelKey);
		nBars = labelVar.noOfValues();
		
		int maxLabelWidth = labelVar.getMaxWidth(g);
		leftBorder = maxLabelWidth + kTickLength + kTickValueGap + 1;
		bottomBorder = ascent + kTickLength + kTickValueGap + 1;
		halfBarWidth = (getSize().height - bottomBorder) / (nBars * 5);
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		int sortedIndex[] = yVar.getSortedIndex();
		yRank = new int[nBars];
		for (int i=0 ; i<nBars ; i++)
			yRank[sortedIndex[i]] = nBars - i - 1;
	}
	
	private int getYPos(double y) {
		return leftBorder + (int)Math.round((getSize().width - leftBorder) * (y - yMin) / (yMax - yMin));
	}
	
	private int getCatPos(int index) {
		double unsortedPos = (index + 0.5) / nBars * (getSize().height - bottomBorder);
		double sortedPos = (yRank[index] + 0.5) / nBars * (getSize().height - bottomBorder);
		double pos = (unsortedPos * (kEndFrame - getCurrentFrame()) + sortedPos * getCurrentFrame()) / kEndFrame;
		return (int)Math.round(pos);
	}
	
	private void drawYAxis(Graphics g, int xOrigin, int yOrigin) {
		g.setColor(Color.black);
		g.drawLine(xOrigin, yOrigin, getSize().width, yOrigin);
		
		NumValue yLabel = new NumValue(yPrintMin, yPrintStep.decimals);
		double yMaxPlus = yMax + (yMax - yMin) * 0.00001;		//	so bigest label does not get missed by rounding
		while (yLabel.toDouble() <= yMaxPlus) {
			int horizPos = getYPos(yLabel.toDouble());
			g.drawLine(horizPos, yOrigin, horizPos, yOrigin + kTickLength);
			yLabel.drawCentred(g, horizPos, yOrigin + kTickLength + kTickValueGap + ascent);
			g.setColor(kGridColor);
			g.drawLine(horizPos, 0, horizPos, getSize().height - bottomBorder - 1);
			g.setColor(Color.black);
			yLabel.setValue(yLabel.toDouble() + yPrintStep.toDouble());
		}
	}
	
	private void drawCatAxis(Graphics g, int xOrigin, int yOrigin) {
		g.setColor(Color.black);
		g.drawLine(xOrigin, yOrigin, xOrigin, 0);
		
		LabelVariable labelVar = (LabelVariable)getVariable(labelKey);
		for (int i=0 ; i<nBars ; i++) {
			int vertPos = getCatPos(i);
			g.drawLine(xOrigin - kTickLength - 1, vertPos, xOrigin, vertPos);
			labelVar.valueAt(i).drawLeft(g, xOrigin - kTickLength - 1 - kTickValueGap, vertPos + (ascent - descent) / 2);
		}
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		int xOrigin = leftBorder;
		int yOrigin = getSize().height - bottomBorder;
		
		g.setColor(Color.white);
		g.fillRect(xOrigin, 0, getSize().width - xOrigin, yOrigin);
		
		drawYAxis(g, xOrigin, yOrigin);
		drawCatAxis(g, xOrigin, yOrigin);
		
		g.setColor(kBarColor);
		NumVariable yVar = (NumVariable)getVariable(yKey);
		for (int i=0 ; i<nBars ; i++) {
			int vertPos = getCatPos(i);
			int horizPos = getYPos(yVar.doubleValueAt(i));
			g.fillRect(leftBorder, vertPos - halfBarWidth, horizPos - leftBorder, 2 * halfBarWidth);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}