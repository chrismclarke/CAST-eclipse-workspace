package sport;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class WinnerScatterView extends ScatterView {
	static final private Color kLightBlue = new Color(0x99CCFF);
	
	protected String rawYKey, handicapYKey, abilityKey;
	
	public WinnerScatterView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String abilityKey, String rawYKey,
						String handicapYKey) {
		super(theData, applet, xAxis, yAxis, abilityKey, rawYKey);
		this.rawYKey = rawYKey;
		this.handicapYKey = handicapYKey;
		this.abilityKey = abilityKey;
	}
	
	public void paintView(Graphics g) {
		HandicapScoreVariable handicapped = (HandicapScoreVariable)getVariable(handicapYKey);
		
		paintBackground(g, handicapped);
		highlightMinimum(g, handicapped);
		
		drawHandicaps(g);
		
//		super.paintView(g);
	}
	
	private void drawHandicaps(Graphics g) {
		NumVariable ability = (NumVariable)getVariable(abilityKey);
		Point scorePoint = null;
		Point handicappedPoint = null;
		
		g.setColor(Color.red);
		ValueEnumeration e = ability.values();
		FlagEnumeration fe = getSelection().getEnumeration();
		int index = 0;
		while (e.hasMoreValues()) {
			NumValue nextVal = (NumValue)e.nextValue();
			boolean nextSel = fe.nextFlag();
			if (nextSel) {
				scorePoint = getScreenPoint(index, nextVal, scorePoint);
				doHilite(g, index, scorePoint);
			}
			index++;
		}
		
		e = ability.values();
		index = 0;
		while (e.hasMoreValues()) {
			NumValue nextVal = (NumValue)e.nextValue();
			scorePoint = getScreenPoint(index, nextVal, scorePoint);
			yKey = handicapYKey;
			handicappedPoint = getScreenPoint(index, nextVal, handicappedPoint);
			yKey = rawYKey;
			
			g.setColor(Color.lightGray);
			if (scorePoint != null && handicappedPoint != null)
				g.drawLine(scorePoint.x, scorePoint.y, handicappedPoint.x, handicappedPoint.y);
			
			g.setColor(Color.red);
			if (handicappedPoint != null)
				drawBlob(g, handicappedPoint);
			
			g.setColor(Color.black);
			if (scorePoint != null)
				drawCross(g, scorePoint);
			index++;
		}
	}
	
	private void highlightMinimum(Graphics g, HandicapScoreVariable handicapped) {
		double minVal = Double.POSITIVE_INFINITY;
		int minIndex = 0;
		int index = 0;
		ValueEnumeration e = handicapped.values();
		while (e.hasMoreValues()) {
			double nextVal = e.nextDouble();
			if (nextVal < minVal) {
				minVal = nextVal;
				minIndex = index;
			}
			index++;
		}
		
		NumVariable xVar = (NumVariable)getVariable(xKey);
		Point winnerPos = getScreenPoint(minIndex, (NumValue)xVar.valueAt(minIndex), null);
		if (winnerPos != null) {
			g.setColor(Color.orange);
			int crossSize = getCrossSize();
			setCrossSize(crossSize + 1);
			drawCrossBackground(g, winnerPos);
			setCrossSize(crossSize);
			g.setColor(getForeground());
		}
	}
	
	private void paintBackground(Graphics g, HandicapScoreVariable handicapped) {
		double minVal = axis.minOnAxis;
		double maxVal = axis.maxOnAxis;
		double slop = (maxVal - minVal) * 0.1;
		minVal -= slop;
		maxVal += slop;
		
//		MeanAbilityVariable abilityVariable = (MeanAbilityVariable)getVariable(abilityKey);
		double yAtMin = handicapped.scoreGivingTarget(minVal);
		double yAtMax = handicapped.scoreGivingTarget(maxVal);
		
		g.setColor(kLightBlue);
		
		int vertPos = yAxis.numValToRawPosition(yAtMin);
		int horizPos = axis.numValToRawPosition(minVal);
		Point p1 = translateToScreen(horizPos, vertPos, null);
		vertPos = yAxis.numValToRawPosition(yAtMax);
		horizPos = axis.numValToRawPosition(maxVal);
		Point p2 = translateToScreen(horizPos, vertPos, null);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		
		vertPos = yAxis.numValToRawPosition(handicapped.getTarget());
		p2 = translateToScreen(horizPos, vertPos, p2);
		g.drawLine(p1.x, p2.y, p2.x, p2.y);
		
		g.setColor(getForeground());
	}
}
	
