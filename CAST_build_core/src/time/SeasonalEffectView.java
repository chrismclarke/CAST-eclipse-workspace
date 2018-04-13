package time;

import java.awt.*;

import dataView.*;
import axis.*;


public class SeasonalEffectView extends DataView {
//	static public final String SEASONAL_PLOT = "seasonalEffect";
	
	static final private Color kLightRed = new Color(0xFF6666);
	
	private HorizAxis xAxis;
	private VertAxis yAxis;
	private String effectKey;
	private SeasonTimeAxis timeAxis;
	
	private boolean selection[];
	
	public SeasonalEffectView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis,
																						String effectKey, SeasonTimeAxis timeAxis) {
		super(theData, applet,  new Insets(5, 5, 5, 5));
		this.effectKey = effectKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.timeAxis = timeAxis;
	}
	
	public void paintView(Graphics g) {
		int nSeasons = timeAxis.getNoOfSeasons();
		int valOneSeason = timeAxis.getFirstValSeason();
		SeasonalEffectVariable effectVar = (SeasonalEffectVariable)getVariable(effectKey);
		int nDataValues = effectVar.noOfValues();
		if (selection == null || selection.length != nDataValues)
			selection = new boolean[nDataValues];
		
		Point p = null;
		Point pLast = null;
		
		int zeroPos = yAxis.numValToRawPosition(0.0);
		p = translateToScreen(0, zeroPos, p);
		int zeroScreenPos = p.y;
		
		g.setColor(Color.lightGray);
		g.drawLine(0, zeroScreenPos, getSize().width, zeroScreenPos);
		
		FlagEnumeration fe = getSelection().getEnumeration();
		int selectedIndex = 0;
		while (fe.hasMoreFlags())
			if (fe.nextFlag()) {
				selectedIndex = (selectedIndex + nSeasons - valOneSeason) % nSeasons;
				
				int horizPos = xAxis.catValToPosition(selectedIndex);
				int vertPos = yAxis.numValToRawPosition(effectVar.doubleValueAt(selectedIndex));
				p = translateToScreen(horizPos, vertPos, p);
				g.setColor(kLightRed);
				int top, bottom;
				if (p.y > zeroScreenPos) {
					top = zeroScreenPos;
					bottom = p.y;
				}
				else {
					top = p.y;
					bottom = zeroScreenPos;
				}
				g.fillRect(p.x - 2, top, 5, bottom - top + 1);
				
				break;
			}
			else
				selectedIndex ++;
			
		int initHorizPos = 2 * xAxis.catValToPosition(0) - xAxis.catValToPosition(1);
		int initVertPos = yAxis.numValToRawPosition(
																	effectVar.doubleValueAt(nSeasons - valOneSeason - 1));
		pLast = translateToScreen(initHorizPos, initVertPos, pLast);
		
		g.setColor(Color.blue);
		for (int i=0 ; i<=nSeasons ; i++) {
			int horizPos = (i < nSeasons) ? xAxis.catValToPosition(i)
														: (2 * xAxis.catValToPosition(nSeasons - 1)
																								- xAxis.catValToPosition(nSeasons - 2));
			int vertPos = yAxis.numValToRawPosition(
														effectVar.doubleValueAt((i + nSeasons - valOneSeason) % nSeasons));
			p = translateToScreen(horizPos, vertPos, p);
			g.drawLine(pLast.x, pLast.y, p.x, p.y);
			Point temp = p;
			p = pLast;
			pLast = temp;
		}
		
		g.setColor(getForeground());
		for (int i=0 ; i<nSeasons ; i++) {
			int horizPos = xAxis.catValToPosition(i);
			int vertPos = yAxis.numValToRawPosition(
														effectVar.doubleValueAt((i + nSeasons - valOneSeason) % nSeasons));
			p = translateToScreen(horizPos, vertPos, p);
			g.drawLine(p.x, p.y, p.x, zeroScreenPos);
		}
	}

//-------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		int hitIndex = xAxis.positionToCatVal(hitPos.x);
		return new IndexPosInfo(hitIndex);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo == null)
			getData().clearSelection();
		else {
			int hitCycle = ((IndexPosInfo)startInfo).itemIndex;
			int noOfSeasons = timeAxis.getNoOfSeasons();
			
			int nVals = selection.length;
			for (int i=0 ; i<nVals ; i++)
				selection[i] = (i % noOfSeasons) == hitCycle;
			getData().setSelection(selection);
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		startDrag(endPos);
	}
}
	
