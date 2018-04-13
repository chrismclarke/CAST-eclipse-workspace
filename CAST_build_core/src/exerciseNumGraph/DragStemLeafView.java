package exerciseNumGraph;

import java.awt.*;
import java.util.*;

import dataView.*;


public class DragStemLeafView extends CoreDragStackedView {
//	static public final String DRAG_STEM_LEAF = "dragStemLeaf";
	
	static final private int kDefaultRightGap = 10;
	static final private int kLeftBorder = 5;
	static final private int kSymbolLeftRightBorder = 1;
	static final private int kSymbolTopBottomBorder = 2;
	static final private int kBarGap = 3;
	static final private int kBarLengthExtra = 3;
	static final private int kKeyBottomBorder = 3;
	static final private int kKeyLeftOffset = 20;
	
	private String kKeyString;
	
	static final private Color kLeafColor = Color.blue;

	private int stemPower, repeatsPerStem, maxLeaves;
	
	private int stemRight, leafLeft;
	private int ascent, keyBaseline, classZeroTop, noOfClasses;
	
	private boolean smallestAtBottom = false;
	
	public DragStemLeafView(DataSet theData, XApplet applet, String yKey, String axisInfo) {
		super(theData, applet, yKey, axisInfo);
		kKeyString = " " + applet.translate("represents the value") + " ";
	}
	
//===========================================================================
	
	private class StemLeaf {
		private int stem, leaf;
		private boolean negative;
		
		StemLeaf(double y) {
			int leafPower = stemPower - 1;
			for (int i=0 ; i<leafPower ; i++)
				y *= 0.1;
			for (int i=0 ; i<-leafPower ; i++)
				y *= 10.0;
			negative = false;
			if (negative)
				y = -y;
			
			int stemLeafInt = (int)Math.round(Math.floor(y));
			leaf = stemLeafInt % 10;
			stem = stemLeafInt / 10;
		}
		
		String getStemString() {
			String s = String.valueOf(stem);
			if (negative)
				s = "-" + s;
			return s;
		}
		
		int getLeaf() {
			return leaf;
		}
		
		boolean negativeStem() {
			return negative;
		}
		
		int stackIndex() {
			int stemIndex = negative ? (-stem - 1) : stem;
			int leafIndex = negative ? (9 - leaf) : leaf;
			return stemIndex * repeatsPerStem + leafIndex / (10 / repeatsPerStem);
		}
		
		void incrementStack() {
			int leafIncrement = 10 / repeatsPerStem;
			if (negative) {
				leaf -= leafIncrement;
				if (leaf < 0) {
					leaf += 10;
					if (negative) {
						stem --;
						if (stem < 0) {
							stem = 0;
							negative = false;
						}
					}
				}
			}
			else {
				leaf += leafIncrement;
				if (leaf > 9) {
					leaf -= 10;
					stem ++;
				}
			}
		}
	}
	
//===========================================================================
	
	protected String getAxisStatus() {
		return axisMin + " " + axisMax + " " + classWidth + " " + stemPower + " " + repeatsPerStem + " " + noOfClasses;
	}
	
	protected void setAxisStatus(String status) {
		StringTokenizer st = new StringTokenizer(status);
		axisMin = Double.parseDouble(st.nextToken());
		axisMax = Double.parseDouble(st.nextToken());
		classWidth = Double.parseDouble(st.nextToken());
		stemPower = Integer.parseInt(st.nextToken());
		repeatsPerStem = Integer.parseInt(st.nextToken());
		noOfClasses = Integer.parseInt(st.nextToken());
	}
	
	public void setSmallestAtBottom(boolean smallestAtBottom) {
		this.smallestAtBottom = smallestAtBottom;
	}
	
