package graphics;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;


public class HorizBarView extends DataView {
//	static final public String HORIZ_BAR_VIEW = "horizBarView";
	
	static final private Color kGridColor = new Color(0xDDDDDD);
	static final private Color kBarColor = new Color(0x000099);
	static final private Color kDimBarColor = new Color(0x9999FF);
	
	static final private int kBarValueGap = 5;
	
	private String yKey;
	private VertAxis catAxis;
	private HorizAxis yAxis;
	
	private boolean showValues = false;
	private boolean valuesInBars = false;
	
	public HorizBarView(DataSet theData, XApplet applet,
															String yKey, VertAxis catAxis, HorizAxis yAxis) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.yKey = yKey;
		this.catAxis = catAxis;
		this.yAxis = yAxis;
	}
	
	public void setShowValues(boolean showValues) {
		this.showValues = showValues;
	}
	
	public void setValuesInBars(boolean valuesInBars) {
		this.valuesInBars = valuesInBars;
	}
	
	private void drawGrid(Graphics g) {
		g.setColor(kGridColor);
		Point p = null;
		Enumeration e = yAxis.getLabels().elements();
		while (e.hasMoreElements()) {
			AxisLabel nextLabel = (AxisLabel)e.nextElement();
			double labelValue = ((NumValue)nextLabel.label).toDouble();
			int y = yAxis.numValToRawPosition(labelValue);
			p = translateToScreen(y, 0, p);
			g.drawLine(p.x, 0, p.x, getSize().height);
		}
	}
	
	public void paintView(Graphics g) {
		drawGrid(g);
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		int nVals = yVar.noOfValues();
		
		Flags selection = getSelection();
		int selectedIndex = selection.findSingleSetFlag();
		
		int halfBarWidth = getSize().height / (nVals * 5);
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		
		Point rightCenter = null;
		for (int i=0 ; i<nVals ; i++) {
			boolean drawDim = selectedIndex >= 0 && !selection.valueAt(i);
			Color barColor = drawDim ? kDimBarColor : kBarColor;
			g.setColor(barColor);
			
			int y = catAxis.catValToPosition(i);
			int x = yAxis.numValToRawPosition(yVar.doubleValueAt(i));
			rightCenter = translateToScreen(x, y, rightCenter);
			g.fillRect(0, rightCenter.y - halfBarWidth, rightCenter.x, 2 * halfBarWidth);
			
			if (showValues && !drawDim) {
				Value yVal = yVar.valueAt(i);
				int baseline = rightCenter.y + Math.min(ascent / 2, halfBarWidth - 1);
				boolean canSuprimpose = valuesInBars && (yVal.stringWidth(g) < rightCenter.x - kBarValueGap);
				if (canSuprimpose) {
					g.setColor(Color.white);
					yVal.drawLeft(g, rightCenter.x - kBarValueGap, baseline);
				}
				else
					yVal.drawRight(g, rightCenter.x + kBarValueGap, baseline);
			}
		}
		
		if (selectedIndex >= 0) {
			int y = catAxis.catValToPosition(selectedIndex);
			int x = yAxis.numValToRawPosition(yVar.doubleValueAt(selectedIndex));
			rightCenter = translateToScreen(x, y, rightCenter);
			g.setColor(Color.red);
			g.drawLine(rightCenter.x, 1, rightCenter.x, getSize().height - 2);
			
			g.drawLine(rightCenter.x - 1, 0, rightCenter.x - 1, rightCenter.y - halfBarWidth - 1);
			g.drawLine(rightCenter.x - 2, 1, rightCenter.x - 2, rightCenter.y - halfBarWidth - 1);
			g.drawLine(rightCenter.x - 1, 0, rightCenter.x - 5, 4);
			g.drawLine(rightCenter.x - 1, 0, rightCenter.x + 3, 4);
			
			
			g.drawLine(rightCenter.x - 1, rightCenter.y + halfBarWidth, rightCenter.x - 1, getSize().height - 1);
			g.drawLine(rightCenter.x  - 2, rightCenter.y + halfBarWidth, rightCenter.x - 2, getSize().height - 2);
			g.drawLine(rightCenter.x - 5, getSize().height - 5, rightCenter.x - 1, getSize().height - 1);
			g.drawLine(rightCenter.x + 3, getSize().height - 5, rightCenter.x - 1, getSize().height - 1);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		int nearestCat = catAxis.positionToCatVal(hitPos.y);
		
		return new IndexPosInfo(nearestCat);
	}
	
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
}