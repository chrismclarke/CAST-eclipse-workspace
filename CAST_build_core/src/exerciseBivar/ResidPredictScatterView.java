package exerciseBivar;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import models.*;


public class ResidPredictScatterView extends DataView {
//	static public final String HILITE_PT_SCATTER = "hilitePtScatter";
	
	static final private Color kArrowColor = new Color(0xFFFF66);
	static final private Color kDimCrossColor = new Color(0x888888);
	static final private Color kVeryDimCrossColor = new Color(0xCCCCCC);
	static final private Color kGridColor = new Color(0xEEEEEE);
	
	private HorizAxis xAxis;
	private VertAxis yAxis;
	private String xKey, yKey, lsKey;
	
	private boolean showResult = false;
	private double predictionX;
	
	public ResidPredictScatterView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey,
						String lsKey) {
		super(theData, applet, new Insets(5, 5, 5, 5));
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.xKey = xKey;
		this.yKey = yKey;
		this.lsKey = lsKey;
	}
	
	public void setPredictionX(double predictionX) {
		this.predictionX = predictionX;
	}
	
	public void setShowResult(boolean showResult) {
		this.showResult = showResult;
	}
	
	private Point getScreenPos(double x, double y, Point p) {
		int vertPos = yAxis.numValToRawPosition(y);
		int horizPos = xAxis.numValToRawPosition(x);
		return translateToScreen(horizPos, vertPos, p);
	}
	
	private void drawLSLine(Graphics g) {
		g.setColor(Color.blue);
		
		LinearModel ls = (LinearModel)getData().getVariable(lsKey);
		ls.drawMean(g, this, xAxis, yAxis);
	}
	
	static final private int kArrow1[] = {8, 20, 20, 38, 38, 20, 20, 8};
	static final private int kArrow2[] = {0, -16, -6, -6, 6, 6, 16, 0};
	
	private void drawOneArrow(Graphics g, int x, int y, boolean v1, boolean v2) {
		int dx[] = new int[kArrow1.length];
		for (int i=0 ; i<dx.length ; i++)
			if (v1)
				dx[i] = kArrow1[i];
			else
				dx[i] = -kArrow1[i];
		
		int dy[];
		if (v2)
			dy = (int[])kArrow2.clone();
		else {
			dy = dx;
			dx = (int[])kArrow2.clone();
		}
		
		for (int i=0 ; i<dx.length ; i++) {
			dx[i] += x;
			dy[i] += y;
		}
		g.fillPolygon(dx, dy, dx.length);
	}
	
	private void drawArrows(Graphics g, int x, int y) {
		g.setColor(kArrowColor);
		drawOneArrow(g, x, y, true, true);
		drawOneArrow(g, x, y, true, false);
		drawOneArrow(g, x, y, false, true);
		drawOneArrow(g, x, y, false, false);
	}
	
	public void paintView(Graphics g) {
		Point p = null;
		
		if (!showResult) {
			g.setColor(kGridColor);
			Vector axisLabels = yAxis.getLabels();
			Enumeration e = axisLabels.elements();
			while (e.hasMoreElements()) {
				AxisLabel l = (AxisLabel)e.nextElement();
				NumValue val = (NumValue)l.label;
				p = getScreenPos(xAxis.minOnAxis, val.toDouble(), p);
				g.drawLine(0, p.y, getSize().width, p.y);
			}
			
			axisLabels = xAxis.getLabels();
			e = axisLabels.elements();
			while (e.hasMoreElements()) {
				AxisLabel l = (AxisLabel)e.nextElement();
				NumValue val = (NumValue)l.label;
				p = getScreenPos(val.toDouble(), yAxis.minOnAxis, p);
				g.drawLine(p.x, 0, p.x, getSize().height);
			}
		}
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		NumVariable xVar = (NumVariable)getVariable(xKey);
		
		int selectedIndex = getSelection().findSingleSetFlag();
		double selectedY = 0, selectedX = 0;
		Point selectedP = null;
		if (selectedIndex >= 0) {
			selectedY = yVar.doubleValueAt(selectedIndex);
			selectedX = xVar.doubleValueAt(selectedIndex);
			selectedP = getScreenPos(selectedX, selectedY, null);
		}
		
		if (!showResult && selectedP != null)
			drawArrows(g, selectedP.x, selectedP.y);
		
		ValueEnumeration ye = yVar.values();
		ValueEnumeration xe = xVar.values();
		setCrossSize(LARGE_CROSS);
		g.setColor(showResult && (selectedP != null) ? kVeryDimCrossColor 
												: !showResult && (selectedP == null) ? Color.black
												: kDimCrossColor);
		while (ye.hasMoreValues() && xe.hasMoreValues()) {
			double y = ye.nextDouble();
			double x = xe.nextDouble();
			p = getScreenPos(x, y, p);
			drawCross(g, p);
		}
		
		if (showResult) {
			LinearModel ls = (LinearModel)getData().getVariable(lsKey);
			if (selectedP != null) {
				g.setColor(kDimCrossColor);
				g.drawLine(0, selectedP.y, selectedP.x, selectedP.y);
				
				double fit = ls.evaluateMean(selectedX);
				Point fitP = getScreenPos(selectedX, fit, null);
				g.drawLine(0, fitP.y, fitP.x, fitP.y);
				
				g.setColor(Color.red);
				int arrowHead = Math.min(4, Math.abs(selectedP.y - fitP.y) - 1);
				if (selectedY < fit)
					arrowHead = -arrowHead;
				g.drawLine(fitP.x, selectedP.y, fitP.x, fitP.y);
				g.drawLine(10, selectedP.y, 10, fitP.y);
				g.drawLine(10, selectedP.y, 10 - arrowHead, selectedP.y + arrowHead);
				g.drawLine(10, selectedP.y, 10 + arrowHead, selectedP.y + arrowHead);
			}
			else {
				g.setColor(Color.red);
				double pred = ls.evaluateMean(predictionX);
				Point predP = getScreenPos(predictionX, pred, null);
				g.drawLine(predP.x, predP.y, predP.x, getSize().height);
				g.drawLine(0, predP.y, predP.x, predP.y);
				g.drawLine(0, predP.y, 5, predP.y + 5);
				g.drawLine(0, predP.y, 5, predP.y - 5);
			}
		}
		
		drawLSLine(g);
		
		if (selectedP != null) {
			setCrossSize(LARGE_CROSS);
			g.setColor(Color.red);
			drawCross(g, selectedP);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean canDrag() {
		return false;
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
}
	