	protected void readAxisInfo(Graphics g) {
		StringTokenizer st = new StringTokenizer(axisInfo);
		axisMin = Double.parseDouble(st.nextToken());
		axisMax = Double.parseDouble(st.nextToken());
		stemPower = Integer.parseInt(st.nextToken());
		repeatsPerStem = Integer.parseInt(st.nextToken());		//	1, 2 or 5
		
		classWidth = 1.0;
		for (int i=0 ; i<stemPower ; i++)
			classWidth *= 10.0;
		for (int i=0 ; i<-stemPower ; i++)
			classWidth *= 0.1;
		classWidth /= repeatsPerStem;
		
		StemLeaf minStem = new StemLeaf(axisMin + classWidth / 2);
		StemLeaf maxStem = new StemLeaf(axisMax - classWidth / 2);
		
		FontMetrics fm = g.getFontMetrics();
		stemRight = kLeftBorder + Math.max(fm.stringWidth(minStem.getStemString()),
																												fm.stringWidth(maxStem.getStemString()));
		leafLeft = stemRight + 2 * kBarGap + 2;
		
		noOfClasses = maxStem.stackIndex() - minStem.stackIndex() + 1;
		
		ascent = g.getFontMetrics().getAscent();
		
		keyBaseline = ascent + kBarLengthExtra + kSymbolTopBottomBorder;
		classZeroTop = keyBaseline + 2 * kBarLengthExtra + kSymbolTopBottomBorder + kKeyBottomBorder;
		
		int usedHeight = classZeroTop + noOfClasses * symbolScreenHeight + kBarLengthExtra;
		classZeroTop += (getSize().height - usedHeight) / 2;
		keyBaseline += (getSize().height - usedHeight) / 2;
	}
	
	protected void setSymbolSize(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		symbolScreenWidth = symbolAxisHeight = 2 * kSymbolLeftRightBorder + fm.stringWidth("0");
		symbolScreenHeight = symbolAxisWidth = 2 * kSymbolTopBottomBorder + fm.getAscent();
	}
	
	public void setMaxLeaves(int maxLeaves) {
		this.maxLeaves = maxLeaves;
	}
	
//-------------------------------------------------------------------
	
	protected Color getSymbolColor(int index) {
		return kLeafColor;
	}
	
	private void drawKey(Graphics g) {
		StemLeaf middle = new StemLeaf((axisMax + 2 * axisMin) / 3);
		String stemString = middle.getStemString();
		int leafDigit = middle.getLeaf();
		
		double value = Double.parseDouble(stemString + leafDigit);
		for (int i=0 ; i<stemPower-1 ; i++)
			value *= 10;
		for (int i=0 ; i<1-stemPower ; i++)
			value /= 10;
		int displayDecimals = Math.max(1 - stemPower, 0);
		NumValue middleValue = new NumValue(value, displayDecimals);
		
		int horiz = kKeyLeftOffset;
		FontMetrics fm = g.getFontMetrics();
		g.drawString(stemString, horiz, keyBaseline);
		
		horiz += fm.stringWidth(stemString) + kBarGap;
		g.drawLine(horiz, keyBaseline - ascent - kSymbolTopBottomBorder - kBarLengthExtra,
																				horiz, keyBaseline + kSymbolTopBottomBorder + kBarLengthExtra);
		horiz += kBarGap + 1;
		
		g.setColor(kLeafColor);
		g.drawString(String.valueOf(leafDigit), horiz + kSymbolLeftRightBorder, keyBaseline);
		horiz += symbolScreenWidth + 5;
		
		g.setColor(getForeground());
		g.drawString(kKeyString, horiz, keyBaseline);
		horiz += fm.stringWidth(kKeyString);
		
		middleValue.drawRight(g, horiz, keyBaseline);
	}
	
	protected void drawAxis(Graphics g) {
		drawKey(g);
		
		g.setColor(Color.white);
		g.fillRect(0, classZeroTop - kBarLengthExtra, leafLeft + (maxLeaves + 2) * symbolScreenWidth,
																									noOfClasses * symbolScreenHeight + 2 * kBarLengthExtra + 1);
		g.setColor(getForeground());
		
		g.drawLine(stemRight + kBarGap, classZeroTop - kBarLengthExtra, stemRight + kBarGap,
																			classZeroTop + noOfClasses * symbolScreenHeight + kBarLengthExtra);
		
		int classTop = classZeroTop;
		if (smallestAtBottom)
			classTop += (noOfClasses - 1) * symbolScreenHeight;
		StemLeaf stem = new StemLeaf(axisMin + classWidth / 2);
		FontMetrics fm = g.getFontMetrics();
		for (int i=0 ; i<noOfClasses ; i++) {
			String stemString = stem.getStemString();
			g.drawString(stemString, stemRight - fm.stringWidth(stemString),
																										classTop + kSymbolTopBottomBorder + ascent);
			stem.incrementStack();
			if (smallestAtBottom)
				classTop -= symbolScreenHeight;
			else
				classTop += symbolScreenHeight;
		}
	}
	
