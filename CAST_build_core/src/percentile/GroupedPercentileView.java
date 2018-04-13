package percentile;

import java.awt.*;

import dataView.*;
import axis.*;


public class GroupedPercentileView extends GroupedQuartileView {
//	static public final int DOT_PLOT = 0;
//	static public final int BOX_PLOT = 1;
//	static public final int QUARTILE_AND_BOX_PLOT = 2;
//	static public final int QUARTILE_AND_DOT_PLOT = 3;
//	static public final int QUARTILE_PLOT = 4;
	static public final int PERCENTILE_AND_DOT_PLOT = 5;
	static public final int PERCENTILE_PLOT = 6;
	
//	static final private String GROUPED_PERCENTILE_PLOT = "groupedPercentilePlot";
	
	private double prob[] = {0.0, 0.25, 0.5, 0.75, 1.0};
	private int selectedCat = -1;
	private PercentileTable linkedTable = null;
	
	public GroupedPercentileView(DataSet theData, XApplet applet, NumCatAxis theAxis, NumCatAxis groupAxis) {
		super(theData, applet, theAxis, groupAxis);
	}
	
	public void setPercentiles(double lowPropn) {
		prob[1] = lowPropn;
		prob[3] = 1.0 - lowPropn;
		repaint();
	}
	
	public void setLinkedTable(PercentileTable linkedTable) {
		this.linkedTable = linkedTable;
	}
	
	public void selectCat(int selectedCat) {
		this.selectedCat = selectedCat;
		repaint();
	}
	
	public double[] getProbs() {
		return prob;
	}
	
	public NumValue[] getSortedSubset(int i) {
		checkInitialisation(getNumVariable());
		return numSubset[i].getSortedData();
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		int groupIndex = groupingVariable.getItemCategory(index);
		if (selectedCat >= 0 && selectedCat != groupIndex)
			return null;
		else
			return super.getScreenPoint(index, theVal, thePoint);
	}
	
//	protected void initialiseAllBoxes() {
//	}
	
	private void drawPercentileBands(Graphics g) {
		int nGroups = groupingVariable.noOfCategories();
		double upperVal[] = new double[nGroups];
		double lowerVal[] = new double[nGroups];
		for (int i=0 ; i<nGroups ; i++) {
			NumValue sortedData[] = numSubset[i].getSortedData();
			lowerVal[i] = PercentileInfo.evaluatePercentile(sortedData, prob[0], PercentileInfo.SMOOTH);
			upperVal[i] = PercentileInfo.evaluatePercentile(sortedData, prob[4], PercentileInfo.SMOOTH);
		}
		g.setColor(kOuterColor);
		fillBand(g, lowerVal, upperVal);
		
		for (int i=0 ; i<nGroups ; i++) {
			NumValue sortedData[] = numSubset[i].getSortedData();
			lowerVal[i] = PercentileInfo.evaluatePercentile(sortedData, prob[1], PercentileInfo.SMOOTH);
			upperVal[i] = PercentileInfo.evaluatePercentile(sortedData, prob[3], PercentileInfo.SMOOTH);
		}
		g.setColor(kInnerColor);
		fillBand(g, lowerVal, upperVal);
		
		for (int i=0 ; i<nGroups ; i++) {
			NumValue sortedData[] = numSubset[i].getSortedData();
			lowerVal[i] = PercentileInfo.evaluatePercentile(sortedData, prob[2], PercentileInfo.SMOOTH);
		}
		g.setColor(kMedianColor);
		drawMedian(g, lowerVal);
	}
	
	protected void drawGroupData(Graphics g, NumVariable variable) {
		if (plotType > DOT_PLOT && plotType <= QUARTILE_PLOT)
			super.drawGroupData(g, variable);
		else {
			switch (plotType) {
				case DOT_PLOT:
					drawDotPlot(g, variable);
					break;
				case PERCENTILE_PLOT:
					drawPercentileBands(g);
					break;
				case PERCENTILE_AND_DOT_PLOT:
					drawPercentileBands(g);
					drawDotPlot(g, variable);
					break;
			}
		}
			
		if (selectedCat >= 0) {
			int catPos = groupAxis.catValToPosition(selectedCat);
			Point p = translateToScreen(0, catPos, null);
//			int horizPos = p.x;
			
			g.setColor(Color.red);
			g.drawLine(p.x, 0, p.x, p.y);
			
			NumValue sortedData[] = numSubset[selectedCat].getSortedData();
			for (int i=0 ; i<prob.length ; i++) {
				double percentile = PercentileInfo.evaluatePercentile(sortedData, prob[i],
																																PercentileInfo.SMOOTH);
				int percentilePos = axis.numValToRawPosition(percentile);
				p = translateToScreen(percentilePos, catPos, p);
				g.drawLine(0, p.y, p.x, p.y);
				for (int j=1 ; j<5 ; j++)
					g.drawLine(j, p.y - j, j, p.y + j);
			}
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return plotType == PERCENTILE_PLOT || plotType == PERCENTILE_AND_DOT_PLOT;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		return getPosition(x, y);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		int catPos = translateFromScreen(x, y, null).y;
		int hitCat = groupAxis.positionToCatVal(catPos);
		return new CatPosInfo(hitCat, true);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		CatPosInfo catPos = (CatPosInfo)startInfo;
		selectedCat = catPos.catIndex;
		repaint();
		if (linkedTable != null)
			linkedTable.selectCat(selectedCat);
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			selectedCat = -1;
			repaint();
		if (linkedTable != null)
			linkedTable.selectCat(-1);
		}
		else
			startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		selectedCat = -1;
		repaint();
		if (linkedTable != null)
			linkedTable.selectCat(-1);
	}
	
	
}