package exper2;

import java.awt.*;

import dataView.*;
import random.*;


public class RandomiseTreatView extends DataView {
	static final private int kUnitLeftRightBorder = 5;
	static final private int kTreatTopBottomBorder = 3;
	static final private int kTreatLeftRightBorder = 10;
	static final private int kHeadingBottomBorder = 5;
	
	static final private int kFrameMax = 50;
	static final private int kFramesPerSec = 20;
	
	static final private Color kHeadingColor = new Color(0x990000);
	static final private Color kUnitGridColor = new Color(0x999999);
	static final private Color kDimTreatmentColor = Color.white;
	
	private LabelValue kTreatmentsString, kUnitsString;
	
	private String catKey;
	
	private int unitRows, unitCols;
	
	private int permutation[] = null;
	
	public RandomiseTreatView(DataSet theData, XApplet applet, String catKey, int unitRows, int unitCols) {
		super(theData, applet, null);
		this.catKey = catKey;
		this.unitRows = unitRows;
		this.unitCols = unitCols;
	
		kTreatmentsString = new LabelValue(applet.translate("Treatments"));
		kUnitsString = new LabelValue(applet.translate("Experimental units"));
	}
	
	public void paintView(Graphics g) {
		CatVariable xVar = (CatVariable)getVariable(catKey);
//		int noOfCats = xVar.noOfCategories();
		int n = xVar.noOfValues();
		
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		int treatWidth = 0;
		for (int i=0 ; i<xVar.noOfCategories() ; i++)
			treatWidth = Math.max(treatWidth, xVar.getLabel(i).stringWidth(g));
		
		Font boldFont = getApplet().getBigBoldFont();
		g.setFont(boldFont);
		fm = g.getFontMetrics();
		int boldAscent = fm.getAscent();
		int boldDescent = fm.getDescent();
		int treatHeadingWidth = kTreatmentsString.stringWidth(g);
		int unitHeadingWidth = kUnitsString.stringWidth(g);
		
		int tableTop = boldAscent + boldDescent + kHeadingBottomBorder;
		
		int unitCellWidth = Math.max(unitHeadingWidth / unitCols, treatWidth + 2 * kUnitLeftRightBorder);
		int unitCellHeight = (getSize().height - tableTop - 1) / unitRows;
		
		int treatCellHeight = ascent + descent + 2 * kTreatTopBottomBorder;
		int treatCellWidth = treatWidth + 2 * kTreatLeftRightBorder;
		int nTreatRows = (getSize().height - tableTop) / treatCellHeight;
		int nTreatCols = n / nTreatRows;
		
		g.setColor(kHeadingColor);
		int treatHeadingLeft = Math.max(treatCellWidth * nTreatCols - treatHeadingWidth, 0);
		kTreatmentsString.drawRight(g, treatHeadingLeft, boldAscent);
		
		int unitHeadingRight = getSize().width - (unitCellWidth * unitCols - unitHeadingWidth) / 2;
		kUnitsString.drawLeft(g, unitHeadingRight, boldAscent);
		
		g.setFont(getFont());
		g.setColor(Color.white);
		g.fillRect(getSize().width - unitCols * unitCellWidth - 1, tableTop, unitCols * unitCellWidth,
																																			unitRows * unitCellHeight);
		
		g.setColor(kUnitGridColor);
		for (int i=0 ; i<=unitRows ; i++) {
			int vert = tableTop + unitCellHeight * i;
			g.drawLine(getSize().width - unitCellWidth * unitCols - 1, vert, getSize().width, vert);
		}
		for (int i=0 ; i<=unitCols ; i++) {
			int horiz = getSize().width - unitCellWidth * i - 1;
			g.drawLine(horiz, tableTop, horiz, tableTop + unitCellHeight * unitRows);
		}
		
		int currentFrame = getCurrentFrame();
		g.setColor(currentFrame == 0 ? getForeground() : kDimTreatmentColor);
//		Point p = null;
		for (int i=0 ; i<n ; i++) {
			Value treat = xVar.valueAt(i);
			int treatRowIndex = i % nTreatRows;
			int treatColIndex = i / nTreatRows;
			int treatHoriz = treatColIndex * treatCellWidth + treatCellWidth / 2;
			int treatVert = tableTop + treatRowIndex * treatCellHeight + kTreatTopBottomBorder + ascent;
			
			treat.drawCentred(g, treatHoriz, treatVert);
		}
		
		if (currentFrame > 0) {
			g.setColor(getForeground());
			for (int i=0 ; i<n ; i++) {
				Value treat = xVar.valueAt(i);
				int treatRowIndex = i % nTreatRows;
				int treatColIndex = i / nTreatRows;
				int treatHoriz = treatColIndex * treatCellWidth + treatCellWidth / 2;
				int treatVert = tableTop + treatRowIndex * treatCellHeight + kTreatTopBottomBorder + ascent;
				
				int unitIndex = permutation[i];
				int unitRowIndex = unitIndex % unitRows;
				int unitColIndex = unitIndex / unitRows;
				int unitHoriz = getSize().width  + (unitColIndex - unitCols) * unitCellWidth + unitCellWidth / 2;
				int unitVert = tableTop + unitRowIndex * unitCellHeight + (unitCellHeight + ascent - descent) / 2;
				
				int horiz = (unitHoriz * currentFrame + treatHoriz * (kFrameMax - currentFrame)) / kFrameMax;
				int vert = (unitVert * currentFrame + treatVert * (kFrameMax - currentFrame)) / kFrameMax;
				treat.drawCentred(g, horiz, vert);
			}
		}
	}
	
	public void animatePermutation() {
		CatVariable xVar = (CatVariable)getVariable(catKey);
		int n = xVar.noOfValues();
		if (permutation == null) {
			permutation = new int[n];
			for (int i=0 ; i<n ; i++)
				permutation[i] = i;
		}
		RandomInteger generator = new RandomInteger(0, n - 1, n);
		int swap[] = generator.generate();
		for (int i=0 ; i<n ; i++)
			if (swap[i] != i) {
				int temp = permutation[i];
				permutation[i] = permutation[swap[i]];
				permutation[swap[i]] = temp;
			}
		animateFrames(0, kFrameMax, kFramesPerSec, null);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}