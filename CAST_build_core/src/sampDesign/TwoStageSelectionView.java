package sampDesign;

import java.awt.*;

import dataView.*;


public class TwoStageSelectionView extends DataView {
	static final private Color kDarkRedColor = new Color(0x990000);
	static final private Color kHiliteColor = new Color(0xFFEEEE);
	static final private int kCrossSize = 4;
	
	private String yKey;
	private int primaryCols, secondaryCols, primaryRows, secondaryRows;
	
	public TwoStageSelectionView(DataSet theData, XApplet applet, String yKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.yKey = yKey;
		
		Sample2StageVariable yVar = (Sample2StageVariable)theData.getVariable(yKey);
		primaryCols = calcCols(yVar.getNPrimaryUnits());
		secondaryCols = calcCols(yVar.getNSecondaryUnits());
		
		primaryRows = calcRows(yVar.getNPrimaryUnits(), primaryCols);
		secondaryRows = calcRows(yVar.getNSecondaryUnits(), secondaryCols);
	}
	
	private int calcCols(int nValues) {
		return (int)Math.round(Math.ceil(Math.sqrt(nValues)));
	}
	
	private int calcRows(int nValues, int nCols) {
		return (nValues - 1) / nCols + 1;
	}
	
	public void paintView(Graphics g) {
		Sample2StageVariable yVar = (Sample2StageVariable)getData().getVariable(yKey);
		
		int unitWidth = getSize().width / (primaryCols * secondaryCols);
		int unitHeight = getSize().height / (primaryRows * secondaryRows);
		
		g.setColor(Color.white);
		g.fillRect(0, 0, primaryCols * secondaryCols * unitWidth,
																				primaryRows * secondaryRows * unitHeight);
		
		g.setColor(kHiliteColor);
		for (int i=0 ; i<yVar.getNPrimaryUnits() ; i++)
			if (yVar.primaryUnitSampled(i)) {
				int left = (i % primaryCols) * secondaryCols * unitWidth;
				int top = (i / primaryCols) * secondaryRows * unitHeight;
				g.fillRect(left, top, unitWidth * secondaryCols, unitHeight * secondaryRows);
			}
		
		g.setColor(Color.lightGray);
		for (int i=1 ; i<primaryRows ; i++) {
			int vert = unitHeight * i * secondaryRows;
			g.drawLine(0, vert, getSize().width, vert);
		}
		for (int i=1 ; i<primaryCols ; i++) {
			int horiz = unitWidth * i * secondaryCols;
			g.drawLine(horiz, 0, horiz, getSize().height);
		}
		
		g.setColor(Color.black);
		g.drawRect(0, 0, primaryCols * secondaryCols * unitWidth - 1,
																					primaryRows * secondaryRows * unitHeight - 1);
		
		int horizOffset = unitWidth / 2;
		int vertOffset = unitHeight / 2;
		int nPrimary = yVar.getNPrimaryUnits();
		int nSecondary = yVar.getNSecondaryUnits();
		
		int index = 0;
		for (int primary=0 ; primary<nPrimary ; primary++) {
			int left = (primary % primaryCols) * secondaryCols * unitWidth;
			int top = (primary / primaryCols) * secondaryRows * unitHeight;
			for (int secondary=0 ; secondary<nSecondary ; secondary++) {
				int crossHorizCentre = left + (secondary % secondaryCols) * unitWidth + horizOffset;
				int crossVertCentre = top + (secondary / secondaryCols) * unitHeight + vertOffset;
				if (yVar.secondaryUnitSampled(index)) {
					g.setColor(kDarkRedColor);
					g.fillOval(crossHorizCentre - kCrossSize, crossVertCentre - kCrossSize,
																				 2 * kCrossSize, 2 * kCrossSize);
				}
				else {
					g.setColor(Color.lightGray);
					g.drawLine(crossHorizCentre - kCrossSize, crossVertCentre - kCrossSize,
															crossHorizCentre + kCrossSize, crossVertCentre + kCrossSize);
					g.drawLine(crossHorizCentre - kCrossSize, crossVertCentre + kCrossSize,
															crossHorizCentre + kCrossSize, crossVertCentre - kCrossSize);
				}
				index ++;
			}
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
	