	protected void setSymbolRect(int axisPix, int offAxisPix, Rectangle r) {
		r.x = leafLeft + offAxisPix - symbolScreenWidth;
		if (smallestAtBottom)
			r.y = classZeroTop + (noOfClasses - 1) * symbolScreenHeight - axisPix;
		else
			r.y = classZeroTop + axisPix;
		r.width = symbolScreenWidth;
		r.height = symbolScreenHeight;
	}
	
	protected void setDefaultSymbolRect(Rectangle r) {
		r.x = Math.min(getSize().width, leafLeft + (maxLeaves + 2) * symbolScreenWidth)
																												- kDefaultRightGap - symbolScreenWidth;
		r.y = (getSize().height - symbolScreenHeight) / 2;
		r.width = symbolScreenWidth;
		r.height = symbolScreenHeight;
	}
	
	protected int getStackIndex(int dragX, int dragY, boolean center) {
		if (center)
			if (smallestAtBottom)
				dragY -= symbolScreenHeight / 2;
			else
				dragY += symbolScreenHeight / 2;
		
		if (smallestAtBottom)
			return (classZeroTop + (noOfClasses - 1) * symbolScreenHeight - dragY) / symbolAxisWidth;
		else
			return (dragY - classZeroTop) / symbolAxisWidth;
	}
	
	protected int getHighIndexOffset(int lowDragStackIndex) {
		int heightOffset;
		if (smallestAtBottom)
			heightOffset = (classZeroTop + (noOfClasses - 1) * symbolScreenHeight - dragY)
																													- lowDragStackIndex * symbolScreenHeight;
		else
			heightOffset = dragY - classZeroTop - lowDragStackIndex * symbolScreenHeight;
		return heightOffset * symbolScreenWidth / symbolScreenHeight;
	}
	
	protected int getDragStackPosition() {
		return Math.max((dragX + symbolAxisHeight / 2 - leafLeft) / symbolAxisHeight, 0);
	}
	
	protected void drawSymbol(Graphics g, Rectangle r, Color crossColor, NumValue y, int index) {
		g.setColor(crossColor);
		int leaf = new StemLeaf(y.toDouble()).getLeaf();
		g.drawString(String.valueOf(leaf), r.x + kSymbolLeftRightBorder, r.y + kSymbolTopBottomBorder + ascent);
	}

//-----------------------------------------------------------------------------------
	
	public boolean[] inWrongPositions() {			//		assumes leaves are in correct stack
		boolean wrongCross[] = new boolean[alreadyUsed.length];
		NumVariable yVar = (NumVariable)getVariable(yKey);
		
		int stackLeaf[] = new int[alreadyUsed.length];
		for (int classIndex=0 ; classIndex<noOfClasses ; classIndex++) {
			int noInClass = 0;
			boolean positive = true;
			for (int i=0 ; i<alreadyUsed.length ; i++)
				if (stackIndex[i] == classIndex) {
					NumValue y = (NumValue)yVar.valueAt(i);
					StemLeaf stemLeaf = new StemLeaf(y.toDouble());
					int position = stackPosition[i];
					positive = !stemLeaf.negativeStem();
					stackLeaf[position] = stemLeaf.getLeaf();
					noInClass = Math.max(noInClass, position + 1);
				}
			
			boolean classOK = true;
			for (int i=1 ; i<noInClass ; i++)
				if ((stackLeaf[i] < stackLeaf[i - 1]) == positive)
					classOK = false;
			
			if (!classOK)
				for (int i=0 ; i<alreadyUsed.length ; i++)
					if (stackIndex[i] == classIndex)
						wrongCross[i] = true;
		}
		return wrongCross;
	}

//-----------------------------------------------------------------------------------
	
	protected PositionInfo getPosition(int x, int y) {
		if (y <= classZeroTop || y >= classZeroTop + noOfClasses * symbolScreenHeight)
			return null;
		
		return new DragPosInfo(x, y);
	}
	
}
	
